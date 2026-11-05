import { useEffect, useRef, useState } from 'react';
import {
    BrowserQRCodeReader,
    type IScannerControls,
} from '@zxing/browser';
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
    const videoRef = useRef<HTMLVideoElement | null>(null);
    const [scanning, setScanning] = useState(false);
    const controlsRef = useRef<IScannerControls | null>(null);

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

    async function startScanner() {
        if (!concertId) {
            setError('Bitte zuerst eine Konzert-ID eingeben.');
            return;
        }

        if (!videoRef.current) {
            return;
        }

        setError(null);
        setScanning(true);

        const reader = new BrowserQRCodeReader();

        try {
            controlsRef.current = await reader.decodeFromConstraints(
                {
                    video: {
                        facingMode: {
                            ideal: 'environment',
                        },
                    },
                },
                videoRef.current,
                (result) => {
                    if (!result) {
                        return;
                    }
                    const token = result.getText();

                    controlsRef.current?.stop();
                    controlsRef.current = null;
                    setScanning(false);

                    void validateScannedToken(token);
                },
            );
        } catch {
            controlsRef.current?.stop();
            controlsRef.current = null;
            setError('Kamera konnte nicht gestartet werden.');
            setScanning(false);
        }
    }

    async function validateScannedToken(token: string) {
        const parsedConcertId = Number(concertId);

        if (!Number.isInteger(parsedConcertId) || parsedConcertId <= 0) {
            setError('Bitte eine gültige Konzert-ID eingeben.');
            return;
        }

        setQrToken(token);
        setLoading(true);
        setError(null);

        try {
            const result = await validateTicket(
                parsedConcertId,
                token,
            );

            setTicket(result);
        } catch {
            setTicket(null);
            setError('Ticket konnte nicht gefunden oder geprüft werden.');
        } finally {
            setLoading(false);
        }
    }

    function stopScanner() {
        controlsRef.current?.stop();
        controlsRef.current = null;
        setScanning(false);
    }

    useEffect(() => {
        return () => {
            controlsRef.current?.stop();
        };
    }, []);

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

            <div>
                <video
                    ref={videoRef}
                    style={{
                        width: '100%',
                        maxWidth: '500px',
                    }}
                />

                {!scanning ? (
                    <button onClick={startScanner}>
                        QR-Code scannen
                    </button>
                ) : (
                    <button onClick={stopScanner}>
                        Scanner stoppen
                    </button>
                )}
            </div>

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