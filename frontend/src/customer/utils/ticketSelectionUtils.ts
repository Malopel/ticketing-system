import type {TicketCategory} from "../api/ticketCategoryApi.ts";

export function clampQuantitiesToAvailability(
    storedQuantities: Record<number, number>,
    ticketCategories: TicketCategory[]
): Record<number,number> {
    return Object.fromEntries(
        ticketCategories.map((category) => {
            const storedQuantity =
                storedQuantities[category.id] ?? 0;

            return [
                category.id,
                Math.min(storedQuantity, category.available),
            ];
        }),
    );
}