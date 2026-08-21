import {
    type ReactNode,
    useState,
} from 'react';

import {
    AdminAuthContext,
} from './adminAuthContext.ts';

import {adminApiClient} from '../api/adminApiClient';
import {checkAdminAuthentication} from '../api/adminAuthApi';

type AdminAuthProviderProps = {
    children: ReactNode;
};

function AdminAuthProvider({
                               children,
                           }: AdminAuthProviderProps) {
    const [isAuthenticated, setIsAuthenticated] =
        useState(false);

    async function login(
        username: string,
        password: string,
    ): Promise<void> {
        adminApiClient.setCredentials({
            username,
            password,
        });

        try {
            await checkAdminAuthentication();

            setIsAuthenticated(true);
        } catch (error) {
            adminApiClient.clearCredentials();
            setIsAuthenticated(false);

            throw error;
        }
    }

    function logout() {
        adminApiClient.clearCredentials();
        setIsAuthenticated(false);
    }

    return (
        <AdminAuthContext.Provider
            value={{
                isAuthenticated,
                login,
                logout,
            }}
        >
            {children}
        </AdminAuthContext.Provider>
    );
}

export default AdminAuthProvider;