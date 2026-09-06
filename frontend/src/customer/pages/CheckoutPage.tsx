import './styles/CheckoutPage.css'

import {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';

import {
    getConcertById,
    type Concert,
} from '../api/concertApi.ts';

import {
    getTicketCategories,
    type TicketCategory,
} from '../api/ticketCategoryApi.ts';

import CustomerDetailsForm
    from '../components/checkout/CustomerDetailsForm.tsx';

import OrderSummary
    from '../components/checkout/OrderSummary.tsx';

import {createOrder} from '../api/orderApi.ts';

import {ApiError} from '../api/apiError.ts';

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

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const navigate = useNavigate();

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

                const loadedConcert =
                    await getConcertById(id);

                if (loadedConcert.status !== 'PUBLISHED') {
                    sessionStorage.removeItem(
                        `checkout-${id}`,
                    );

                    navigate(
                        `/concerts/${id}`,
                        {
                            replace: true,
                        },
                    );

                    return;
                }

                const loadedCategories =
                    await getTicketCategories(id);

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
    }, [id, navigate]);

    const selectedCategories = ticketCategories.filter(
        (category) => (quantities[category.id] ?? 0) > 0,
    );

    async function handleCreateOrder(
        customerEmail: string,
    ) {
        await new Promise((resolve) =>
            setTimeout(resolve, 5000),
        );

        if (!concert) {
            throw new Error(
                'Konzert konnte nicht geladen werden.',
            );
        }

        const items = selectedCategories.map(
            (category) => ({
                ticketCategoryId: category.id,
                quantity:
                    quantities[category.id] ?? 0,
            }),
        );

        if (items.length === 0) {
            throw new Error(
                'Es wurden keine Tickets ausgewählt.',
            );
        }

        let response;

        try {
            response = await createOrder(
                concert.id,
                {
                    customerEmail,
                    items,
                }
            );
        } catch (error) {
            if (
                error instanceof ApiError &&
                (
                    error.code === 'NOT_ENOUGH_TICKETS_AVAILABLE' ||
                    error.code === 'TICKET_CATEGORY_NOT_FOUND'
                )
            ) {
                navigate(
                    `/concerts/${concert.id}`,
                    {
                        state: {
                            availabilityChanged: true,
                        },
                    },
                );

                return;
            }

            throw error;
        }

        sessionStorage.setItem(
            `order-${response.order.id}`,
            JSON.stringify({
                concertId: response.order.concertId,
                accessToken: response.accessToken,
            }),
        );

        sessionStorage.removeItem(
            `checkout-${concert.id}`,
        );

        navigate(`/orders/${response.order.id}`);
    }

    if (loading) {
        return <p>Bestellung wird geladen...</p>;
    }

    if (error) {
        return (
            <main className="checkout-page">
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
        <main className="checkout-page">
            <Link
                className="back-link"
                to={`/concerts/${concert.id}`}
            >
                ← Zurück zur Ticketauswahl
            </Link>

            <header className="checkout-header">
                <h1>Bestellung</h1>
                <p>{concert.title}</p>
            </header>

            <OrderSummary
                ticketCategories={ticketCategories}
                quantities={quantities}
            />

            <CustomerDetailsForm onSubmit={handleCreateOrder}/>
        </main>
    );
}

export default CheckoutPage;