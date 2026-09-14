import {
    type FormEvent,
    useState,
} from 'react';

import {
    type AdminConcert,
    updateAdminConcert,
} from '../api/adminConcertApi';

type UpdateConcertFormProps = {
    concertId: AdminConcert,
    onUpdated: (concert: AdminConcert) => void,
    onCancel: () => void,
}

function UpdateConcertForm({
                               concert,
                               onUpdated,
                               onCancel,
                           }: UpdateConcertFormProps) {
    const [title, setTitle] =
        useState(concert.title);

    const [description, setDescription] =
        useState(concert.description);

    const [startTime, setStartTime] =
        useState(concert.startTime.slice(0, 16));

    const [location, setLocation] =
        useState(concert.location);

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

            const updatedConcert =
                await updateAdminConcert(
                    concert.id,
                    {
                        title,
                        description,
                        startTime,
                        location,
                    },
                );

            onUpdated(updatedConcert);
        } catch {
            setError(
                'Das Konzert konnte nicht aktualisiert werden.',
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <form
            className="admin-concert-form"
            onSubmit={handleSubmit}
        >
            <label className="admin-form-field">
                <span>Titel</span>

                <input
                    type="text"
                    value={title}
                    onChange={(event) =>
                        setTitle(event.target.value)
                    }
                    required
                />
            </label>

            <label className="admin-form-field">
                <span>Beschreibung</span>

                <textarea
                    value={description}
                    onChange={(event) =>
                        setDescription(event.target.value)
                    }
                />
            </label>

            <label className="admin-form-field">
                <span>Termin</span>

                <input
                    type="datetime-local"
                    value={startTime}
                    onChange={(event) =>
                        setStartTime(event.target.value)
                    }
                    required
                />
            </label>

            <label className="admin-form-field">
                <span>Veranstaltungsort</span>

                <input
                    type="text"
                    value={location}
                    onChange={(event) =>
                        setLocation(event.target.value)
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

export default UpdateConcertForm;