package com.example.communicationpatterns06.repos;


import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class AccountRepo {

    private static final Map<Integer, Integer> dbBalances = IntStream.range(1, 10)
            .boxed()
            .collect(Collectors.toConcurrentMap(Function.identity(), v -> 1000 * v));


    public static Integer getBalance(int accountNumber){
        return dbBalances.get(accountNumber);
    }

    public static Map<Integer, Integer> getAllAccounts(){
        return Collections.unmodifiableMap(dbBalances);
    }

    public static void deductMoney(int accountNumber, int amount){
        dbBalances.computeIfPresent(accountNumber, (k, v) -> v - amount);
    }

    public static void depositMoney(int accountNumber, int amount){
        dbBalances.computeIfPresent(accountNumber, (k, v) -> v + amount);
    }
}

