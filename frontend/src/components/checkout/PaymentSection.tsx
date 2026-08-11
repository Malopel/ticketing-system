import {useState} from 'react';

import type {OrderResponse} from '../../api/orderApi';
import {startPayment} from '../../api/paymentApi';

type PaymentSectionProps = {
    order: OrderResponse;
    accessToken: string;
};

function PaymentSection({
                            order,
                            accessToken,
                        }: PaymentSectionProps) {
    const [startingPayment, setStartingPayment] =
        useState(false);

    const [paymentError, setPaymentError] =
        useState<string | null>(null);

    const currencyFormatter = new Intl.NumberFormat(
        'de-DE',
        {
            style: 'currency',
            currency: 'EUR',
        },
    );

    async function handleStartPayment() {
        try {
            setStartingPayment(true);
            setPaymentError(null);

            const paymentSession = await startPayment(
                order.concertId,
                order.id,
                accessToken,
            );

            window.location.href =
                paymentSession.checkoutUrl;
        } catch (error) {
            if (error instanceof Error) {
                setPaymentError(error.message);
            } else {
                setPaymentError(
                    'Zahlung konnte nicht gestartet werden.',
                );
            }
        } finally {
            setStartingPayment(false);
        }
    }

    return (
        <section>
            <h2>Zahlung</h2>

            <p>
                Die Bestellung wurde reserviert und
                wartet auf die Zahlung.
            </p>

            <p>
                Zu zahlen:{' '}
                <strong>
                    {currencyFormatter.format(
                        order.totalAmount,
                    )}
                </strong>
            </p>

            {paymentError && (
                <p>Fehler: {paymentError}</p>
            )}

            <button
                type="button"
                onClick={handleStartPayment}
                disabled={startingPayment}
            >
                {startingPayment
                    ? 'Zahlung wird gestartet...'
                    : 'Jetzt bezahlen'}
            </button>
        </section>
    );
}

export default PaymentSection;