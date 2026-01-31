package com.financial.coa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Financial Journal Maker.
 * 
 * This application provides RESTful APIs for:
 * - Chart of Accounts (COA) Management: Creating and managing hierarchical account structures
 * - Accounting Rules Management: Defining and managing accounting rules with trigger conditions
 * - Mapping accounts to Formance Ledger
 * - Importing accounts from Excel/CSV files
 * - Tracking account references and enforcing immutability
 * - Numscript generation and rule simulation
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.financial")
@EntityScan(basePackages = "com.financial")
@ComponentScan(basePackages = "com.financial")
public class CoaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoaApplication.class, args);
    }
}
