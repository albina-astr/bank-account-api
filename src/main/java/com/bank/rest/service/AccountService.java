package com.bank.rest.service;

import com.bank.rest.exception.ApiException;
import com.bank.rest.model.Account;
import com.bank.rest.repo.AccountRepository;
import org.apache.commons.lang3.RandomUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * Service for account operations.
 */
public class AccountService {

    private static final AccountService SINGLE_INSTANCE = new AccountService();
    private AccountRepository accountRepository;

    private AccountService() {
        accountRepository = AccountRepository.getSingleInstance();
    }

    public static AccountService getSingleInstance() {
        return SINGLE_INSTANCE;
    }

    /**
     * Creates new account with unique account number.
     *
     * @param owner account user data.
     * @return created object.
     */
    public Account createAccount(String owner) {
        if (isNull(owner)) {
            throw new ApiException("Cannot create account for null owner");
        }

        long accountNumber = generateAccountNumber();
        Account account = new Account.Builder()
                .accountNumber(accountNumber)
                .forOwner(owner)
                .withBalance(BigDecimal.ZERO)
                .isDisabled(false)
                .build();

        return accountRepository.save(account);
    }

    public Account getInfo(Long accountNumber) {
        validateAccountNumber(accountNumber);

        return accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new ApiException("No account found with number " + accountNumber));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Updates account only if it exists.
     *
     * @param account new account data.
     * @return updated object.
     */
    public Account updateAccount(Account account) {
        validateAccountNumber(account.getNumber());
        validateBalance(account.getBalance());

        boolean accountExist = accountRepository.findByNumber(account.getNumber()).isPresent();
        if (!accountExist) {
            throw new ApiException("Cannot update non existing account " + account.getNumber());
        }

        return accountRepository.save(account);
    }

    /**
     * Adds money to account using its number.
     *
     * @param accountNumber account number to which money will be added
     * @param amount        money amount for top up.
     * @return account with updated balance.
     */
    public Account topUp(Long accountNumber, BigDecimal amount) {
        validateAccountNumber(accountNumber);

        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new ApiException("No account found with number " + accountNumber));

        Object lock = account.getLock();
        synchronized (lock) {
            account.topUp(amount);
            account = accountRepository.save(account);
        }

        return account;
    }

    /**
     * Disables account by its number leaving all account info.
     * All transactions are not available for disabled account.
     *
     * @param accountNumber account number for disable.
     * @return disabled account.
     */
    public Account deleteAccount(Long accountNumber) {
        validateAccountNumber(accountNumber);

        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new ApiException("No account found with number " + accountNumber));

        if (account.isDisabled()) {
            throw new ApiException("Cannot disable disabled account " + account.getNumber());
        }

        account.setDisabled(true);

        return accountRepository.save(account);
    }

    /**
     * Executes money transfer between 2 accounts using account numbers.
     *
     * @param accountNumberFrom account number from which money will be written off.
     * @param accountNumberTo   account number to which money will be transferred.
     * @param amount            money amount for transfer.
     */
    public void transfer(Long accountNumberFrom, Long accountNumberTo, BigDecimal amount) {
        validateAccountNumber(accountNumberFrom);
        validateAccountNumber(accountNumberTo);

        Account accountFrom = accountRepository.findByNumber(accountNumberFrom)
                .orElseThrow(() -> new ApiException("No account found with number " + accountNumberFrom));
        Account accountTo = accountRepository.findByNumber(accountNumberTo)
                .orElseThrow(() -> new ApiException("No account found with number " + accountNumberTo));

        Object lock1, lock2;
        if (accountFrom.getNumber() < accountTo.getNumber()) {
            lock1 = accountFrom.getLock();
            lock2 = accountTo.getLock();
        } else {
            lock1 = accountTo.getLock();
            lock2 = accountFrom.getLock();
        }

        synchronized (lock1) {
            synchronized (lock2) {
                accountFrom.writeOff(amount);
                accountTo.topUp(amount);
            }
        }
    }

    /**
     * Generates unique account number according to existing account numbers.
     *
     * @return unique long sequence.
     */
    long generateAccountNumber() {
        int rnd = RandomUtils.nextInt();
        long accountNumber = rnd > 0 ? rnd : rnd * -1;

        Set<Long> allAccountNumbers = accountRepository.getAllAccountNumbers();
        while (allAccountNumbers.contains(accountNumber)) {
            rnd = RandomUtils.nextInt();
            accountNumber = rnd > 0 ? rnd : rnd * -1;
        }

        return accountNumber;
    }

    private void validateAccountNumber(Long accountNumber) {
        if (isNull(accountNumber)) {
            throw new ApiException("Null account number is not supported fot this operation");
        }
    }

    private void validateBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException("Balance cannot be negative");
        }
    }

}
