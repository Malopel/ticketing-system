import "./styles/CountdownPage.css";

import {useEffect, useState} from 'react';

const TARGET = new Date('2026-11-19T18:00:00+01:00');

function CountdownPage() {
    const [remaining, setRemaining] = useState<number | null>(null);

    useEffect(() => {
        const updateRemaining = () => {
            setRemaining(
                Math.max(0, TARGET.getTime() - Date.now())
            );
        };

        updateRemaining();

        const interval = window.setInterval(
            updateRemaining,
            1000
        );

        return () => window.clearInterval(interval);
    }, []);

    if (remaining === null) {
        return null;
    }

    const totalSeconds = Math.floor(remaining / 1000);

    const days = Math.floor(totalSeconds / 86400);

    const hours =
        Math.floor((totalSeconds % 86400) / 3600);

    const minutes =
        Math.floor((totalSeconds % 3600) / 60);

    const seconds =
        totalSeconds % 60;

    return (
        <main className="countdown-page">
            <img
                src="/logo.png"
                alt="Accordion Symphonic"
                className="countdown-logo"
            />

            <h1>Der Vorverkauf startet bald</h1>

            <p>
                Der Online-Vorverkauf für Accordion Symphonic
                startet am Donnerstag, den 19. November.
            </p>

            <div className="countdown">
                <div>
                    <strong>{days}</strong>
                    <span>Tage</span>
                </div>

                <div>
                    <strong>{hours}</strong>
                    <span>Stunden</span>
                </div>

                <div>
                    <strong>{minutes}</strong>
                    <span>Minuten</span>
                </div>

                <div>
                    <strong>{seconds}</strong>
                    <span>Sekunden</span>
                </div>
            </div>

            <p className="countdown-event">
                Konzert am 20. November 2027 · Heidelberg Congress Center
            </p>
        </main>
    );
}

export default CountdownPage;