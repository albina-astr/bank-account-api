package com.gimaletdinova.rest;

import com.gimaletdinova.rest.exception.ApiException;
import com.gimaletdinova.rest.model.Account;
import com.gimaletdinova.rest.repo.AccountRepository;
import com.gimaletdinova.rest.service.AccountService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by agimaletdinova on 14.09.2019.
 */
public class AccountServiceTest {

    private AccountService accountService = AccountService.getSingleInstance();
    private AccountRepository accountRepository = AccountRepository.getSingleInstance();

    @Before
    public void setup() {
        //repo init
        accountRepository.save(new Account(1L, "Harry Potter", BigDecimal.valueOf(1000), false));
        accountRepository.save(new Account(2L, "Voldemort", BigDecimal.valueOf(1000), false));
        accountRepository.save(new Account(3L, "Albus Dumbledore", BigDecimal.valueOf(1000), false));
    }

    @Test
    public void create_Test_Successful() {
        String owner = "Harry Potter";

        Account resultAccount = accountService.createAccount(owner);

        assert resultAccount.getNumber() > 0L;
        assertEquals(BigDecimal.ZERO, resultAccount.getBalance());
        assertEquals(owner, resultAccount.getOwner());
        assertFalse(resultAccount.isDisabled());
    }

    @Test
    public void create_Test_NullOwner() {
        ApiException e = assertThrows(ApiException.class, () -> accountService.createAccount(null));

        assertEquals("Cannot create account for null owner", e.getMessage());
    }

    @Test
    public void getInfo_Test_Successful() {
        Account expected = accountRepository.findByNumber(1L).get();

        Account result = accountService.getInfo(expected.getNumber());

        assertEquals(expected.getNumber(), result.getNumber());
        assertEquals(expected.getBalance(), result.getBalance());
        assertEquals(expected.getOwner(), result.getOwner());
        assertEquals(expected.isDisabled(), result.isDisabled());
    }

    @Test
    public void getInfo_Test_ExceptionGivenAccountNumberDoesNotExist() {
        long notExistAccountNumber = 10;

        ApiException e = assertThrows(ApiException.class, () -> accountService.getInfo(notExistAccountNumber));

        assertEquals("No account found with number " + notExistAccountNumber, e.getMessage());
    }

    @Test
    public void getInfo_Test_ExceptionNullAccountNumber() {
        ApiException e = assertThrows(ApiException.class, () -> accountService.getInfo(null));

        assertEquals("Null account number is not supported fot this operation", e.getMessage());
    }

    @Test
    public void updateAccount_Test_Successful() {
        Account aliceAccount = accountService.createAccount("Alice");
        aliceAccount.setBalance(BigDecimal.valueOf(1000));
        aliceAccount.setOwner("Alice Brown");

        Account updatedAccount = accountService.updateAccount(aliceAccount);

        assertEquals(aliceAccount.getNumber(), updatedAccount.getNumber());
        assertEquals("Alice Brown", updatedAccount.getOwner());
        assertEquals(BigDecimal.valueOf(1000), updatedAccount.getBalance());
        assertFalse(updatedAccount.isDisabled());
    }

    @Test
    public void updateAccount_Test_ExceptionNullAccount() {
        Account account = new Account.Builder()
                .accountNumber(null)
                .withBalance(BigDecimal.valueOf(1000))
                .build();

        ApiException e = assertThrows(ApiException.class, () -> accountService.updateAccount(account));

        assertEquals("Null account number is not supported fot this operation", e.getMessage());
    }

    @Test
    public void updateAccount_Test_ExceptionAccountNotExist() {
        Account account = new Account.Builder()
                .accountNumber(9999L)
                .withBalance(BigDecimal.valueOf(1000))
                .build();

        ApiException e = assertThrows(ApiException.class, () -> accountService.updateAccount(account));

        assertEquals("Cannot update non existing account " + account.getNumber(), e.getMessage());
    }

