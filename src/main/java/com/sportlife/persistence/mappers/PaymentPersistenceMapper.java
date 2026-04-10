package com.sportlife.persistence.mappers;

import com.sportlife.core.models.Payment;
import com.sportlife.persistence.entities.PaymentDocument;

public final class PaymentPersistenceMapper {

    private PaymentPersistenceMapper() {
    }

    public static Payment toModel(PaymentDocument doc) {
        return Payment.builder()
            .id(doc.getId())
            .orderId(doc.getOrderId())
            .method(doc.getMethod())
            .amount(doc.getAmount())
            .approved(doc.isApproved())
            .build();
    }

    public static PaymentDocument toDocument(Payment payment) {
        PaymentDocument doc = new PaymentDocument();
        doc.setId(payment.getId());
        doc.setOrderId(payment.getOrderId());
        doc.setMethod(payment.getMethod());
        doc.setAmount(payment.getAmount());
        doc.setApproved(payment.isApproved());
        return doc;
    }
}

