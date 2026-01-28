package com.financial.coa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Chart of Accounts Management API.
 * 
 * This application provides RESTful APIs for:
 * - Creating and managing hierarchical chart of accounts structures
 * - Mapping accounts to Formance Ledger
 * - Importing accounts from Excel/CSV files
 * - Tracking account references and enforcing immutability
 */
@SpringBootApplication
@EnableJpaAuditing
public class CoaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoaApplication.class, args);
    }
}
