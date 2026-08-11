import {useCallback, useEffect, useRef, useState} from 'react';
import {Link, useParams} from 'react-router-dom';

import {
    getOrder,
    type OrderResponse,
} from '../api/orderApi';

import PaymentSection
    from '../components/checkout/PaymentSection';
import OrderStatusBadge from '../components/order/OrderStatusBadge';

type StoredOrderAccess = {
    concertId: number;
    accessToken: string;
};

function calculateRemainingSeconds(
    expiresAt: string,
): number {
    const remainingMilliseconds =
        new Date(expiresAt).getTime() - Date.now();

    return Math.max(
        0,
        Math.ceil(remainingMilliseconds / 1000),
    );
}

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

    const [countdown, setCountdown] = useState<{
        orderId: number;
        seconds: number;
    } | null>(null);

    const expirationRefreshTriggered =
        useRef<number | null>(null);

    const fetchOrder = useCallback(
        async (
            concertId: number,
            currentAccessToken: string,
        ) => {
            const loadedOrder = await getOrder(
                concertId,
                id,
                currentAccessToken,
            );

            setAccessToken(currentAccessToken);
            setOrder(loadedOrder);
        },
        [id],
    );

    useEffect(() => {
        async function initializeOrder() {
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

                await fetchOrder(
                    parsedAccess.concertId,
                    parsedAccess.accessToken,
                );
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

        void initializeOrder();
    }, [id, fetchOrder]);

    useEffect(() => {
        if (!order || order.status !== 'RESERVED') {
            return;
        }

        const currentOrderId = order.id;
        const expiresAt = order.expiresAt;

        const intervalId = window.setInterval(() => {
            setCountdown({
                orderId: currentOrderId,
                seconds:
                    calculateRemainingSeconds(expiresAt),
            });
        }, 1000);

        return () => {
            window.clearInterval(intervalId);
        };
    }, [order]);

    const remainingSeconds =
        order?.status === 'RESERVED'
            ? countdown?.orderId === order.id
                ? countdown.seconds
                : calculateRemainingSeconds(
                    order.expiresAt,
                )
            : null;

    useEffect(() => {
        if (
            !order ||
            order.status !== 'RESERVED' ||
            remainingSeconds !== 0 ||
            !accessToken
        ) {
            return;
        }

        if (
            expirationRefreshTriggered.current ===
            order.id
        ) {
            return;
        }

        expirationRefreshTriggered.current = order.id;

        const concertId = order.concertId;
        const currentOrderId = order.id;
        const currentAccessToken = accessToken;

        async function refreshOrderAfterExpiration() {
            try {
                const refreshedOrder = await getOrder(
                    concertId,
                    currentOrderId,
                    currentAccessToken,
                );

                setOrder(refreshedOrder);
            } catch (error) {
                if (error instanceof Error) {
                    setError(error.message);
                } else {
                    setError(
                        'Bestellstatus konnte nicht aktualisiert werden.',
                    );
                }
            }
        }

        void refreshOrderAfterExpiration();
    }, [remainingSeconds, order, accessToken]);

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
        <main>
            <Link to="/">
                ← Zurück zum Ticketshop
            </Link>

            <h1>Bestellung #{order.id}</h1>

            <h2>{order.concertTitle}</h2>

            <p>
                Status:{' '}
                <strong>
                    <OrderStatusBadge status={order.status}/>
                </strong>
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
                <>
                    {formattedRemainingTime && (
                        <section>
                            <p>
                                <strong>
                                    Reservierungszeit verbleibend:
                                </strong>{' '}
                                {formattedRemainingTime} Minuten
                            </p>
                        </section>
                    )}
                    <PaymentSection
                        order={order}
                        accessToken={accessToken}
                    />
                </>
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