import './styles/PaymentSection.css'

import {useState} from 'react';

import {
    cancelOrder as cancelOrderRequest,
    type OrderResponse
} from '../../api/orderApi';
import {startPayment} from '../../api/paymentApi';

type PaymentSectionProps = {
    order: OrderResponse;
    accessToken: string;
    onOrderUpdated: (orderResponse: OrderResponse) => void;
};

function PaymentSection({
                            order,
                            accessToken,
                            onOrderUpdated,
                        }: PaymentSectionProps) {
    const [startingPayment, setStartingPayment] =
        useState(false);

    const [showCancelConfirmation, setShowCancelConfirmation] =
        useState(false);

    const [cancelingOrder, setCancelingOrder] =
        useState(false);

    const [paymentError, setPaymentError] =
        useState<string | null>(null);

    const [cancelError, setCancelError] =
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

    async function handleCancelOrder() {
        try {
            setCancelingOrder(true);
            setCancelError(null);

            const cancelledOrder = await cancelOrderRequest(
                order.concertId,
                order.id,
                accessToken,
            );

            setShowCancelConfirmation(false);
            onOrderUpdated(cancelledOrder);
        } catch (error) {
            if (error instanceof Error) {
                setCancelError(error.message);
            } else {
                setCancelError(
                    'Bestellung konnte nicht storniert werden.',
                );
            }
        } finally {
            setCancelingOrder(false);
        }
    }

    return (
        <section className="payment-section">
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
                <p className="error-message">
                    Fehler: {paymentError}
                </p>
            )}

            <button
                className="primary-button"
                type="button"
                onClick={handleStartPayment}
                disabled={
                    startingPayment ||
                    cancelingOrder
                }
            >
                {startingPayment
                    ? 'Zahlung wird gestartet...'
                    : 'Jetzt bezahlen'}
            </button>

            <div className="cancel-section">
                {!showCancelConfirmation && (
                    <>
                        <p>
                            Bestellung doch nicht abschließen?
                        </p>

                        <button
                            className="danger-button"
                            type="button"
                            onClick={() =>
                                setShowCancelConfirmation(true)
                            }
                            disabled={
                                startingPayment ||
                                cancelingOrder
                            }
                        >
                            Bestellung stornieren
                        </button>
                    </>
                )}

                {showCancelConfirmation && (
                    <div className="cancel-confirmation">
                        <p>
                            Möchtest du diese Bestellung
                            wirklich stornieren?
                        </p>

                        {cancelError && (
                            <p className="error-message">
                                Fehler: {cancelError}
                            </p>
                        )}

                        <div className="cancel-actions">
                            <button
                                className="secondary-button"
                                type="button"
                                onClick={() =>
                                    setShowCancelConfirmation(false)}
                                disabled={cancelingOrder}
                            >
                                Abbrechen
                            </button>

                            <button
                                className="danger-button"
                                type="button"
                                onClick={handleCancelOrder}
                                disabled={cancelingOrder}
                            >
                                {cancelingOrder
                                    ? 'Bestellung wird storniert...'
                                    : 'Bestellung stornieren'}
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </section>
    );
}

export default PaymentSection;