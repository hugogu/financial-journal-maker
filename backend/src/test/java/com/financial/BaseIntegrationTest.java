package com.financial;

import com.financial.coa.CoaApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for integration tests using real PostgreSQL database.
 * Provides common setup and utilities for all controller tests.
 */
@SpringBootTest(
        classes = CoaApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected String baseUrl;

    @BeforeEach
    void setUpBase() {
        baseUrl = "http://localhost:" + port;
        cleanDatabase();
    }
    
    /**
     * Get a plain RestTemplate for operations that TestRestTemplate doesn't support well.
     */
    protected RestTemplate getPlainRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Cleans all test data from database before each test.
     * Order matters due to foreign key constraints.
     */
    protected void cleanDatabase() {
        // Clean rules module tables
        jdbcTemplate.execute("DELETE FROM trigger_conditions");
        jdbcTemplate.execute("DELETE FROM entry_lines");
        jdbcTemplate.execute("DELETE FROM entry_templates");
        jdbcTemplate.execute("DELETE FROM accounting_rule_versions");
        jdbcTemplate.execute("DELETE FROM accounting_rules");
        
        // Clean COA module tables
        jdbcTemplate.execute("DELETE FROM account_mappings");
        jdbcTemplate.execute("DELETE FROM account_references");
        jdbcTemplate.execute("DELETE FROM import_jobs");
        jdbcTemplate.execute("DELETE FROM accounts");
    }
}
