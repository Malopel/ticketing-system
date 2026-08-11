export type Concert = {
    id: number;
    title: string;
    description: string;
    startTime: string;
    location: string;
    status: 'PUBLISHED';
};

export async function getConcerts(): Promise<Concert[]> {
    const response = await fetch('/api/concerts');

    if (!response.ok) {
        throw new Error('Konzerte konnten nicht geladen werden.');
    }

    return response.json();
}

export async function getConcertById(concertId: number): Promise<Concert> {
    const response = await fetch(`/api/concerts/${concertId}`);

    if (!response.ok) {
        throw new Error('Konzert konnte nicht geladen werden.');
    }

    return response.json();
}