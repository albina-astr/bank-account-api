package com.bank.rest.repo;

import com.bank.rest.model.Account;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AccountRepository {

    private static final AccountRepository SINGLE_INSTANCE = new AccountRepository();
    // where key is account number
    private Map<Long, Account> accounts = new ConcurrentHashMap<>();

    private AccountRepository() {
    }

    public static AccountRepository getSingleInstance() {
        return SINGLE_INSTANCE;
    }

    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    public Account save(Account account) {
        accounts.put(account.getNumber(), account);
        return accounts.get(account.getNumber());
    }

    public Optional<Account> findByNumber(Long accountNumber) {
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    public Set<Long> getAllAccountNumbers() {
        return new HashSet<>(accounts.keySet());
    }

}