    @Test
    public void updateAccount_Test_ExceptionNegativeBalance() {
        Account account = new Account.Builder()
                .accountNumber(1L)
                .withBalance(BigDecimal.valueOf(-1000))
                .build();

        ApiException e = assertThrows(ApiException.class, () -> accountService.updateAccount(account));

        assertEquals("Balance cannot be negative", e.getMessage());
    }

    @Test
    public void getAllAccounts_Test_Successful() {
        int expected = accountRepository.findAll().size();

        List<Account> allAccounts = accountService.getAllAccounts();

        assertEquals(expected, allAccounts.size());
    }

    @Test
    public void topUp_Test_Successful() {
        String owner = "Drako";
        Account created = accountService.createAccount(owner);

        created = accountService.topUp(created.getNumber(), BigDecimal.valueOf(1000));

        assertEquals(BigDecimal.valueOf(1000), created.getBalance());
    }

    @Test
    public void topUp_Test_ExceptionNullAccount() {
        ApiException e = assertThrows(ApiException.class, () -> accountService.topUp(null, BigDecimal.valueOf(1000)));

        assertEquals("Null account number is not supported fot this operation", e.getMessage());
    }

    @Test
    public void topUp_Test_AccountNotExist() {
        long notExistAccountNumber = 10;

        ApiException e = assertThrows(ApiException.class, () -> accountService.topUp(notExistAccountNumber, BigDecimal.valueOf(1000)));

        assertEquals("No account found with number 10", e.getMessage());
    }

    @Test
    public void topUp_Test_ExceptionDisabledAccount() {
        Account account = accountService.createAccount("Wizard");
        accountService.deleteAccount(account.getNumber());

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.topUp(account.getNumber(), BigDecimal.valueOf(100)));

