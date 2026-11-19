import './styles/AdminBankStatementsPage.css';

import {useState, type FormEvent} from 'react';

import {
    importCamtStatement,
    type CamtImportResult,
    type CamtImportItemStatus,
} from '../api/adminBankStatementApi';

import {
    previewVolksbankCsv,
    type CsvPreviewResult,
    confirmVolksbankCsv,
    type CsvConfirmResult,
} from '../api/adminBankStatementApi';

const STATUS_LABELS: Record<CamtImportItemStatus, string> = {
    PAID: 'Bezahlt',
    ALREADY_PAID: 'Bereits bezahlt – prüfen',
    NO_ORDER_REFERENCE: 'Keine Bestellnummer',
    ORDER_NOT_FOUND: 'Bestellung nicht gefunden',
    NON_EUR: 'Fremdwährung',
    AMOUNT_MISMATCH: 'Betrag abweichend',
    WRONG_PAYMENT_METHOD: 'Andere Zahlungsart',
    ORDER_NOT_PENDING: 'Nicht mehr ausstehend',
    ORDER_EXPIRED: 'Zahlungsfrist abgelaufen',
    PAYMENT_REJECTED: 'Zahlung abgelehnt',
};

const currencyFormatter = new Intl.NumberFormat('de-DE', {
    style: 'currency',
    currency: 'EUR',
});

