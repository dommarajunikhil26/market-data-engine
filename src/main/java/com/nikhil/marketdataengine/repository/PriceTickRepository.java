package com.nikhil.marketdataengine.repository;

import com.nikhil.marketdataengine.model.PriceTick;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceTickRepository extends ReactiveMongoRepository<PriceTick, String> {
}
