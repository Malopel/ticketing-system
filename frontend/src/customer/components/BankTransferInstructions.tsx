import './styles/BankTransferInstructions.css';

import {useState, useRef, type ReactNode} from 'react';
import {QRCodeCanvas} from "qrcode.react";

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

    const qrCanvasRef = useRef<HTMLCanvasElement>(null);

    function downloadQrCode() {
        const canvas = qrCanvasRef.current;
        if (!canvas) return;

        const link = document.createElement("a");

        link.href = canvas.toDataURL("image/png");
        link.download = `ueberweisung-${payment.reference}.png`;

        link.click();
    }

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


                <a
                    className="bank-app-button"
                    href={payment.paytoUri}
                >
                    Banking-App öffnen (falls unterstützt)
                </a>

                <p className="bank-payment__hint">
                    Nicht alle Banking-Apps unterstützen diesen Button.
                    Falls sich nichts öffnet, verwende den QR-Code
                    oder kopiere die Überweisungsdaten.
                </p>

                <div className="bank-payment__qr">
                    <h3>Mit Banking-App bezahlen</h3>

                    <QRCodeCanvas
                        ref={qrCanvasRef}
                        value={payment.epcQrPayload}
                        size={220}
                        level="M"
                        marginSize={4}
                    />

                    <p>
                        <strong>So funktioniert's:</strong>
                    </p>

                    <p>
                        Öffne deine Banking-App und wähle
                        „Überweisung“ oder „Geld senden“.
                        Suche dort nach „QR-Code scannen“
                        und scanne den Code.
                    </p>

                    <p className="bank-payment__hint">
                        Die Überweisungsdaten werden automatisch
                        vorausgefüllt, sofern deine Banking-App
                        GiroCode unterstützt. Bitte überprüfe
                        alle Angaben vor der Freigabe.
                    </p>

                    <button
                        type="button"
                        onClick={downloadQrCode}
                        className="bank-payment__download"
                    >
                        QR-Code als Bild speichern
                    </button>

                    <p className="bank-payment__hint">
                        Du bist am Smartphone? Speichere den QR-Code
                        als Bild. Einige Banking-Apps ermöglichen
                        das Einlesen eines gespeicherten QR-Codes.
                    </p>
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
