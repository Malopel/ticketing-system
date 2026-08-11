import { useState } from 'react';
import {useParams} from 'react-router-dom';
import { completeFakePayment } from '../api/paymentApi';
import type { OrderResponse } from '../api/orderApi';

function FakePaymentPage() {
    const { providerPaymentId } = useParams<{providerPaymentId: string;}>();

    const [paying, setPaying] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [order, setOrder] = useState<OrderResponse | null>(null);

    async function handleCompletePayment() {
        if (!providerPaymentId) {
            setError('Ungültige Zahlungs-ID.');
            return;
        }

        try {
            setPaying(true);
            setError(null);

            const paidOrder =
                await completeFakePayment(providerPaymentId);

            setOrder(paidOrder);
        } catch (error) {
            if (error instanceof Error) {
                setError(error.message);
            } else {
                setError(
                    'Ein unbekannter Fehler ist aufgetreten.',
                );
            }
        } finally {
            setPaying(false);
        }
    }

    if (!providerPaymentId) {
        return <p>Ungültige Zahlungs-ID.</p>;
    }

    return (
        <main>
            <h1>Fake Payment</h1>

            {order === null ? (
                <>
                    <p>
                        Dies ist der lokale Test-Zahlungsanbieter.
                    </p>

                    <button
                        type="button"
                        onClick={handleCompletePayment}
                        disabled={paying}
                    >
                        {paying
                            ? 'Zahlung wird verarbeitet...'
                            : 'Zahlung erfolgreich simulieren'}
                    </button>
                </>
            ) : (
                <>
                    <h2>Zahlung erfolgreich</h2>

                    <p>
                        Bestellung #{order.id} wurde bezahlt.
                    </p>

                    <p>
                        Status: <strong>{order.status}</strong>
                    </p>

                    <a href="/">
                        Zurück zum Ticketshop
                    </a>
                </>
            )}

            {error && (
                <p>Fehler: {error}</p>
            )}
        </main>
    );
}

export default FakePaymentPage;