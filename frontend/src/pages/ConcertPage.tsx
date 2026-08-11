import {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router-dom';

import {
    getConcertById,
    type Concert
} from '../api/concertApi';
import {
    getTicketCategories,
    type TicketCategory,
} from '../api/ticketCategoryApi';

import TicketSelection from '../components/TicketSelection';

function ConcertPage() {
    const {concertId} = useParams<{
        concertId: string;
    }>();

    const [concert, setConcert] = useState<Concert | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [ticketCategories, setTicketCategories] =
        useState<TicketCategory[]>([]);
    const [loadingTickets, setLoadingTickets] = useState(true);
    const [ticketError, setTicketError] = useState<string | null>(null);

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
                {loadingTickets && (
                    <p>Ticketkategorien werden geladen...</p>
                )}

                {ticketError && (
                    <p>Fehler: {ticketError}</p>
                )}

                {!ticketError && !loadingTickets && (
                    <TicketSelection
                        concertId={concert.id}
                        ticketCategories={ticketCategories}
                    />
                )}
            </section>
        </main>
    );
}

export default ConcertPage;