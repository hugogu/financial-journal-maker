package com.financial.coa.repository;

import com.financial.coa.domain.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity.
 * Provides CRUD operations and custom queries for account management.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Find account by unique code.
     */
    Optional<Account> findByCode(String code);
    
    /**
     * Check if account with given code exists.
     */
    boolean existsByCode(String code);
    
    /**
     * Find all child accounts for a given parent.
     */
    List<Account> findByParentId(Long parentId);
    
    /**
     * Find all child accounts for a given parent code.
     */
    @Query("SELECT a FROM Account a WHERE a.parent.code = :parentCode")
    List<Account> findByParentCode(@Param("parentCode") String parentCode);
    
    /**
     * Find all root accounts (accounts with no parent).
     */
    @Query("SELECT a FROM Account a WHERE a.parent IS NULL")
    List<Account> findRootAccounts();
    
    /**
     * Find accounts by shared flag with pagination.
     */
    Page<Account> findBySharedAcrossScenarios(Boolean shared, Pageable pageable);
    
    /**
     * Get full account tree using recursive CTE.
     * Returns accounts in hierarchical order.
     */
    @Query(value = """
        WITH RECURSIVE account_tree AS (
            SELECT id, code, name, description, parent_id, 
                   shared_across_scenarios, version, created_at, updated_at, created_by,
                   0 AS level, CAST(code AS VARCHAR(1000)) AS path
            FROM accounts
            WHERE parent_id IS NULL
            UNION ALL
            SELECT a.id, a.code, a.name, a.description, a.parent_id,
                   a.shared_across_scenarios, a.version, a.created_at, a.updated_at, a.created_by,
                   at.level + 1, CAST(at.path || '/' || a.code AS VARCHAR(1000))
            FROM accounts a
            INNER JOIN account_tree at ON a.parent_id = at.id
        )
        SELECT id, code, name, description, parent_id, 
               shared_across_scenarios, version, created_at, updated_at, created_by
        FROM account_tree
        ORDER BY path
        """, nativeQuery = true)
    List<Account> findAccountTree();
    
    /**
     * Get subtree for a specific account using recursive CTE.
     */
    @Query(value = """
        WITH RECURSIVE account_tree AS (
            SELECT id, code, name, description, parent_id,
                   shared_across_scenarios, version, created_at, updated_at, created_by,
                   0 AS level, CAST(code AS VARCHAR(1000)) AS path
            FROM accounts
            WHERE code = :rootCode
            UNION ALL
            SELECT a.id, a.code, a.name, a.description, a.parent_id,
                   a.shared_across_scenarios, a.version, a.created_at, a.updated_at, a.created_by,
                   at.level + 1, CAST(at.path || '/' || a.code AS VARCHAR(1000))
            FROM accounts a
            INNER JOIN account_tree at ON a.parent_id = at.id
        )
        SELECT id, code, name, description, parent_id,
               shared_across_scenarios, version, created_at, updated_at, created_by
        FROM account_tree
        ORDER BY path
        """, nativeQuery = true)
    List<Account> findSubtree(@Param("rootCode") String rootCode);
    
    /**
     * Count child accounts for a given parent.
     */
    long countByParentId(Long parentId);
    
    /**
     * Check if an account has any children.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.parent.id = :parentId")
    boolean hasChildren(@Param("parentId") Long parentId);
}
