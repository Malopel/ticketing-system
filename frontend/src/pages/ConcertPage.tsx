import {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';

import {
    getConcertById,
    type Concert
} from '../api/concertApi';
import {
    getTicketCategories,
    type TicketCategory,
} from '../api/ticketCategoryApi';

import TicketCategoryCard from '../components/TicketCategoryCard';

function ConcertPage() {
    const {concertId} = useParams<{
        concertId: string;
    }>();

    const [concert, setConcert] = useState<Concert | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [ticketCategories, setTicketCategories] =
        useState<TicketCategory[]>([]);
    const [quantities, setQuantities] =
        useState<Record<number, number>>({});
    const [loadingTickets, setLoadingTickets] = useState(true);
    const [ticketError, setTicketError] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        async function loadConcert() {
            const id = Number(concertId);

            if (!Number.isInteger(id) || id <= 0) {
                setError('Ungültige Konzert-ID.');
                setLoading(false);
                return;
            }

            try {
                setLoading(true);
                setError(null);

                const loadedConcert = await getConcertById(id);

                setConcert(loadedConcert);
            } catch {
                setError('Konzert konnte nicht geladen werden.');
            } finally {
                setLoading(false);
            }
        }

        void loadConcert();
    }, [concertId]);

    useEffect(() => {
        async function loadTicketCategories() {
            const id = Number(concertId);

            if (!Number.isInteger(id) || id <= 0) {
                return;
            }

            try {
                setLoadingTickets(true);
                setTicketError(null);

                const categories = await getTicketCategories(id);

                setTicketCategories(categories);
            } catch (error) {
                if (error instanceof Error) {
                    setTicketError(error.message);
                } else {
                    setTicketError(
                        'Ticketkategorien konnten nicht geladen werden.',
                    );
                }
            } finally {
                setLoadingTickets(false);
            }
        }

        void loadTicketCategories();
    }, [concertId]);

    function handleQuantityChange(
        categoryId: number,
        quantity: number,
    ) {
        setQuantities((currentQuantities) => ({
            ...currentQuantities,
            [categoryId]: quantity,
        }));
    }

    function handleContinueToCheckout() {
        const id = Number(concertId);

        if (!Number.isInteger(id) || id <= 0 || totalQuantity === 0) {
            return;
        }

        sessionStorage.setItem(
            `checkout-${id}`,
            JSON.stringify(quantities),
        );

        navigate(`/concerts/${id}/checkout`);
    }

    const totalQuantity = Object.values(quantities).reduce(
        (sum, quantity) => sum + quantity,
        0,
    );

    const totalPrice = ticketCategories.reduce(
        (sum, category) => {
            const quantity = quantities[category.id] ?? 0;

            return sum + category.price * quantity;
        },
        0,
    );

    const currencyFormatter = new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
    });

    if (loading) {
        return <p>Konzert wird geladen...</p>;
    }

    if (error) {
        return (
            <main>
                <p>Fehler: {error}</p>
                <Link to="/">Zurück zu den Konzerten</Link>
            </main>
        );
    }

    if (!concert) {
        return null;
    }

    const date = new Date(concert.startTime);

    const formattedDate = date.toLocaleDateString('de-DE', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    });

    const formattedTime = date.toLocaleTimeString('de-DE', {
        hour: '2-digit',
        minute: '2-digit',
    });

    return (
        <main>
            <Link to="/">← Zurück zu den Konzerten</Link>

            <h1>{concert.title}</h1>

            <p>{concert.description}</p>

            <p>
                {formattedDate} um {formattedTime} Uhr
            </p>

            <p>{concert.location}</p>

            <section>
                <h2>Tickets</h2>

                {loadingTickets && (
                    <p>Ticketkategorien werden geladen...</p>
                )}

                {ticketError && (
                    <p>Fehler: {ticketError}</p>
                )}

                {!loadingTickets &&
                    !ticketError &&
                    ticketCategories.length === 0 && (
                        <p>
                            Für dieses Konzert sind aktuell keine Tickets verfügbar.
                        </p>
                    )}

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
                            <strong>Ausgewählte Tickets:</strong>{' '}
                            {totalQuantity}
                        </p>

                        <p>
                            <strong>Gesamtpreis:</strong>{' '}
                            {currencyFormatter.format(totalPrice)}
                        </p>
                    </div>
                )}

                {totalQuantity > 0 && (
                    <button onClick={handleContinueToCheckout}>
                        Weiter zur Bestellung
                    </button>
                )}
            </section>
        </main>
    );
}

export default ConcertPage;