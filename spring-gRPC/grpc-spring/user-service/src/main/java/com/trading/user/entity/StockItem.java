package com.trading.user.entity;

import com.trading.common.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItem {

    @Id
    @GeneratedValue
    private Integer id;
    private Integer traderId;
    private Stock stock;
    private Integer quantity;
}
