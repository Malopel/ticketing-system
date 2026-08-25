import {
    Link,
    NavLink,
    Outlet
} from 'react-router-dom';

import {useAdminAuth} from '../auth/adminAuthContext';

import './AdminLayout.css';

function AdminLayout() {
    const {logout} = useAdminAuth();

    return (
        <div className="admin-layout">
            <header className="admin-header">
                <div className="admin-brand">
                    <Link
                        to="/admin"
                        className="admin-brand-link"
                    >
                        Accordion Symphonic
                    </Link>

                    <span className="admin-header-label">
                        Administration
                    </span>
                </div>

                <a
                    href="/"
                    target="_blank"
                    rel="noreferrer"
                    className="admin-shop-link"
                >
                    Ticketshop öffnen ↗
                </a>

                <button
                    type="button"
                    onClick={logout}
                    className="admin-logout-button"
                >
                    Abmelden
                </button>
            </header>

            <div className="admin-body">
                <aside className="admin-sidebar">
                    <nav className="admin-navigation">
                        <NavLink
                            to="/admin"
                            end
                            className={({isActive}) =>
                                isActive
                                    ? 'admin-nav-link active'
                                    : 'admin-nav-link'
                            }
                        >
                            Dashboard
                        </NavLink>

                        <NavLink
                            to="/admin/concerts"
                            className={({isActive}) =>
                                isActive
                                    ? 'admin-nav-link active'
                                    : 'admin-nav-link'
                            }
                        >
                            Konzerte
                        </NavLink>

                        <NavLink
                            to="/admin/orders"
                            className={({isActive}) =>
                                isActive
                                    ? 'admin-nav-link active'
                                    : 'admin-nav-link'
                            }
                        >
                            Bestellungen
                        </NavLink>

                        <NavLink
                            to="/admin/scanner"
                            className={({isActive}) =>
                                isActive
                                    ? 'admin-nav-link active'
                                    : 'admin-nav-link'
                            }
                        >
                            Einlass
                        </NavLink>
                    </nav>
                </aside>

                <main className="admin-content">
                    <Outlet/>
                </main>
            </div>
        </div>
    );
}

export default AdminLayout;