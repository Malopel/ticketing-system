import {useState} from "react";

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

function CopyButton({
                        value,
                        children,
                    }: {
    value: string;
    children: React.ReactNode;
}) {
    const [copied, setCopied] = useState(false);

    async function copy() {
        await navigator.clipboard.writeText(value);

        setCopied(true);

        window.setTimeout(
            () => setCopied(false),
            1500
        );
    }

    return (
        <button type="button" onClick={copy}>
            {copied ? "Kopiert ✓" : children}
        </button>
    );
}

function BankTransferInstruction({payment}: Props) {
    const amount = payment.amount.toFixed(2);

    const paymentText =
        `Empfänger: ${payment.recipient}
IBAN: ${payment.iban}
Betrag: ${amount} EUR
Verwendungszweck: ${payment.reference}`;

    return (
        <div className="bank-payment">
            <h2>Per Überweisung bezahlen</h2>

            <p>
                Bitte führen Sie die Zahlung möglichst als{" "}
                <strong>Echtzeitüberweisung</strong> aus.
            </p>

            <a
                className="bank-app-button"
                href={payment.paytoUri}
            >
                In Banking-App öffnen
            </a>

            <CopyButton value={paymentText}>
                Alle Zahlungsdaten kopieren
            </CopyButton>

            <p>
                <strong>Empfänger:</strong>{" "}
                {payment.recipient}
            </p>

            <p>
                <strong>IBAN:</strong>{" "}
                {payment.iban}
            </p>

            <p>
                <strong>Betrag:</strong>{" "}
                {amount} €
            </p>

            <p>
                <strong>Verwendungszweck:</strong>{" "}
                {payment.reference}
            </p>

            <CopyButton value={payment.reference}>
                Verwendungszweck kopieren
            </CopyButton>
        </div>
    );
}

export default BankTransferInstruction;