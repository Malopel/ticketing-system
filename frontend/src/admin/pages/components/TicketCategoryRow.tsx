import type {
    AdminTicketCategory,
} from '../../api/adminTicketCategoryApi';

type TicketCategoryRowProps = {
    category: AdminTicketCategory;
    editable: boolean;
    onEdit: () => void;
};

function TicketCategoryRow({
                               category,
                               editable,
                               onEdit,
                           }: TicketCategoryRowProps) {
    const formattedPrice =
        new Intl.NumberFormat('de-DE', {
            style: 'currency',
            currency: 'EUR',
        }).format(category.price);

    return (
        <article className="admin-ticket-category-row">
            <div>
                <h3>{category.name}</h3>

                <p>
                    {formattedPrice}
                    {' · '}
                    {category.available} von{' '}
                    {category.capacity} verfügbar
                </p>
            </div>

            {editable && (
                <button
                    type="button"
                    className="admin-secondary-button"
                    onClick={onEdit}
                >
                    Bearbeiten
                </button>
            )}
        </article>
    );
}

export default TicketCategoryRow;