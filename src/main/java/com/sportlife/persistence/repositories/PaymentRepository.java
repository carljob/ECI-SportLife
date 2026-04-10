package com.sportlife.persistence.repositories;

import com.sportlife.persistence.entities.PaymentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<PaymentDocument, String> {
}

