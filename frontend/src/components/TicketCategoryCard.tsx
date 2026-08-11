import type {TicketCategory} from '../api/ticketCategoryApi';

type TicketCategoryCardProps = {
    category: TicketCategory;
    quantity: number;
    onQuantityChange: (categoryId: number, quantity: number) => void;
};

function TicketCategoryCard({
                                category,
                                quantity,
                                onQuantityChange,
                            }: TicketCategoryCardProps) {
    function handleDecrease() {
        if (quantity > 0) {
            onQuantityChange(category.id, quantity - 1)
        }
    }

    function handleIncrease() {
        if (quantity < category.available) {
            onQuantityChange(category.id, quantity + 1)
        }
    }

    return (
        <article>
            <h4>{category.name}</h4>

            <p>
                Preis: {category.price.toFixed(2)} €
            </p>

            <p>
                Verfügbar: {category.available}
            </p>

            <button type="button"
                    onClick={handleDecrease}
                    disabled={quantity === 0}>
                -
            </button>

            <span>{quantity}</span>

            <button type="button"
                    onClick={handleIncrease}
                    disabled={quantity >= category.available}>
                +
            </button>
        </article>
    );
}

export default TicketCategoryCard;