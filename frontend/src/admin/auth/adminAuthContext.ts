import {createContext, useContext} from 'react';

export type AdminAuthContextValue = {
    isAuthenticated: boolean;

    login: (
        username: string,
        password: string,
    ) => Promise<void>;

    logout: () => void;
};

export const AdminAuthContext =
    createContext<AdminAuthContextValue | null>(null);

export function useAdminAuth(): AdminAuthContextValue {
    const context = useContext(AdminAuthContext);

    if (!context) {
        throw new Error(
            'useAdminAuth must be used inside AdminAuthProvider',
        );
    }

    return context;
}