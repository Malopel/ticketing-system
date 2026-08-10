package accordion_symphonic.ticketing.payment;

import accordion_symphonic.ticketing.order.Order;

public interface PaymentProvider {

    PaymentSession createPayment(Order order);
}