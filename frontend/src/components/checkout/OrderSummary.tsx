import type { TicketCategory } from '../../api/ticketCategoryApi';

type OrderSummaryProps = {
    ticketCategories: TicketCategory[];
    quantities: Record<number, number>;
};

function OrderSummary({
                          ticketCategories,
                          quantities,
                      }: OrderSummaryProps) {
    const currencyFormatter = new Intl.NumberFormat('de-DE', {
        style: 'currency',
        currency: 'EUR',
    });

    const selectedCategories = ticketCategories.filter(
        (category) => (quantities[category.id] ?? 0) > 0,
    );

    const totalQuantity = selectedCategories.reduce(
        (sum, category) =>
            sum + (quantities[category.id] ?? 0),
        0,
    );

    const totalPrice = selectedCategories.reduce(
        (sum, category) =>
            sum +
            category.price *
            (quantities[category.id] ?? 0),
        0,
    );

    return (
        <section>
            <h3>Deine Tickets</h3>

            {selectedCategories.map((category) => {
                const quantity = quantities[category.id] ?? 0;

                return (
                    <div key={category.id}>
                        <p>
                            <strong>{category.name}</strong>
                        </p>

                        <p>
                            {quantity} ×{' '}
                            {currencyFormatter.format(category.price)}
                        </p>

                        <p>
                            {currencyFormatter.format(
                                category.price * quantity,
                            )}
                        </p>
                    </div>
                );
            })}

            <p>
                <strong>Tickets:</strong> {totalQuantity}
            </p>

            <p>
                <strong>Gesamt:</strong>{' '}
                {currencyFormatter.format(totalPrice)}
            </p>
        </section>
    );
}

export default OrderSummary;