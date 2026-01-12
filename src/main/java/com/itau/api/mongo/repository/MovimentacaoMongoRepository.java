package com.itau.api.mongo.repository;

import com.itau.api.mongo.model.MovimentacaoMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimentacaoMongoRepository extends MongoRepository<MovimentacaoMongo, String> {
}
