import { useState } from 'react';
import { completeFakePayment } from '../api/paymentApi';
import type { OrderResponse } from '../api/orderApi';

type FakePaymentPageProps = {
    providerPaymentId: string;
};

function FakePaymentPage({
                             providerPaymentId,
                         }: FakePaymentPageProps) {
    const [paying, setPaying] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [order, setOrder] = useState<OrderResponse | null>(null);

    async function handleCompletePayment() {
        setPaying(true);
        setError(null);

        try {
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