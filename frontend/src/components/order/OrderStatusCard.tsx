import type {OrderResponse} from '../../api/orderApi';
import OrderStatusBadge from './OrderStatusBadge';

type OrderStatusCardProps = {
    order: OrderResponse;
    remainingSeconds: number | null;
    refreshing: boolean;
    refreshError: string | null;
    onRefresh: () => void;
};

function OrderStatusCard({
                             order,
                             remainingSeconds,
                             refreshing,
                             refreshError,
                             onRefresh,
                         }: OrderStatusCardProps) {
    const remainingMinutes =
        remainingSeconds !== null
            ? Math.floor(remainingSeconds / 60)
            : null;

    const remainingSecondsPart =
        remainingSeconds !== null
            ? remainingSeconds % 60
            : null;

    const formattedRemainingTime =
        remainingMinutes !== null &&
        remainingSecondsPart !== null
            ? `${remainingMinutes}:${remainingSecondsPart
                .toString()
                .padStart(2, '0')}`
            : null;

    return (
        <section className="order-status-card">
            <OrderStatusBadge status={order.status}/>

            {order.status === 'RESERVED' &&
                formattedRemainingTime && (
                    <p>
                        Deine Tickets sind noch{' '}
                        <strong>
                            {formattedRemainingTime} Minuten
                        </strong>{' '}
                        für dich reserviert.
                    </p>
                )}

            {order.status === 'PAYMENT_PENDING' &&
                formattedRemainingTime && (
                    <p>
                        Deine Zahlung wird verarbeitet.
                        Das Zahlungsfenster ist noch{' '}
                        <strong>
                            {formattedRemainingTime} Minuten
                        </strong>{' '}
                        geöffnet.
                    </p>
                )}

            {order.status === 'PAYMENT_PENDING' &&
                !formattedRemainingTime && (
                    <p>
                        Deine Zahlung wird verarbeitet.
                        Aktualisiere den Status in Kürze erneut.
                    </p>
                )}

            {(order.status === 'RESERVED' ||
                order.status === 'PAYMENT_PENDING') && (
                <div className="status-refresh">
                    <button
                        type="button"
                        onClick={onRefresh}
                        disabled={refreshing}
                    >
                        {refreshing
                            ? 'Status wird aktualisiert...'
                            : 'Status aktualisieren'}
                    </button>

                    {refreshError && (
                        <p className="error-message">
                            Fehler: {refreshError}
                        </p>
                    )}
                </div>
            )}

            {order.status === 'PAID' && (
                <p>
                    Deine Bestellung wurde erfolgreich bezahlt.
                </p>
            )}

            {order.status === 'EXPIRED' && (
                <p>
                    Die Reservierungszeit ist abgelaufen.
                    Diese Bestellung kann nicht mehr bezahlt werden.
                </p>
            )}

            {order.status === 'CANCELLED' && (
                <p>
                    Diese Bestellung wurde storniert.
                </p>
            )}
        </section>
    );
}

export default OrderStatusCard;