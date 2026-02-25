# Exercises 8 - Fetch & Async



<br>



# Exercise 1: Live Search and Filter Dashboard



<br>

This week we will simulate a backend by using a JSON mock service, so we need to use the Fetch API. 

In real-world applications, you rarely just display a static list. Users need to search, sort, and filter the data dynamically. So we will be building a dynamic dashboard. When the user types in the search box or changes the dropdown, the list of drones shown on the screen should instantly update to match the filters.



<br>





## 0. Setting up the Mock Backend

<br>

### Step 1: Create .html and .js files, and copy in the HTML

------

<br>

Create `index.html` and `script.js`.

<br>

Copy below into our **index.html**

<br>

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Drone Dashboard</title>
    <style>
        body { font-family: sans-serif; padding: 20px; background: #f4f4f4; }
        .controls { margin-bottom: 20px; display: flex; gap: 10px; }
        input, select { padding: 8px; font-size: 16px; }
        
        /* We will dynamically generate these cards */
        #dashboard { display: flex; flex-wrap: wrap; gap: 15px; }
        .card { 
            background: white; padding: 15px; border-radius: 8px; 
            box-shadow: 0 2px 4px rgba(0,0,0,0.1); width: 200px; 
        }
    </style>
</head>
<body>
    <h1>Drone Fleet Manager</h1>
    
    <div class="controls">
        <input type="text" id="searchInput" placeholder="Search by model name...">
        
        <select id="statusFilter">
            <option value="all">All Statuses</option>
            <option value="active">Active</option>
            <option value="maintenance">In Maintenance</option>
            <option value="retired">Retired</option>
        </select>
    </div>

    <div id="dashboard"></div>

    <script src="script.js"></script>
</body>
</html>
```



<br>

We are going to use the tool **JSON Server** to simulate a backend database (like our Spring backend). This allows us to practice our frontend `fetch` skills locally by providing real URLs to send `GET`, `POST`, and `DELETE` requests to.

<br>

### Step 2: Install JSON Server

------

<br>

First, we need to install JSON Server. Open your **terminal**, ensure you have Node.js installed, and run the following command to install JSON Server on your machine:

<br>

```
npm install json-server
```

<br>

### Step 3: Create the Database File

------

<br>

Next, we provide the server with some initial data. Navigate to your root project folder (the exact same folder where your `index.html` and `script.js` files are located) and create a new file called e.g. `db.json`.

**Copy the code block below and paste it into this new file db.json**. Do not change the formatting or remove any double quotes.

<br>

```
{
  "drones": [
    { "id": 1, "model": "SkyRider X1", "battery": 85, "status": "active" },
    { "id": 2, "model": "SkyRider X2", "battery": 12, "status": "maintenance" },
    { "id": 3, "model": "AeroDelivery Pro", "battery": 100, "status": "active" },
    { "id": 4, "model": "AeroDelivery Pro", "battery": 45, "status": "active" },
    { "id": 5, "model": "HeavyLift V8", "battery": 0, "status": "retired" },
    { "id": 6, "model": "Scout Mini", "battery": 92, "status": "active" },
    { "id": 7, "model": "Scout Mini", "battery": 8, "status": "maintenance" },
    { "id": 8, "model": "SkyRider X2", "battery": 88, "status": "active" },
    { "id": 9, "model": "HeavyLift V8", "battery": 65, "status": "active" },
    { "id": 10, "model": "NanoSwarm", "battery": 15, "status": "maintenance" },
    { "id": 11, "model": "NanoSwarm", "battery": 100, "status": "active" },
    { "id": 12, "model": "AeroDelivery Pro", "battery": 0, "status": "retired" },
    { "id": 13, "model": "SkyRider X1", "battery": 5, "status": "maintenance" },
    { "id": 14, "model": "AgriScout", "battery": 76, "status": "active" },
    { "id": 15, "model": "AgriScout", "battery": 42, "status": "active" },
    { "id": 16, "model": "RescueHawk", "battery": 100, "status": "active" },
    { "id": 17, "model": "HeavyLift V8", "battery": 0, "status": "retired" }
  ]
}
```

<br>



### Step 4: Start and Verify the Server

------

<br>

We start the server so it can serve our db.json file. In your terminal, navigate into the folder containing your `db.json` file, and run:

<br>

```
npx json-server db.json
```

<br>

You will know it is running successfully when the terminal says: **JSON Server started on PORT :3000**. Keep this terminal window open in the background. 

To verify it is working, open your browser and go to **`http://localhost:3000/drones`**. You should see your JSON array of 17 drones.

<br>

## 1. Fetching and Rendering the Data

<br>

### Step 1: The Render Function

<br>

Write a function called `renderDrones(dataArray)`. For now, this function won't do anything because we haven't fetched our data yet.

This function should take an array of drone objects as an argument. Inside the function, follow these steps:

1. Select the dashboard container by using `document.querySelector("#dashboard")` and store it in a variable.
2. Clear out any existing HTML inside that container by setting its `.innerHTML` to an empty string (`""`). This prevents us from stacking duplicate cards if the function is run multiple times.
3. Set up a loop to go through every drone in the array using `for (let drone of dataArray) { ... }`.
4. Inside the loop, create a new HTML element for the drone card using `document.createElement("div")` and store it in a variable called `card`.
5. Apply the correct styling by adding the CSS class `"card"` to your new element using `card.classList.add("card")`.
6. Build the content of the card by setting `card.innerHTML` equal to a template literal (using `). Inside the ``, write standard HTML(like h3, p, etc.) and inject the drone's properties (`${drone.model}`, `${drone.battery}`, and `${drone.status}`).
7. Finally, make the card appear on the screen by attaching it to the dashboard container using the `.appendChild()`method.

<br>

**Hint:** You have done rendering loops last time. The difference today is that instead of making a table row (`<tr>`), you are creating `<div>` elements and explicitly adding a class to them using `.classList.add()`.

<details> <summary><b>Extra hint</b></summary>Add your code directly below each numbered comment:

```javascript
function renderDrones(dataArray) {
    let dashboard = document.querySelector("#dashboard");
    
    // TODO 1. Clear the board
    dashboard.innerHTML = ""; 
    
    for (let drone of dataArray) {
        // TODO 2. Create a div
        let card = document.createElement("div");
        
        // TODO 3. Add the "card" class to it
        
        // TODO 4. Set its innerHTML using the drone object and a template literal
        
        // TODO 5. Append the completed div to the dashboard
        
    }
}
```

</details>

<details> <summary><b>Extra extra hint</b></summary>You need to fill in the TODO step to make the cards actually appear on the screen.

```javascript
function renderDrones(dataArray) {
    let dashboard = document.querySelector("#dashboard");
    dashboard.innerHTML = ""; 
    
    for (let drone of dataArray) {
        let card = document.createElement("div");
        card.classList.add("card"); 
        
        card.innerHTML = `
            <h3>${drone.model}</h3>
            <p>Battery: ${drone.battery}%</p>
            <p>Status: ${drone.status}</p>
        `;
        
        // TODO: Use appendChild to attach the 'card' variable to the 'dashboard' variable here
        
    }
}
```

</details>

<br>

------

### Step 2: Fetch the Data

<br>

Before we can display anything, we need to get the drone data from our mock server.

Follow these steps to build the fetch request:

1. Create a global variable `let allDrones = [];` at the very top of your script to hold the data for later filtering.
2. Write a function called `fetchDrones()` that makes a `fetch()` request to `http://localhost:3000/drones`.
3. Use a `.then()` block to parse the response into JSON.
4. In a second `.then(data => { ... })` block, save the fetched array into `allDrones` with `allDrones = data;`, then call `renderDrones(allDrones)`.
5. Call `fetchDrones();` at the bottom of your script so it executes immediately when the page loads.

<br>

**Hint:** The fetch API uses `.then()` blocks because network requests take time to complete. It is standard practice to use arrow functions (`=>`) inside these blocks. Your first step after the fetch is to convert the raw response: `.then(response => response.json())`.

<details> <summary><b>Extra hint</b></summary>Use the numbered comments to guide where your logic goes based on the steps above.

```javascript
let allDrones = []; 

function fetchDrones() {
    fetch("http://localhost:3000/drones")
        // TODO: 3. Add your first .then() to parse the JSON
        
        // TODO: 4. Add your second .then() to save the array and render it
        
}

// TODO: 5. Trigger it on page load
fetchDrones(); 
```

</details>

<details> <summary><b>Extra extra hint</b></summary>The code for fetching the data, spend a few minutes on understanding the fetch and .then structure.

```javascript
let allDrones = [];

function fetchDrones() {
    fetch("http://localhost:3000/drones")
        .then(response => response.json())
        .then(data => {
            allDrones = data;
						renderDrones(allDrones);
            
        });
}

fetchDrones();
```

</details>

<br>

------



### Step 3: Live Text Search

<br>

Now we will make the search bar functional so that the dashboard updates instantly as the user types.

Follow these steps to build the feature:

1. Select your search input box (`"#searchInput"`) and add an `"input"` event listener to it.
2. Inside the event listener, grab the exact text the user just typed using `event.target.value`. Immediately convert it to lowercase using `.toLowerCase()` and save it in a variable.
3. Create a new variable called `filteredDrones`. Set it equal to `allDrones.filter(...)` to start creating your narrowed-down list.
4. Inside the `.filter()` method, write the condition: return only the drones where the drone's `model` (also converted to lowercase) `.includes()` your search text variable.
5. Finally, call your `renderDrones()` function and pass in your `filteredDrones` variable to redraw the screen with only the matching drones.

<br>

**Hint:** The `"input"` event fires on every single keystroke. Remember to use `.toLowerCase()` on both the search text and the drone models before comparing them.

<details> <summary><b>Extra hint</b></summary>The event listener is set up for you. Add code below the numbered TODO comments.

```javascript
document.querySelector("#searchInput").addEventListener("input", (event) => {
    
    // TODO: 2. Grab the text and convert to lowercase
    
    // TODO: 3 & 4. Filter the allDrones array based on the model name
    
    // TODO: 5. Call renderDrones with the newly filtered array
    
});
```

</details>

<details> <summary><b>Extra extra hint</b></summary>The variables and the filter loop are set up for you. You need to provide the exact condition to filter by, and then trigger the render function.

```javascript
document.querySelector("#searchInput").addEventListener("input", (event) => {
    let searchText = event.target.value.toLowerCase();
    
    let filteredDrones = allDrones.filter(drone => {
        // TODO: return true if drone.model.toLowerCase() includes the searchText
        
    });
    
    // TODO: call renderDrones(filteredDrones)
    
});
```

</details>

<br>

------

<br>

### Step 4: The Dropdown Filter

<br>

Now let's make the status dropdown work so we can quickly see which drones are active, maintenance, or retired.

Follow these steps to build the filter:

1. Select your status dropdown (`"#statusFilter"`) and attach an event listener that triggers when the user changes their selection.
2. Inside the listener, capture the value of the option the user just clicked.
3. Set up conditional logic. If the captured value is `"all"`, show the entire dashboard by passing your global `allDrones` array into your render function.
4. If the value is a specific status, use the array `.filter()` method to create a new list. Keep only the drones where the drone's status strictly matches the user's selection.
5. Finally, call your render function with this newly filtered list so the screen updates.

**Hint:** The event you are listening for on a `<select>` element is called `"change"`. You must handle the `"all"` category with its own conditional logic, because our database does not contain any drones with an actual status of "all" to filter by.

<details> <summary><b>Extra hint</b></summary> The event listener is set up for you. Use the numbered comments to guide where your logic goes based on the steps above.

```javascript
document.querySelector("#statusFilter").addEventListener("change", (event) => {
    
    // TODO: 2. Capture the selected value
    
    
    // TODO 3. Setup the conditional check for "all"
    if ( /* condition goes here */ ) {
        
        // TODO: Render everything
        
    } else {
        
        // TODO: 4. Filter the allDrones array based on the exact status
        
        // TODO: 5. Render the newly filtered array
        
    }
});
```

</details>

<details> <summary><b>Extra extra hint</b></summary>The conditional block and the variables are set up for you. You need to grab the value from the event, trigger the render for the "all" scenario, and write the exact match condition for the specific status scenario.

```javascript
document.querySelector("#statusFilter").addEventListener("change", (event) => {
    // TODO: set selectedStatus equal to the value of the event target
    let selectedStatus = 
    
    if (selectedStatus === "all") {
        // TODO: call renderDrones(allDrones)
        
    } else {
        let filteredDrones = allDrones.filter(drone => {
            // TODO: return true if drone.status strictly matches selectedStatus
            
        });
        
        // TODO: call renderDrones(filteredDrones)
    }
});
```

</details>

<br>

------

### Step 5: Combine the Filters

<br>

Right now, if you search for "SkyRider" and then change the dropdown to "active", the dashboard forgets your text search! The two event listeners are overwriting each other instead of working together:(

Follow these steps to build a combined filtering system:

1. Create a single, new function called `updateDashboard()`.
2. Inside this function, grab the current values from **both** the text input (`"#searchInput"`) and the dropdown (`"#statusFilter"`).
3. Create a variable called `filteredData` and set it equal to your full `allDrones` array. This is your starting point.
4. Set up two separate `if` statements. First, if the search text is not empty (`""`), filter `filteredData` by the search text. Second, if the dropdown is not `"all"`, filter `filteredData` **again** by the status.
5. Call `renderDrones()` with your final `filteredData`.
6. Finally, delete the logic inside your two existing event listeners from Steps 3 and 4. Instead, make both listeners call this new `updateDashboard` function.

<br>

**Hint:** Think of filtering like an object passing through multiple nets. To make this work, you must overwrite your `filteredData` variable inside each `if` block (e.g., `filteredData = filteredData.filter(...)`). This ensures the second filter narrows down the results of the first filter, rather than starting over from the beginning.

<details> <summary><b>Extra hint</b></summary>The variables and the event listeners are set up. Use the numbered comments to guide where your logic goes based on the steps above.

```javascript
function updateDashboard() {
    // TODO: 2. Grab values from both inputs
    let searchText = document.querySelector("#searchInput").value.toLowerCase();
    let selectedStatus = document.querySelector("#statusFilter").value;
    
    // TODO: 3. Start with the full list
    let filteredData = allDrones; 

    // TODO: 4. Apply text filter (if there is text)
    if (searchText !== "") {
        // Reassign filteredData using .filter() for the text search
        
    }

    // TODO: 4. Apply status filter (if not "all")
    if (selectedStatus !== "all") {
        // Reassign filteredData using .filter() for the exact status
        
    }

    // TODO: 5. Render the final result
    
}

// TODO: 6. Make both inputs trigger the same central function
document.querySelector("#searchInput").addEventListener("input", updateDashboard);
document.querySelector("#statusFilter").addEventListener("change", updateDashboard);
```

</details>

<details> <summary><b>Extra extra hint</b></summary>The structural flow is set up for you. You need to write the specific .filter() conditions inside the if blocks, and trigger the final render.

```javascript
function updateDashboard() {
    let searchText = document.querySelector("#searchInput").value.toLowerCase();
    let selectedStatus = document.querySelector("#statusFilter").value;
    
    let filteredData = allDrones; 

    if (searchText !== "") {
        filteredData = filteredData.filter(drone => {
            // TODO: return true if drone.model.toLowerCase() includes searchText
            
        });
    }

    if (selectedStatus !== "all") {
        filteredData = filteredData.filter(drone => {
            // TODO: return true if drone.status strictly matches selectedStatus
            
        });
    }

    // TODO: call renderDrones(filteredData)
}

document.querySelector("#searchInput").addEventListener("input", updateDashboard);
document.querySelector("#statusFilter").addEventListener("change", updateDashboard);
```

</details>

<br>

We have now built a dynamic frontend that fetches data from a mock REST API and filters it in real-time based on different user inputs. This is exactly how a modern web dashboard works.

<br>

Open your browser and test your UI to make sure everything works:

- Type "Scout" into the search bar, you should only see Scout Mini and AgriScout drones.
- While "Scout" is still in the search bar, change the dropdown to "maintenance", you should now only see one specific drone.
- Clear the search bar, the list should instantly update to show all "maintenance" drones.
- Change the dropdown back to "all", your original list of 17 drones should return.

<br>

------

<br>

# Exercise 2: Alert System

<br>

In this exercise, we are building a real-time dashboard for a Alert System. <br>

## 0. Setting up "Backend" & Initial Fetch (GET)

------

<br>

### Step 1: Creating files and Copying in HTML

------

<br>

Create `index.html` and `script.js`.

<br>

Copy below into our **index.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Alert System</title>
    <style>
        body { font-family: sans-serif; padding: 20px; background: #f4f4f4; }
        .form-container { background: white; padding: 15px; margin-bottom: 20px; border: 1px solid #ccc; }
        input, select, button { padding: 8px; margin-right: 10px; }
        table { border-collapse: collapse; width: 100%; background: white; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #eee; }
    </style>
</head>
<body>
    <h1>Alert Dashboard</h1>
    
    <div class="form-container">
        <h3>Log New Alert</h3>
        <form id="alertForm">
            <input type="text" id="locationInput" placeholder="Location" required>
            <select id="typeInput">
                <option value="Fire">Fire</option>
                <option value="Chemical">Chemical</option>
                <option value="Medical">Medical</option>
            </select>
            <select id="statusInput">
                <option value="Active">Active</option>
                <option value="Resolved">Resolved</option>
            </select>
            <button type="submit">Create Alert</button>
        </form>
    </div>

    <table>
        <thead>
            <tr>
                <th>Location</th>
                <th>Type</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody id="alertTableBody">
            </tbody>
    </table>

    <script src="script.js"></script>
</body>
</html>
```

<br>

------



### **Step 2: Create the Mock Database** 

<br>

Create a file named `db.json` in your project folder and paste in this starting data:

```json
{
  "alerts": [
    { "id": "1", "location": "Tivoli Gardens", "type": "Fire", "status": "Active" },
    { "id": "2", "location": "Nyhavn", "type": "Chemical", "status": "Resolved" },
    { "id": "3", "location": "Amalienborg Palace", "type": "Medical", "status": "Active" },
    { "id": "4", "location": "The Little Mermaid", "type": "Fire", "status": "Active" },
    { "id": "5", "location": "Rundetårn", "type": "Medical", "status": "Resolved" },
    { "id": "6", "location": "Strøget", "type": "Fire", "status": "Resolved" },
    { "id": "7", "location": "Christiansborg Palace", "type": "Chemical", "status": "Active" },
    { "id": "8", "location": "Rosenborg Castle", "type": "Medical", "status": "Active" },
    { "id": "9", "location": "Børsen", "type": "Fire", "status": "Active" },
    { "id": "10", "location": "Copenhagen Opera House", "type": "Chemical", "status": "Resolved" }
  ]
}
```

<br>

Open your terminal and start your mock server: `npx json-server db.json`

<br>

------



### Step 3: The Render Function

<br>

First, let's make the setup to display the data. Our `db.json` will be an array of objects, and each object has properties like `location`, `type`, and `status`.

In your `script.js`, write a function called `renderAlerts(alertsArray)`. This function should take that array of objects as an argument. Inside the function, follow these steps:

1. Select the `<tbody id="alertTableBody">` and completely clear its current contents (so we don't accidentally stack duplicate rows later).
2. Loop through the `alertsArray`.
3. For each alert object in the array, create a new HTML table row (`<tr>`).
4. Inside that row, add four table data (`<td>`) columns. Insert the alert's `location`, `type`, and `status` into the first three using template literals (`${...}`). Leave the fourth column completely empty for now (we will put a Delete button there later).
5. Append the finished row to the table body.

<br>

**Hint:** Use `document.querySelector("#alertTableBody").innerHTML = ""` to clear the board. Use `alertsArray.forEach(alert => { ... })` to loop through the data. Inside the loop, use `document.createElement("tr")` to make the row.

<details> <summary><b>Extra hint</b></summary> If you know the logic but need help with the syntax, here is the skeleton:


```javascript
function renderAlerts(alertsArray) {
    let tableBody = document.querySelector("#alertTableBody");
    tableBody.innerHTML = ""; 

    alertsArray.forEach(alert => {
        // 1. Create row: let row = document.createElement("tr")
        // 2. Set innerHTML using template literals (` `) for the <td> tags
        // 3. Append to DOM: tableBody.appendChild(row)
    });
}
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the code for the render loop:

```javascript
function renderAlerts(alertsArray) {
    let tableBody = document.querySelector("#alertTableBody");
    tableBody.innerHTML = ""; 

    alertsArray.forEach(alert => {
        let row = document.createElement("tr");

        row.innerHTML = `
            <td>${alert.location}</td>
            <td>${alert.type}</td>
            <td>${alert.status}</td>
            <td></td>
        `;

        tableBody.appendChild(row);
    });
}
```

</details>

<br>

------

### Step 4: Fetch the Data (GET)

<br>

Now that our UI is ready to receive data, let's get it from the mock server.

Write an `async` function called `fetchAlerts()`. Inside it, use the `fetch()` API to make a `GET` request to `http://localhost:3000/alerts`.

Because it takes time to connect to the server, you must wait for the response, and then wait again to convert that response into readable JSON format. Once you have that final JSON array, pass it directly into your `renderAlerts()` function to draw the data on the screen.

Finally, call `fetchAlerts()` at the very bottom of your file so the process starts immediately when the page loads.

<br>

**Hint:** Because this is an `async` function, you must use the `await` keyword. It usually looks like this: `let response = await fetch("URL_HERE");` followed by `let data = await response.json();`.

<details> <summary><b>Extra hint</b></summary> Here is the structure of the fetch call. You need to trigger the render function once the data arrives:


```javascript
async function fetchAlerts() {
    let response = await fetch("http://localhost:3000/alerts");
    let alertsData = await response.json();
    
    // Call renderAlerts and pass in the alertsData variable here
}

fetchAlerts(); // Run on page load
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the code to fetch the data and trigger your render function:


```javascript
async function fetchAlerts() {
    let response = await fetch("http://localhost:3000/alerts");
    let alertsData = await response.json();
    
    // TODO: call renderAlerts with alertsData
}

fetchAlerts();
```

</details>

<br>

------

### Step 5: Form Handling & Building the Package

<br>

Before we send data to the server, we need to capture what the user typed into the form and bundle it into a package.

At the bottom of your `script.js`, select the `#alertForm` and add an event listener for the `"submit"` event. Make sure the callback function is `async` and receives the `event` parameter. Inside this listener, follow these steps:

1. **Stop the refresh:** Forms naturally want to refresh the page when submitted. Stop this immediately by calling `event.preventDefault()` as your very first line.
2. **Gather the data:** Create three variables to store the text the user typed. You do this by selecting the input and asking for its `.value` (for example: `document.querySelector("#locationInput").value`). Do this for the location, type, and status inputs.
3. **Build the package:** Create a new JavaScript object. Give it `location`, `type`, and `status` properties, and set them equal to the three variables you just created.
4. **Test it:** For now, just `console.log()` your new object. Fill out the form in your browser and click "Create Alert" to make sure the data prints correctly in your console.

<br>

**Hint:** The setup for the listener looks exactly like this: `document.querySelector("#alertForm").addEventListener("submit", async function(event) { ... })`. Inside, remember that making an object looks like this: `let myObject = { location: locationVariable, ... };`.

<details> <summary><b>Extra hint</b></summary> If you understand the flow but need help structuring the code, here is the skeleton:

```javascript
document.querySelector("#alertForm").addEventListener("submit", async function(event) {
    // 1. Stop the page reload
    event.preventDefault();

    // 2. Grab the input values using .value
    let newLocation = document.querySelector("#locationInput").value;
    // (Grab newType and newStatus here using their IDs)

    // 3. Build the object
    let newAlert = {
        location: newLocation,
        // (Add type and status here)
    };

    // 4. Test it
    console.log(newAlert);
});
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the code to capture the form data:

```javascript
document.querySelector("#alertForm").addEventListener("submit", async function(event) {
    event.preventDefault(); 

    let newLocation = document.querySelector("#locationInput").value;
    // TODO: get value from #typeInput
		let newType = /* TODO */
		// TODO: get value from #statusInput
    let newStatus = /* TODO */

    let newAlert = {
        location: newLocation,
        // TODO: add type and status properties
    };

    console.log(newAlert);
});
```

</details>

<br>

------

### Step 6: Sending the Data (POST)

Now that we have successfully captured the user's input into our `newAlert` object, we need to send it to the mock database.

Go back into your submit event listener and remove the `console.log()`. Replace it with an `await fetch()` request to `http://localhost:3000/alerts`.

Normally, `fetch` just reads data (a `GET` request). To **send** data (a `POST` request), we have to give `fetch` a second argument: a configuration object that tells the server exactly what we are handing it.

Here is what needs to go inside that configuration object:

1. `method`: Set this to `"POST"`.
2. `headers`: This is another object. Set it to `{ "Content-Type": "application/json" }` so the server knows we are sending JSON data.
3. `body`: This is the actual data. You must use `JSON.stringify(newAlert)` to turn your JavaScript object into a text format the server can read.
4. **Update the UI:** On the next line, *after* the `fetch` completes, call your `fetchAlerts()` function. This tells the table to re-download the data and immediately display the alert you just saved.

**Hint:** Creating that configuration object is the hardest part. It sits right after your URL in the fetch call, separated by a comma: `await fetch("URL_HERE", { method: "POST", ... });`.

<details> <summary><b>Extra hint</b></summary> Here is the exact structure of the POST request. Add this where your console.log used to be:

```javascript
    // ... previous form code ...

    await fetch("http://localhost:3000/alerts", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(newAlert)
    });

    // Refresh the table by calling your GET function
    fetchAlerts(); 
});
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the event listener, fill in the TODOs:


```javascript
document.querySelector("#alertForm").addEventListener("submit", async function(event) {
    event.preventDefault(); 

    let newLocation = document.querySelector("#locationInput").value;
    let newType = document.querySelector("#typeInput").value;
    let newStatus = document.querySelector("#statusInput").value;

    let newAlert = {
        location: newLocation,
        type: newType,
        status: newStatus
    };

    await fetch("http://localhost:3000/alerts", {
    		// TODO: set method to POST
    		method: ,
    		headers: {
        		"Content-Type": "application/json"
    		},
    		// TODO: stringify newAlert into body
    		body:
		});

    fetchAlerts(); 
});
```

</details>

<br>

------

### Step 7: Adding the Delete Buttons

<br>

To delete an alert, the user needs a button to click. We need to go back up to our `renderAlerts(alertsArray)` function and finally fill in that empty fourth column.

**The catch:** If you have 10 alerts on the screen, you will have 10 "Delete" buttons. When you click one, how does the code know which alert to destroy?

Our mock database automatically gives every alert a unique `id` ("1", "2", "3", etc.). We need to attach that specific `id`directly to the button so it can pass the ID to our delete function.

Go to your `renderAlerts()` function and look inside the `.forEach()` loop. Find the template literal (\`) where you created the `<td>` tags.

1. Inside the empty fourth `<td>`, add a `<button>` element. Make the text of the button say "Delete".
2. Add an `onclick` attribute to this `<button>`.
3. Set the `onclick` attribute so that it calls a function named `deleteAlert()`.
4. Inside the parentheses of `deleteAlert()`, insert the specific `alert.id` using the template literal syntax (`${...}`). Remember to wrap it in single quotes so the browser treats the ID as a string!

<br>

**Hint:** Your fourth column needs to look exactly like this: `<td><button onclick="deleteAlert('${alert.id}')">Delete</button></td>`. The single quotes around `${alert.id}` **are important.** Without them, the browser will think the ID is an undefined variable instead of a piece of text.

<details> <summary><b>Extra hint</b></summary> If you need help placing it, here is what your updated innerHTML block should look like inside the renderAlerts loop:

```javascript
        row.innerHTML = `
            <td>${alert.location}</td>
            <td>${alert.type}</td>
            <td>${alert.status}</td>
            <td>
                <button onclick="deleteAlert('${alert.id}')">Delete</button>
            </td>
        `;
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the renderAlerts function with the buttons included:


```javascript
function renderAlerts(alertsArray) {
    let tableBody = document.querySelector("#alertTableBody");
    tableBody.innerHTML = ""; 

    alertsArray.forEach(alert => {
        let row = document.createElement("tr");

        row.innerHTML = `
            <td>${alert.location}</td>
            <td>${alert.type}</td>
            <td>${alert.status}</td>
            <td>
                <button onclick="deleteAlert('${alert.id}')">Delete</button>
            </td>
        `;

        tableBody.appendChild(row);
    });
}
```

</details>

<br>

------

### Step 8: Sending the DELETE Request

<br>

Now our buttons are trying to call a function named `deleteAlert()`, and they are passing the correct `id` into it. But the function doesn't exist yet, lets build it to talk to the database.

At the bottom of your `script.js`, write an `async` function called `deleteAlert(id)`. Notice that it must accept `id` as a parameter.

Inside this function, follow these steps:

1. **Target the specific alert:** To delete an item in a REST API, you don't send the ID in a "body" like we did with POST. Instead, you put the ID directly at the end of the URL. Create a URL that looks like this: `http://localhost:3000/alerts/THE_ID_HERE`.
2. **Send the request:** Use `await fetch()`. Pass your special URL with the ID attached as the first argument.
3. **Configure the method:** Pass a configuration object as the second argument to `fetch`. Set the `method` to `"DELETE"`. (Because a DELETE request just deletes data, you do not need to send headers or a body).
4. **Update the UI:** After the `fetch` finishes running, call your `fetchAlerts()` function. This tells the table to re-download all the data from the server. Since the server just deleted the alert, it won't send it back, and the row will instantly vanish from your screen.

<br>

**Hint:** To attach the `id` variable to the end of your URL, use backticks to create a template string. It will look like this: `` await fetch(`http://localhost:3000/alerts/${id}`, { ... }) ``.

<details> <summary><b>Extra hint</b></summary>Fill in the TODOs:


```javascript
async function deleteAlert(id) {
    await fetch(
      	// TODO: build URL with id at the end, surround by `
      , {
        // TODO: set method to DELETE
        method:
    });

    fetchAlerts(); 
}
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the delete function:


```javascript
async function deleteAlert(id) {
    await fetch(`http://localhost:3000/alerts/${id}`, {
        method: "DELETE" 
    });

    fetchAlerts(); 
}
```

</details>

<br><br>

## (Optional) Exercise 3 - Add-on Features

<br>

### Extra 1: Auto-Update

<br>

Right now, our dashboard only gets new data when we first load the page or click a button. In a real emergency central, if another dispatcher added a new alert to the database from a different computer, your screen wouldn't show it unless you manually refreshed the browser.

We can fix this by setting up a background timer to automatically fetch new data every 10 seconds ("polling" the server).

1. Go to the bottom of your `script.js` file.
2. You currently have a single line of code that fetches the data once when the page loads: `fetchAlerts();`. Keep this line so the screen isn't blank when you first open it.
3. Right below it, type `setInterval()`.
4. `setInterval` requires two pieces of information inside its parentheses, separated by a comma: the name of the function you want it to run, and the time delay in milliseconds.
5. Tell it to run your `fetchAlerts` function every `10000` milliseconds.

**Important:** When you pass a function to `setInterval`, do **not** put parentheses `()` after the function name.

- `setInterval(fetchAlerts(), 10000)` runs the function instantly and breaks the timer.
- `setInterval(fetchAlerts, 10000)` correctly hands the name of the function to the timer so it can call it repeatedly later.

<br>

**Hint:** The bottom of your file should have two separate lines of code. The first line runs the fetch immediately. The second line sets up the repeating timer. It should look like this: `setInterval(NAME_OF_FUNCTION, NUMBER_OF_MILLISECONDS);`

<details> <summary><b>Extra hint</b></summary> If you need help setting up the exact syntax at the bottom of your file, here is the skeleton:

```javascript
// 1. Run once immediately
fetchAlerts();

// 2. Set the loop to run every 10 seconds
setInterval(fetchAlerts, 10000);
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the code that should sit at the very bottom of your script.js file:

```javascript
fetchAlerts();
setInterval(fetchAlerts, 10000);
```

</details>

<br>

------

### Extra 2: Loading Spinner

<br>

Network requests take time. When `fetchAlerts()` runs, there is a fraction of a second where the browser is waiting for the server. Professional applications show a "Loading..." indicator during this wait time so the user knows the app hasn't frozen.

<br>

#### **Step 1: HTML & CSS** 

<br>

Go into your `index.html`. 

Right above your `<table>`, add this HTML element: `<div id="loadingMessage" style="display: none; color: blue; font-weight: bold; margin-bottom: 10px;">Loading data...</div>` 

Notice that `display: none` hides it by default.

<br>

**Step 2: Javascript** 

<br>

Go to your `script.js` and look inside your `fetchAlerts()`  function.

1. At the very top of `fetchAlerts()`—before the `await fetch` happens—select the `#loadingMessage` element and change its `.style.display` to `"block"`. This makes the message visible.
2. After your data has been fetched, converted to JSON, and passed into your `renderAlerts()` function, select the `#loadingMessage` element again and change its `.style.display` back to `"none"`. This hides it the exact moment the data appears on the screen.

<br>

**Hint:** You are creating a sandwich. Show the message, wait for the fetch, hide the message. `document.querySelector("#loadingMessage").style.display = "block";` is how you make it appear.

<details> <summary><b>Extra hint</b></summary> Here is exactly where to place your display logic inside the existing fetch function:

```javascript
async function fetchAlerts() {
    // 1. Show the loading message
    document.querySelector("#loadingMessage").style.display = "block";

    // 2. Do the heavy lifting (fetching the data)
    let response = await fetch("http://localhost:3000/alerts");
    let alertsData = await response.json();
    
    renderAlerts(alertsData);

    // 3. Hide the loading message
    document.querySelector("#loadingMessage").style.display = "none";
}
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the fetchAlertsfunction with the loading logic included:

```javascript
async function fetchAlerts() {
    let loadingElement = document.querySelector("#loadingMessage");
    
    loadingElement.style.display = "block";

    let response = await fetch("http://localhost:3000/alerts");
    let alertsData = await response.json();
    
    renderAlerts(alertsData);

    loadingElement.style.display = "none";
}
```

</details>

<br>

------

### Extra 3: The Sorting Button

<br>

A good dashboard lets you organize the chaos. Let's add a button that sorts our alerts alphabetically by their `status` so that all the "Active" alerts group together at the top, and "Resolved" alerts drop to the bottom.

<br>

#### **Step 1: The HTML** 

<br>

Go to your `index.html`. Inside your `.form-container` div, next to the "Create Alert" button, add a new button: `<button id="sortBtn" type="button">Sort by Status</button>`

<br>

#### **Step 2: Javascript** 

<br>

In your `script.js`, select `#sortBtn` and add a `"click"` event listener. Make the callback function `async`. Inside the listener:

1. Re-fetch the current data by using `await fetch("http://localhost:3000/alerts")` and converting it to JSON. Store this in a variable called `alertsData`.
2. Use the JavaScript array `.sort()` method on `alertsData`. To sort alphabetically by a string property, you compare the properties using `.localeCompare()`.
3. Pass your newly sorted array directly into `renderAlerts()` to redraw the table.

<br>

**Hint:** The sorting logic for strings looks exactly like this: `alertsData.sort((a, b) => a.status.localeCompare(b.status));`

<details> <summary><b>Extra hint</b></summary> Here is the skeleton for your new event listener:

```
document.querySelector("#sortBtn").addEventListener("click", async function() {
    // 1. Fetch the fresh data
    let response = await fetch("http://localhost:3000/alerts");
    let alertsData = await response.json();

    // 2. Sort the data array alphabetically by status
    alertsData.sort((a, b) => {
        return a.status.localeCompare(b.status);
    });

    // 3. Render the sorted data
    renderAlerts(alertsData);
});
```

</details>

<details> <summary><b>Extra extra hint</b></summary> Here is the code for the sorting button feature:


```
document.querySelector("#sortBtn").addEventListener("click", async function() {
    let response = await fetch("http://localhost:3000/alerts");
    let alertsData = await response.json();

    alertsData.sort((a, b) => a.status.localeCompare(b.status));

    renderAlerts(alertsData);
});
```

</details>

------

