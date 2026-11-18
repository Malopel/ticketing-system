import './styles/PaymentSection.css'

import {useState} from 'react';

import {
    cancelOrder as cancelOrderRequest,
    type OrderResponse
} from '../../api/orderApi.ts';

import {startBankTransfer} from '../../api/bankTransferApi.ts';

import type {BankTransferInstructions} from
        '../BankTransferInstructions.tsx';

type PaymentSectionProps = {
    order: OrderResponse;
    accessToken: string;
    onOrderUpdated: (orderResponse: OrderResponse) => void;
    onBankTransferStarted: (
        payment: BankTransferInstructions
    ) => void;
    paymentDisabled: boolean;
};

function PaymentSection({
                            order,
                            accessToken,
                            onOrderUpdated,
                            onBankTransferStarted,
                            paymentDisabled,
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
        if (paymentDisabled || startingPayment) return;

        try {
            setStartingPayment(true);
            setPaymentError(null);

            const bankTransfer = await startBankTransfer(
                order.concertId,
                order.id,
                accessToken,
            );

            onBankTransferStarted(bankTransfer);

            onOrderUpdated({
                ...order,
                status: 'PAYMENT_PENDING',
                paymentMethod: 'BANK_TRANSFER',
                paymentExpiresAt: bankTransfer.expiresAt,
            });

        } catch (error) {
            setPaymentError(
                error instanceof Error
                    ? error.message
                    : 'Banküberweisung konnte nicht gestartet werden.',
            );
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

            {order.concertStatus === 'PUBLISHED' ? (
                <>
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

                    <p>
                        Nach dem nächsten Schritt erhältst du die
                        Bankverbindung und einen QR-Code.
                        Deine Tickets werden nach bestätigtem
                        Zahlungseingang per E-Mail versendet.
                    </p>
                </>
            ) : (
                <p>
                    Das Konzert wurde abgesagt.
                    Diese Bestellung kann daher nicht mehr bezahlt werden.
                </p>
            )}

            {paymentError && (
                <p className="error-message">
                    Fehler: {paymentError}
                </p>
            )}

            {order.concertStatus === 'PUBLISHED' && (
                <button
                    className="primary-button"
                    type="button"
                    onClick={handleStartPayment}
                    disabled={
                        paymentDisabled ||
                        startingPayment ||
                        cancelingOrder
                    }
                >
                    {startingPayment
                        ? 'Überweisung wird vorbereitet...'
                        : paymentDisabled
                            ? 'Reservierung abgelaufen'
                            : 'Überweisungsdaten anzeigen'}
                </button>
            )}

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