function AdminBankStatementsPage() {
    const [file, setFile] = useState<File | null>(null);
    const [confirmed, setConfirmed] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [result, setResult] = useState<CamtImportResult | null>(null);
    const [error, setError] = useState<string | null>(null);

    const [csvFile, setCsvFile] = useState<File | null>(null);
    const [csvPreview, setCsvPreview] =
        useState<CsvPreviewResult | null>(null);
    const [csvLoading, setCsvLoading] = useState(false);
    const [csvError, setCsvError] = useState<string | null>(null);

    const [confirmingCsv, setConfirmingCsv] = useState(false);

    const [csvConfirmResult, setCsvConfirmResult] =
        useState<CsvConfirmResult | null>(null);

    async function handleImport(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        if (!file || !confirmed || uploading) {
            return;
        }

        const proceed = window.confirm(
            'Den Kontoauszug jetzt verbindlich importieren?\n\n' +
            'Erkannte Zahlungen werden sofort als bezahlt ' +
            'markiert und Ticket-E-Mails werden ausgelöst.'
        );

        if (!proceed) {
            return;
        }

        try {
            setUploading(true);
            setError(null);
            setResult(null);

            const imported = await importCamtStatement(file);
            setResult(imported);
            setConfirmed(false);
        } catch (exception) {
            setError(
                exception instanceof Error
                    ? exception.message
                    : 'Der CAMT-Import ist fehlgeschlagen.',
            );
        } finally {
            setUploading(false);
        }
    }

    async function handleCsvPreview() {
        if (!csvFile || csvLoading) return;

        try {
            setCsvLoading(true);
            setCsvError(null);
            setCsvPreview(null);

            const preview = await previewVolksbankCsv(csvFile);
            setCsvPreview(preview);
        } catch (error) {
            setCsvError(
                error instanceof Error
                    ? error.message
                    : 'CSV-Datei konnte nicht geprüft werden.',
            );
        } finally {
            setCsvLoading(false);
        }
    }

    async function handleCsvConfirm() {
        if (!csvFile || !csvPreview ||
            csvPreview.ready === 0 || confirmingCsv) {
            return;
        }

        const approved = window.confirm(
            `${csvPreview.ready} Zahlungen verbindlich verbuchen?\n\n` +
            'Die betreffenden Bestellungen werden als bezahlt ' +
            'markiert und Ticket-E-Mails werden ausgelöst.'
        );

        if (!approved) return;

        try {
            setConfirmingCsv(true);
            setCsvError(null);

            const result = await confirmVolksbankCsv(
                csvFile,
                csvPreview
            );

            setCsvConfirmResult(result);
            setCsvPreview(null);
            setCsvFile(null);

        } catch (error) {
            setCsvError(
                error instanceof Error
                    ? error.message
                    : 'Die Verbuchung ist fehlgeschlagen.'
            );
        } finally {
            setConfirmingCsv(false);
        }
    }

    return (
        <div className="admin-bank-page">
            <header className="admin-bank-header">
                <h1>Zahlungsabgleich</h1>
                <p>
                    Zahlungseingänge automatisch mit
                    Ticketbestellungen abgleichen.
                </p>
            </header>


            <section className="admin-bank-card">
                <h2>Volksbank-CSV prüfen</h2>

                <p>
                    Lade einen CSV-Kontoumsatzexport hoch.
                    Passende Ticketzahlungen werden erkannt,
                    aber noch nicht verbucht.
                </p>

                <div className="admin-bank-upload">
                    <label htmlFor="volksbank-csv">
                        CSV-Datei auswählen
                    </label>

                    <input
                        id="volksbank-csv"
                        type="file"
                        accept=".csv,text/csv"
                        onChange={(event) => {
                            setCsvFile(
                                event.currentTarget.files?.[0] ?? null
                            );
                            setCsvPreview(null);
                            setCsvConfirmResult(null);
                            setCsvError(null);
                        }}
                    />
                </div>

                <button
                    type="button"
                    className="admin-bank-submit"
                    onClick={() => void handleCsvPreview()}
                    disabled={!csvFile || csvLoading}
                >
                    {csvLoading
                        ? 'CSV wird geprüft...'
                        : 'CSV-Vorschau anzeigen'}
                </button>

                {csvError && (
                    <p role="alert" className="admin-bank-error">
                        {csvError}
                    </p>
                )}

                {csvPreview && (
                    <div className="admin-bank-preview">
                        <h3>Erkannte Buchungen</h3>

                        <div className="admin-bank-summary">
                            <div>
                                <span>Gelesen</span>
                                <strong>{csvPreview.rowsRead}</strong>
                            </div>
                            <div>
                                <span>Passende Zahlungen</span>
                                <strong>{csvPreview.ready}</strong>
                            </div>
                            <div>
                                <span>Zu prüfen</span>
                                <strong>{csvPreview.reviewRequired}</strong>
                            </div>
                        </div>

                        <p>
                            {csvPreview.ignored} andere Vereinsumsätze
                            wurden ignoriert.
                        </p>

                        {csvPreview.items.length === 0 ? (
                            <p>
                                Keine Ticketzahlungen mit AS-Bestellnummer
                                gefunden.
                            </p>
                        ) : (
                            <div className="admin-bank-table-wrapper">
                                <table className="admin-bank-table">
                                    <thead>
                                    <tr>
                                        <th>Buchungstag</th>
                                        <th>Bestellung</th>
                                        <th>Betrag</th>
                                        <th>Ergebnis</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {csvPreview.items.map((item) => (
                                        <tr key={item.rowNumber}>
                                            <td>{item.bookingDate}</td>
                                            <td>
                                                {item.orderReference ?? '—'}
                                            </td>
                                            <td>
                                                {new Intl.NumberFormat(
                                                    'de-DE', {
                                                        style: 'currency',
                                                        currency: 'EUR',
                                                    }
                                                ).format(item.amount)}
                                            </td>
                                            <td>
                                                <strong
                                                    className={
                                                        item.status === 'READY'
                                                            ? 'admin-bank-result-paid'
                                                            : 'admin-bank-result-review'
                                                    }
                                                >
                                                    {item.status === 'READY'
                                                        ? 'Zuordnung möglich'
                                                        : 'Prüfung nötig'}
                                                </strong>
                                                <small>{item.message}</small>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                )}

                {csvPreview && csvPreview.ready > 0 && (
                    <div className="admin-bank-confirm-actions">
                        <p>
                            {csvPreview.ready} eindeutig zuordenbare
                            Zahlungen können jetzt verbucht werden.
                        </p>

                        <button
                            type="button"
                            className="admin-bank-submit"
                            disabled={confirmingCsv}
                            onClick={() => void handleCsvConfirm()}
                        >
                            {confirmingCsv
                                ? 'Zahlungen werden verbucht...'
                                : `${csvPreview.ready} Zahlungen bestätigen`}
                        </button>
                    </div>
                )}

                {csvConfirmResult && (
                    <div className="admin-bank-card" role="status">
                        <h3>Verbuchung abgeschlossen</h3>
                        <p>
                            {csvConfirmResult.paid} Bestellungen
                            wurden als bezahlt markiert.
                        </p>
                        <p>
                            Die Ticket-E-Mails wurden zur
                            Verarbeitung angestoßen.
                        </p>
                    </div>
                )}
            </section>

            <section className="admin-bank-card">
                <h2>CAMT-Kontoauszug importieren</h2>

                <p>
                    Unterstützt wird aktuell unser
                    CAMT.053-Import. Es werden nur gebuchte
                    Zahlungseingänge berücksichtigt.
                </p>

                <form onSubmit={handleImport}>
                    <div className="admin-bank-upload">
                        <label htmlFor="camt-file">
                            XML-Datei auswählen
                        </label>

                        <input
                            id="camt-file"
                            type="file"
                            accept=".xml,application/xml,text/xml"
                            onChange={(event) => {
                                const selected =
                                    event.currentTarget.files?.[0] ?? null;

                                setFile(selected);
                                setConfirmed(false);
                                setResult(null);
                                setError(null);
                            }}
                        />

                        {file && (
                            <p className="admin-bank-filename">
                                Ausgewählt: {file.name}
                            </p>
                        )}
                    </div>

                    <div className="admin-bank-warning">
                        <strong>Achtung: Verbindlicher Import</strong>

                        <p>
                            Die Datei wird nicht nur eingelesen.
                            Passende Bestellungen werden direkt
                            als bezahlt markiert. Dadurch werden
                            Tickets erzeugt und E-Mails versendet.
                        </p>

                        <label className="admin-bank-confirmation">
                            <input
                                type="checkbox"
                                checked={confirmed}
                                onChange={(event) =>
                                    setConfirmed(event.target.checked)
                                }
                            />

                            <span>
                                Ich habe den Kontoauszug und die
                                enthaltenen Buchungen geprüft.
                            </span>
                        </label>
                    </div>

                    {error && (
                        <p role="alert" className="admin-bank-error">
                            {error}
                        </p>
                    )}

                    <button
                        type="submit"
                        className="admin-bank-submit"
                        disabled={!file || !confirmed || uploading}
                    >
                        {uploading
                            ? 'Zahlungen werden abgeglichen...'
                            : 'Zahlungen verbindlich abgleichen'}
                    </button>
                </form>
            </section>

            {result && (
                <section className="admin-bank-card" aria-live="polite">
                    <h2>Importergebnis</h2>

                    <div className="admin-bank-summary">
                        <div>
                            <span>Buchungen erkannt</span>
                            <strong>{result.transactionsFound}</strong>
                        </div>

                        <div>
                            <span>Automatisch bezahlt</span>
                            <strong>{result.paid}</strong>
                        </div>

                        <div>
                            <span>Zu prüfen</span>
                            <strong>{result.reviewRequired}</strong>
                        </div>
                    </div>

                    {result.transactionsFound === 0 && (
                        <p>
                            Keine gebuchten Gutschriften gefunden.
                            Es wurden keine Bestellungen bezahlt.
                        </p>
                    )}

                    {result.reviewRequired > 0 && (
                        <p className="admin-bank-review-notice">
                            Bitte prüfe die nicht automatisch
                            zugeordneten Buchungen. Insbesondere
                            bereits bezahlte Bestellungen können
                            auf Doppelüberweisungen hinweisen.
                        </p>
                    )}

                    {result.items.length > 0 && (
                        <div className="admin-bank-table-wrapper">
                            <table className="admin-bank-table">
                                <thead>
                                    <tr>
                                        <th>Bestellung</th>
                                        <th>Bankreferenz</th>
                                        <th>Betrag</th>
                                        <th>Ergebnis</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {result.items.map((item, index) => (
                                        <tr key={`${item.bankReference ?? 'entry'}-${index}`}>
                                            <td>
                                                {item.orderReference ?? '—'}
                                            </td>

                                            <td>
                                                {item.bankReference ?? '—'}
                                            </td>

                                            <td>
                                                {currencyFormatter.format(item.amount)}
                                            </td>

                                            <td>
                                                <span
                                                    className={
                                                        item.status === 'PAID'
                                                            ? 'admin-bank-result-paid'
                                                            : 'admin-bank-result-review'
                                                    }
                                                >
                                                    {STATUS_LABELS[item.status] ?? item.status}
                                                </span>

                                                <small>{item.message}</small>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            )}
        </div>
    );
}

export default AdminBankStatementsPage;
