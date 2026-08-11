import {useState} from 'react';

import type {CreatedOrderResponse} from '../../api/orderApi';
import {startPayment} from '../../api/paymentApi';

type PaymentSectionProps = {
    createdOrder: CreatedOrderResponse;
};

function PaymentSection({
                            createdOrder,
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
                createdOrder.order.concertId,
                createdOrder.order.id,
                createdOrder.accessToken,
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
            <h3>Bestellung erstellt</h3>

            <p>
                Bestellnummer:{' '}
                <strong>
                    {createdOrder.order.id}
                </strong>
            </p>

            <p>
                Status:{' '}
                <strong>
                    {createdOrder.order.status}
                </strong>
            </p>

            <p>
                Gesamtbetrag:{' '}
                <strong>
                    {currencyFormatter.format(
                        createdOrder.order.totalAmount,
                    )}
                </strong>
            </p>

            <p>
                Die Bestellung wurde reserviert und
                wartet auf die Zahlung.
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