

# Exercises 7 - DOM Manipulation



<br>

<br>

------

# Exercise 1: Guess My Number

In this exercise, we will build a classic "Guess My Number" game. We will read user input, handle button clicks, and update the DOM based on logical conditions.



<br>

## 0. The Starter Code

Create two files: `index.html` and `script.js`.

<br>


Copy this boilerplate into **index.html**. Take note of the IDs used for the input, the buttons, and the message paragraph.

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Guess My Number</title>
    <style>
        body { font-family: sans-serif; text-align: center; margin-top: 50px; }
        input { padding: 10px; font-size: 16px; width: 100px; }
        button { padding: 10px 20px; font-size: 16px; cursor: pointer; }
        #message { font-size: 20px; font-weight: bold; margin-top: 20px; }
    </style>
</head>
<body>
    <h1>Guess My Number!</h1>
    <p>Guess a number between 1 and 20</p>
    
    <input type="number" id="guessInput" min="1" max="20">
    <button id="guessBtn">Guess</button>
    <button id="resetBtn">Reset</button>

    <p id="message">Start guessing...</p>

    <script src="script.js"></script>
</body>
</html>
```

<br>

Paste this at the top of your **script.js** file. This generates the secret number when the page loads.

```javascript
// Generates a random integer between 1 and 20
let randomNumber = Math.trunc(Math.random() * 20) + 1;
```



<br>



------





<br>



### Step 1: The Event Listener

Add an event listener to the "Guess" button. When the user clicks it, just print "Button clicked!" to the console to ensure your connection works.





<br>

**Hint:** Keep in mind that all the logic related to checking the user's guess will need to exist *inside* a function within this event listener. This callback function ensures the code only executes when the click actually happens.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Here is the basic structure for setting up your event listener with a callback function:

```javascript
document.querySelector("#guessBtn").addEventListener("click", function() {
    console.log("Button clicked!");
    // All of your reading and guessing logic from the next steps will go inside here!
});
```
</details>

<br>


### Step 2: Read the Input

Inside your event listener, you need to read what the user typed into the input field and save it in a variable.

<br>

**Hint:** You need to use `document.querySelector` to find the input field and then access its `.value` property. 

**Note:** HTML inputs always return strings, so you must convert it to a number.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

You can convert the string to a number using the `Number()` function.

```
let guess = Number(document.querySelector("#guessInput").value);
console.log(guess);
```

</details>





<br>



### Step 3: The Game Logic

Now, compare the user's `guess` to the `randomNumber`. Instead of logging to the console, manipulate the DOM so the `<p id="message">` tag updates to tell the user if they guessed correctly, or if they need to guess higher or lower. Before comparing, handle invalid input (empty or outside 1-20) and show a message instead. 

<br>

**Hint:** You will need an `if / else if / ... / else` statement to handle the invalid input and do the comparisons. 

Inside each block, select the message paragraph and update its `textContent`. 

<details> <summary><b>Extra hint (use if stuck)</b></summary>

First, select the message element and store it in a variable so you don't have to query it multiple times: `const messageDisplay = document.querySelector("#message");`

Start with if and then else if to check for invalid inputs, then continue the logic with else if (correct) / else if (too high) / too low. Update #message with textContent.

</details>

<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>

Here is the structure for the logic:

```
const messageDisplay = document.querySelector("#message");

