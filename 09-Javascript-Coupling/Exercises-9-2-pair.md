
# Exercise 2: Connect the two sides of our Order Board

<br>

Now we take the Order Board from Exercise 1 and replace the hardcoded arrays with requests to a Spring backend.

<br>

From Exercise 1, the backend should have:

- response DTOs for menu items and orders
- one controller with the two `GET` endpoints

<br>

## API contract

<br>

The backend should expose these endpoints in the main part of the exercise:

- `GET /api/menu-items`
- `GET /api/orders`
- `POST /api/orders`

<br>

------

## 1. Prepare the two sides for integration

<br>

The backend should run on `http://localhost:8080`.

The frontend should run on another origin, for example `http://localhost:5500`.

<br>

**The frontend responsible** should change the local arrays at the top of `app.js` so they are ready to be replaced by fetched data.

That means `menuItems` and `orders` should start out as empty arrays, while `statuses` can stay as it is for later extension work.

We keep the render functions from Exercise 1.

<br>

**The backend responsible** should add `@CrossOrigin(...)` to the controller from Exercise 1.

The controller should allow the frontend origin, for example `http://localhost:5500`.

<br>

<details>
  <summary><b>Hint</b></summary>


The frontend could start like this:

```js
let menuItems = [];
let orders = [];
const statuses = ["NEW", "PREPARING", "READY"];
```

The controller needs:

```java
@CrossOrigin(origins = "http://localhost:5500")
```

</details>

<br>

------

## 2. Pair Programming: load menu items and orders with `fetch()`

<br>

Now we connect the frontend to the backend.

You both work together doing the Extreme activity of Pair Programming - to talk about the way we fetch the data, and because I accidentally made too much frontend work ;)

<br>

Write functions to fetch:

- menu items from `/api/menu-items`
- orders from `/api/orders`

Store the returned data in `menuItems` and `orders`, then call your render functions.

<br>

It is a good idea to make a function called `loadAllData()`.

That function should:

1. load the menu items
2. load the orders
3. render the menu dropdown
4. render the orders on the page

<br>

Talk through the flow while you make it:

- which endpoint is called
- what JSON comes back
- where it is saved in JavaScript
- when the render functions should run

<br>

<details>
  <summary><b>Hint</b></summary>

Start with only one endpoint.

For example, make one async function that loads the menu items from the backend, saves the result in `menuItems`, and then checks whether `renderMenuOptions()` still works.

When that works, do the same for `orders`.

</details>

<details>
  <summary><b>Extra hint</b></summary>

It will be easier if you split the work into three functions:

```javascript
async function loadMenuItems() {}
async function loadOrders() {}
async function loadAllData() {}
```

Inside `loadMenuItems()` and `loadOrders()` you should:

- call `fetch(...)`
- wait for the response
- convert it to JSON
- save the result in the correct variable

Then `loadAllData()` should:

- call `loadMenuItems()`
- call `loadOrders()`
- call the render functions

</details>

<details>
  <summary><b>Extra extra hint (Discuss it a bit before clicking :)</b></summary>
We are close to fetching the data from the backend, we just need to replace the 'old' render calls:

```javascript
async function loadMenuItems() {
    const response = await fetch("http://localhost:8080/api/menu-items");
    menuItems = await response.json();
}

async function loadOrders() {
    const response = await fetch("http://localhost:8080/api/orders");
    orders = await response.json();
}

async function loadAllData() {
    await loadMenuItems();
    await loadOrders();

    renderMenuOptions();
    renderOrders();
}

// TODO: The two render calls are now inside loadAllData(), so the ones you made at the end of app.js should now be replaced with calling loadAllData()
```

</details>

<br>

------

## 3. Backend: add `POST /api/orders`

<br>

For the rest of todays exercise you are encouraged to do pair programming - as you then talk through some of the difficult parts of the exercise. You can also split up again and meet in task 5.

<br>

Now add the backend part of creating an order.

Use the controller from Exercise 1 and make it support `POST /api/orders`.

To do that, you need:

- a `CreateOrderRequest` record for the request body
- a `createOrder(...)` method in `OrderService`
- a controller method that returns the created order as `OrderResponse`

Inside `createOrder(...)`, find the chosen menu item from `menuItemId`, create a new order, set `status` to `NEW`, add it to the list, and return it.

Because this exercise does not use a database, the service also needs a simple way to make the next id.

<br>

<details>
  <summary><b>Hint</b></summary>

Start in `OrderService`.

Add `createOrder(...)` and get that working first.

For that method, `OrderService` needs access to `MenuItemService`, so add a `MenuItemService` field and receive it in the constructor.

Then add a simple `nextOrderId`, create the new order, and return it.

When the service method works, add `CreateOrderRequest`, and then wire it into the controller with `@PostMapping("/orders")` and `@RequestBody`.

</details>

<details>
  <summary><b>Extra hint</b></summary>

Put these three parts in three different places:

- `CreateOrderRequest` should be its own record
- `nextOrderId` and `createOrder(...)` should go in `OrderService`
- `@PostMapping("/orders")` should go in your current controller

