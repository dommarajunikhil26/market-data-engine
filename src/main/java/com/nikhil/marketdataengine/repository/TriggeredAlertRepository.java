package com.nikhil.marketdataengine.repository;

import com.nikhil.marketdataengine.model.TriggeredAlert;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TriggeredAlertRepository extends ReactiveMongoRepository<TriggeredAlert, String> {
}
