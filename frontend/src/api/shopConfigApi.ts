export type ShopConfig = {
    maxTicketsPerOrder: number;
};

export async function getShopConfig(): Promise<ShopConfig> {
    const response = await fetch('/api/shop-config');

    if (!response.ok) {
        throw new Error(
            'Shop-Konfiguration konnte nicht geladen werden.',
        );
    }

    return response.json();
}