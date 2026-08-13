import './styles/ConcertPage.css'

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
import {
    getShopConfig,
    type ShopConfig,
} from '../api/shopConfigApi';

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
    const [shopConfig, setShopConfig] =
        useState<ShopConfig | null>(null);

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
        async function loadTicketData() {
            const id = Number(concertId);

            if (!Number.isInteger(id) || id <= 0) {
                return;
            }

            try {
                setLoadingTickets(true);
                setTicketError(null);

                const [
                    categories,
                    loadedShopConfig,
                ] = await Promise.all([
                    getTicketCategories(id),
                    getShopConfig(),
                ]);

                setTicketCategories(categories);
                setShopConfig(loadedShopConfig);
            } catch (error) {
                if (error instanceof Error) {
                    setTicketError(error.message);
                } else {
                    setTicketError(
                        'Ticketinformationen konnten nicht geladen werden.',
                    );
                }
            } finally {
                setLoadingTickets(false);
            }
        }

        void loadTicketData();
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
        <main className="concert-page">
            <Link
                className="back-link"
                to="/"
            >
                ← Zurück zu den Konzerten
            </Link>

            <header className="concert-header">
                <h1>{concert.title}</h1>

                <p className="concert-description">
                    {concert.description}
                </p>

                <div className="concert-meta">
                    <p>
                        <strong>Termin</strong>
                        <span>
                            {formattedDate} um {formattedTime} Uhr
                        </span>
                    </p>

                    <p>
                        <strong>Ort</strong>
                        <span>{concert.location}</span>
                    </p>
                </div>
            </header>

            {loadingTickets && (
                <p>Ticketkategorien werden geladen...</p>
            )}

            {ticketError && (
                <p className="error-message">
                    Fehler: {ticketError}
                </p>
            )}

            {!ticketError &&
                !loadingTickets &&
                shopConfig && (
                    <TicketSelection
                        concertId={concert.id}
                        ticketCategories={ticketCategories}
                        maxTicketsPerOrder={
                            shopConfig.maxTicketsPerOrder
                        }
                    />
                )}
        </main>
    );
}

export default ConcertPage;