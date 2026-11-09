providers = [
    {
        "name":"Mollie",
        "card_percent": 1.8,
        "card_fixed": 0.25,
        "monthly_fee": 0,
        "split_payments": True,
        "sepa": True,
        "integration": "einfach",
    },
    {
        "name": "Stripe",
        "card_percent": 1.5,
        "card_fixed": 0.25,
        "monthly_fee": 0,
        "split_payments": True,
        "sepa": True,
        "integration": "mittel",
    },
]

number_of_orders_per_categorie = 500
price_of_cat_1 = 36
price_of_cat_2 = 26

revenue = number_of_orders_per_categorie * (price_of_cat_1 + price_of_cat_2)

print(f"Bei ausverkaufter Halle machen {revenue} € Umsatz")

for provider in providers:
    fees = (
        revenue * provider["card_percent"] / 100
        + number_of_orders_per_categorie * 2 * provider["card_fixed"]
    )

    print(provider["name"])
    print("-" * len(provider["name"]))
    print(f"Geschätzte Gebühren: {fees:.2f} €")
    print(f"Split Payments: {'Ja' if provider['split_payments'] else 'Nein'}")
    print(f"SEPA: {'Ja' if provider['sepa'] else 'Nein'}")
    print(f"Integrationsaufwand: {provider['integration']}")
    print(f"Übrig bleibt für uns: {revenue - fees} €")
    print()
