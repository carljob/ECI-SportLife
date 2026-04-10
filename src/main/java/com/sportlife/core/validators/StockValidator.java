package com.sportlife.core.validators;

import com.sportlife.handlers.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class StockValidator {

    public void validate(Integer available, Integer requested) {
        if (requested <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a 0");
        }
        if (available < requested) {
            throw new BusinessException("Stock insuficiente");
        }
    }
}

