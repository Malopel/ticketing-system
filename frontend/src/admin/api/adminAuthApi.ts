import {adminApiClient} from './adminApiClient';

export async function checkAdminAuthentication(): Promise<void> {
    await adminApiClient.request<void>(
        '/api/admin/auth/check',
    );
}