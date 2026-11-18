import {throwApiError} from './apiError.ts';
import type {BankTransferInstructions} from
        '../components/BankTransferInstructions.tsx';

export async function startBankTransfer(
    concertId: number,
    orderId: number,
    accessToken: string,
): Promise<BankTransferInstructions> {
    const response = await fetch(
        `/api/concerts/${concertId}/orders/${orderId}/bank-transfer`,
        {
            method: 'POST',
            headers: {
                'X-Order-Access-Token': accessToken,
            },
        },
    );

    if (!response.ok) {
        await throwApiError(
            response,
            'Banküberweisung konnte nicht gestartet werden.',
        );
    }

    return response.json();
}

export async function getBankTransferInstructions(
    publicId: string,
    accessToken: string,
): Promise<BankTransferInstructions> {
    const response = await fetch(
        `/api/orders/${encodeURIComponent(publicId)}/bank-transfer`,
        {
            headers: {
                'X-Order-Access-Token': accessToken,
            },
        },
    );

    if (!response.ok) {
        await throwApiError(
            response,
            'Überweisungsdaten konnten nicht geladen werden.',
        );
    }

    return response.json();
}