package com.nikhil.marketdataengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class MarketDataEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketDataEngineApplication.class, args);
	}

}
