const loadAdminConcertsButton = document.getElementById("loadAdminConcertsButton");
const loadOrdersButton = document.getElementById("loadOrdersButton");
const showWebhookBodyButton = document.getElementById("showWebhookBodyButton");

const adminConcertsContainer = document.getElementById("adminConcerts");
const ordersContainer = document.getElementById("orders");

const adminErrorSection = document.getElementById("adminErrorSection");
const adminErrorMessage = document.getElementById("adminErrorMessage");

const createConcertButton = document.getElementById("createConcertButton");
const addTicketCategoryButton = document.getElementById("addTicketCategoryButton");
const newTicketCategoriesContainer = document.getElementById("newTicketCategories");

createConcertButton.addEventListener("click", createConcertWithTicketCategories);
addTicketCategoryButton.addEventListener("click", addTicketCategoryForm);

loadAdminConcertsButton.addEventListener("click", loadAdminConcerts);
loadOrdersButton.addEventListener("click", loadOrders);
showWebhookBodyButton.addEventListener("click", showWebhookBody);

function getAuthHeader() {
    const username = document.getElementById("adminUsername").value;
    const password = document.getElementById("adminPassword").value;

    return "Basic " + btoa(username + ":" + password);
}

async function loadAdminConcerts() {
    hideAdminError();

    try {
        const response = await fetch("/api/admin/concerts", {
            headers: {
                "Authorization": getAuthHeader()
            }
        });

        if (!response.ok) {
            throw new Error("Admin-Konzerte konnten nicht geladen werden.");
        }

        const concerts = await response.json();

        adminConcertsContainer.innerHTML = "";

        if (concerts.length === 0) {
            adminConcertsContainer.innerHTML = "<p>Keine Konzerte vorhanden.</p>";
            return;
        }

        concerts.forEach(concert => {
            const element = document.createElement("div");
            element.className = "item";

            element.innerHTML = `
                <h3>${concert.title}</h3>
                <p><strong>ID:</strong> ${concert.id}</p>
                <p><strong>Ort:</strong> ${concert.location}</p>
                <p><strong>Beginn:</strong> ${formatDate(concert.startTime)}</p>    <p><strong>Status:</strong> ${formatConcertStatus(concert)}</p>

                <button type="button" class="publishConcertButton">
                    Veröffentlichen
                </button>
            `;

            const publishButton = element.querySelector(".publishConcertButton");
            publishButton.addEventListener("click", () => publishConcert(concert.id));

            adminConcertsContainer.appendChild(element);
        });
    } catch (error) {
        showAdminError(error.message);
    }
}

function addTicketCategoryForm() {
    const element = document.createElement("div");
    element.className = "ticket-category-form";

    element.innerHTML = `
        <label>
            Name
            <input class="ticketCategoryName" placeholder="z. B. Ermäßigt">
        </label>

        <label>
            Preis
            <input class="ticketCategoryPrice" type="number" step="0.01" min="0" placeholder="15.00">
        </label>

        <label>
            Kapazität
            <input class="ticketCategoryCapacity" type="number" min="1" placeholder="50">
        </label>
    `;

    newTicketCategoriesContainer.appendChild(element);
}

async function createConcertWithTicketCategories() {
    hideAdminError();

    try {
        const title = document.getElementById("newConcertTitle").value;
        const description = document.getElementById("newConcertDescription").value;
        const location = document.getElementById("newConcertLocation").value;
        const startTime = document.getElementById("newConcertStartTime").value;

        if (!title || !description || !location || !startTime) {
            throw new Error("Bitte alle Konzertdaten ausfüllen.");
        }

        const ticketCategories = readTicketCategoryForms();

        if (ticketCategories.length === 0) {
            throw new Error("Bitte mindestens eine Ticketkategorie anlegen.");
        }

        const concertResponse = await fetch("/api/admin/concerts", {
            method: "POST",
            headers: {
                "Authorization": getAuthHeader(),
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                title: title,
                description: description,
                startTime: startTime,
                location: location
            })
        });

        if (!concertResponse.ok) {
            throw new Error("Konzert konnte nicht erstellt werden.");
        }

        const createdConcert = await concertResponse.json();

        for (const category of ticketCategories) {
            const categoryResponse = await fetch(`/api/admin/concerts/${createdConcert.id}/ticket-categories`, {
                method: "POST",
                headers: {
                    "Authorization": getAuthHeader(),
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(category)
            });

            if (!categoryResponse.ok) {
                throw new Error("Ticketkategorie konnte nicht erstellt werden.");
            }
        }

        alert("Konzert und Ticketkategorien wurden erstellt.");

        await loadAdminConcerts();

    } catch (error) {
        showAdminError(error.message);
    }
}

