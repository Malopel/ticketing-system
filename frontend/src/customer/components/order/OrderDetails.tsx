import type {OrderResponse} from '../../api/orderApi.ts';

type OrderDetailsProps = {
    order: OrderResponse;
};

function OrderDetails({
                          order,
                      }: OrderDetailsProps) {
    const currencyFormatter =
        new Intl.NumberFormat(
            'de-DE',
            {
                style: 'currency',
                currency: 'EUR',
            },
        );

    return (
        <section className="order-details">
            <h2>Deine Bestellung</h2>

            <div className="order-items">
                {order.items.map((item) => (
                    <div
                        key={item.id}
                        className="order-item"
                    >
                        <div>
                            <strong>
                                {item.ticketCategoryName}
                            </strong>

                            <span>
                                {item.quantity} ×{' '}
                                {currencyFormatter.format(
                                    item.unitPrice,
                                )}
                            </span>
                        </div>

                        <strong>
                            {currencyFormatter.format(
                                item.totalPrice,
                            )}
                        </strong>
                    </div>
                ))}
            </div>

            <div className="status-order-total">
                <span>Gesamt</span>

                <strong>
                    {currencyFormatter.format(
                        order.totalAmount,
                    )}
                </strong>
            </div>
        </section>
    );
}

export default OrderDetails;