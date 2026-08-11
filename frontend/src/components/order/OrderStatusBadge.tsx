import type {OrderResponse} from '../../api/orderApi';

type OrderStatusBadgeProps = {
    status: OrderResponse['status'];
};

function OrderStatusBadge({ status }: OrderStatusBadgeProps) {
    switch (status) {
        case 'RESERVED':
            return <span>🕒 Zahlung ausstehend</span>;

        case 'PAID':
            return <span>✅ Bezahlt</span>;

        case 'EXPIRED':
            return <span>⌛ Reservierung abgelaufen</span>;

        case 'CANCELLED':
            return <span>❌ Storniert</span>;

        default:
            return <span>{status}</span>;
    }
}

export default OrderStatusBadge;