import {
    type FormEvent,
    useState,
} from 'react';

import {
    Link,
    useNavigate,
} from 'react-router-dom';

import {
    createAdminConcert,
} from '../api/adminConcertApi'

function AdminConcertCreatePage() {
    const navigate = useNavigate();

    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [startTime, setStartTime] = useState('');
    const [location, setLocation] = useState('');

    const [isSubmitting, setIsSubmitting] = useState(false);

    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        setError(null);
        setIsSubmitting(true);

        try {
            await createAdminConcert({
                title: title.trim(),
                description: description.trim(),
                startTime,
                location: location.trim(),
            });

            navigate('/admin/concerts');
        } catch {
            setError(
                'Das Konzert konnte nicht erstellt werden.',
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <section>
            <header className="admin-page-header">
                <div>
                    <h1>Neues Konzert</h1>

                    <p>
                        Lege die grundlegenden Konzertdaten an.
                    </p>
                </div>
            </header>

            <form
                className="admin-concert-form"
                onSubmit={handleSubmit}
            >
                <div className="admin-form-field">
                    <label htmlFor="title">
                        Titel
                    </label>

                    <input
                        id="title"
                        type="text"
                        value={title}
                        onChange={(event) =>
                            setTitle(event.target.value)}
                        required
                    />
                </div>

                <div className="admin-form-field">
                    <label htmlFor="description">
                        Beschreibung
                    </label>

                    <textarea
                        id="description"
                        value={description}
                        onChange={(event) =>
                            setDescription(event.target.value)
                        }
                        rows={5}
                    />
                </div>

                <div className="admin-form-field">
                    <label htmlFor="startTime">
                        Beginn
                    </label>

                    <input
                        id="startTime"
                        type="datetime-local"
                        value={startTime}
                        onChange={(event) =>
                            setStartTime(event.target.value)
                        }
                        required
                    />
                </div>

                <div className="admin-form-field">
                    <label htmlFor="location">
                        Veranstaltungsort
                    </label>

                    <input
                        id="location"
                        type="text"
                        value={location}
                        onChange={(event) =>
                            setLocation(event.target.value)
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
                    <Link
                        to="/admin/concerts"
                        className="admin-secondary-action"
                    >
                        Abbrechen
                    </Link>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                    >
                        {isSubmitting
                            ? 'Wird erstellt...'
                            : 'Konzert erstellen'}
                    </button>
                </div>
            </form>
        </section>
    );
}

export default AdminConcertCreatePage;