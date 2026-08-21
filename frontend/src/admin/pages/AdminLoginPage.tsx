import {
    type FormEvent,
    useState,
} from 'react';

import {
    useLocation,
    useNavigate,
} from 'react-router-dom';

import {AdminApiError} from '../api/adminApiClient';
import {useAdminAuth} from '../auth/adminAuthContext';

type LoginLocationState = {
    from?: string;
};

function AdminLoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const {login} = useAdminAuth();

    const navigate = useNavigate();
    const location = useLocation();

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        setError(null);
        setIsSubmitting(true);

        try {
            await login(username, password);

            const state =
                location.state as LoginLocationState | null;

            navigate(
                state?.from ?? '/admin',
                {replace: true},
            );
        } catch (error) {
            if (
                error instanceof AdminApiError &&
                error.status === 401
            ) {
                setError(
                    'Benutzername oder Passwort ist falsch.',
                );
            } else {
                setError(
                    'Die Anmeldung ist fehlgeschlagen.',
                );
            }
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <main>
            <h1>Admin-Anmeldung</h1>

            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="username">
                        Benutzername
                    </label>

                    <input
                        id="username"
                        type="text"
                        value={username}
                        onChange={(event) =>
                            setUsername(event.target.value)
                        }
                        autoComplete="username"
                        required
                    />
                </div>

                <div>
                    <label htmlFor="password">
                        Passwort
                    </label>

                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={(event) =>
                            setPassword(event.target.value)
                        }
                        autoComplete="current-password"
                        required
                    />
                </div>

                {error && (
                    <p role="alert">
                        {error}
                    </p>
                )}

                <button
                    type="submit"
                    disabled={isSubmitting}
                >
                    {isSubmitting
                        ? 'Anmeldung läuft...'
                        : 'Anmelden'}
                </button>
            </form>
        </main>
    );
}

export default AdminLoginPage;