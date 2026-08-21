import {useAdminAuth} from '../auth/adminAuthContext';

function AdminDashboardPage() {
    const {logout} = useAdminAuth();

    return (
        <main>
            <h1>Adminbereich</h1>

            <p>
                Verwaltung von Konzerten,
                Bestellungen und Tickets.
            </p>

            <button
                type="button"
                onClick={logout}
            >
                Abmelden
            </button>
        </main>
    );
}

export default AdminDashboardPage;