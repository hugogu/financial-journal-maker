package com.financial.transactionflow.dto;

/**
 * Visual representation of an account node for flow diagrams.
 */
public class AccountNodeDto {
    private String accountCode;
    private String accountName;
    private AccountType accountType;
    private AccountState accountState;
    private Position position;
    private boolean linkedToCoA;

    public AccountNodeDto() {
    }

    public AccountNodeDto(String accountCode, String accountName, AccountType accountType, 
                         AccountState accountState, Position position, boolean linkedToCoA) {
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.accountState = accountState;
        this.position = position;
        this.linkedToCoA = linkedToCoA;
    }

    // Getters and Setters
    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountState getAccountState() {
        return accountState;
    }

    public void setAccountState(AccountState accountState) {
        this.accountState = accountState;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean isLinkedToCoA() {
        return linkedToCoA;
    }

    public void setLinkedToCoA(boolean linkedToCoA) {
        this.linkedToCoA = linkedToCoA;
    }
}
