import {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router-dom';

import {
    getOrder,
    type OrderResponse,
} from '../api/orderApi';

import PaymentSection
    from '../components/checkout/PaymentSection';

type StoredOrderAccess = {
    concertId: number;
    accessToken: string;
};

function OrderStatusPage() {
    const {orderId} = useParams<{
        orderId: string;
    }>();

    const id = Number(orderId);

    const [order, setOrder] =
        useState<OrderResponse | null>(null);

    const [accessToken, setAccessToken] =
        useState<string | null>(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] =
        useState<string | null>(null);

    useEffect(() => {
        async function loadOrder() {
            if (!Number.isInteger(id) || id <= 0) {
                setError('Ungültige Bestellnummer.');
                setLoading(false);
                return;
            }

            const storedAccess = sessionStorage.getItem(
                `order-${id}`,
            );

            if (!storedAccess) {
                setError(
                    'Die Zugriffsdaten für diese Bestellung fehlen.',
                );
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                setError(null);

                const parsedAccess =
                    JSON.parse(storedAccess) as StoredOrderAccess;

                if (
                    !Number.isInteger(parsedAccess.concertId) ||
                    parsedAccess.concertId <= 0 ||
                    typeof parsedAccess.accessToken !== 'string' ||
                    !parsedAccess.accessToken
                ) {
                    throw new Error(
                        'Ungültige gespeicherte Zugriffsdaten.',
                    );
                }

                const loadedOrder = await getOrder(
                    parsedAccess.concertId,
                    id,
                    parsedAccess.accessToken,
                );

                setAccessToken(parsedAccess.accessToken);
                setOrder(loadedOrder);
            } catch (error) {
                if (error instanceof Error) {
                    setError(error.message);
                } else {
                    setError(
                        'Bestellung konnte nicht geladen werden.',
                    );
                }
            } finally {
                setLoading(false);
            }
        }

        void loadOrder();
    }, [id]);

    if (loading) {
        return <p>Bestellung wird geladen...</p>;
    }

    if (error) {
        return (
            <main>
                <h1>Bestellung</h1>

                <p>Fehler: {error}</p>

                <Link to="/">
                    Zurück zum Ticketshop
                </Link>
            </main>
        );
    }

    if (!order || !accessToken) {
        return null;
    }

    const currencyFormatter = new Intl.NumberFormat(
        'de-DE',
        {
            style: 'currency',
            currency: 'EUR',
        },
    );

    return (
        <main>
            <Link to="/">
                ← Zurück zum Ticketshop
            </Link>

            <h1>Bestellung #{order.id}</h1>

            <h2>{order.concertTitle}</h2>

            <p>
                Status:{' '}
                <strong>{order.status}</strong>
            </p>

            <section>
                <h2>Tickets</h2>

                {order.items.map((item) => (
                    <div key={item.id}>
                        <p>
                            <strong>
                                {item.ticketCategoryName}
                            </strong>
                        </p>

                        <p>
                            {item.quantity} ×{' '}
                            {currencyFormatter.format(
                                item.unitPrice,
                            )}
                        </p>

                        <p>
                            {currencyFormatter.format(
                                item.totalPrice,
                            )}
                        </p>
                    </div>
                ))}

                <p>
                    <strong>Gesamt:</strong>{' '}
                    {currencyFormatter.format(
                        order.totalAmount,
                    )}
                </p>
            </section>

            {order.status === 'RESERVED' && (
                <PaymentSection
                    order={order}
                    accessToken={accessToken}
                />
            )}

            {order.status === 'PAID' && (
                <section>
                    <h2>Zahlung erfolgreich</h2>

                    <p>
                        Deine Bestellung wurde bezahlt.
                    </p>

                    <p>
                        Deine Tickets wurden an{' '}
                        <strong>
                            {order.customerEmail}
                        </strong>{' '}
                        gesendet.
                    </p>
                </section>
            )}

            {order.status === 'EXPIRED' && (
                <section>
                    <h2>Reservierung abgelaufen</h2>

                    <p>
                        Diese Bestellung kann nicht mehr
                        bezahlt werden.
                    </p>
                </section>
            )}

            {order.status === 'CANCELLED' && (
                <section>
                    <h2>Bestellung storniert</h2>

                    <p>
                        Diese Bestellung wurde storniert.
                    </p>
                </section>
            )}
        </main>
    );
}

export default OrderStatusPage;