function readTicketCategoryForms() {
    const forms = document.querySelectorAll(".ticket-category-form");

    const categories = [];

    forms.forEach(form => {
        const name = form.querySelector(".ticketCategoryName").value;
        const price = form.querySelector(".ticketCategoryPrice").value;
        const capacity = form.querySelector(".ticketCategoryCapacity").value;

        if (!name && !price && !capacity) {
            return;
        }

        if (!name || !price || !capacity) {
            throw new Error("Bitte alle Felder jeder Ticketkategorie ausfüllen.");
        }

        categories.push({
            name: name,
            price: Number(price),
            capacity: Number(capacity)
        });
    });

    return categories;
}

async function loadOrders() {
    hideAdminError();

    const concertId = document.getElementById("ordersConcertId").value;

    if (!concertId) {
        showAdminError("Bitte eine Konzert-ID eingeben.");
        return;
    }

    try {
        const response = await fetch(`/api/admin/concerts/${concertId}/orders`, {
            headers: {
                "Authorization": getAuthHeader()
            }
        });

        if (!response.ok) {
            throw new Error("Bestellungen konnten nicht geladen werden.");
        }

        const orders = await response.json();

        ordersContainer.innerHTML = "";

        if (orders.length === 0) {
            ordersContainer.innerHTML = "<p>Für dieses Konzert gibt es noch keine Bestellungen.</p>";
            return;
        }

        orders.forEach(order => {
            const element = document.createElement("div");
            element.className = "item";

            element.innerHTML = `
                <h3>Bestellung #${order.id}</h3>
                <p><strong>Kunde:</strong> ${order.customerEmail}</p>
                <p><strong>Status:</strong> ${order.status}</p>
                <p><strong>Erstellt:</strong> ${formatDate(order.createdAt)}</p>
            `;

            ordersContainer.appendChild(element);
        });
    } catch (error) {
        showAdminError(error.message);
    }
}

function showWebhookBody() {
    hideAdminError();

    const orderId = document.getElementById("paymentOrderId").value;

    if (!orderId) {
        showAdminError("Bitte eine Order-ID eingeben.");
        return;
    }

    const eventId = "evt_demo_" + Date.now();

    const body = {
        eventId: eventId,
        orderId: Number(orderId),
        status: "PAID"
    };

    document.getElementById("webhookBody").textContent = JSON.stringify(body);
    document.getElementById("webhookBox").classList.remove("hidden");
}

function showAdminError(message) {
    adminErrorMessage.textContent = message;
    adminErrorSection.classList.remove("hidden");
}

function hideAdminError() {
    adminErrorSection.classList.add("hidden");
    adminErrorMessage.textContent = "";
}

function formatDate(value) {
    if (!value) {
        return "unbekannt";
    }

    return new Date(value).toLocaleString("de-DE");
}

async function publishConcert(concertId) {
    hideAdminError();

    try {
        const response = await fetch(`/api/admin/concerts/${concertId}/publish`, {
            method: "PATCH",
            headers: {
                "Authorization": getAuthHeader()
            }
        });

        if (!response.ok) {
            throw new Error("Konzert konnte nicht veröffentlicht werden.");
        }

        await loadAdminConcerts();

        alert("Konzert wurde veröffentlicht.");
    } catch (error) {
        showAdminError(error.message);
    }
}

function formatConcertStatus(concert) {
    if (concert.status) {
        return concert.status;
    }

    if (concert.published === true) {
        return "Veröffentlicht";
    }

    if (concert.published === false) {
        return "Entwurf";
    }

    return "unbekannt";
}