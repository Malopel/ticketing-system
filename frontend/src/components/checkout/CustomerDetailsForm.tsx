import {useState, type FormEvent} from 'react';

type CustomerDetailsFormProps = {
    onSubmit: (customerEmail: string) => Promise<void>;
};

function CustomerDetailsForm({
                                 onSubmit,
                             }: CustomerDetailsFormProps) {
    const [customerEmail, setCustomerEmail] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        const email = customerEmail.trim();

        if (!email) {
            setError('Bitte gib eine E-Mail-Adresse ein.');
            return;
        }

        try {
            setSubmitting(true);
            setError(null);

            await onSubmit(email);
        } catch (error) {
            if (error instanceof Error) {
                setError(error.message);
            } else {
                setError(
                    'Bestellung konnte nicht erstellt werden.',
                );
            }
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <section className="customer-details">
            <h3>Kontaktdaten</h3>

            <form
                className="customer-form"
                onSubmit={handleSubmit}
            >
                <label htmlFor="customerEmail">
                    E-Mail-Adresse
                </label>

                <input
                    id="customerEmail"
                    type="email"
                    value={customerEmail}
                    onChange={(event) =>
                        setCustomerEmail(event.target.value)
                    }
                    placeholder="name@example.de"
                    disabled={submitting}
                    required
                />

                {error && (
                    <p>Fehler: {error}</p>
                )}

                <button
                    className="primary-button"
                    type="submit"
                    disabled={
                        submitting ||
                        !customerEmail.trim()
                    }
                >
                    {submitting
                        ? 'Bestellung wird erstellt...'
                        : 'Zahlungspflichtig bestellen'}
                </button>
            </form>
        </section>
    );
}

export default CustomerDetailsForm;