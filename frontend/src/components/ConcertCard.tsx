import {useState} from 'react';
import type {Concert} from '../api/concertApi';
import { startPayment } from '../api/paymentApi';
import {
    getTicketCategories,
    type TicketCategory,
} from '../api/ticketCategoryApi';
import {
    createOrder,
    getOrder,
    type CreatedOrderResponse,
    type OrderResponse,
} from '../api/orderApi';
import TicketCategoryCard from './TicketCategoryCard';


type ConcertCardProps = {
    concert: Concert;
};

function ConcertCard({concert}: ConcertCardProps) {
    const [ticketCategories, setTicketCategories] = useState<TicketCategory[]>([]);
    const [quantities, setQuantities] = useState<Record<number, number>>({});
    const [loadingTickets, setLoadingTickets] = useState(false);
    const [ticketError, setTicketError] = useState<string | null>(null);
    const [customerEmail, setCustomerEmail] = useState('');
    const [ordering, setOrdering] = useState(false);
    const [orderError, setOrderError] = useState<string | null>(null);
    const [createdOrder, setCreatedOrder] = useState<CreatedOrderResponse | null>(null);
    const [loadedOrder, setLoadedOrder] = useState<OrderResponse | null>(null);
    const [startingPayment, setStartingPayment] = useState(false);
    const [paymentError, setPaymentError] = useState<string | null>(null);

    const startTime = new Date(concert.startTime);

    const formattedDate = startTime.toLocaleDateString('de-DE', {
        day: '2-digit',
        month: 'long',
        year: 'numeric',
    });

    const formattedTime = startTime.toLocaleTimeString('de-DE', {
        hour: '2-digit',
        minute: '2-digit',
    });

    async function handleShowTickets() {
        setLoadingTickets(true);
        setTicketError(null);

        try {
            const categories = await getTicketCategories(concert.id);
            setTicketCategories(categories);
        } catch (error) {
            if (error instanceof Error) {
                setTicketError(error.message);
            } else {
                setTicketError('Ein unbekannter Fehler ist aufgetreten.')
            }
        } finally {
            setLoadingTickets(false);
        }
    }

    function handleQuantityChange(categoryId: number, quantity: number) {
        setQuantities((currentQuantities) => ({
            ...currentQuantities,
            [categoryId]: quantity,
        }));
    }

    const totalQuantity = Object.values(quantities).reduce(
        (sum, quantity) => sum + quantity,
        0,
    );

    const totalPrice = ticketCategories.reduce((sum, category) => {
        const quantity = quantities[category.id] ?? 0;

        return sum + category.price * quantity;
    }, 0);

    const currencyFormatter = new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
    });

    async function handleOrder() {
        setOrdering(true);
        setOrderError(null);

        const items = ticketCategories
            .map((category) => ({
                ticketCategoryId: category.id,
                quantity: quantities[category.id] ?? 0,
            }))
            .filter((item) => item.quantity > 0);

        try {
            const result = await createOrder(concert.id, {
                customerEmail,
                items,
            });

            setCreatedOrder(result);
            setQuantities({});

            console.log('Order erstellt:', result);
        } catch (error) {
            if (error instanceof Error) {
                setOrderError(error.message);
            } else {
                setOrderError('Ein unbekannter Fehler ist aufgetreten.');
            }
        } finally {
            setOrdering(false);
        }
    }

    async function handleReloadOrder() {
        if (createdOrder === null) {
            return;
        }

        try {
            const order = await getOrder(
                concert.id,
                createdOrder.order.id,
                createdOrder.accessToken,
            );

            setLoadedOrder(order);
        } catch (error) {
            if (error instanceof Error) {
                setOrderError(error.message);
            } else {
                setOrderError('Ein unbekannter Fehler ist aufgetreten.');
            }
        }
    }

    async function handleStartPayment() {
        if (createdOrder === null) {
            return;
        }

        setStartingPayment(true);
        setPaymentError(null);

        try {
            const paymentSession = await startPayment(
                concert.id,
                createdOrder.order.id,
                createdOrder.accessToken,
            );

            console.log('Payment gestartet:', paymentSession);

            window.location.href = paymentSession.checkoutUrl;
        } catch (error) {
            if (error instanceof Error) {
                setPaymentError(error.message);
            } else {
                setPaymentError('Ein unbekannter Fehler ist aufgetreten.');
            }
        } finally {
            setStartingPayment(false);
        }
    }

    const currentOrder = loadedOrder ?? createdOrder?.order ?? null;

    return (
        <article>
            <h3>{concert.title}</h3>

            <p>{concert.description}</p>

            <p>
                <strong>Ort:</strong> {concert.location}
            </p>

            <p>
                <strong>Termin:</strong> {formattedDate} · {formattedTime} Uhr
            </p>

            <button onClick={handleShowTickets}>
                Tickets anzeigen
            </button>

            {loadingTickets && (
                <p>Ticketkategorien werden geladen...</p>
            )}

            {ticketError && (
                <p>Fehler: {ticketError}</p>
            )}

            {ticketCategories.length > 0 && (
                <div>
                    <h4>Tickets</h4>

                    {ticketCategories.map((category) => (
                        <TicketCategoryCard
                            key={category.id}
                            category={category}
                            quantity={quantities[category.id] ?? 0}
                            onQuantityChange={handleQuantityChange}
                        />
                    ))}

                    {totalQuantity > 0 && (
                        <div>
                            <p>
                                <strong>Ausgewählte Tickets:</strong> {totalQuantity}
                            </p>

                            <p>
                                <strong>Gesamtpreis:</strong> {currencyFormatter.format(totalPrice)}
                            </p>

                            <label>
                                E-Mail-Adresse
                                <input
                                    type="email"
                                    value={customerEmail}
                                    onChange={(event) => setCustomerEmail(event.target.value)}
                                />
                            </label>

                            <button
                                type="button"
                                onClick={handleOrder}
                                disabled={ordering || customerEmail.trim() === ''}
                            >
                                {ordering ? 'Bestellung wird erstellt...' : 'Jetzt bestellen'}
                            </button>
                        </div>
                    )}
                </div>
            )}
            {orderError && (
                <p>Fehler: {orderError}</p>
            )}

            {createdOrder !== null && (
                <div>
                    <h4>Bestellung erfolgreich erstellt</h4>

                    <p>
                        <strong>Bestellnummer:</strong> {createdOrder.order.id}
                    </p>

                    <p>
                        <strong>Status:</strong> {currentOrder?.status}
                    </p>

                    <p>
                        <strong>E-Mail:</strong> {createdOrder.order.customerEmail}
                    </p>

                    <p>
                        <strong>Gesamtpreis:</strong>{' '}
                        {currencyFormatter.format(createdOrder.order.totalAmount)}
                    </p>

                    <p>
                        <strong>Reserviert bis:</strong>{' '}
                        {new Date(createdOrder.order.expiresAt).toLocaleString('de-DE')}
                    </p>

                    {currentOrder?.status === 'PAID' ? (
                        <p>
                            Zahlung erfolgreich. Deine Tickets wurden erstellt.
                        </p>
                    ) : currentOrder?.status === 'RESERVED' ? (
                        <button
                            type="button"
                            onClick={handleStartPayment}
                            disabled={startingPayment}
                        >
                            {startingPayment
                                ? 'Zahlung wird gestartet...'
                                : 'Jetzt bezahlen'}
                        </button>
                    ) : null}

                    {paymentError && (
                        <p>Fehler: {paymentError}</p>
                    )}

                    <button
                        type="button"
                        onClick={handleReloadOrder}
                    >
                        Bestellung erneut laden
                    </button>

                    {loadedOrder !== null && (
                        <p>
                            Bestellung vom Backend geladen. Aktueller Status:{' '}
                            <strong>{loadedOrder.status}</strong>
                        </p>
                    )}
                </div>
            )}
        </article>
    );
}

export default ConcertCard;