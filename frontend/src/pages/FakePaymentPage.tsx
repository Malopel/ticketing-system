import './styles/FakePaymentPage.css'

import {useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import {completeFakePayment} from '../api/paymentApi';
import type {OrderResponse} from '../api/orderApi';

function FakePaymentPage() {
    const {providerPaymentId} = useParams<{ providerPaymentId: string; }>();

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
        <main className="fake-payment-page">
            <section className="fake-payment-card">
                <p className="fake-payment-label">
                    Test-Zahlungsanbieter
                </p>

                <h1>Fake Payment</h1>

                {!order && (
                    <>
                        <p>
                            Diese Seite simuliert einen externen
                            Zahlungsanbieter.
                        </p>

                        {error && (
                            <p className="error-message">
                                Fehler: {error}
                            </p>
                        )}

                        <button
                            type="button"
                            className="primary-button"
                            onClick={handleCompletePayment}
                            disabled={paying}
                        >
                            {paying
                                ? 'Zahlung wird verarbeitet...'
                                : 'Zahlung erfolgreich simulieren'}
                        </button>
                    </>
                )}

                {order && (
                    <>
                        <p className="payment-success">
                            ✓ Zahlung erfolgreich
                        </p>

                        <p>
                            Bestellung #{order.id} wurde bezahlt.
                        </p>

                        <Link
                            className="button-link"
                            to={`/orders/${order.id}`}
                        >
                            Zur Bestellung
                        </Link>
                    </>
                )}
            </section>
        </main>
    );
}

export default FakePaymentPage;