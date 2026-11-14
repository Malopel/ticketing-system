import {Link, Outlet} from 'react-router-dom';

import './PublicLayout.css';

function PublicLayout() {
    return (
        <div className="public-layout">
            <div className="public-layout__content">
                <Outlet/>
            </div>

            <footer className="site-footer">
                <div className="site-footer__content">
                    <span>© {new Date().getFullYear()} Accordion Symphonic</span>

                    <nav
                        className="site-footer__links"
                        aria-label="Rechtliche Informationen"
                    >
                        <Link to="/impressum">Impressum</Link>
                        <Link to="/datenschutz">Datenschutz</Link>
                    </nav>
                </div>
            </footer>
        </div>
    );
}

export default PublicLayout;
