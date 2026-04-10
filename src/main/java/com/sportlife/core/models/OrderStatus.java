package com.sportlife.core.models;

/**
 * Estados posibles de una orden de compra.
 * <ul>
 *   <li>PENDING  – orden creada, esperando pago</li>
 *   <li>PAID     – pago aprobado, stock ya descontado</li>
 *   <li>REJECTED – pago rechazado</li>
 * </ul>
 */
public enum OrderStatus {
    PENDING,
    PAID,
    REJECTED
}
