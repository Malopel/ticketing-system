import './styles/OrderStatusPage.css'

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

    const [refreshing, setRefreshing] = useState(false);
    const [refreshError, setRefreshError] =
        useState<string | null>(null);

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

    async function handleRefreshStatus() {
        if (!order || !accessToken) {
            return;
        }

        try {
            setRefreshing(true);
            setRefreshError(null);

            await fetchOrder(
                order.concertId,
                accessToken,
            );
        } catch (error) {
            if (error instanceof Error) {
                setRefreshError(error.message);
            } else {
                setRefreshError(
                    'Bestellstatus konnte nicht aktualisiert werden.',
                );
            }
        } finally {
            setRefreshing(false);
        }
    }

    return (
        <main className="order-status-page">
            <Link
                className="back-link"
                to="/"
            >
                ← Zurück zum Ticketshop
            </Link>

            <header className="order-page-header">
                <h1>Bestellung #{order.id}</h1>
                <p>{order.concertTitle}</p>
            </header>

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
                    )
                }

                {order.status === 'RESERVED' && (
                    <div className="status-refresh">
                        <button
                            type="button"
                            onClick={handleRefreshStatus}
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

            <section className="order-details">
                <h2>Deine Bestellung</h2>

                <div className="order-items">
                    {order.items.map((item) => (
                        <div
                            key={item.id}
                            className="order-item"
                        >
                            <div>
                                <strong>
                                    {item.ticketCategoryName}
                                </strong>

                                <span>
                                {item.quantity} ×{' '}
                                    {currencyFormatter.format(
                                        item.unitPrice,
                                    )}
                            </span>
                            </div>

                            <strong>
                                {currencyFormatter.format(
                                    item.totalPrice,
                                )}
                            </strong>
                        </div>
                    ))}
                </div>

                <div className="status-order-total">
                    <span>Gesamt</span>

                    <strong>
                        {currencyFormatter.format(
                            order.totalAmount,
                        )}
                    </strong>
                </div>
            </section>

            {order.status === 'RESERVED' && (
                <PaymentSection
                    order={order}
                    accessToken={accessToken}
                    onOrderUpdated={setOrder}
                />
            )}

            {order.status === 'PAID' && (
                <section className="ticket-delivery-card">
                    <h2>Deine Tickets</h2>

                    <p>
                        Die Tickets wurden an{' '}
                        <strong>{order.customerEmail}</strong>{' '}
                        gesendet.
                    </p>
                </section>
            )}
        </main>
    );
}

export default OrderStatusPage;