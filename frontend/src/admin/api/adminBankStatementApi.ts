import {adminApiClient} from './adminApiClient';

export type CamtImportItemStatus =
    | 'PAID'
    | 'ALREADY_PAID'
    | 'NO_ORDER_REFERENCE'
    | 'ORDER_NOT_FOUND'
    | 'NON_EUR'
    | 'AMOUNT_MISMATCH'
    | 'WRONG_PAYMENT_METHOD'
    | 'ORDER_NOT_PENDING'
    | 'ORDER_EXPIRED'
    | 'PAYMENT_REJECTED';

export type CamtImportItemResult = {
    bankReference: string | null;
    orderReference: string | null;
    amount: number;
    status: CamtImportItemStatus;
    message: string;
};

export type CamtImportResult = {
    transactionsFound: number;
    paid: number;
    reviewRequired: number;
    items: CamtImportItemResult[];
};

export async function importCamtStatement(
    file: File,
): Promise<CamtImportResult> {
    const formData = new FormData();
    formData.append('file', file);

    return adminApiClient.request<CamtImportResult>(
        '/api/admin/bank-statements/camt',
        {
            method: 'POST',
            body: formData,
        },
    );
}