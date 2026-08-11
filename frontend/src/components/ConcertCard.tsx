import {Link} from 'react-router-dom';

import type {Concert} from '../api/concertApi';

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

    return (
        <article className="concert-card">
            <h2>{concert.title}</h2>

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
                to={`/concerts/${concert.id}`}>
                Tickets auswählen
            </Link>
        </article>
    );
}

export default ConcertCard;