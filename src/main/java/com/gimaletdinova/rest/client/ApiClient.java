package com.gimaletdinova.rest.client;

import com.gimaletdinova.rest.dto.TopUpDto;
import com.gimaletdinova.rest.dto.TransferMoneyDto;
import com.gimaletdinova.rest.exception.ApiWebException;
import com.gimaletdinova.rest.model.Account;
import com.gimaletdinova.rest.service.AccountService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/accounts")
public class ApiClient {

    private AccountService accountService = AccountService.getSingleInstance();

    @POST
    @Path("/create/{owner}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account create(@PathParam("owner") String owner) {
        Account account;
        try {
            account = accountService.createAccount(owner);
        } catch (Exception ex) {
            throw new ApiWebException(ex.getMessage());
        }

        return account;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> findAll() {
        return accountService.getAllAccounts();
    }

    @GET
    @Path("/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getByNumber(@PathParam("accountNumber") Long accountNumber) {
        Account account;
        try {
            account = accountService.getInfo(accountNumber);
        } catch (Exception ex) {
            throw new ApiWebException(ex.getMessage());
        }

        return account;
    }

    @PUT
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public Account update(Account account) {
        Account updated;
        try {
            updated = accountService.updateAccount(account);
        } catch (Exception ex) {
            throw new ApiWebException(ex.getMessage());
        }

        return updated;
    }

    @POST
    @Path("/top_up")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Account topUp(TopUpDto dto) {
        Account account;
        try {
            account = accountService.topUp(dto.getAccountNumber(), dto.getAmount());
        } catch (Exception ex) {
            throw new ApiWebException(ex.getMessage());
        }

        return account;
    }

    @DELETE
    @Path("/delete/{accountNumber}")
    public void delete(@PathParam("accountNumber") Long accountNumber) {
        try {
            accountService.deleteAccount(accountNumber);
        } catch (Exception ex) {
            throw new ApiWebException(ex.getMessage());
        }
    }

    @POST
    @Path("/transfer")
    public void transfer(TransferMoneyDto dto) {
        try {
            accountService.transfer(dto.getAccountNumberFrom(), dto.getAccountNumberTo(), dto.getAmount());
        } catch (Exception ex) {
            throw new ApiWebException(ex.getMessage());
        }
    }

}
