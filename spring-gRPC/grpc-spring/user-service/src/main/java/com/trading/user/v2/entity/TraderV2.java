package com.trading.user.v2.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trader_v2")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraderV2 {

    @Id
    private Integer id;
    private String name;
    private Double balance;
}
