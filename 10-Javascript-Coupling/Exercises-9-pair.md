# Exercise 1: Build each side of a Cafe Order Board from a shared JSON structure

<br>

We begin with hardcoded JavaScript data.

Later we replace the hardcoded data and instead `fetch` it from our Spring backend.

<br>

This exercise was originally done in-class in pairs where one person worked on the frontend and another made the backend.

**But if you are making it at home you can just make both parts of the Exercises.** 

The two exercises are made to follow each other, so fast finish part 1 and then do part 2.

<br>

**A key understanding today is that the frontend arrays and the JSON from the backend uses the same structure, how this is mapped, and what issues can arise in the coupling.**

<br>

## Starter

<br>

Copy below into `index.html`

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cafe Order Board</title>
    <style>
        body {
            font-family: sans-serif;
            padding: 20px;
            background: #f4f4f4;
            margin: 0;
        }

        h1, h2 {
            margin-top: 0;
        }

        .page {
            max-width: 900px;
            margin: 0 auto;
        }

        form {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            margin-bottom: 20px;
        }

        input, select, button {
            padding: 8px 10px;
            font-size: 16px;
        }

        #ordersContainer {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .order-card {
            background: white;
            padding: 14px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .order-card p {
            margin: 6px 0;
        }

        .order-card select,
        .order-card button {
            margin-top: 8px;
            margin-right: 8px;
        }
    </style>
</head>
<body>
    <div class="page">
        <h1>Cafe Order Board</h1>

        <form id="orderForm">
            <input
                type="text"
                id="customerNameInput"
                placeholder="Kundenavn"
                required
            >

            <select id="menuItemSelect"></select>

            <button type="submit">Tilføj bestilling</button>
        </form>
        <div id="ordersContainer"></div>
    </div>

    <script src="app.js"></script>
</body>
</html>
```

<br>

We grab these elements from the HTML page:

- `#orderForm`
- `#customerNameInput`
- `#menuItemSelect`
- `#ordersContainer`

<br>

Copy below into `app.js`

```js
const menuItems = [
  { id: 1, name: "Latte", category: "Drikkevarer", price: 45.0 },
  { id: 2, name: "Croissant", category: "Bagværk", price: 28.0 },
  { id: 3, name: "Espresso", category: "Drikkevarer", price: 32.0 },
  { id: 4, name: "Te", category: "Drikkevarer", price: 30.0 }
];

const orders = [
  {
    id: 1,
    customerName: "Maja",
    menuItemId: 1,
    menuItemName: "Latte",
    status: "NEW"
  },
  {
    id: 2,
    customerName: "Ali",
    menuItemId: 2,
    menuItemName: "Croissant",
    status: "READY"
  }
];

const statuses = ["NEW", "PREPARING", "READY"];
// We use these later on

function renderMenuOptions() {
  const menuItemSelect = document.querySelector("#menuItemSelect");
  menuItemSelect.innerHTML = "";

  for (const menuItem of menuItems) {
    const option = document.createElement("option");
    option.value = menuItem.id;
    option.textContent = `${menuItem.name} - ${menuItem.price} kr.`;
    menuItemSelect.appendChild(option);
  }
}
```

<br>

If you are the **frontend responsible**, you work mainly in `index.html` and `app.js`.

If you are the **backend responsible**, open the Spring starter from GitHub and work there.

<br>

