import './styles/HomePage.css'

import { useEffect, useState } from 'react';
import { getConcerts, type Concert } from '../api/concertApi.ts';
import ConcertCard from '../components/ConcertCard.tsx';

function HomePage() {
    const [concerts, setConcerts] = useState<Concert[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        getConcerts()
            .then((data) => {
                setConcerts(data);
            })
            .catch((error: Error) => {
                setError(error.message);
            })
            .finally(() => setLoading(false));
    }, []);

    if (loading) {
        return <p>Konzerte werden geladen...</p>
    }

    if (error) {
        return <p>Fehler: {error}</p>
    }

    return (
        <main>
            <header className="page-header">
                <h1>Accordion Symphonic</h1>
                <h2>Konzerte</h2>
                <p>
                    Hier findest du unsere Konzerte und aktuelle
                    Informationen zu den Veranstaltungen.
                </p>
            </header>

            <div className="concert-grid">
                {concerts.map((concert) => (
                    <ConcertCard
                        key={concert.id}
                        concert={concert}
                    />
                ))}
            </div>
        </main>
    );
}

export default HomePage;