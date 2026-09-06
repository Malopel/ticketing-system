import './styles/ConcertCard.css'

import {Link} from 'react-router-dom';

import type {Concert} from '../api/concertApi.ts';

type ConcertCardProps = {
    concert: Concert;
};

function ConcertCard({concert}: ConcertCardProps) {
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

    const isCancelled = concert.status === 'CANCELLED';

    return (
        <article className="concert-card">
            <div className="concert-card-title-row">
                <h2>{concert.title}</h2>

                {isCancelled && (
                    <span className="concert-cancelled-badge">
                        Abgesagt
                    </span>
                )}
            </div>

            <p>{concert.description}</p>

            <p>
                <strong>Ort:</strong> {concert.location}
            </p>

            <p>
                <strong>Termin:</strong>{' '}
                {formattedDate} · {formattedTime} Uhr
            </p>

            <Link
                className="button-link"
                to={`/concerts/${concert.id}`}
            >
                {isCancelled
                    ? 'Details ansehen'
                    : 'Tickets auswählen'}
            </Link>
        </article>
    );
}

export default ConcertCard;