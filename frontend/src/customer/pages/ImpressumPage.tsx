import './styles/LegalPage.css';

function ImpressumPage() {
    return (
        <main className="legal-page">
            <h1>Impressum</h1>

            {/*
                TODO VOR VVK:
                Mit dem Vorstand bestätigen, ob das HAO alleiniger Anbieter des
                Ticketshops ist oder ob beide Vereine gemeinsam auftreten.
                Falls beide Anbieter sind, die Angaben des zweiten Vereins ergänzen.
            */}
            <section>
                <h2>Anbieter des Ticketshops</h2>
                <p>
                    <strong>Hohner-Akkordeon-Orchester 1964 e. V. Reilingen</strong>
                    <br/>
                    Robert-Koch-Straße 7/1
                    <br/>
                    69207 Sandhausen
                    <br/>
                    Deutschland
                </p>
            </section>

            <section>
                <h2>Vertretungsberechtigt</h2>
                <p>
                    Julia Wiesener, 1. Vorsitzende
                    <br/>
                    Nancy Freitagsmüller, 2. Vorsitzende
                </p>
            </section>

            <section>
                <h2>Kontakt</h2>
                <p>
                    Telefon: 06224 / 54602
                    <br/>
                    E-Mail:{' '}
                    <a href="mailto:reilingen.hao@gmail.com">
                        reilingen.hao@gmail.com
                    </a>
                </p>
            </section>

            <section>
                <h2>Vereinsregister</h2>
                <p>
                    Registergericht: Amtsgericht Mannheim
                    <br/>
                    Registernummer: VR 420225
                </p>
            </section>

            <section>
                <h2>Mitveranstalter</h2>
                <p>
                    Handharmonikavereinigung 1934 Rheingold Mannheim e. V.
                </p>
                {/* TODO VOR VVK: Rolle und ggf. vollständige Pflichtangaben ergänzen. */}
            </section>
        </main>
    );
}

export default ImpressumPage;
