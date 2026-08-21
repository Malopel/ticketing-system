export type AdminCredentials = {
    username: string;
    password: string;
}

export class AdminApiError extends Error{
    readonly status: number;

    constructor(
        status: number,
        message: string
    ) {
        super(message);

        this.status = status;
        this.name = 'AdminApiError';
    }
}

class AdminApiClient {
    private credentials: AdminCredentials | null = null;

    setCredentials(credentials: AdminCredentials): void {
        this.credentials = credentials;
    }

    clearCredentials(): void {
        this.credentials = null;
    }

    async request<T>(
        path: string,
        options: RequestInit = {},
    ): Promise<T> {
        const headers = new Headers(options.headers);

        if (this.credentials) {
            headers.set(
                'Authorization',
                `Basic ${encodeBasicAuth(
                    this.credentials.username,
                    this.credentials.password,
                )}`
            );
        }

        const response = await fetch(path, {
            ...options,
            headers,
        });

        if (!response.ok) {
            throw new AdminApiError(
                response.status,
                `Admin Api request failed with status ${response.status}`,
            );
        }

        if (response.status === 204) {
            return undefined as T;
        }

        return await response.json() as T;
    }
}

function encodeBasicAuth(
    username: string,
    password: string,
): string {
    const value = `${username}:${password}`;
    const bytes = new TextEncoder().encode(value);

    let binary = '';

    for (const byte of bytes) {
        binary += String.fromCharCode(byte);
    }

    return btoa(binary);
}

export const adminApiClient = new AdminApiClient();