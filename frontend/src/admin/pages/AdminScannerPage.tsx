import { useState } from 'react';
import {
    type TicketResponse,
    markTicketAsUsed,
    validateTicket,
} from '../api/ticketValidationApi';

export function AdminScannerPage() {
    const [concertId, setConcertId] = useState('');
    const [qrToken, setQrToken] = useState('');
    const [ticket, setTicket] = useState<TicketResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function handleValidate() {
        setLoading(true);
        setError(null);

        try {
            const result = await validateTicket(
                Number(concertId),
                qrToken.trim(),
            );

            setTicket(result);
        } catch {
            setTicket(null);
            setError('Ticket konnte nicht gefunden oder geprüft werden.');
        } finally {
            setLoading(false);
        }
    }

    async function handleUse() {
        setLoading(true);
        setError(null);

        try {
            const result = await markTicketAsUsed(
                Number(concertId),
                qrToken.trim(),
            );

            setTicket(result);
        } catch {
            setError('Ticket konnte nicht entwertet werden.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h1>Einlasskontrolle</h1>

            <label>
                Konzert-ID
                <input
                    type="number"
                    value={concertId}
                    onChange={(event) => {
                        setConcertId(event.target.value);
                        setTicket(null);
                        setError(null);
                    }}
                />
            </label>

            <label>
                QR-Code / Ticket-Code
                <input
                    value={qrToken}
                    onChange={(event) => {
                        setQrToken(event.target.value);
                        setTicket(null);
                        setError(null);
                    }}
                />
            </label>

            <button
                onClick={handleValidate}
                disabled={!concertId || !qrToken.trim() || loading}
            >
                Ticket prüfen
            </button>

            {error && <p>{error}</p>}

            {ticket && (
                <div>
                    <h2>
                        {ticket.status === 'VALID'
                            ? '✓ Ticket gültig'
                            : 'Ticket bereits verwendet'}
                    </h2>

                    <p>
                        <strong>Kategorie:</strong> {ticket.ticketCategoryName}
                    </p>

                    {ticket.status === 'VALID' && (
                        <button
                            onClick={handleUse}
                            disabled={loading}
                        >
                            {loading ? 'Wird entwertet...' : 'Ticket entwerten'}
                        </button>
                    )}
                </div>
            )}
        </div>
    );
}

export default AdminScannerPage;