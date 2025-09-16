package com.trading.user.v2.repository;

import com.trading.user.v2.entity.TraderV2;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraderRepoV2 extends CrudRepository<TraderV2, Integer> {
}
