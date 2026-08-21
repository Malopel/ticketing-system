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
import AdminOrdersPage from './pages/AdminOrdersPage';
import AdminScannerPage from './pages/AdminScannerPage';

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