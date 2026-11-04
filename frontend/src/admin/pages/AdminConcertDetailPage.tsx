import {
    useEffect,
    useState,
} from 'react';

import {
    Link,
    useParams,
} from 'react-router-dom';

import {
    archiveAdminConcert,
    cancelAdminConcert,
    getAdminConcert,
    publishAdminConcert,
    type AdminConcert,
    type AdminConcertStatus,
} from '../api/adminConcertApi';

import UpdateConcertForm
    from '../components/UpdateConcertForm';

import TicketCategorySection
    from './components/TicketCategorySection';

import './styles/AdminConcertDetailPage.css';

const STATUS_LABELS: Record<AdminConcertStatus, string> = {
    DRAFT: 'Entwurf',
    PUBLISHED: 'Veröffentlicht',
    CANCELLED: 'Abgesagt',
    ARCHIVED: 'Archiviert',
};

type ConcertAction =
    | 'publish'
    | 'cancel'
    | 'archive';

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

    const [isLoading, setIsLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);

    const [isUpdatingConcert, setIsUpdatingConcert] =
        useState(false);

    const [pendingAction, setPendingAction] =
        useState<ConcertAction | null>(null);

    const [actionError, setActionError] =
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

    async function handleConcertAction(
        action: ConcertAction,
    ) {
        if (!concert) {
            return;
        }

        if (
            action === 'cancel' &&
            !window.confirm(
                'Möchtest du dieses Konzert wirklich absagen?',
            )
        ) {
            return;
        }

        if (
            action === 'archive' &&
            !window.confirm(
                'Möchtest du dieses Konzert wirklich archivieren?',
            )
        ) {
            return;
        }

        setActionError(null);
        setPendingAction(action);

        try {
            let updatedConcert: AdminConcert;

            switch (action) {
                case 'publish':
                    updatedConcert =
                        await publishAdminConcert(concert.id);
                    break;

                case 'cancel':
                    updatedConcert =
                        await cancelAdminConcert(concert.id);
                    break;

                case 'archive':
                    updatedConcert =
                        await archiveAdminConcert(concert.id);
                    break;
            }

            setConcert(updatedConcert);
        } catch {
            setActionError(
                'Die Aktion konnte nicht ausgeführt werden.',
            );
        } finally {
            setPendingAction(null);
        }
    }

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

                {(concert.status === 'PUBLISHED' ||
                    concert.status === 'CANCELLED') && (
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
                <h2>Konzertstatus</h2>

                {actionError && (
                    <p
                        className="admin-error-message"
                        role="alert"
                    >
                        {actionError}
                    </p>
                )}

                <div className="admin-concert-actions">
                    {concert.status === 'DRAFT' && (
                        <button
                            type="button"
                            onClick={() =>
                                void handleConcertAction('publish')
                            }
                            disabled={pendingAction !== null}
                        >
                            {pendingAction === 'publish'
                                ? 'Wird veröffentlicht...'
                                : 'Veröffentlichen'}
                        </button>
                    )}

                    {concert.status === 'DRAFT' && (
                        <button
                            type="button"
                            className="admin-secondary-button"
                            onClick={() =>
                                setIsUpdatingConcert(true)
                            }
                            disabled={isUpdatingConcert}
                        >
                            Bearbeiten
                        </button>
                    )}

                    {concert.status === 'PUBLISHED' && (
                        <button
                            type="button"
                            className="admin-secondary-button"
                            onClick={() =>
                                void handleConcertAction('cancel')
                            }
                            disabled={pendingAction !== null}
                        >
                            {pendingAction === 'cancel'
                                ? 'Wird abgesagt...'
                                : 'Konzert absagen'}
                        </button>
                    )}

                    {(concert.status === 'PUBLISHED' ||
                        concert.status === 'CANCELLED') && (
                        <button
                            type="button"
                            className="admin-secondary-button"
                            onClick={() =>
                                void handleConcertAction('archive')
                            }
                            disabled={pendingAction !== null}
                        >
                            {pendingAction === 'archive'
                                ? 'Wird archiviert...'
                                : 'Archivieren'}
                        </button>
                    )}

                    {concert.status === 'ARCHIVED' && (
                        <p className="admin-muted-text">
                            Dieses Konzert ist archiviert.
                        </p>
                    )}
                </div>

                {isUpdatingConcert && (
                    <UpdateConcertForm
                        concert={concert}
                        onCancel={() =>
                            setIsUpdatingConcert(false)
                        }
                        onUpdated={(updatedConcert) => {
                            setConcert(updatedConcert);
                            setIsUpdatingConcert(false);
                        }}
                    />
                )}
            </section>

            <TicketCategorySection
                concertId={concert.id}
                concertStatus={concert.status}
            />
        </section>
    );
}

export default AdminConcertDetailPage;