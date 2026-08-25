import {
    Route,
    Routes,
} from 'react-router-dom';

import AdminAuthProvider from './auth/AdminAuthProvider';
import RequireAdminAuth from './auth/RequireAdminAuth';

import AdminLayout from './layout/AdminLayout';

import AdminLoginPage from './pages/AdminLoginPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import AdminConcertsPage from './pages/AdminConcertsPage';
import AdminConcertDetailPage from './pages/AdminConcertDetailPage';
import AdminOrdersPage from './pages/AdminOrdersPage';
import AdminScannerPage from './pages/AdminScannerPage';
import AdminConcertCreatePage
    from './pages/AdminConcertCreatePage';

import './styles/AdminForms.css';

function AdminApp() {
    return (
        <AdminAuthProvider>
            <Routes>
                <Route
                    path="login"
                    element={<AdminLoginPage/>}
                />

                <Route element={<RequireAdminAuth/>}>
                    <Route element={<AdminLayout/>}>
                        <Route
                            index
                            element={<AdminDashboardPage/>}
                        />

                        <Route
                            path="concerts"
                            element={<AdminConcertsPage/>}
                        />

                        <Route
                            path="concerts/new"
                            element={<AdminConcertCreatePage/>}
                        />

                        <Route
                            path="concerts/:concertId"
                            element={<AdminConcertDetailPage/>}
                        />

                        <Route
                            path="orders"
                            element={<AdminOrdersPage/>}
                        />

                        <Route
                            path="scanner"
                            element={<AdminScannerPage/>}
                        />
                    </Route>
                </Route>
            </Routes>
        </AdminAuthProvider>
    );
}

export default AdminApp;