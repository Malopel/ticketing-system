import './styles/LegalPage.css';

function DatenschutzPage() {
    return (
        <main className="legal-page">
            <h1>Datenschutzerklärung</h1>

            {/*
                TODO VOR VVK:
                Mit dem Vorstand klären, ob das HAO allein datenschutzrechtlich
                verantwortlich ist oder ob beide Vereine gemeinsam über Zweck und
                Mittel der Datenverarbeitung entscheiden.
            */}
            <section>
                <h2>1. Verantwortlicher</h2>
                <p>
                    Hohner-Akkordeon-Orchester 1964 e. V. Reilingen
                    <br/>
                    Robert-Koch-Straße 7/1
                    <br/>
                    69207 Sandhausen
                    <br/>
                    Deutschland
                </p>

                <p>
                    E-Mail:{' '}
                    <a href="mailto:reilingen.hao@gmail.com">
                        reilingen.hao@gmail.com
                    </a>
                </p>
            </section>

            <section>
                <h2>2. Ticketbestellungen</h2>
                <p>
                    Wenn Sie über unseren Ticketshop Eintrittskarten bestellen,
                    verarbeiten wir die von Ihnen angegebene E-Mail-Adresse sowie
                    die zur Bestellung gehörenden Daten.
                </p>

                <p>Hierzu gehören insbesondere:</p>
                <ul>
                    <li>E-Mail-Adresse,</li>
                    <li>bestellte Ticketkategorien und Stückzahlen,</li>
                    <li>Bestellnummer,</li>
                    <li>Bestell- und Zahlungsstatus sowie</li>
                    <li>Zeitpunkte von Bestellung und Zahlung.</li>
                </ul>

                <p>
                    Die Verarbeitung erfolgt zur Durchführung und Abwicklung Ihrer
                    Ticketbestellung, insbesondere zur Reservierung der Tickets,
                    Zahlungsabwicklung und Zusendung der Eintrittskarten.
                    Rechtsgrundlage ist Art. 6 Abs. 1 lit. b DSGVO.
                </p>
            </section>

            <section>
                <h2>3. Zahlungsabwicklung über Mollie</h2>
                <p>
                    Für die Zahlungsabwicklung nutzen wir Mollie. Im Rahmen des
                    Bezahlvorgangs werden die für die Zahlungsabwicklung
                    erforderlichen Daten an Mollie übermittelt beziehungsweise von
                    Mollie verarbeitet.
                </p>

                <p>
                    Mollie B.V.
                    <br/>
                    Keizersgracht 126
                    <br/>
                    1015 CW Amsterdam
                    <br/>
                    Niederlande
                </p>

                <p>
                    Weitere Informationen finden Sie in der Datenschutzerklärung
                    von Mollie:{' '}
                    <a
                        href="https://www.mollie.com/de/legal/privacy"
                        target="_blank"
                        rel="noreferrer"
                    >
                        Datenschutzerklärung von Mollie
                    </a>
                </p>
            </section>

            <section>
                <h2>4. Ticketversand per E-Mail</h2>
                <p>
                    Nach erfolgreicher Zahlung verwenden wir Ihre E-Mail-Adresse,
                    um Ihnen die erworbenen Tickets und Informationen zu Ihrer
                    Bestellung zuzusenden. Für den technischen Versand setzen wir
                    einen E-Mail-Dienstleister ein.
                </p>

                {/* TODO VOR VVK: Brevo als eingesetzten Dienstleister final bestätigen. */}
            </section>

            <section>
                <h2>5. Server- und Protokolldaten</h2>
                <p>
                    Beim Aufruf des Ticketshops werden technisch erforderliche
                    Informationen verarbeitet. Dazu können insbesondere IP-Adresse,
                    Zeitpunkt des Zugriffs, aufgerufene Ressource sowie technische
                    Angaben zu Browser und Endgerät gehören. Die Verarbeitung dient
                    dem sicheren und zuverlässigen Betrieb des Ticketshops.
                </p>
            </section>

            <section>
                <h2>6. Speicherdauer</h2>
                <p>
                    Personenbezogene Daten werden nur so lange gespeichert, wie dies
                    für die Abwicklung der Bestellung erforderlich ist oder
                    gesetzliche Aufbewahrungspflichten bestehen. Nach Wegfall des
                    jeweiligen Zwecks werden die Daten gelöscht, sofern keine
                    gesetzlichen Pflichten einer Löschung entgegenstehen.
                </p>

                {/* TODO VOR VVK: interne Lösch-/Aufbewahrungsregel mit dem Vorstand festlegen. */}
            </section>

            <section>
                <h2>7. Empfänger personenbezogener Daten</h2>
                <p>
                    Daten können im erforderlichen Umfang insbesondere an folgende
                    Empfänger beziehungsweise Kategorien von Empfängern übermittelt
                    werden:
                </p>

                <ul>
                    <li>Mollie B.V. zur Zahlungsabwicklung,</li>
                    <li>einen E-Mail-Dienstleister für den Ticketversand sowie</li>
                    <li>Hosting- und Infrastruktur-Dienstleister für den Betrieb des Ticketshops.</li>
                </ul>
            </section>

            <section>
                <h2>8. Ihre Rechte</h2>
                <p>
                    Sie haben im Rahmen der gesetzlichen Voraussetzungen insbesondere
                    Rechte auf Auskunft, Berichtigung, Löschung und Einschränkung der
                    Verarbeitung Ihrer personenbezogenen Daten. Außerdem können Ihnen
                    ein Recht auf Datenübertragbarkeit und ein Widerspruchsrecht
                    zustehen.
                </p>

                <p>
                    Sie haben außerdem das Recht, sich bei einer zuständigen
                    Datenschutz-Aufsichtsbehörde zu beschweren.
                </p>
            </section>

            <section>
                <h2>9. Cookies und lokale Speicherung</h2>
                <p>
                    Der Ticketshop verwendet keine Analyse- oder Marketing-Cookies.
                    Soweit technisch erforderliche Speichermechanismen im Browser
                    verwendet werden, dienen diese ausschließlich der Bereitstellung
                    der von Ihnen angeforderten Funktionen, beispielsweise dem
                    Zwischenspeichern einer Ticketauswahl oder des Zugriffs auf eine
                    Bestellung.
                </p>
            </section>

            <section>
                <h2>10. Stand</h2>
                <p>November 2026</p>
            </section>
        </main>
    );
}

export default DatenschutzPage;