if (guess < 1) {
    messageDisplay.textContent = "Enter a number between 1 and 20.";
} else if (guess > 20) {
    messageDisplay.textContent = "Enter a number between 1 and 20.";
} else if (guess === randomNumber) {
    messageDisplay.textContent = "You guessed right!!!";
} else if (guess > randomNumber) {
    messageDisplay.textContent = "Too high!";
} else {
    messageDisplay.textContent = "Too low!";
}
```

</details>



<br>





### Step 4: Add Functionality - Reset

Make the "Reset" button work. When clicked, the game state should completely reset so the user can play again without refreshing the page.

<br>

**Hint:** Add a new event listener to the reset button. Inside it, you need to generate a new `randomNumber`, clear out the input field, and reset the message text back to "Start guessing...".

<details> <summary><b>Extra hint (use if stuck)</b></summary>

To clear the input field, set its `.value` back to an empty string `""`. To generate a new number, reassign your existing variable (do not use `let` again, just `randomNumber = ...`).

</details>

<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>

Here is how the reset logic should look:

```
document.querySelector("#resetBtn").addEventListener("click", () => {
    // 1. Generate new number
    randomNumber = Math.trunc(Math.random() * 20) + 1;
    
    // 2. Reset message
    document.querySelector("#message").textContent = "Start guessing...";
    
    // 3. Clear input
    document.querySelector("#guessInput").value = "";
});
```

</details>





<br>



### Advanced Optional: High Score

If a user guesses the correct answer on their first try, they build up a "streak".

- Keep track of the high score.
- If the streak is higher than the current high score, update the high score on the screen.
- If the reset button is pressed, the High Score should reset to 0.



<br>


<br>

------

# Exercise 2: Build a Table

------

In full stack development, we very often need to fetch a list of data from a backend server and display it on our frontend. 

In this exercise, we are practicing exactly that. We write a function that takes an array of Javascript objects and dynamically generate an HTML table using only Javascript.



------

<br>

## 0. The Starter Code

Create two files: `index.html` and `script.js`.

<br>



Copy below boilerplate into **index.html**. 

Notice the empty `<div id="table-container">` - **this is our target.**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Mountain Data</title>
    <style>
        table { border-collapse: collapse; width: 100%; margin-top: 20px; font-family: sans-serif; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>Mountains</h1>
    
    <div id="table-container"></div>

    <script src="script.js"></script>
</body>
</html>
```



<br>



Paste this array at the top of your **script.js** file. This simulates the JSON data you would normally receive from an API.

```javascript
const mountains = [
  { name: "Kilimanjaro", height: 5895, place: "Tanzania" },
  { name: "Everest", height: 8848, place: "Nepal" },
  { name: "Mount Fuji", height: 3776, place: "Japan" },
  { name: "Vaalserberg", height: 323, place: "Netherlands" },
  { name: "Denali", height: 6190, place: "United States" },
  { name: "Popocatepetl", height: 5465, place: "Mexico" },
  { name: "Mont Blanc", height: 4808, place: "Italy/France" }
];
```

<br>



------



<br>



### Step 1: Create the Table

Write a function called `buildTable(data)`.

Inside your function, use `document.createElement` to generate a blank `<table>` element.



<br>



### Step 2: Generate the Headers

Your table needs a header row (`<tr>`) containing `<th>` elements for each data key (name, height, place).

**Rule:** Do not hardcode the words "name" or "height". Make JavaScript figure it out by looking at the first object in the array.

<br>

**Hint:** You can extract the keys automatically using a built-in `Object` method that looks at the first item in your array.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Use `Object.keys(data[0])`. This gives you an array of strings: `["name", "height", "place"]`.

Once you have that array, run a `for...of` loop on it. Inside the loop, create a `<th>`, set its text content to the key, and append it to your header row.

</details>

<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>

Here is the code structure to build the header row:

```
let headerRow = document.createElement("tr");
let keys = Object.keys(data[0]);

for (let key of keys) {
    let th = document.createElement("th");
    th.textContent = key;
    headerRow.appendChild(th);
};

table.appendChild(headerRow);
```

</details>



<br>



### Step 3: Generate the Data Rows

Now you need to populate the table. This requires a **nested loop**.

1. **Outer loop:** Loop through the main `data` array (you can use a `for...of` loop). For every mountain, create a new `<tr>`.
2. **Inner loop:** For the current mountain, extract its values and loop through them to create the `<td>` cells.

<br>

**Hint:** `Object.values()` is very helpful for extracting the data you need for the inner loop. Pay close attention to where you create the row versus where you create the cells.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Here is the skeleton for your nested loop. Notice what happens inside versus outside the inner loop:

```
for (let mountain of data) {
    let tr = document.createElement("tr");
    let values = Object.values(mountain);

    for (let value of values) {
        // 1. Create a <td>
        // 2. Set its text content to 'value'
        // 3. Append it to 'tr'
    }

    // Append 'tr' to your main table element here
}
```

</details>

<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>

Here is how you fill in the blanks for the inner loop:

```
for (let value of values) {
    let td = document.createElement("td");
    td.textContent = value;
    tr.appendChild(td);
}

table.appendChild(tr); // Make sure this happens after the inner loop finishes!
```

</details>



<br>



### Step 4: Render to the DOM

Make sure your `buildTable` function actually returns the finished `<table>` element at the very end.

