import {
    type FormEvent,
    useState,
} from 'react';

import {
    createAdminTicketCategory,
} from '../api/adminTicketCategoryApi';

type TicketCategoryCreateFormProps = {
    concertId: number;
    onCreated: () => Promise<void>;
    onCancel: () => void;
};

function TicketCategoryCreateForm({
                                      concertId,
                                      onCreated,
                                      onCancel,
                                  }: TicketCategoryCreateFormProps) {
    const [name, setName] = useState('');
    const [price, setPrice] = useState('');
    const [capacity, setCapacity] = useState('');

    const [isSubmitting, setIsSubmitting] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        setError(null);
        setIsSubmitting(true);

        try {
            await createAdminTicketCategory(
                concertId,
                {
                    name: name.trim(),
                    price: Number(price),
                    capacity: Number(capacity),
                },
            );

            await onCreated();
        } catch {
            setError(
                'Die Ticketkategorie konnte nicht erstellt werden.',
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <form
            className="admin-ticket-category-form"
            onSubmit={handleSubmit}
        >
            <div className="admin-form-field">
                <label htmlFor="category-name">
                    Name
                </label>

                <input
                    id="category-name"
                    type="text"
                    value={name}
                    onChange={(event) =>
                        setName(event.target.value)
                    }
                    required
                />
            </div>

            <div className="admin-form-field">
                <label htmlFor="category-price">
                    Preis
                </label>

                <input
                    id="category-price"
                    type="number"
                    min="0"
                    step="0.01"
                    value={price}
                    onChange={(event) =>
                        setPrice(event.target.value)
                    }
                    required
                />
            </div>

            <div className="admin-form-field">
                <label htmlFor="category-capacity">
                    Kapazität
                </label>

                <input
                    id="category-capacity"
                    type="number"
                    min="1"
                    step="1"
                    value={capacity}
                    onChange={(event) =>
                        setCapacity(event.target.value)
                    }
                    required
                />
            </div>

            {error && (
                <p
                    className="admin-error-message"
                    role="alert"
                >
                    {error}
                </p>
            )}

            <div className="admin-form-actions">
                <button
                    type="button"
                    onClick={onCancel}
                    className="admin-secondary-button"
                    disabled={isSubmitting}
                >
                    Abbrechen
                </button>

                <button
                    type="submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting
                        ? 'Wird erstellt...'
                        : 'Ticketkategorie erstellen'}
                </button>
            </div>
        </form>
    );
}

export default TicketCategoryCreateForm;