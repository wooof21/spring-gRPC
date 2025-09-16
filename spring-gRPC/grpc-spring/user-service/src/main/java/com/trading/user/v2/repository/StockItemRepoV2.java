package com.trading.user.v2.repository;

import com.trading.common.Stock;
import com.trading.user.v2.entity.StockItemV2;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepoV2 extends CrudRepository<StockItemV2, String> {

    List<StockItemV2> findAllByTraderId(Integer traderId);

    List<StockItemV2> findByTraderIdAndStock(Integer traderId, Stock stock);

    Optional<StockItemV2> findByIdAndTraderId(String id, Integer traderId);
}
