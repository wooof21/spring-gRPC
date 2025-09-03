package com.example.timeout09.repository;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AccountRepo {

    private static final Map<Integer, Integer> db =
            IntStream.rangeClosed(1, 10)
                    .filter( v -> v != 2 )
                    .boxed()
                    .collect(Collectors.toConcurrentMap(
                            Function.identity(),
                            v -> 1000 * v
                    ));

    public static Integer getBalance(int accountNumber){
        return db.get(accountNumber);
    }

    public static void depositMoney(int accountNumber, int amount){
        db.computeIfPresent(accountNumber, (k, v) -> v + amount);
    }

    public static void deductMoney(int accountNumber, int amount){
        db.computeIfPresent(accountNumber, (k, v) -> v - amount);
    }

    public static boolean isAccountExist(int accountNumber) {
        return db.containsKey(accountNumber);
    }

    public static Map<Integer, Integer> getAllAccounts(){
        return Collections.unmodifiableMap(db);
    }


}
