import {
    Route,
    Routes,
} from 'react-router-dom';

import AdminAuthProvider from './auth/AdminAuthProvider';
import RequireAdminAuth from './auth/RequireAdminAuth';

import AdminDashboardPage from './pages/AdminDashboardPage';
import AdminLoginPage from './pages/AdminLoginPage';

function AdminApp() {
    return (
        <AdminAuthProvider>
            <Routes>
                <Route
                    path="login"
                    element={<AdminLoginPage/>}
                />

                <Route element={<RequireAdminAuth/>}>
                    <Route
                        index
                        element={<AdminDashboardPage/>}
                    />
                </Route>
            </Routes>
        </AdminAuthProvider>
    );
}

export default AdminApp;