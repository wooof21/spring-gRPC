package com.trading.user.v2.entity;

import com.trading.common.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_item_v2")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItemV2 {

    @Id
    private String id; // UUID as primary key
    private Integer traderId;
    private Stock stock;
    private Integer quantity;
    private Double purchasePrice;
}
