package com.sportlife.persistence.repositories;

import com.sportlife.persistence.entities.CartDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<CartDocument, String> {
    Optional<CartDocument> findByUserId(Long userId);
}

