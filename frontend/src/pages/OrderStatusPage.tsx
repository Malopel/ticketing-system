import './styles/OrderStatusPage.css'

import {useCallback, useEffect, useRef, useState} from 'react';
import {Link, useParams} from 'react-router-dom';

import {
    getOrder,
    type OrderResponse,
} from '../api/orderApi';
import {useOrderCountdown} from '../hooks/useOrderCountdown';

import PaymentSection
    from '../components/checkout/PaymentSection';
import OrderStatusCard from '../components/order/OrderStatusCard';
import OrderDetails from '../components/order/OrderDetails';

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

    const [refreshing, setRefreshing] = useState(false);
    const [refreshError, setRefreshError] =
        useState<string | null>(null);

    const expirationRefreshTriggered =
        useRef<string | null>(null);

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

    const remainingSeconds =
        useOrderCountdown(order);

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
        if (
            !order ||
            remainingSeconds !== 0 ||
            !accessToken
        ) {
            return;
        }

        const refreshKey =
            `${order.id}:${order.status}`;

        if (
            expirationRefreshTriggered.current ===
            refreshKey
        ) {
            return;
        }

        expirationRefreshTriggered.current =
            refreshKey;

        const concertId = order.concertId;
        const currentOrderId = order.id;
        const currentAccessToken = accessToken;

        async function refreshOrderAfterExpiration() {
            try {
                const refreshedOrder =
                    await getOrder(
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
    }, [
        remainingSeconds,
        order,
        accessToken,
    ]);

    const pollingOrderId = order?.id;
    const pollingConcertId = order?.concertId;
    const pollingStatus = order?.status;

    useEffect(() => {
        if (
            pollingOrderId === undefined ||
            pollingConcertId === undefined ||
            pollingStatus !== 'PAYMENT_PENDING' ||
            !accessToken
        ) {
            return;
        }

        const currentOrderId = pollingOrderId;
        const currentConcertId = pollingConcertId;
        const currentAccessToken = accessToken;

        let timeoutId: number;
        let active = true;

        async function pollOrderStatus() {
            try {
                const refreshedOrder =
                    await getOrder(
                        currentConcertId,
                        currentOrderId,
                        currentAccessToken,
                    );

                if (!active) {
                    return;
                }

                setOrder(refreshedOrder);
            } catch {
                // Ein einzelner Polling-Fehler soll nicht
                // die ganze Bestellseite zerstören.
            }

            if (active) {
                timeoutId = window.setTimeout(
                    pollOrderStatus,
                    2500,
                );
            }
        }

        timeoutId = window.setTimeout(
            pollOrderStatus,
            2500,
        );

        return () => {
            active = false;
            window.clearTimeout(timeoutId);
        };
    }, [
        pollingOrderId,
        pollingConcertId,
        pollingStatus,
        accessToken,
    ]);

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

            <OrderStatusCard
                order={order}
                remainingSeconds={remainingSeconds}
                refreshing={refreshing}
                refreshError={refreshError}
                onRefresh={handleRefreshStatus}
            />

            <OrderDetails order={order}/>

            {order.status === 'RESERVED' && (
                <PaymentSection
                    order={order}
                    accessToken={accessToken}
                    onOrderUpdated={setOrder}
                    paymentDisabled={remainingSeconds === 0}
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