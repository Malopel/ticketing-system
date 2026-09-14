import {
    type FormEvent,
    useState,
} from 'react';

import {
    type AdminTicketCategory,
    updateAdminTicketCategory,
} from '../api/adminTicketCategoryApi';

type TicketCategoryUpdateFormProps = {
    concertId: number;
    category: AdminTicketCategory;
    onUpdated: () => Promise<void>;
    onCancel: () => void;
};

function TicketCategoryUpdateForm({
                                      concertId,
                                      category,
                                      onUpdated,
                                      onCancel,
                                  }: TicketCategoryUpdateFormProps) {
    const [name, setName] =
        useState(category.name);

    const [price, setPrice] =
        useState(category.price.toString());

    const [capacity, setCapacity] =
        useState(category.capacity.toString());

    const [isSubmitting, setIsSubmitting] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        try {
            setIsSubmitting(true);
            setError(null);

            await updateAdminTicketCategory(
                concertId,
                category.id,
                {
                    name,
                    price: Number(price),
                    capacity: Number(capacity),
                },
            );

            await onUpdated();
        } catch {
            setError(
                'Die Ticketkategorie konnte nicht aktualisiert werden.',
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
            <label className="admin-form-field">
                <span>Name</span>

                <input
                    type="text"
                    value={name}
                    onChange={(event) =>
                        setName(event.target.value)
                    }
                    required
                />
            </label>

            <label className="admin-form-field">
                <span>Preis</span>

                <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={price}
                    onChange={(event) =>
                        setPrice(event.target.value)
                    }
                    required
                />
            </label>

            <label className="admin-form-field">
                <span>Kapazität</span>

                <input
                    type="number"
                    min="1"
                    step="1"
                    value={capacity}
                    onChange={(event) =>
                        setCapacity(event.target.value)
                    }
                    required
                />
            </label>

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
                    type="submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting
                        ? 'Wird gespeichert...'
                        : 'Speichern'}
                </button>

                <button
                    type="button"
                    className="admin-secondary-button"
                    onClick={onCancel}
                    disabled={isSubmitting}
                >
                    Abbrechen
                </button>
            </div>
        </form>
    );
}

export default TicketCategoryUpdateForm;