Finally, outside the function, you need to grab the `#table-container` div from your HTML and append your generated table to it.

<br>

**Hint:** Call your function, save its return value into a variable, and then use `document.querySelector` to find the container.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

At the very bottom of your `script.js` file, you can wire it up like this:

```
const myTable = buildTable(mountains); 
// Now select "#table-container" and use .appendChild(myTable) on it.
```

</details>



<br>



<br>

------

# Exercise 3: Tabs

Building a tabbed interface is a classic frontend pattern. In this exercise, we will dynamically create navigation buttons based on HTML attributes.



<br>



## 0. The Starter Code

Create our `index.html` and `script.js` files.

<br>


Copy this boilerplate into **index.html**. Notice the `data-tabname` attributes on the inner divs. This is where we will extract the button names.

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tabbed Interface</title>
    <style>
        body { font-family: sans-serif; margin: 40px; }
        .tab-panel { display: none; padding: 20px; border: 1px solid #ccc; margin-top: -1px; }
				.tab-panel.active { display: block; }
        button { padding: 10px 20px; cursor: pointer; border: 1px solid #ccc; background: #f9f9f9; }
        button.active { background: #fff; border-bottom-color: #fff; font-weight: bold; }
    </style>
</head>
<body>
    <h1>Language Guide</h1>
    
    <div id="tab-container">
        <div data-tabname="HTML" class="tab-panel">
            <h2>HTML</h2>
            <p>The skeleton of the web.</p>
        </div>
        <div data-tabname="CSS" class="tab-panel">
            <h2>CSS</h2>
            <p>The styling of the web.</p>
        </div>
        <div data-tabname="JavaScript" class="tab-panel">
            <h2>JavaScript</h2>
            <p>The behavior of the web.</p>
        </div>
    </div>

    <script src="script.js"></script>
</body>
</html>
```

<br>

Paste this skeleton at the top of our **script.js** file. We wrap the logic in a function so it can be reused on any container.



```javascript
function asTabs(node) {
    // Your logic will go here
}

asTabs(document.querySelector("#tab-container"));
```



<br>

------

<br>



### Step 1: Query the Panels and Create a Button Container

Our goal is to build a function that reads the child elements of `node`, generates a row of buttons at the top, and hides/shows the correct panels when those buttons are clicked.

Inside our `asTabs` function, you first need to find all the panel elements inside the provided `node`. Then, create a `<div>`element that will hold your generated buttons.

<br>

**Hint:** You can find the panels by querying for elements that have the `data-tabname` attribute.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Use `node.querySelectorAll("[data-tabname]")` to get a list of all the panels. Store this in a variable. Then, use `document.createElement("div")` to make your button container.

</details>



<br>





### Step 2: Generate the Buttons

Now, loop through the panels you found in Step 1. For every panel, create a `<button>`. Set the text of the button to be the value of the panel's `data-tabname` attribute. Append the button to your button container.

Finally, place the button container at the very top of the main `node`.

<br>

**Hint:** You can read custom data attributes in JavaScript using the `dataset` property. To put the button container at the top of the node, look into the `.prepend()` method.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Inside your loop: `let btn = document.createElement("button");` `btn.textContent = panel.dataset.tabname;` `buttonContainer.appendChild(btn);`

After the loop finishes: `node.prepend(buttonContainer);`

</details>



<br>





### Step 3: The Click Logic

Right now, the buttons do nothing, and no panels are visible yet.

Inside the same loop where you created the button, attach a `"click"` event listener to it. When the button is clicked, it needs to hide every panel, and then reveal only the panel associated with that specific button.

<br>

**Hint:** When a click happens, you need a quick inner loop to go through all panels and remove the "active" class. After that inner loop finishes, add the "active" class to the current panel.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Your event listener should look something like this:

```javascript
btn.addEventListener("click", () => {
    // 1. Loop through all panels and remove the "active" class
    // 2. Loop through all generated buttons and remove the "active" class
    
    // 3. Add the "active" class to THIS panel
    // 4. Add the "active" class to THIS button
});
```

</details>

<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>

Because `querySelectorAll` returns a NodeList, we can loop through it. Here is the exact logic for the click event:

```javascript
btn.addEventListener("click", () => {
    // Hide all panels
    for (let p of panels) {
        p.classList.remove("active");
    }
    
    // Remove active class from all buttons
    for (let b of buttonContainer.children) {
        b.classList.remove("active");
    }
    
    // Show the clicked panel and highlight the button
    panel.classList.add("active");
    btn.classList.add("active");
});
```

</details>



<br>





### Step 4: Set the Default State

When the page first loads, the interface looks broken because no panels are visible and no button is highlighted.

You need to trigger the initial state so only the first tab is visible.

<br>

**Hint:** You can either manually add "active" to the first panel/button, or force a click on the first generated button right after creation.

<details> <summary><b>Extra hint (use if stuck)</b></summary>

The easiest way is to select the first button inside your button container and call the `.click()` method on it at the very bottom of your `asTabs` function.

```
buttonContainer.querySelector("button").click();
```

</details>



<br>


<br>


------


# Exercise 4: Mouse Trail (Optional Exercise)

Now we take a break from forms and tables and focuses on rapid event firing (`mousemove`) and absolute CSS positioning. 

Your goal is to make a trail of dots that follow the user's mouse cursor as they move it across the screen.



<br>



## 0. The Starter Code

Create `index.html` and `script.js`.

<br>

Copy below boilerplate into our **index.html** file.

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Mouse Trail</title>
    <style>
        body { height: 100vh; margin: 0; overflow: hidden; background-color: #222; }
        .trail { 
            position: absolute; 
            width: 10px; 
            height: 10px; 
            background-color: cyan; 
            border-radius: 50%; 
            pointer-events: none; 
        }
    </style>
</head>
<body>
    <script src="script.js"></script>
</body>
</html>
```

<br>



------



<br>



### Step 1: Track the Mouse

Add an event listener to the `window` for the `mousemove` event. Whenever the mouse moves, you need to grab its current X and Y coordinates.

<br>

**Hint:** The event object passed into your callback function has two properties you need: `clientX` and `clientY`.

<details> <summary><b>Extra hint (use if stuck)</b></summary>
```
window.addEventListener("mousemove", (event) => {
    let x = event.clientX;
    let y = event.clientY;
    // You will build the trail here
});
```

</details>



<br>



### Step 2: Draw the Trail

Inside your event listener, create a new `<div>`, give it the class `"trail"`, and append it to `document.body`.

You must position this new div exactly where the mouse is using inline CSS (`style.left` and `style.top`).

<br>

<details> <summary><b>Extra hint (use if stuck)</b></summary> CSS left and top properties require a unit. You cannot just assign them a number; you must add "px" to the end of the coordinate. </details>



<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>


```javascript
let dot = document.createElement("div");
dot.className = "trail";
dot.style.left = event.clientX + "px";
dot.style.top = event.clientY + "px";
document.body.appendChild(dot);
```

</details>



<br>



### Step 3: Prevent DOM Overload

If you move your mouse around for a minute, you will create 10,000 `<div>` elements and crash the browser.

You need to limit the trail. The cleanest way is to create a fixed number of dots (e.g., 20) upfront, store them in an array, and cycle through them, moving the oldest dot to the newest mouse position.

<br>

<details> <summary><b>Extra hint (use if stuck)</b></summary> Set up a global array and a counter variable outside your event listener. Instead of creating a new &lt;div&gt; on every movement, select the next one in your array and just update its style.left and style.top. </details>



<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>

Here is the structural logic for a cycling trail:

```
const dots = [];
for (let i = 0; i < 20; i++) {
    let dot = document.createElement("div");
    dot.className = "trail";
    document.body.appendChild(dot);
    dots.push(dot);
}

let currentDot = 0;

window.addEventListener("mousemove", (event) => {
    let dot = dots[currentDot];
    dot.style.left = event.clientX + "px";
    dot.style.top = event.clientY + "px";
    
    currentDot = (currentDot + 1) % dots.length; // Cycles back to 0 when it hits 20
});
```

</details>

<br>


<br>

------

# Exercise 5: The Balloon (Optional Exercise)

This exercise focuses on computed CSS properties and handling keyboard events. 

You have a text balloon ("O"). When you press the up arrow on your keyboard, it should inflate (font size increases by 10%). When you press the down arrow, it should deflate (font size decreases by 10%).

If it gets bigger than `150px`, it pops (changes text to "POP!" and stops responding to keys).



<br>



## 0. The Starter Code

Create `index.html` and `script.js`.

<br>

Copy below boilerplate into our **index.html** file. 

```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Balloon</title>
    <style>
        body { display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        p { font-size: 30px; }
    </style>
</head>
<body>
    <p id="balloon">O</p>

    <script src="script.js"></script>
</body>
</html>
```



<br>

------



<br>



### Step 1: The Keyboard Listener

Add an event listener to the `window` for `keydown`. Check which key was pressed.

<br>

**Hint:** Console.log the event object and look for a property that tells you which key was just pressed.



<details> <summary><b>Extra hint (use if stuck)</b></summary> The event object has a `.key` property. You are looking for the exact strings "ArrowUp" and "ArrowDown". </details>



<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary> 

```
window.addEventListener("keydown", (event) => {
    if (event.key === "ArrowUp") {
        // Inflate
    } else if (event.key === "ArrowDown") {
        // Deflate
    }
});
```

</details>



<br>



### Step 2: Read the Current Size

To increase the size by 10%, you first need to know how big it currently is. You cannot just read `balloon.style.fontSize` because that only reads inline styles, not CSS stylesheets.

<br>

**Hint:** Read the current size inside the keydown handler each time a key is pressed. You must use the `window.getComputedStyle()` method to read the actual rendered font size, and then use `parseFloat()` to strip away the `"px"` so you can do math on it.

<details> <summary><b>Extra hint (use if stuck)</b></summary>
```
let balloon = document.querySelector("#balloon");
let currentSize = window.getComputedStyle(balloon).fontSize;
let sizeNumber = parseFloat(currentSize);
```

</details>



<br>



### Step 3: Change the Size and Stop Scrolling

Once you have the number, multiply it by `1.1` (for up) or `0.9` (for down), and apply it back to `balloon.style.fontSize` (do not forget to add `"px"` back on!).

Also, pressing the arrow keys normally scrolls the browser window. You need to stop that native behavior.

<br>

**Hint:** Notice how pressing the arrow keys makes the whole browser window scroll? You need to tell Javascript to block that native behavior.

<details> <summary><b>Extra hint (use if stuck)</b></summary> Call `event.preventDefault()` inside your keydown listener before you change the font size. </details>

<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>
```
if (event.key === "ArrowUp") {
    event.preventDefault();
    balloon.style.fontSize = (sizeNumber * 1.1) + "px";
}
```

</details>



<br>



### Step 4: The Pop

Before you apply the new size, check if it is greater than `150`. If it is, change the balloon's `textContent` to `"POP!"` and remove the event listener so the game ends.

<br>

**Hint:** To remove an event listener, you cannot use an anonymous function `(event) => { ... }`. You must declare a named function first, pass it to `addEventListener`, and later pass it to `removeEventListener`.

<details> <summary><b>Extra hint (use if stuck)</b></summary>
To remove a keydown listener, JavaScript needs the **same function reference**.  
**So:** create a named function (for example `handleArrowKeys`), pass it to `addEventListener`, and when the balloon pops, pass that same name to `removeEventListener`.

</details>

<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>


Change your structure to this:

```
function handleArrowKeys(event) {
    // Your size logic here...
    
    if (newSize > 150) {
        balloon.textContent = "POP!";
        window.removeEventListener("keydown", handleArrowKeys);
    } else {
        balloon.style.fontSize = newSize + "px";
    }
}

window.addEventListener("keydown", handleArrowKeys);
```

</details>

<br>


<br>


------

# Exercise 6: Live Search and Filter Dashboard (Super Optional Exercise)

In real-world applications (and at exams:), you rarely just display a static list. Users need to search, sort, and filter the data dynamically. 

This challenge is open-ended and more difficult. You are given the data and the required functionality, but how you write the logic and structure is up to you.

We are building a dynamic dashboard. When the user types in the search box or changes the dropdown, the list of drones shown on the screen should instantly update to match the filters.

<br>





## 0. The Starter Code

Create `index.html` and `script.js`.

<br>

Copy below into our **index.html**

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
        
        /* You will dynamically generate these cards */
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

Copy below into our **script.js**

```javascript
const drones = [
    { id: 1, model: "SkyRider X1", battery: 85, status: "active" },
    { id: 2, model: "SkyRider X2", battery: 12, status: "maintenance" },
    { id: 3, model: "AeroDelivery Pro", battery: 100, status: "active" },
    { id: 4, model: "AeroDelivery Pro", battery: 45, status: "active" },
    { id: 5, model: "HeavyLift V8", battery: 0, status: "retired" },
    { id: 6, model: "Scout Mini", battery: 92, status: "active" },
    { id: 7, model: "Scout Mini", battery: 8, status: "maintenance" },
    { id: 8, model: "SkyRider X2", battery: 88, status: "active" },
    { id: 9, model: "HeavyLift V8", battery: 65, status: "active" },
    { id: 10, model: "NanoSwarm", battery: 15, status: "maintenance" },
    { id: 11, model: "NanoSwarm", battery: 100, status: "active" },
    { id: 12, model: "AeroDelivery Pro", battery: 0, status: "retired" },
    { id: 13, model: "SkyRider X1", battery: 5, status: "maintenance" },
    { id: 14, model: "AgriScout", battery: 76, status: "active" },
    { id: 15, model: "AgriScout", battery: 42, status: "active" },
    { id: 16, model: "RescueHawk", battery: 100, status: "active" },
    { id: 17, model: "HeavyLift V8", battery: 0, status: "retired" }
];
```



<br>

------



<br>







### Step 1: Render the Initial State

Write a function that takes an array of drones and renders them as HTML "cards" inside the `#dashboard` container.

Call this function when the page loads so the user sees all 17 drones immediately.

<br>

**Hint:** You did this in Exercise 2 (Build a Table). This time, instead of `<tr>` and `<td>`, you are creating `<div>` elements, adding the `"card"` class, and inserting text. 

<details> <summary><b>Extra hint (use if stuck)</b></summary>
Write a `renderDrones(dataArray)` function. It should clear old cards first. The fastest way to wipe a container is `dashboard.innerHTML = "";`. 

Then inside it, loop through `dataArray`. For each drone, create a div, set its `innerHTML` to a formatted string (using template literals \`) containing the drone's model, battery, and status, and append it to the dashboard.

</details>



<br>



### Step 2: Live Text Search

Add an event listener to the search input. Whenever the user types a keystroke, filter the drones to only show models that include the typed text.

<br>

**Hint:** Think about how to trigger code every time the user types a keystroke. Also, remember that JavaScript is case-sensitive ("sky" is not the same as "Sky").

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Listen for the `"input"` event on your search box. Inside, use the array `.filter()` method, and consider converting both the drone model and the search text to lowercase (`.toLowerCase()`) so they match easily.

</details>



<br>



### Step 3: The Dropdown Filter

Add an event listener to the `<select>` element. When it changes, show only the drones that match the selected status (or show all if "all" is selected).

<br>

**Hint:** Which event fires when a user selects a new option from a `<select>` dropdown?

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Listen for the `"change"` event.

</details>



<br>



### Step 4: Combine the Filters

If a user searches for "Scout" AND selects "maintenance" from the dropdown, they should only see exactly one drone.

<br>

**Hint:** If you just filter the already-filtered array, what happens when the user presses Backspace to delete a letter? How do the removed drones come back?

<details> <summary><b>Extra hint (use if stuck)</b></summary>

Do not mutate the original `drones` array. Every time an event fires, start fresh with the full, original array and run it through *both* filters consecutively before handing the final result to your render function.

</details>

<details> <summary><b>Extra extra hint (use if completely stuck)</b></summary>

Create a single, centralized function called `updateDashboard()`. Have both your input listener and your dropdown listener call this exact same function instead of doing the filtering inside the event listeners themselves.

</details>

<details> <summary><b>Okay, I'm completely lost on how to combine them. Show me the structure.</b></summary>

Inside your centralized `updateDashboard()` function:

```
// 1. Grab current values from both DOM inputs
let searchText = document.querySelector("#searchInput").value.toLowerCase();
let statusText = document.querySelector("#statusFilter").value;

// 2. Start with the original data
let filteredData = drones;

// 3. Apply text filter
if (searchText !== "") {
    filteredData = filteredData.filter(d => d.model.toLowerCase().includes(searchText));
}

// 4. Apply status filter
if (statusText !== "all") {
    filteredData = filteredData.filter(d => d.status === statusText);
}

// 5. Render the final result
renderDrones(filteredData);
```

</details>



<br>



### Possible Extensions to make:

If you finish this quickly, add these features:

- **No Results:** If a filter combination results in an empty array, render a message like "No drones match your search" instead of leaving a blank screen.
- **Sorting:** Add a button that sorts the currently visible drones by battery percentage (Highest to Lowest).

------

