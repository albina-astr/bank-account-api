package com.gimaletdinova.rest.dto;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

/**
 * Created by agimaletdinova on 16.09.2019.
 */
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
