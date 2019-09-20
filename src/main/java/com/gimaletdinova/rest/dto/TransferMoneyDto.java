package com.gimaletdinova.rest.dto;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;

/**
 * Created by agimaletdinova on 14.09.2019.
 */
public class TransferMoneyDto {

    Long accountNumberFrom;
    Long accountNumberTo;
    BigDecimal amount;

    @JsonCreator
    public TransferMoneyDto(@JsonProperty("accountNumberFrom") Long accountNumberFrom,
                            @JsonProperty("accountNumberTo") Long accountNumberTo,
                            @JsonProperty("amount") BigDecimal amount) {
        this.accountNumberFrom = accountNumberFrom;
        this.accountNumberTo = accountNumberTo;
        this.amount = amount;
    }

    public Long getAccountNumberFrom() {
        return accountNumberFrom;
    }

    public Long getAccountNumberTo() {
        return accountNumberTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
