import {
    useEffect,
    useState,
} from 'react';

import {
    getAdminConcerts,
    type AdminConcert,
    type AdminConcertStatus,
} from '../api/adminConcertApi';

import './AdminConcertsPage.css'

const STATUS_LABELS: Record<AdminConcertStatus, string> = {
    DRAFT: 'Entwurf',
    PUBLISHED: 'Veröffentlicht',
    CANCELLED: 'Abgesagt',
    ARCHIVED: 'Archiviert',
};

function AdminConcertsPage() {
    const [concerts, setConcerts] = useState<AdminConcert[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const controller = new AbortController();

        async function loadConcerts() {
            try {
                const data = await getAdminConcerts(
                    controller.signal,
                );

                setConcerts(data);
            } catch (error) {
                if (
                    error instanceof DOMException &&
                    error.name === 'AbortError'
                ) {
                    return;
                }

                setError(
                    'Konzerte konnten nicht geladen werden.',
                );
            } finally {
                if (!controller.signal.aborted) {
                    setIsLoading(false);
                }
            }
        }

        void loadConcerts();

        return () => {
            controller.abort();
        }
    }, []);

    return (
        <section>
            <header className="admin-page-header">
                <div>
                    <h1>Konzerte</h1>

                    <p>
                        Konzerte anlegen und verwalten.
                    </p>
                </div>
            </header>

            {isLoading && (
                <p>Konzerte werden geladen...</p>
            )}

            {error && (
                <p
                    className="admin-error-message"
                    role="alert"
                >
                    {error}
                </p>
            )}

            {!isLoading &&
                !error &&
                concerts.length === 0 && (
                    <div className="admin-empty-state">
                        <h2>Noch keine Konzerte</h2>

                        <p>
                            Es wurden bisher keine Konzerte
                            angelegt.
                        </p>
                    </div>
                )}

            {!isLoading &&
                !error &&
                concerts.length > 0 && (
                    <div className="admin-concert-list">
                        {concerts.map((concert) => (
                            <ConcertRow
                                key={concert.id}
                                concert={concert}
                            />
                        ))}
                    </div>
                )}
        </section>
    );
}

type ConcertRowProps = {
    concert: AdminConcert;
};

function ConcertRow({
                        concert,
                    }: ConcertRowProps) {
    const startTime = new Date(concert.startTime);

    const formattedDate =
        startTime.toLocaleDateString('de-DE', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        });

    const formattedTime =
        startTime.toLocaleTimeString('de-DE', {
            hour: '2-digit',
            minute: '2-digit',
        });

    return (
        <article className="admin-concert-row">
            <div className="admin-concert-main">
                <div className="admin-concert-title-row">
                    <h2>{concert.title}</h2>

                    <span
                        className={`admin-status admin-status-${concert.status.toLowerCase()}`}
                    >
                        {STATUS_LABELS[concert.status]}
                    </span>
                </div>

                <p className="admin-concert-meta">
                    {formattedDate}
                    {' · '}
                    {formattedTime} Uhr
                    {' · '}
                    {concert.location}
                </p>
            </div>
        </article>
    );
}

export default AdminConcertsPage;