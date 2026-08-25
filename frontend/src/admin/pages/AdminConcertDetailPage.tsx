import {
    useEffect,
    useState,
} from 'react';

import {
    Link,
    useParams,
} from 'react-router-dom';

import {
    getAdminConcert,
    type AdminConcert,
    type AdminConcertStatus,
} from '../api/adminConcertApi';

import {
    getAdminTicketCategories,
    type AdminTicketCategory,
} from '../api/adminTicketCategoryApi';

import './AdminConcertDetailPage.css';

const STATUS_LABELS: Record<AdminConcertStatus, string> = {
    DRAFT: 'Entwurf',
    PUBLISHED: 'Veröffentlicht',
    CANCELLED: 'Abgesagt',
    ARCHIVED: 'Archiviert',
};

type TicketCategoryRowProps = {
    category: AdminTicketCategory;
};

function TicketCategoryRow({
                               category,
                           }: TicketCategoryRowProps) {
    const formattedPrice =
        new Intl.NumberFormat('de-DE', {
            style: 'currency',
            currency: 'EUR',
        }).format(category.price);

    return (
        <article className="admin-ticket-category-row">
            <div>
                <h3>{category.name}</h3>

                <p>
                    {formattedPrice}
                    {' · '}
                    {category.available} von{' '}
                    {category.capacity} verfügbar
                </p>
            </div>
        </article>
    );
}

function AdminConcertDetailPage() {
    const {concertId} = useParams();

    const parsedConcertId = Number(concertId);

    const routeError =
        !concertId
            ? 'Keine Konzert-ID angegeben.'
            : !Number.isInteger(parsedConcertId) || parsedConcertId <= 0
                ? 'Ungültige Konzert-ID.'
                : null;

    const [concert, setConcert] =
        useState<AdminConcert | null>(null);

    const [ticketCategories, setTicketCategories] =
        useState<AdminTicketCategory[]>([]);

    const [isLoading, setIsLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);

    const [areCategoriesLoading, setAreCategoriesLoading] =
        useState(true);

    const [categoriesError, setCategoriesError] =
        useState<string | null>(null);

    useEffect(() => {
        if (routeError) {
            return;
        }

        const controller = new AbortController();

        async function loadConcert() {
            try {
                const data = await getAdminConcert(
                    parsedConcertId,
                    controller.signal,
                );

                setConcert(data);
            } catch (error) {
                if (
                    error instanceof DOMException &&
                    error.name === 'AbortError'
                ) {
                    return;
                }

                setError(
                    'Das Konzert konnte nicht geladen werden.',
                );
            } finally {
                if (!controller.signal.aborted) {
                    setIsLoading(false);
                }
            }
        }

        void loadConcert();

        return () => {
            controller.abort();
        };
    }, [parsedConcertId, routeError]);

    useEffect(() => {
        if (routeError) {
            return;
        }

        const controller = new AbortController();

        async function loadTicketCategories() {
            try {
                const data = await getAdminTicketCategories(
                    parsedConcertId,
                    controller.signal,
                );

                setTicketCategories(data);
            } catch (error) {
                if (
                    error instanceof DOMException &&
                    error.name === 'AbortError'
                ) {
                    return;
                }

                setCategoriesError(
                    'Ticketkategorien konnten nicht geladen werden.',
                );
            } finally {
                if (!controller.signal.aborted) {
                    setAreCategoriesLoading(false);
                }
            }
        }

        void loadTicketCategories();

        return () => {
            controller.abort();
        };
    }, [parsedConcertId, routeError]);

    if (routeError) {
        return (
            <p
                className="admin-error-message"
                role="alert"
            >
                {routeError}
            </p>
        );
    }

    if (isLoading) {
        return <p>Konzert wird geladen...</p>;
    }

    if (error || !concert) {
        return (
            <p
                className="admin-error-message"
                role="alert"
            >
                {error ?? 'Konzert nicht gefunden.'}
            </p>
        );
    }

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
        <section>
            <div className="admin-detail-back">
                <Link to="/admin/concerts">
                    ← Zurück zu Konzerte
                </Link>
            </div>

            <header className="admin-page-header">
                <div>
                    <div className="admin-concert-title-row">
                        <h1>{concert.title}</h1>

                        <span
                            className={`admin-status admin-status-${concert.status.toLowerCase()}`}
                        >
                            {STATUS_LABELS[concert.status]}
                        </span>
                    </div>

                    <p>
                        {formattedDate}
                        {' · '}
                        {formattedTime} Uhr
                    </p>
                </div>

                {concert.status === 'PUBLISHED' && (
                    <a
                        href={`/concerts/${concert.id}`}
                        target="_blank"
                        rel="noreferrer"
                        className="admin-secondary-button"
                    >
                        Öffentliche Seite ↗
                    </a>
                )}
            </header>

            <div className="admin-detail-card">
                <dl className="admin-detail-list">
                    <div>
                        <dt>Veranstaltungsort</dt>
                        <dd>{concert.location}</dd>
                    </div>

                    <div>
                        <dt>Beschreibung</dt>
                        <dd>
                            {concert.description ||
                                'Keine Beschreibung hinterlegt.'}
                        </dd>
                    </div>

                    <div>
                        <dt>Status</dt>
                        <dd>
                            {STATUS_LABELS[concert.status]}
                        </dd>
                    </div>

                    <div>
                        <dt>Konzert-ID</dt>
                        <dd>{concert.id}</dd>
                    </div>
                </dl>
            </div>

            <section className="admin-detail-section">
                <div className="admin-section-header">
                    <div>
                        <h2>Ticketkategorien</h2>

                        <p>
                            Preise und verfügbare Plätze für dieses
                            Konzert.
                        </p>
                    </div>
                </div>

                {areCategoriesLoading ? (
                    <p>Ticketkategorien werden geladen...</p>
                ) : categoriesError ? (
                    <p
                        className="admin-error-message"
                        role="alert"
                    >
                        {categoriesError}
                    </p>
                ) : ticketCategories.length === 0 ? (
                    <div className="admin-empty-state">
                        <h3>Noch keine Ticketkategorien</h3>

                        <p>
                            Für dieses Konzert wurden noch keine
                            Ticketkategorien angelegt.
                        </p>
                    </div>
                ) : (
                    <div className="admin-ticket-category-list">
                        {ticketCategories.map((category) => (
                            <TicketCategoryRow
                                key={category.id}
                                category={category}
                            />
                        ))}
                    </div>
                )}
            </section>
        </section>
    );
}

export default AdminConcertDetailPage;