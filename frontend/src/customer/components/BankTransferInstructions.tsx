
import './styles/BankTransferInstructions.css';

import {useState, type ReactNode} from 'react';
import {QRCodeSVG} from 'qrcode.react';

export type BankTransferInstructions = {
    recipient: string;
    iban: string;
    bic: string;
    amount: number;
    reference: string;
    expiresAt: string;
    paytoUri: string;
    epcQrPayload: string;
};

type Props = {
    payment: BankTransferInstructions;
};

type CopyButtonProps = {
    value: string;
    children: ReactNode;
};

function CopyButton({value, children}: CopyButtonProps) {
    const [feedback, setFeedback] =
        useState<'idle' | 'copied' | 'error'>('idle');

    async function copy() {
        try {
            await navigator.clipboard.writeText(value);
            setFeedback('copied');
        } catch {
            setFeedback('error');
        }

        window.setTimeout(() => {
            setFeedback('idle');
        }, 2000);
    }

    return (
        <button
            type="button"
            className="bank-payment__copy-button"
            onClick={copy}
        >
            {feedback === 'copied'
                ? 'Kopiert ✓'
                : feedback === 'error'
                    ? 'Kopieren fehlgeschlagen'
                    : children}
        </button>
    );
}

function BankTransferInstruction({payment}: Props) {
    const formattedAmount = new Intl.NumberFormat(
        'de-DE',
        {
            style: 'currency',
            currency: 'EUR',
        }
    ).format(payment.amount);

    const paymentText = [
        `Empfänger: ${payment.recipient}`,
        `IBAN: ${payment.iban}`,
        ...(payment.bic ? [`BIC: ${payment.bic}`] : []),
        `Betrag: ${payment.amount.toFixed(2)} EUR`,
        `Verwendungszweck: ${payment.reference}`,
    ].join('\n');

    return (
        <section
            className="bank-payment"
            aria-labelledby="bank-payment-heading"
        >
            <header className="bank-payment__header">
                <div>
                    <h2 id="bank-payment-heading">
                        Per Überweisung bezahlen
                    </h2>
                    <p>
                        Überweise den folgenden Betrag möglichst
                        per Echtzeitüberweisung.
                    </p>
                </div>

                <div className="bank-payment__amount">
                    <span>Zu überweisen</span>
                    <strong>{formattedAmount}</strong>
                </div>
            </header>

            <div className="bank-payment__content">
                <div className="bank-payment__details">
                    <h3>Überweisungsdaten</h3>

                    <div className="bank-payment__field">
                        <span className="bank-payment__label">
                            Empfänger
                        </span>

                        <div className="bank-payment__field-content">
                            <strong>{payment.recipient}</strong>
                            <CopyButton value={payment.recipient}>
                                Kopieren
                            </CopyButton>
                        </div>
                    </div>

                    <div className="bank-payment__field">
                        <span className="bank-payment__label">
                            IBAN
                        </span>

                        <div className="bank-payment__field-content">
                            <strong className="bank-payment__reference">
                                {payment.iban}
                            </strong>
                            <CopyButton value={payment.iban}>
                                Kopieren
                            </CopyButton>
                        </div>
                    </div>

                    {payment.bic && (
                        <div className="bank-payment__field">
                            <span className="bank-payment__label">
                                BIC
                            </span>

                            <div className="bank-payment__field-content">
                                <strong>{payment.bic}</strong>
                            </div>
                        </div>
                    )}

                    <div className="bank-payment__field">
                        <span className="bank-payment__label">
                            Verwendungszweck
                        </span>

                        <div className="bank-payment__field-content">
                            <strong className="bank-payment__reference">
                                {payment.reference}
                            </strong>
                            <CopyButton value={payment.reference}>
                                Kopieren
                            </CopyButton>
                        </div>
                    </div>

                    <CopyButton value={paymentText}>
                        Alle Zahlungsdaten kopieren
                    </CopyButton>
                </div>

                <div className="bank-payment__qr-section">
                    <h3>Mit Banking-App bezahlen</h3>

                    <div className="bank-payment__qr">
                        <QRCodeSVG
                            value={payment.epcQrPayload}
                            size={214}
                            level="M"
                        />
                    </div>

                    <p>
                        QR-Code mit einer kompatiblen
                        Banking-App scannen und die
                        Überweisungsdaten übernehmen.
                    </p>

                    <a
                        className="bank-payment__app-link"
                        href={payment.paytoUri}
                    >
                        In Banking-App öffnen
                    </a>
                </div>
            </div>

            <footer className="bank-payment__footer">
                <strong>Wichtig:</strong> Bitte verwende den
                angegebenen Verwendungszweck unverändert.
                Deine Tickets werden nach bestätigtem
                Zahlungseingang per E-Mail versendet.
            </footer>
        </section>
    );
}

export default BankTransferInstruction;