The request DTO can look like this:

```java
public record CreateOrderRequest(
        String customerName,
        Long menuItemId
) {
}
```

The service method needs to do the actual creation work:

```java
public Order createOrder(CreateOrderRequest request) {
    // TODO: find the chosen menu item from request.menuItemId()
    // TODO: create a new Order with a new id
    // TODO: copy customerName, menuItemId, and menuItemName
    // TODO: set status to "NEW"
    // TODO: add the order to the list and return it
}
```

We need a simple way to make the next id, for example:

```java
private Long nextOrderId = 3L;
```

Then the controller method should receive the request, call the service, and return an `OrderResponse`:

```java
@PostMapping("/orders")
public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
    // TODO: call the service
    // TODO: map the created order to OrderResponse
}
```

</details>

<details>
  <summary><b>Extra extra hint (Try for a bit and discuss with your partner:)</b></summary>


This is almost the full backend flow.

Put the record in its own file:

```java
public record CreateOrderRequest(
        String customerName,
        Long menuItemId
) {
}
```

Put this field and method in `OrderService`:

```java
private Long nextOrderId = 3L;

public Order createOrder(CreateOrderRequest request) {
    MenuItem selectedMenuItem = null;

    for (MenuItem menuItem : menuItemService.findAll()) {
        if (menuItem.getId().equals(request.menuItemId())) {
            selectedMenuItem = menuItem;
            break;
        }
    }

    if (selectedMenuItem == null) {
        throw new IllegalArgumentException("Unknown menu item id");
    }

    Order newOrder = new Order(
            nextOrderId,
            request.customerName(),
            request.menuItemId(),
            selectedMenuItem.getName(),
            "NEW"
    );

    nextOrderId++;

    // TODO: add the new order to the list

    return newOrder;
}
```

Put this in your current controller:

```java
@PostMapping("/orders")
public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
    Order createdOrder = orderService.createOrder(request);

    // TODO: map createdOrder to OrderResponse and return it
}
```

</details>

<br>

------

## 4. Frontend: send a new order with `POST`

<br>

Now we build the frontend part of the `POST` flow.

Connect the form to the backend so a new order is sent when the user submits it. Read the values from the form, send them to `/api/orders`, and then load the fresh data again so the new order appears on the board.

<br>

<details>
  <summary><b>Hint</b></summary>

Start with the form and work top to bottom.

- select `#orderForm`
- add a `submit` listener
- stop the reload with `event.preventDefault()`
- read `#customerNameInput` and `#menuItemSelect`
- convert the selected id to a number
- send a `POST` request to `/api/orders`
- call `loadAllData()` afterwards

</details>

<details>
  <summary><b>Extra hint</b></summary>

This could be the structure, fill in the missing parts:

```js
const orderForm = document.querySelector("#orderForm");

orderForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    // TODO: select the input and the dropdown
    // TODO: read customerName and menuItemId

    await fetch("http://localhost:8080/api/orders", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            // TODO: send customerName and menuItemId
        })
    });

    // TODO: call loadAllData()
});
```

</details>

<details>
  <summary><b>Extra extra hint (Try for a bit and discuss with your partner)</b></summary>


Now we are close, make sure to load the data again:

```js
const orderForm = document.querySelector("#orderForm");

orderForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const customerNameInput = document.querySelector("#customerNameInput");
    const menuItemSelect = document.querySelector("#menuItemSelect");

    const customerName = customerNameInput.value.trim();
    const menuItemId = Number(menuItemSelect.value);

    await fetch("http://localhost:8080/api/orders", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            customerName: customerName,
            menuItemId: menuItemId
        })
    });

    // TODO: call loadAllData()
});
```

</details>

<br>

------

## 5. Test both ends together

<br>

Now sit together on one computer and test if it works. Run the backend from IntelliJ and the frontend from e.g. Live Server in Visual Studio (or IntelliJs built-in server).

<br>

Check:

- does the page load menu items and orders from the backend?
- can you fill in the form and submit it?
- does the browser send a `POST` request?
- does the backend return a successful response?
- does the new order appear in the board?

If something does not work, there is a lot of places to look :)

- CORS
- the `GET` requests
- the form data in the frontend
- the JSON body in `fetch()`
- the controller
- the request DTO
- the service

<br>

------

## 6. Optional backend task: update order status with `PATCH`

<br>

Great job so far!

If there is time left, you could make status changes work on the backend.

<br>

#### **If you want to be a bit more creative on a monday afternoon, you can also try to together make a feature you come up with yourself!**

<br>

Add `PATCH /api/orders/{id}` to the same controller.

To make that work, you also need:

- an `UpdateOrderStatusRequest` record for the request body
- an `updateStatus(...)` method in `OrderService`
- a controller method that receives the order id and the new status

Inside `updateStatus(...)`, find the order with the matching id, update its status, and return the updated order.

<br>

<details>
  <summary><b>Hint</b></summary>

Start in `OrderService`.

