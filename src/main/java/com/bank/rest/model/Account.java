package com.bank.rest.model;

import com.bank.rest.exception.ApiException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties({"lock"})
public class Account {

    private final Object lock = new Object();

    private Long number;
    private String owner;
    private BigDecimal balance;
    private boolean disabled;

    @JsonCreator
    public Account(@JsonProperty("number") Long number, @JsonProperty("owner") String owner,
                   @JsonProperty("balance") BigDecimal balance, @JsonProperty("disabled") boolean disabled) {
        this.number = number;
        this.owner = owner;
        this.balance = balance;
        this.disabled = disabled;
    }

    public void writeOff(BigDecimal amount) {
        if (this.disabled) {
            throw new ApiException("Could not execute write off from disabled account " + this.number);
        } else if (this.balance.compareTo(amount) < 0) {
            throw new ApiException("Not sufficient funds for write off on account " + this.number);
        } else if (amount.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ApiException("Write off amount cannot be zero or negative. Account " + this.number);
        }

        BigDecimal newBalance = this.getBalance().subtract(amount);
        this.setBalance(newBalance);
    }

    public void topUp(BigDecimal amount) {
        if (this.disabled) {
            throw new ApiException("Could not execute top up on disabled account " + this.number);
        } else if (amount.compareTo(BigDecimal.ZERO) < 0 || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ApiException("Top up amount cannot be zero or negative. Account " + this.number);
        }

        BigDecimal newBalance = this.getBalance().add(amount);
        this.setBalance(newBalance);
    }

    /**
     * Getters.
     */
    public Object getLock() {
        return lock;
    }

    public Long getNumber() {
        return number;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Setters.
     */
    public void setNumber(Long number) {
        this.number = number;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Builder class.
     */
    public static class Builder {

        private Long accountNumber;
        private String owner;
        private BigDecimal balance;
        private boolean disabled;

        public Builder accountNumber(Long accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder forOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder withBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder isDisabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public Account build() {
            return new Account(accountNumber, owner, balance, disabled);
        }

    }

}
