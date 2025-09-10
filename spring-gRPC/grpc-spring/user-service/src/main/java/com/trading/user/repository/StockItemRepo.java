package com.trading.user.repository;

import com.trading.common.Stock;
import com.trading.user.entity.StockItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepo extends CrudRepository<StockItem, Integer> {

    List<StockItem> findAllByTraderId(Integer traderId);

    Optional<StockItem> findByTraderIdAndStock(Integer traderId, Stock stock);
}