Add `updateStatus(...)` and get that working first.

The method should:

- take the order id
- take the new status from the request body
- find the correct order in the list
- change or replace that order with the new status
- return the updated order

When that works, add `UpdateOrderStatusRequest`, and then wire it into the controller with `@PatchMapping("/orders/{id}")`, `@PathVariable`, and `@RequestBody`.

</details>

<details>
  <summary><b>Extra hint</b></summary>

Put these three parts in three different places:

- `UpdateOrderStatusRequest` should be its own record
- `updateStatus(...)` should go in `OrderService`
- `@PatchMapping("/orders/{id}")` should go in your current controller

The request DTO can look like this:

```java
public record UpdateOrderStatusRequest(
        String status
) {
}
```

The service method could begin like this:

```java
public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
    for (int i = 0; i < orders.size(); i++) {
        Order currentOrder = orders.get(i);

        if (currentOrder.getId().equals(id)) {
            // TODO: create an updated Order with the new status
            // TODO: replace the old order in the list
            // TODO: return the updated order
        }
    }

    throw new IllegalArgumentException("Unknown order id");
}
```

Then add the controller method and return `OrderResponse`.

</details>

<details>
  <summary><b>Extra extra hint (Try to discuss the problem a bit with your partner :)</b></summary>


This is almost what we need.

Put the record in its own file:

```java
public record UpdateOrderStatusRequest(
        String status
) {
}
```

Put this in `OrderService`:

```java
public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
    for (int i = 0; i < orders.size(); i++) {
        Order currentOrder = orders.get(i);

        if (currentOrder.getId().equals(id)) {
            Order updatedOrder = new Order(
                    currentOrder.getId(),
                    currentOrder.getCustomerName(),
                    currentOrder.getMenuItemId(),
                    currentOrder.getMenuItemName(),
                    request.status()
            );

            // TODO: replace the old order in the list

            return updatedOrder;
        }
    }

    throw new IllegalArgumentException("Unknown order id");
}
```

Put this in your current controller:

```java
@PatchMapping("/orders/{id}")
public OrderResponse updateStatus(
        @PathVariable Long id,
        @RequestBody UpdateOrderStatusRequest request
) {
    Order updatedOrder = orderService.updateStatus(id, request);

    // TODO: map updatedOrder to OrderResponse and return it
}
```

</details>

<br>

------

## 7. Optional frontend: send status changes with `PATCH`

<br>

If you still have time left, you can now make the status UI work in the browser.

Inside `renderOrders()`, add a status dropdown for each order. Build the dropdown from the `statuses` array, show the current status as selected, and send a `PATCH` request when the user changes it.

After the request succeeds, call `loadAllData()` so the board updates again.

You can keep the current status text, or replace it with the dropdown.

<br>

<details>
  <summary><b>Hint</b></summary>

Each order needs its own `<select>`.

So inside the `for (const order of orders)` loop in `renderOrders()`:

- create a `<select>`
- create one `<option>` for each status
- select the current status
- add a `change` listener
- send the new value to `/api/orders/{id}`
- call `loadAllData()`

</details>

<details>
  <summary><b>Extra hint</b></summary>

A good order is:

- create `statusSelect` inside the order loop
- loop through `statuses`
- append the options to the dropdown
- add `statusSelect.addEventListener("change", async function () { ... })`
- inside the listener, send a `PATCH` request to `/api/orders/{id}`
- use `method: "PATCH"`
- send the new status in a JSON body
- call `loadAllData()` afterwards

The dropdown setup could begin like this, then add an event listener after:

```js
const statusSelect = document.createElement("select");

for (const status of statuses) {
    const option = document.createElement("option");
    option.value = status;
    option.textContent = status;

    if (status === order.status) {
        option.selected = true;
    }

    statusSelect.appendChild(option);
}
```

</details>

<details>
  <summary><b>Extra extra hint (Try for a bit or ask the person next to you:)</b></summary>

Now we are close, but we still need to render all the data again and append the dropdown:

```js
const statusSelect = document.createElement("select");

for (const status of statuses) {
    const option = document.createElement("option");
    option.value = status;
    option.textContent = status;

    if (status === order.status) {
        option.selected = true;
    }

    statusSelect.appendChild(option);
}

statusSelect.addEventListener("change", async function () {
    await fetch(`http://localhost:8080/api/orders/${order.id}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            status: statusSelect.value
        })
    });

    // TODO: call loadAllData()
});

// TODO: append the dropdown to the order card
```

</details>

<br>

------

## 8. Optional: add one more feature together

<br>

Good job so far! 

**If you still have time left, come up with a new feature and build it together.**

<br>

Make a feature yourselves, or try to add one of these:

- add a `DELETE /api/orders/{id}` endpoint and a **Fjern bestilling** button
- add filtering by status, for example only show `READY` orders

Use the same pattern as before:

- frontend sends a request
- backend returns data
- frontend loads fresh data and renders again

<br>

## 9. The end

<br>

Our board is now talking to a Spring backend.

------