In the Spring starter(https://github.com/ek-kiil/CafeStarter), the model classes, `MenuItemResponse` and the services are already there.

The services already contain the starter data for:

- menu items
- orders

That means the **backend responsible** can focus on the controller, `OrderResponse`, and the JSON that comes back from the backend.

<br>

------

## 1. Frontend: Render the current orders

<br>

Write a function called `renderOrders()`.

Start by finding the container where the orders should be shown, and clear any old content from it.

Then loop through the `orders` array. For each order, create a new element for the order card and show:

- customer name
- bestilling
- pris
- status

The `orders` array does not include the price, so you will need to look up the matching menu item in the `menuItems` array by using `menuItemId`.

If `.find()` does not find a matching menu item, it returns `undefined`. As we can not assume `matchingMenuItem.price` always exists, try to guard against this so one bad order does not break the render.

When the card is ready, append it to the orders container.

Try to build the card with `createElement()` and `textContent`, so you stay in control of exactly what gets inserted into the page.

<br>

You will call the render functions at the bottom of `app.js` later in task 4.

<br>

<details>
  <summary><b>Hint</b></summary>

You need one card per order.

Some values can be taken directly from the `order` object, but the price has to be found somewhere else first.

Also remember that `.find()` can return `undefined`, so do not read `.price` unless you know there is a matching menu item.

</details>

<details>
  <summary><b>Extra hint</b></summary>

You can use `.find()` to get the menu item that belongs to the order.

It will look something like this:

```js
const matchingMenuItem = menuItems.find(menuItem => menuItem.id === order.menuItemId);
```

Then you can use `matchingMenuItem.price` when you create the text for the card, if there is a match.

If there is no matching menu item, you can for example show `Pris: Ukendt` instead.

</details>

<details>
  <summary><b>Extra extra hint</b></summary>

A good structure is:

- clear the orders container
- loop through `orders`
- find the matching menu item
- create a card `<div>`
- create a few elements with `createElement()`
- set their `textContent`
- append them to the card
- append the card to the container

It could begin like this:

```js
function renderOrders() {
    const ordersContainer = document.querySelector("#ordersContainer");
    ordersContainer.innerHTML = "";

    for (const order of orders) {
        const matchingMenuItem = menuItems.find(menuItem => menuItem.id === order.menuItemId);

        const orderCard = document.createElement("div");
        orderCard.classList.add("order-card");

        const kunde = document.createElement("h3");
        kunde.textContent = order.customerName;

        // TODO: Continue making the rest of the card

        // TODO: Append each element (e.g. kunde) to the card, and append the whole card to the ordersContainer
    }
}
```

</details>

<details>
  <summary><b>Extra extra extra hint</b></summary>

We are almost ready, append the card to the container to make the cards appear on the page:

```js
function renderOrders() {
    const ordersContainer = document.querySelector("#ordersContainer");
    ordersContainer.innerHTML = "";

    for (const order of orders) {
        const matchingMenuItem = menuItems.find(menuItem => menuItem.id === order.menuItemId);

        const orderCard = document.createElement("div");
        orderCard.classList.add("order-card");

        const kunde = document.createElement("h3");
        kunde.textContent = order.customerName;

        const bestilling = document.createElement("p");
        bestilling.textContent = `Bestilling: ${order.menuItemName}`;

        const pris = document.createElement("p");
        if (matchingMenuItem) {
            pris.textContent = `Pris: ${matchingMenuItem.price} kr.`;
        } else {
            pris.textContent = "Pris: Ukendt";
        }

        const statusTekst = document.createElement("p");
        statusTekst.textContent = `Status: ${order.status}`;

        orderCard.appendChild(kunde);
        orderCard.appendChild(bestilling);
        orderCard.appendChild(pris);
        orderCard.appendChild(statusTekst);

        // TODO: append the card to ordersContainer
    }
}
```

</details>

<br>

------

## 2. Backend: create `OrderResponse` to match the frontend orders array

<br>

In the Spring starter, `MenuItemResponse` is already there.

Now create `OrderResponse` so it matches the structure that the frontend uses in `app.js`.

That means the field names should be the same as the ones in the frontend orders array.

`MenuItemResponse` is already provided for you. The job here is to make `OrderResponse`.

<br>

<details>
  <summary><b>Hint</b></summary>

Open `app.js` and look at the shape of one `order`.

`OrderResponse` should contain the same fields.

</details>

<details>
  <summary><b>Extra hint</b></summary>

`OrderResponse` should match:

```js
{
  id: 1,
  customerName: "Maja",
  menuItemId: 1,
  menuItemName: "Latte",
  status: "NEW"
}
```

</details>

<details>
  <summary><b>Extra extra hint (Try for a bit first, then ask your frontend partner what they expect)</b></summary>


It could look like this:

```java
public record OrderResponse(
        Long id,
        String customerName,
        Long menuItemId,
        String menuItemName,
        String status
) {
}
```

</details>

<br>

------

## 3. Backend: add two `GET` mappings that return the DTOs

<br>

Make one controller that returns the same structure as the arrays in `app.js`.

We want these endpoints:

- `GET /api/menu-items`
- `GET /api/orders`

<br>

The controller should:

- use `@RestController`
- listen on `/api`
- inject the services via constructor
- use the menu item service from the starter
- use the order service from the starter
- map the models to the response DTOs
- return the DTOs from the controller

<br>

<details>
  <summary><b>Hint</b></summary>

You do not need to make the lists yourself. They are already in the services from the starter.

One controller can have more than one `@GetMapping`. In larger programs we split up the controller but for this exercise its ok to put the mappings in one.

If the controller listens on `/api`, then the methods can listen on `/menu-items` and `/orders`.

The final piece is that the controller should return `MenuItemResponse` for menu items and your new `OrderResponse` for orders, not the model objects directly.

</details>

<details>
  <summary><b>Extra hint</b></summary>

The controller will need:

- `@RestController`
- `@RequestMapping("/api")`
- a private final `MenuItemService`
- a private final `OrderService`
- a constructor injecting both services
- one method with `@GetMapping("/menu-items")`
- one method with `@GetMapping("/orders")`
- mapping from `MenuItem` to `MenuItemResponse`
- mapping from `Order` to `OrderResponse`

</details>

<details>
  <summary><b>Extra extra hint (Try for a bit first, then ask your frontend partner for help)</b></summary>


This is almost the full controller. The missing part is mapping the lists from the services:

```java
@RestController
@RequestMapping("/api")
public class CafeController {

    private final MenuItemService menuItemService;
    private final OrderService orderService;

    public CafeController(MenuItemService menuItemService, OrderService orderService) {
        this.menuItemService = menuItemService;
        this.orderService = orderService;
    }

    @GetMapping("/menu-items")
    public List<MenuItemResponse> getMenuItems() {
        // TODO: return the menu items from the service mapped to MenuItemResponse
    }

    @GetMapping("/orders")
    public List<OrderResponse> getOrders() {
        // TODO: return the orders from the service mapped to OrderResponse
    }
}
```

We have not worked with streams yet, so a plain loop is fine here.

For example, one method could look like this:

```java
@GetMapping("/menu-items")
public List<MenuItemResponse> getMenuItems() {
    List<MenuItemResponse> responses = new ArrayList<>();

    for (MenuItem menuItem : menuItemService.findAll()) {
        responses.add(new MenuItemResponse(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getCategory(),
                menuItem.getPrice()
        ));
    }

    return responses;
}
```

Do the same kind of mapping for `orders`.

</details>

<br>

------

## 4. Frontend: make the page work when it loads

<br>

When the page opens, the dropdown and the current orders should already be shown.

Call the functions you need at the bottom of your script.

<details>
  <summary><b>Hint</b></summary>

`renderMenuOptions()` is already in the starter.

You just need to make sure both render functions are called once when the script runs.

</details>

<br>

------

## 5. Meet up: compare the frontend arrays with the backend JSON and check that both sides work

<br>

Sit together and compare the two sides.

In the backend view the JSON from:

- `/api/menu-items`
- `/api/orders`

The frontend should compare that JSON with the hardcoded arrays in `app.js`.

<br>

At the same time, check that both sides work on their own.

The frontend should:

- show the menu items in the dropdown
- show the current orders
- show the correct price for each order

The backend should:

- return menu items from `/api/menu-items`
- return orders from `/api/orders`
- return JSON that matches the frontend structure

<br>

------

## 6. Discussion

<br>

Discuss with your partner the objects we have been working with.

- What is our stable id today? Where would this usually come from in an application?
- Why do we have both `menuItemId` and `menuItemName` in an order?
- Which parts of your code now depend on the exact field names in the objects?
- Why is it useful to compare the backend JSON with the frontend arrays before starting with `fetch()`?
- In Task 1 it was suggested to use `textContent` instead of using e.g. `innerHTML`, can you think of why one of them might be safer to use than the other?

<br>

------