        assertEquals("Could not execute top up on disabled account " + account.getNumber(), exception.getMessage());
    }

    @Test
    public void topUp_Test_ExceptionNegativeAmount() {
        Account account = accountService.createAccount("Oliver");

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.topUp(account.getNumber(), BigDecimal.valueOf(-100)));

        assertEquals("Top up amount cannot be zero or negative. Account " + account.getNumber(), exception.getMessage());
    }

    @Test
    public void delete_Test_Successful() {
        Account created = accountService.createAccount("Robin");

        Account result = accountService.deleteAccount(created.getNumber());

        assertTrue(result.isDisabled());
    }

    @Test
    public void delete_Test_ExceptionAccountNotFound() {
        long notExistAccountNumber = 10;

        Throwable exception = assertThrows(ApiException.class, () -> accountService.deleteAccount(notExistAccountNumber));

        assertEquals("No account found with number 10", exception.getMessage());
    }

    @Test
    public void delete_Test_ExceptionNullAccount() {
        Throwable exception = assertThrows(ApiException.class, () -> accountService.deleteAccount(null));

        assertEquals("Null account number is not supported fot this operation", exception.getMessage());
    }

    @Test
    public void delete_Test_ExceptionAccountAlreadyDisabled() {
        Account account = accountService.createAccount("Robin");
        accountService.deleteAccount(account.getNumber());

        Throwable exception = assertThrows(ApiException.class, () -> accountService.deleteAccount(account.getNumber()));

        assertEquals("Cannot disable disabled account " + account.getNumber(), exception.getMessage());
    }

    @Test
    public void transfer_Test_TransferAllMoneySuccessful() {
        Account accountFrom = accountService.createAccount("Lola");
        Account accountTo = accountService.createAccount("Tina");
        accountFrom.topUp(BigDecimal.valueOf(1000));
        accountTo.topUp(BigDecimal.valueOf(1000));

        accountService.transfer(accountFrom.getNumber(), accountTo.getNumber(), BigDecimal.valueOf(1000));

        //accounts exist
        accountFrom = accountRepository.findByNumber(accountFrom.getNumber()).get();
        accountTo = accountRepository.findByNumber(accountTo.getNumber()).get();

        assertEquals(BigDecimal.valueOf(0), accountFrom.getBalance());
        assertEquals(BigDecimal.valueOf(2000), accountTo.getBalance());
    }

    @Test
    public void transfer_Test_TransferPartOfMoneySuccessful() {
        //get data from repo
        long accountNumberFrom = 1;
        long accountNumberTo = 2;

        accountService.transfer(accountNumberFrom, accountNumberTo, BigDecimal.valueOf(1000));

        //accounts definitely exist
        Account accountFrom = accountRepository.findByNumber(accountNumberFrom).get();
        Account accountTo = accountRepository.findByNumber(accountNumberTo).get();

        assertEquals(BigDecimal.valueOf(0), accountFrom.getBalance());
        assertEquals(BigDecimal.valueOf(2000), accountTo.getBalance());
    }

    @Test
    public void transfer_Test_ExceptionDisabledAccountFrom() throws ApiException {
        Account accountFrom = accountService.createAccount("Benedict Cumberbatch");
        Account accountTo = accountService.createAccount("John Watson");
        accountService.deleteAccount(accountFrom.getNumber());

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.transfer(accountFrom.getNumber(), accountTo.getNumber(), BigDecimal.valueOf(100)));

        assertEquals("Could not execute write off from disabled account " + accountFrom.getNumber(), exception.getMessage());
    }

    @Test
    public void transfer_Test_ExceptionDisabledAccountTo() throws ApiException {
        Account accountFrom = accountService.createAccount("Bob");
        Account accountTo = accountService.createAccount("Tom");
        accountService.topUp(accountFrom.getNumber(), BigDecimal.valueOf(1000));
        accountService.deleteAccount(accountTo.getNumber());

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.transfer(accountFrom.getNumber(), accountTo.getNumber(), BigDecimal.valueOf(100)));

        assertEquals("Could not execute top up on disabled account " + accountTo.getNumber(), exception.getMessage());
    }

    @Test
    public void transfer_Test_ExceptionNotSufficientMoneyOnAccountFrom() throws ApiException {
        Account accountFrom = accountService.createAccount("Andy");
        Account accountTo = accountService.createAccount("Sandy");
        accountFrom.topUp(BigDecimal.valueOf(1000));
        accountTo.topUp(BigDecimal.valueOf(500));

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.transfer(accountFrom.getNumber(), accountTo.getNumber(), BigDecimal.valueOf(1001)));

        assertEquals("Not sufficient funds for write off on account " + accountFrom.getNumber(), exception.getMessage());
    }

    @Test
    public void transfer_Test_ExceptionNegativeTransferAmount() throws ApiException {
        //get data from repo
        long accountNumberFrom = 1;
        long accountNumberTo = 2;

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.transfer(accountNumberFrom, accountNumberTo, BigDecimal.valueOf(-100)));

        assertEquals("Write off amount cannot be zero or negative. Account " + accountNumberFrom, exception.getMessage());
    }

    @Test
    public void transfer_Test_ExceptionZeroTransferAmount() throws ApiException {
        //get data from repo
        long accountNumberFrom = 1;
        long accountNumberTo = 2;

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.transfer(accountNumberFrom, accountNumberTo, BigDecimal.ZERO));

        assertEquals("Write off amount cannot be zero or negative. Account " + accountNumberFrom, exception.getMessage());
    }

    @Test
    public void transfer_Test_ExceptionNullAccountFrom() throws ApiException {
        //get data from repo
        Long accountNumberFrom = null;
        long accountNumberTo = 2;

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.transfer(accountNumberFrom, accountNumberTo, BigDecimal.valueOf(1000)));

        assertEquals("Null account number is not supported fot this operation", exception.getMessage());
    }

    @Test
    public void transfer_Test_ExceptionNullAccountTo() throws ApiException {
        //get data from repo
        long accountNumberFrom = 1;
        Long accountNumberTo = null;

        Throwable exception = assertThrows(ApiException.class,
                () -> accountService.transfer(accountNumberFrom, accountNumberTo, BigDecimal.valueOf(1000)));

        assertEquals("Null account number is not supported fot this operation", exception.getMessage());
    }

}