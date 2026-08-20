import './styles/OrderStatusBadge.css'

import type {OrderResponse} from '../../api/orderApi';

type OrderStatusBadgeProps = {
    status: OrderResponse['status'];
};

function OrderStatusBadge({
                              status,
                          }: OrderStatusBadgeProps) {
    switch (status) {
        case 'RESERVED':
            return (
                <span className="order-status-badge order-status-reserved">
                    🕒 Zahlung ausstehend
                </span>
            );

        case 'PAYMENT_PENDING':
            return (
                <span className="order-status-badge order-status-reserved">
                    🕒 Zahlung ausstehend
                </span>
            );

        case 'PAID':
            return (
                <span className="order-status-badge order-status-paid">
                    ✓ Bezahlt
                </span>
            );

        case 'EXPIRED':
            return (
                <span className="order-status-badge order-status-expired">
                    ⌛ Reservierung abgelaufen
                </span>
            );

        case 'CANCELLED':
            return (
                <span className="order-status-badge order-status-cancelled">
                    ✕ Storniert
                </span>
            );
    }
}

export default OrderStatusBadge;