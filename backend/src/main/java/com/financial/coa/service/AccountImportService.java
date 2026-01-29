package com.financial.coa.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.coa.domain.Account;
import com.financial.coa.domain.ImportJob;
import com.financial.coa.dto.ImportErrorResponse.ValidationError;
import com.financial.coa.dto.ImportJobResponse;
import com.financial.coa.exception.InvalidImportFileException;
import com.financial.coa.repository.AccountRepository;
import com.financial.coa.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for importing accounts from Excel/CSV files.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountImportService {
    
    private final FileParserService fileParserService;
    private final AccountRepository accountRepository;
    private final ImportJobRepository importJobRepository;
    private final ObjectMapper objectMapper;
    
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9.-]+$");
    private static final int MAX_CODE_LENGTH = 50;
    private static final int MAX_NAME_LENGTH = 255;
    
    /**
     * Validate import file without persisting data.
     */
    public List<ValidationError> validateImportFile(MultipartFile file) throws IOException {
        log.info("Validating import file: {}", file.getOriginalFilename());
        
        ImportJob.FileFormat format = fileParserService.detectFileFormat(file);
        List<ImportRecord> records = parseFile(file, format);
        
        return validateRecords(records);
    }
    
    /**
     * Perform full import of accounts from file.
     */
    @Transactional
    public ImportJobResponse performImport(MultipartFile file) throws IOException {
        log.info("Starting import from file: {}", file.getOriginalFilename());
        
        // Create import job record
        ImportJob.FileFormat format = fileParserService.detectFileFormat(file);
        ImportJob job = createImportJob(file.getOriginalFilename(), format);
        
        try {
            // Parse file
            List<ImportRecord> records = parseFile(file, format);
            job.setTotalRecords(records.size());
            job.setStatus(ImportJob.ImportStatus.PROCESSING);
            job.setStartedAt(LocalDateTime.now());
            importJobRepository.save(job);
            
            // Validate records
            List<ValidationError> errors = validateRecords(records);
            if (!errors.isEmpty()) {
                return failImportJob(job, errors);
            }
            
            // Create accounts in dependency order
            int processedCount = createAccountsInOrder(records);
            
            // Complete job
            job.setProcessedRecords(processedCount);
            job.setStatus(ImportJob.ImportStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            ImportJob saved = importJobRepository.save(job);
            
            log.info("Import completed successfully: {} accounts created", processedCount);
            return toJobResponse(saved);
            
        } catch (Exception e) {
            log.error("Import failed: {}", e.getMessage(), e);
            job.setStatus(ImportJob.ImportStatus.FAILED);
            job.setErrorDetails(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            importJobRepository.save(job);
            throw e;
        }
    }
    
    /**
     * Get import job status by ID.
     */
    @Transactional(readOnly = true)
    public ImportJobResponse getImportJob(Long jobId) {
        ImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Import job not found: " + jobId));
        return toJobResponse(job);
    }
    
    private List<ImportRecord> parseFile(MultipartFile file, ImportJob.FileFormat format) throws IOException {
        return switch (format) {
            case EXCEL -> fileParserService.parseExcel(file);
            case CSV -> fileParserService.parseCsv(file);
        };
    }
    
    private List<ValidationError> validateRecords(List<ImportRecord> records) {
        List<ValidationError> errors = new ArrayList<>();
        Set<String> seenCodes = new HashSet<>();
        Map<String, Integer> codeToRow = new HashMap<>();
        
        for (ImportRecord record : records) {
            int row = record.getRowNumber();
            
            // Required field validation
            if (record.getCode() == null || record.getCode().isBlank()) {
                errors.add(ValidationError.builder()
                        .row(row).field("code").message("Code is required").build());
                continue;
            }
            
            if (record.getName() == null || record.getName().isBlank()) {
                errors.add(ValidationError.builder()
                        .row(row).field("name").message("Name is required")
                        .value(record.getCode()).build());
            }
            
            // Code format validation
            String code = record.getCode();
            if (code.length() > MAX_CODE_LENGTH) {
                errors.add(ValidationError.builder()
                        .row(row).field("code")
                        .message("Code exceeds maximum length of " + MAX_CODE_LENGTH)
                        .value(code).build());
            }
            
            if (!CODE_PATTERN.matcher(code).matches()) {
                errors.add(ValidationError.builder()
                        .row(row).field("code")
                        .message("Code can only contain alphanumeric characters, dots, and hyphens")
                        .value(code).build());
            }
            
            // Name length validation
            if (record.getName() != null && record.getName().length() > MAX_NAME_LENGTH) {
                errors.add(ValidationError.builder()
                        .row(row).field("name")
                        .message("Name exceeds maximum length of " + MAX_NAME_LENGTH)
                        .value(record.getName()).build());
            }
            
            // Duplicate code detection
            if (seenCodes.contains(code)) {
                errors.add(ValidationError.builder()
                        .row(row).field("code")
                        .message("Duplicate code found at row " + codeToRow.get(code))
                        .value(code).build());
            } else {
                seenCodes.add(code);
                codeToRow.put(code, row);
            }
            
            // Check if code already exists in database
            if (accountRepository.existsByCode(code)) {
                errors.add(ValidationError.builder()
                        .row(row).field("code")
                        .message("Account code already exists in database")
                        .value(code).build());
            }
        }
        
        // Validate parent references
        errors.addAll(validateParentReferences(records, seenCodes));
        
        // Check for circular references
        errors.addAll(checkCircularReferences(records));
        
        return errors;
    }
    
    private List<ValidationError> validateParentReferences(List<ImportRecord> records, Set<String> importCodes) {
        List<ValidationError> errors = new ArrayList<>();
        
        for (ImportRecord record : records) {
            if (record.getParentCode() != null && !record.getParentCode().isBlank()) {
                String parentCode = record.getParentCode();
                
                // Parent must exist either in import file or in database
                boolean existsInImport = importCodes.contains(parentCode);
                boolean existsInDb = accountRepository.existsByCode(parentCode);
                
                if (!existsInImport && !existsInDb) {
                    errors.add(ValidationError.builder()
                            .row(record.getRowNumber()).field("parent_code")
                            .message("Parent account does not exist")
                            .value(parentCode).build());
                }
            }
        }
        
        return errors;
    }
    
    private List<ValidationError> checkCircularReferences(List<ImportRecord> records) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Build parent-child map
        Map<String, String> parentMap = records.stream()
                .filter(r -> r.getParentCode() != null && !r.getParentCode().isBlank())
                .collect(Collectors.toMap(ImportRecord::getCode, ImportRecord::getParentCode, (a, b) -> a));
        
        for (ImportRecord record : records) {
            Set<String> visited = new HashSet<>();
            String current = record.getCode();
            
            while (current != null && parentMap.containsKey(current)) {
                if (visited.contains(current)) {
                    errors.add(ValidationError.builder()
                            .row(record.getRowNumber()).field("parent_code")
                            .message("Circular reference detected in parent hierarchy")
                            .value(record.getCode()).build());
                    break;
                }
                visited.add(current);
                current = parentMap.get(current);
            }
        }
        
        return errors;
    }
    
    private int createAccountsInOrder(List<ImportRecord> records) {
        // Sort records: roots first, then by dependency order
        List<ImportRecord> sorted = topologicalSort(records);
        
        int created = 0;
        for (ImportRecord record : sorted) {
            Account parent = null;
            if (record.getParentCode() != null && !record.getParentCode().isBlank()) {
                parent = accountRepository.findByCode(record.getParentCode()).orElse(null);
            }
            
            Account account = Account.builder()
                    .code(record.getCode())
                    .name(record.getName())
                    .description(record.getDescription())
                    .parent(parent)
                    .sharedAcrossScenarios(record.getSharedAcrossScenarios() != null 
                        ? record.getSharedAcrossScenarios() : false)
                    .build();
            
            accountRepository.save(account);
            created++;
        }
        
        return created;
    }
    
    private List<ImportRecord> topologicalSort(List<ImportRecord> records) {
        Map<String, ImportRecord> codeToRecord = records.stream()
                .collect(Collectors.toMap(ImportRecord::getCode, r -> r, (a, b) -> a));
        
        Set<String> visited = new HashSet<>();
        List<ImportRecord> result = new ArrayList<>();
        
        for (ImportRecord record : records) {
            visit(record, codeToRecord, visited, result);
        }
        
        return result;
    }
    
    private void visit(ImportRecord record, Map<String, ImportRecord> codeToRecord, 
                       Set<String> visited, List<ImportRecord> result) {
        if (visited.contains(record.getCode())) {
            return;
        }
        
        // Visit parent first if it's in the import set
        if (record.getParentCode() != null && codeToRecord.containsKey(record.getParentCode())) {
            visit(codeToRecord.get(record.getParentCode()), codeToRecord, visited, result);
        }
        
        visited.add(record.getCode());
        result.add(record);
    }
    
    private ImportJob createImportJob(String fileName, ImportJob.FileFormat format) {
        ImportJob job = ImportJob.builder()
                .fileName(fileName)
                .fileFormat(format)
                .status(ImportJob.ImportStatus.PENDING)
                .build();
        return importJobRepository.save(job);
    }
    
    private ImportJobResponse failImportJob(ImportJob job, List<ValidationError> errors) {
        job.setStatus(ImportJob.ImportStatus.FAILED);
        job.setFailedRecords(errors.size());
        job.setCompletedAt(LocalDateTime.now());
        
        try {
            job.setErrorDetails(objectMapper.writeValueAsString(errors));
        } catch (JsonProcessingException e) {
            job.setErrorDetails("Validation failed with " + errors.size() + " errors");
        }
        
        importJobRepository.save(job);
        
        throw new InvalidImportFileException(
                "Import validation failed with " + errors.size() + " error(s)",
                errors.stream().map(e -> String.format("Row %d: %s - %s", e.getRow(), e.getField(), e.getMessage()))
                        .collect(Collectors.toList())
        );
    }
    
    private ImportJobResponse toJobResponse(ImportJob job) {
        return ImportJobResponse.builder()
                .id(job.getId())
                .fileName(job.getFileName())
                .fileFormat(job.getFileFormat())
                .status(job.getStatus())
                .totalRecords(job.getTotalRecords())
                .processedRecords(job.getProcessedRecords())
                .failedRecords(job.getFailedRecords())
                .errorDetails(job.getErrorDetails())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .createdAt(job.getCreatedAt())
                .build();
    }
}
