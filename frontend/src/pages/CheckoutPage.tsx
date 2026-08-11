import {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router-dom';

import {
    getConcertById,
    type Concert,
} from '../api/concertApi';

import {
    getTicketCategories,
    type TicketCategory,
} from '../api/ticketCategoryApi';

import { startPayment } from '../api/paymentApi';

import {
    createOrder,
    type CreatedOrderResponse,
} from '../api/orderApi';

function CheckoutPage() {
    const {concertId} = useParams<{
        concertId: string;
    }>();

    const id = Number(concertId);

    const [concert, setConcert] = useState<Concert | null>(null);

    const [ticketCategories, setTicketCategories] =
        useState<TicketCategory[]>([]);

    const [quantities, setQuantities] =
        useState<Record<number, number>>({});

    const [customerEmail, setCustomerEmail] = useState('');

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [ordering, setOrdering] = useState(false);
    const [orderError, setOrderError] = useState<string | null>(null);

    const [createdOrder, setCreatedOrder] =
        useState<CreatedOrderResponse | null>(null);

    const [startingPayment, setStartingPayment] = useState(false);
    const [paymentError, setPaymentError] = useState<string | null>(null);

    useEffect(() => {
        async function loadCheckout() {
            if (!Number.isInteger(id) || id <= 0) {
                setError('Ungültige Konzert-ID.');
                setLoading(false);
                return;
            }

            const storedQuantities = sessionStorage.getItem(
                `checkout-${id}`,
            );

            if (!storedQuantities) {
                setError('Es wurden keine Tickets ausgewählt.');
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                setError(null);

                const parsedQuantities: Record<number, number> =
                    JSON.parse(storedQuantities);

                const [loadedConcert, loadedCategories] =
                    await Promise.all([
                        getConcertById(id),
                        getTicketCategories(id),
                    ]);

                setQuantities(parsedQuantities);
                setConcert(loadedConcert);
                setTicketCategories(loadedCategories);
            } catch {
                setError(
                    'Bestellinformationen konnten nicht geladen werden.',
                );
            } finally {
                setLoading(false);
            }
        }

        void loadCheckout();
    }, [id]);

    const selectedCategories = ticketCategories.filter(
        (category) => (quantities[category.id] ?? 0) > 0,
    );

    const totalQuantity = selectedCategories.reduce(
        (sum, category) =>
            sum + (quantities[category.id] ?? 0),
        0,
    );

    const totalPrice = selectedCategories.reduce(
        (sum, category) =>
            sum +
            category.price *
            (quantities[category.id] ?? 0),
        0,
    );

    const currencyFormatter = new Intl.NumberFormat(
        'de-DE',
        {
            style: 'currency',
            currency: 'EUR',
        },
    );

    async function handleCreateOrder() {
        if (!concert) {
            return;
        }

        if (!customerEmail.trim()) {
            setOrderError('Bitte gib eine E-Mail-Adresse ein.');
            return;
        }

        const items = selectedCategories.map((category) => ({
            ticketCategoryId: category.id,
            quantity: quantities[category.id] ?? 0,
        }));

        if (items.length === 0) {
            setOrderError('Es wurden keine Tickets ausgewählt.');
            return;
        }

        try {
            setOrdering(true);
            setOrderError(null);

            const response = await createOrder(concert.id, {
                customerEmail: customerEmail.trim(),
                items,
            });

            setCreatedOrder(response);
        } catch (error) {
            if (error instanceof Error) {
                setOrderError(error.message);
            } else {
                setOrderError(
                    'Bestellung konnte nicht erstellt werden.',
                );
            }
        } finally {
            setOrdering(false);
        }
    }

    async function handleStartPayment() {
        if (!createdOrder) {
            return;
        }

        try {
            setStartingPayment(true);
            setPaymentError(null);

            const paymentSession = await startPayment(
                createdOrder.order.concertId,
                createdOrder.order.id,
                createdOrder.accessToken,
            );

            window.location.href = paymentSession.checkoutUrl;
        } catch (error) {
            if (error instanceof Error) {
                setPaymentError(error.message);
            } else {
                setPaymentError(
                    'Zahlung konnte nicht gestartet werden.',
                );
            }
        } finally {
            setStartingPayment(false);
        }
    }

    if (loading) {
        return <p>Bestellung wird geladen...</p>;
    }

    if (error) {
        return (
            <main>
                <p>Fehler: {error}</p>

                {Number.isInteger(id) && id > 0 && (
                    <Link to={`/concerts/${id}`}>
                        Zurück zur Ticketauswahl
                    </Link>
                )}
            </main>
        );
    }

    if (!concert) {
        return null;
    }

    return (
        <main>
            <Link to={`/concerts/${concert.id}`}>
                ← Zurück zur Ticketauswahl
            </Link>

            <h1>Bestellung</h1>

            <h2>{concert.title}</h2>

            <section>
                <h3>Deine Tickets</h3>

                {selectedCategories.map((category) => {
                    const quantity =
                        quantities[category.id] ?? 0;

                    return (
                        <div key={category.id}>
                            <p>
                                <strong>{category.name}</strong>
                            </p>

                            <p>
                                {quantity} ×{' '}
                                {currencyFormatter.format(
                                    category.price,
                                )}
                            </p>

                            <p>
                                {currencyFormatter.format(
                                    category.price * quantity,
                                )}
                            </p>
                        </div>
                    );
                })}

                <p>
                    <strong>Tickets:</strong>{' '}
                    {totalQuantity}
                </p>

                <p>
                    <strong>Gesamt:</strong>{' '}
                    {currencyFormatter.format(totalPrice)}
                </p>
            </section>

            {!createdOrder && (
                <section>
                    <h3>Kontaktdaten</h3>

                    <label htmlFor="customerEmail">
                        E-Mail-Adresse
                    </label>

                    <br />

                    <input
                        id="customerEmail"
                        type="email"
                        value={customerEmail}
                        onChange={(event) =>
                            setCustomerEmail(event.target.value)
                        }
                        placeholder="name@example.de"
                        disabled={ordering}
                    />

                    {orderError && (
                        <p>Fehler: {orderError}</p>
                    )}

                    <button
                        type="button"
                        onClick={handleCreateOrder}
                        disabled={
                            ordering ||
                            totalQuantity === 0 ||
                            !customerEmail.trim()
                        }
                    >
                        {ordering
                            ? 'Bestellung wird erstellt...'
                            : 'Zahlungspflichtig bestellen'}
                    </button>
                </section>
            )}
            
            {createdOrder && (
                <section>
                    <h3>Bestellung erstellt</h3>

                    <p>
                        Bestellnummer:{' '}
                        <strong>{createdOrder.order.id}</strong>
                    </p>

                    <p>
                        Status:{' '}
                        <strong>{createdOrder.order.status}</strong>
                    </p>

                    <p>
                        Gesamtbetrag:{' '}
                        <strong>
                            {currencyFormatter.format(
                                createdOrder.order.totalAmount,
                            )}
                        </strong>
                    </p>

                    <p>
                        Die Bestellung wurde reserviert und
                        wartet auf die Zahlung.
                    </p>

                    {paymentError && (
                        <p>Fehler: {paymentError}</p>
                    )}

                    <button
                        type="button"
                        onClick={handleStartPayment}
                        disabled={startingPayment}
                    >
                        {startingPayment
                            ? 'Zahlung wird gestartet...'
                            : 'Jetzt bezahlen'}
                    </button>
                </section>
            )}
        </main>
    );
}

export default CheckoutPage;