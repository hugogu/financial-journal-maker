package com.financial.coa.service;

import com.financial.coa.domain.ImportJob;
import com.financial.coa.exception.InvalidImportFileException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing Excel and CSV files containing account data.
 */
@Slf4j
@Service
public class FileParserService {
    
    /**
     * Detect file format based on file extension and content type.
     */
    public ImportJob.FileFormat detectFileFormat(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        if (filename != null) {
            String lowerName = filename.toLowerCase();
            if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
                return ImportJob.FileFormat.EXCEL;
            }
            if (lowerName.endsWith(".csv")) {
                return ImportJob.FileFormat.CSV;
            }
        }
        
        if (contentType != null) {
            if (contentType.contains("spreadsheet") || contentType.contains("excel")) {
                return ImportJob.FileFormat.EXCEL;
            }
            if (contentType.contains("csv") || contentType.equals("text/plain")) {
                return ImportJob.FileFormat.CSV;
            }
        }
        
        throw new InvalidImportFileException("Unable to determine file format. Supported formats: Excel (.xlsx, .xls), CSV (.csv)");
    }
    
    /**
     * Parse Excel file (XLSX or XLS format).
     */
    public List<ImportRecord> parseExcel(MultipartFile file) throws IOException {
        log.info("Parsing Excel file: {}", file.getOriginalFilename());
        
        List<ImportRecord> records = new ArrayList<>();
        
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = createWorkbook(file.getOriginalFilename(), is);
            Sheet sheet = workbook.getSheetAt(0);
            
            // Parse header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new InvalidImportFileException("Excel file is empty or has no header row");
            }
            
            int[] columnIndexes = findColumnIndexes(headerRow);
            validateRequiredColumns(columnIndexes);
            
            // Parse data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                
                ImportRecord record = parseExcelRow(row, columnIndexes, i + 1);
                records.add(record);
            }
            
            workbook.close();
        }
        
        log.info("Parsed {} records from Excel file", records.size());
        return records;
    }
    
    /**
     * Parse CSV file.
     */
    public List<ImportRecord> parseCsv(MultipartFile file) throws IOException {
        log.info("Parsing CSV file: {}", file.getOriginalFilename());
        
        List<ImportRecord> records = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> allRows = reader.readAll();
            
            if (allRows.isEmpty()) {
                throw new InvalidImportFileException("CSV file is empty");
            }
            
            // Parse header row
            String[] headers = allRows.get(0);
            int[] columnIndexes = findCsvColumnIndexes(headers);
            validateRequiredColumns(columnIndexes);
            
            // Parse data rows
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                if (isRowEmpty(row)) {
                    continue;
                }
                
                ImportRecord record = parseCsvRow(row, columnIndexes, i + 1);
                records.add(record);
            }
            
        } catch (CsvException e) {
            throw new InvalidImportFileException("Failed to parse CSV file: " + e.getMessage());
        }
        
        log.info("Parsed {} records from CSV file", records.size());
        return records;
    }
    
    private Workbook createWorkbook(String filename, InputStream is) throws IOException {
        if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(is);
        }
        return new HSSFWorkbook(is);
    }
    
    private int[] findColumnIndexes(Row headerRow) {
        // [code, name, parentCode, description, shared]
        int[] indexes = {-1, -1, -1, -1, -1};
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
            
            String header = getCellStringValue(cell).toLowerCase().trim();
            
            if (header.equals("code")) indexes[0] = i;
            else if (header.equals("name")) indexes[1] = i;
            else if (header.equals("parent_code") || header.equals("parentcode") || header.equals("parent")) indexes[2] = i;
            else if (header.equals("description") || header.equals("desc")) indexes[3] = i;
            else if (header.equals("shared") || header.equals("shared_across_scenarios")) indexes[4] = i;
        }
        
        return indexes;
    }
    
    private int[] findCsvColumnIndexes(String[] headers) {
        int[] indexes = {-1, -1, -1, -1, -1};
        
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].toLowerCase().trim();
            
            if (header.equals("code")) indexes[0] = i;
            else if (header.equals("name")) indexes[1] = i;
            else if (header.equals("parent_code") || header.equals("parentcode") || header.equals("parent")) indexes[2] = i;
            else if (header.equals("description") || header.equals("desc")) indexes[3] = i;
            else if (header.equals("shared") || header.equals("shared_across_scenarios")) indexes[4] = i;
        }
        
        return indexes;
    }
    
    private void validateRequiredColumns(int[] columnIndexes) {
        List<String> missing = new ArrayList<>();
        if (columnIndexes[0] == -1) missing.add("code");
        if (columnIndexes[1] == -1) missing.add("name");
        
        if (!missing.isEmpty()) {
            throw new InvalidImportFileException("Missing required columns: " + String.join(", ", missing));
        }
    }
    
    private ImportRecord parseExcelRow(Row row, int[] indexes, int rowNumber) {
        return ImportRecord.builder()
                .rowNumber(rowNumber)
                .code(getCellStringValue(row.getCell(indexes[0])))
                .name(getCellStringValue(row.getCell(indexes[1])))
                .parentCode(indexes[2] >= 0 ? getCellStringValue(row.getCell(indexes[2])) : null)
                .description(indexes[3] >= 0 ? getCellStringValue(row.getCell(indexes[3])) : null)
                .sharedAcrossScenarios(indexes[4] >= 0 ? getCellBooleanValue(row.getCell(indexes[4])) : false)
                .build();
    }
    
    private ImportRecord parseCsvRow(String[] row, int[] indexes, int rowNumber) {
        return ImportRecord.builder()
                .rowNumber(rowNumber)
                .code(getArrayValue(row, indexes[0]))
                .name(getArrayValue(row, indexes[1]))
                .parentCode(indexes[2] >= 0 ? getArrayValue(row, indexes[2]) : null)
                .description(indexes[3] >= 0 ? getArrayValue(row, indexes[3]) : null)
                .sharedAcrossScenarios(indexes[4] >= 0 ? parseBoolean(getArrayValue(row, indexes[4])) : false)
                .build();
    }
    
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
    
    private Boolean getCellBooleanValue(Cell cell) {
        if (cell == null) return false;
        
        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> parseBoolean(cell.getStringCellValue());
            case NUMERIC -> cell.getNumericCellValue() != 0;
            default -> false;
        };
    }
    
    private String getArrayValue(String[] row, int index) {
        if (index < 0 || index >= row.length) return null;
        String value = row[index].trim();
        return value.isEmpty() ? null : value;
    }
    
    private Boolean parseBoolean(String value) {
        if (value == null) return false;
        String lower = value.toLowerCase().trim();
        return lower.equals("true") || lower.equals("yes") || lower.equals("1");
    }
    
    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell);
                if (value != null && !value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isRowEmpty(String[] row) {
        for (String value : row) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
