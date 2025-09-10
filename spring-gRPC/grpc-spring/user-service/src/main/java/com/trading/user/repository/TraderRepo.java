package com.trading.user.repository;

import com.trading.user.entity.Trader;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraderRepo extends CrudRepository<Trader, Integer> {
}
