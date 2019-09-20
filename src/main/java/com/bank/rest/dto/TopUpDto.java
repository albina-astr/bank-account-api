package com.bank.rest.dto;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

public class TopUpDto {

    private Long accountNumber;
    private BigDecimal amount;

    @JsonCreator
    public TopUpDto(@JsonProperty("accountNumber") Long accountNumber, @JsonProperty("amount") BigDecimal amount) {
        this.accountNumber = accountNumber;
        this.amount = amount;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
