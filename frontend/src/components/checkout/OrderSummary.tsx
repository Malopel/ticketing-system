import type {TicketCategory} from '../../api/ticketCategoryApi';

type OrderSummaryProps = {
    ticketCategories: TicketCategory[];
    quantities: Record<number, number>;
};

function OrderSummary({
                          ticketCategories,
                          quantities,
                      }: OrderSummaryProps) {
    const currencyFormatter = new Intl.NumberFormat(
        'de-DE',
        {
            style: 'currency',
            currency: 'EUR',
        },
    );

    const selectedCategories = ticketCategories.filter(
        (category) =>
            (quantities[category.id] ?? 0) > 0,
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
        <section className="order-summary">
            <h2>Deine Tickets</h2>

            <div className="order-items">
                {selectedCategories.map((category) => {
                    const quantity =
                        quantities[category.id] ?? 0;

                    return (
                        <div
                            key={category.id}
                            className="order-item"
                        >
                            <div>
                                <strong>
                                    {category.name}
                                </strong>

                                <span>
                                    {quantity} ×{' '}
                                    {currencyFormatter.format(
                                        category.price,
                                    )}
                                </span>
                            </div>

                            <strong>
                                {currencyFormatter.format(
                                    category.price *
                                    quantity,
                                )}
                            </strong>
                        </div>
                    );
                })}
            </div>

            <div className="order-total">
                <span>
                    {totalQuantity}{' '}
                    {totalQuantity === 1
                        ? 'Ticket'
                        : 'Tickets'}
                </span>

                <div>
                    <span>Gesamt</span>

                    <strong>
                        {currencyFormatter.format(
                            totalPrice,
                        )}
                    </strong>
                </div>
            </div>
        </section>
    );
}

export default OrderSummary;