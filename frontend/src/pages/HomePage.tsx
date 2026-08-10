import { useEffect, useState } from 'react';
import { getConcerts, type Concert } from '../api/concertApi';
import ConcertCard from '../components/ConcertCard';

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
            <h1>Accordion Symphonic Ticketshop</h1>

            <h2>Konzerte</h2>

            {concerts.length === 0 ? (
                <p>Aktuell sind keine Konzerte verfügbar.</p>
            ) : (
                <div>
                    {concerts.map((concert) => (
                        <ConcertCard
                            key={concert.id}
                            concert={concert}
                        />
                    ))}
                </div>
            )}
        </main>
    );
}

export default HomePage;