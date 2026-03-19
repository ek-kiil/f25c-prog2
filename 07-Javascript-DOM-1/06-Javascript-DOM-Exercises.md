# 6 - Javascript DOM Manipulation





# 1. Peer Quiz - DOM Edition

------





**1. HTML Input**

**HTML:**

```html
<input type="number" id="age" value="20">
```

**Javascript:**

```javascript
const input = document.querySelector("#age");
console.log(input.value + 10);
```

**Question:** What will be logged to the console? 

**A)** `30` 

**B)** `"2010"`

 **C)** `NaN` 

**D)** An error





------





**2. Changing a const**

**HTML:**

```html
<button>Submit</button>
<div>Container</div>
```

**Javascript:**

```javascript
const btn = document.querySelector("button"); // Line 1
btn.innerText = "Don't Click Me";             // Line 2
btn.style.background = "red";                 // Line 3
btn = document.querySelector("div");          // Line 4
```

**Question:** Which line causes an error? 

**A)** Line 2

**B)** Line 3

**C)** Line 4

**D)** All lines are correct





------





**3. Ghost Elements**

**HTML:**

```html
<h1>Hello F25C</h1>
```

**JavaScript:**

```javascript
const el = document.querySelector("#ghost");
console.log(el);
```

**Question:** What is logged? 

**A)** `undefined` 

**B)** `null` 

**C)** An empty array `[]` 

**D)** Program crashes





------





**4. Live HTMLCollection**

**HTML:**

```html
<div>Item 1</div>
<div>Item 2</div>
<div>Item 3</div>
```

**JavaScript:**

```javascript
const divs = document.getElementsByTagName("div"); // Returns a LIVE HTMLCollection

for (let i = 0; i < divs.length; i++) {
    divs[i].remove();
}
```

**Question:** What happens to the elements? 

**A)** All 3 divs are removed correctly 

**B)** It crashes because the list is read-only

**C)** It skips elements (Only the 1st and 3rd are removed) 

**D)** It removes the first div and then stops





------





**5. Reference Equality**

**HTML:**

```html
<div class="box"></div>
<div class="box"></div>
```

**JavaScript:**

```javascript
const listA = document.querySelectorAll("div");
const listB = document.querySelectorAll("div");

console.log(listA === listB);
```

**Question:** What does the console print? 

**A)** `true`

**B)** `false` 

**C)** `undefined`





------









# 2. Exercises - Higher Order Functions

------





**A)** **Given an array of large cats, use the `filter()` function to create a new array that only contains names with 4 LETTERS OR LESS**.

***Challenge:*** Try using an **Arrow Function** (`=>`) for cleaner syntax. But a standard function works as well!



**Javascript:**

```javascript
const cats = ["Lion", "Tiger", "Jaguar", "Leopard", "Puma", "Liger", "Cougar", "Cheetah"];

// TODO - write the function below:
const shortCatNames = 

console.log(shortCatNames); 
// Expected output: ["Lion", "Puma"]
```





------





**B)** **Given an array of numbers, use the `map()` function to create a new array where each number is tripled (multiplied by 3).**

***Challenge:*** Try using an **Arrow Function** (`=>`) for cleaner syntax. But a standard function works as well!

***Hint:*** `.map()` takes a callback function that receives each element and returns a transformed element.

***Hint:*** The result of `.map()` is a new array.

https://www.w3schools.com/Jsref/jsref_map.asp



**Javascript:**

```javascript
const numbers = [1, 2, 3, 4, 5];

// TODO - write the function below:
const tripledNumbers = 

console.log(tripledNumbers); 
// Expected output: [3, 6, 9, 12, 15]
```





------





**C)** **Research the `setTimeout()` function [link](https://www.w3schools.com/JSREF/met_win_settimeout.asp). We want to simulate a slow network request (like fetching data from a server).**

1. Print "Fetching data..." immediately.
2. Wait **5 seconds**.
3. Print "Data received!" inside the timeout function.





------





**D)** **Use a `forEach` loop (or `reduce`) to calculate the average price of the following products.**

***Hint:*** You must create a variable `let total = 0` *before* you start the loop to store the sum.

**Javascript:**

```javascript
const products = [
  { name: "Keyboard", category: "Electronics", price: 29.99 },
  { name: "T-Shirt",  category: "Clothing",    price: 9.99  },
  { name: "Mug",      category: "Kitchen",     price: 5.0   },
  { name: "TV",       category: "Electronics", price: 299.99},
  { name: "Jeans",    category: "Clothing",    price: 39.99 },
  { name: "Blender",  category: "Kitchen",     price: 45.0  },
];
```





------





**E) (Optional**) **Count how many products are in each category, such that you end up with a result like this:** 

`{ Electronics: 2, Clothing: 2, Kitchen: 2 }`.

You should **not** hardcode the categories as new categories might arise.

***Hint:*** Use an object `{}` as a data structure to store the counts.

***Hint:*** When you encounter a category for the first time, its value will be `undefined` (which you can't add 1 to), so you must check **if** it exists or provide a default value of 0 before incrementing.



------







# 3. Exercises - DOM Manipulation 

------





We want to analyze the following web page and change it by traversing the DOM.

**The Setup:** Create an `index.html` file and copy the entire below code block into it.

**HTML:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>Feline Facts</title>
    <style>
        /* Basic Reset */
        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Arial', sans-serif; }
        
        body { 
            background-color: #f7f7f7; 
            display: flex; flex-direction: column; min-height: 100vh; 
            color: #333; 
            transition: background 0.3s, color 0.3s; 
        }

        /* Dark Mode */
        body.dark-mode { background-color: #222; color: #f7f7f7; }
        body.dark-mode .fact-card { background-color: #333; color: #fff; }
        body.dark-mode h2 { color: #b19cd9; }

        /* Header */
        header { background-color: #b19cd9; padding: 1rem 2rem; display: flex; align-items: center; justify-content: space-between; }
        header .logo { font-size: 1.5rem; font-weight: bold; color: #fff; }
        nav ul { list-style: none; display: flex; gap: 1rem; }
        nav li a { text-decoration: none; color: #fff; font-weight: 600; }

        /* Hero */
        .hero { background: linear-gradient(135deg, #e1d0f9 0%, #b19cd9 100%); text-align: center; padding: 3rem 1rem; color: #4a295c; }
        .hero h1 { font-size: 2.5rem; margin-bottom: 1.5rem; }
        .hero button { padding: 0.75rem 1.5rem; font-size: 1rem; border: none; border-radius: 5px; background-color: #7c5ca7; color: #fff; cursor: pointer; }
        
        /* Main & Grid */
        main { flex: 1; padding: 2rem; max-width: 1200px; margin: 0 auto; }
        .cat-facts { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 2rem; margin-top: 2rem; }
        
        /* Fact Card */
        .fact-card { background-color: #fff; border-radius: 8px; box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1); overflow: hidden; display: flex; flex-direction: column; transition: transform 0.3s, opacity 0.5s; }
        .fact-card:hover { transform: translateY(-3px); }
        .fact-card img { width: 100%; height: 180px; object-fit: cover; }
        .fact-content { padding: 1rem; flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
        .fact-content h2 { font-size: 1.3rem; margin-bottom: 0.5rem; color: #4a295c; }
        .fact-content p { font-size: 1rem; margin-bottom: 0.75rem; line-height: 1.4; }
        .more-info { align-self: flex-start; padding: 0.5rem 1rem; background-color: #d3acef; color: #4a295c; border: none; border-radius: 4px; cursor: pointer; margin-top: 1rem;}
        
        footer { background-color: #b19cd9; text-align: center; color: #fff; padding: 1rem; }
    </style>
</head>
<body>
<header>
    <div class="logo">Feline Facts</div>
    <nav>
        <ul>
            <li><a href="#hero">Home</a></li>
            <li><a href="#facts">Facts</a></li>
        </ul>
    </nav>
</header>

<section class="hero" id="hero">
    <h1>All the Meow-tastic Info You Need</h1>
    <p>Welcome to our cat fact corner!</p>
    <button id="theme-btn">Toggle Dark Mode</button>
</section>

<main>
    <h2 style="text-align:center; color: #4a295c; margin-bottom: 1rem;">Did You Know?</h2>
    <section class="cat-facts" id="facts">
        <div class="fact-card">
            <img src="https://loremflickr.com/400/300/cat?random=1" alt="Cat">
            <div class="fact-content">
                <h2>Nap Masters</h2>
                <p>Cats can sleep up to 16 hours a day.</p>
                <button class="more-info">More Info</button>
            </div>
        </div>
        
        <div class="fact-card">
            <img src="https://loremflickr.com/401/300/cat?random=2" alt="Cat">
            <div class="fact-content">
                <h2>Communication Experts</h2>
                <p>Cats have over 100 different vocal sounds.</p>
                <button class="more-info">More Info</button>
            </div>
        </div>
        
        <div class="fact-card">
            <img src="https://loremflickr.com/402/300/cat?random=3" alt="Cat">
            <div class="fact-content">
                <h2>No Sweet Tooth</h2>
                <p>Cats are the only mammals who cannot taste sweetness due to a genetic defect.</p>
                <button class="more-info">More Info</button>
            </div>
        </div>
        
        <div class="fact-card">
            <img src="https://loremflickr.com/403/300/cat?random=4" alt="Cat">
            <div class="fact-content">
                <h2>Worst Postmen</h2>
                <p>In 1879, Belgium tried to use 37 cats to deliver mail. It was a total disaster.</p>
                <button class="more-info">More Info</button>
            </div>
        </div>
        
        <div class="fact-card">
            <img src="https://loremflickr.com/404/300/cat?random=5" alt="Cat">
            <div class="fact-content">
                <h2>CIA Spy Cat</h2>
                <p>The CIA spent $20 million training cat spies in the 60s. The first spy was hit by a taxi immediately. Very tragic.</p>
                <button class="more-info">More Info</button>
            </div>
        </div>
    </section>
</main>
<footer><p>2026 Feline Facts</p></footer>
<script src="script.js"></script>
</body>
</html>
```



**NOTE:** Near the bottom of the body we link to a .js file(should be in the same folder) called "script.js". Create this file - this is where we write our Javascript.



------



**Common Selectors Reference:**

**JavaScript:**

```javascript
document.querySelector("#some-id");       // Single element by ID
document.querySelector(".some-class");    // Single element by Class
document.querySelectorAll(".some-class"); // List of elements (NodeList)
```



------







**A) Add a "Dark Mode" feature to our cat facts site.**

1. Select the "Toggle Dark Mode" button (It has `id="theme-btn"`) and store it in a variable.

2. Add a `click` event listener to that button.

3. Inside the function, toggle the class `"dark-mode"` on the body element.

***Hint:*** You don't need to select the body manually, you can access it directly using `document.body`.

***Hint:*** To toggle dark mode use `.classList.toggle("dark-mode")`.





------





**B) Count the Cards**

1. Select all `<div>` elements with the class `"fact-card"` and store them in a variable.
2. Log the total number of cards to the console (Use `.length`).





------





**C) Highlight specific headlines based on their text.**

1. Select all `<h2>` elements inside the fact cards and save them to a variable.
2. Loop through them using `for..of` or `.forEach()`.
3. Inside the loop:
   - Create a variable (e.g. `text`) and store the current element's text using `.innerText`.
   - Write an `if / else` statement to check that text variable.
4. The if/else:
   - If the text contains the word "Cat" (or "Nap"), change the text color to **Orange**.
   - Else, change the text color to **Black**.

***Hint:*** To check for a specific word, you can use the `.includes()` method on the headline's text. **Example:** `if (text.includes("Cat"))`.

***Hint:*** You can change the style using `element.style.color = "orange"`.





------





**D) (Optional) Remove all fact cards that contain `<p>` tags with LESS THAN 10 WORDS**.

1. Select all the fact cards and store them in a variable.

2. Look at each card one by one.

3. Inside the loop, find the `<p>` tag of *that specific card*.

4. Count the words (see ***Hint*** below).

5. If it’s less than 10, remove the card.

***Hint:*** `querySelectorAll` returns a **NodeList**. You can loop over it using `for...of`. If you want to use `.filter()` instead, you must convert it to an array first using `Array.from(yourList)`.

***Hint:*** You can turn a sentence into an array of words by using `.split(" ")`. The `.length` of that new array will tell you how many words are in the paragraph.

***Hint:*** Use `.remove()` on the element you want to delete.





------





**E) (Optional) Add a new cat fact card to the end of the list.**

1. Create a variable (string) using backticks (\``) and copy the HTML structure of a card into it.

2. Select the parent container (the section with `id="facts"`).

3. Use `insertAdjacentHTML` to insert your new card string into the container.

***Hint:*** Insert it into the container using `container.insertAdjacentHTML('beforeend', yourString)`.





------







# 4. Extra Exercises (All Optional) - Event Driven Programming

------





**A) Lets try to test event.target**

1. Select the "Toggle Dark Mode" button (ID `#theme-btn`) and store it in a variable.
2. Add a `click` event listener to that variable.
3. Inside the function, log `event.target` to the console.
4. **Step 3:** Click the *edge* of the button vs the *text* inside the button. Does the target change in the console?





------





**B) We want to make the "More Info" buttons act as "Delete" buttons.**

1. Select all buttons with class `.more-info` and save it to a variable.
2. Loop through them(what loop have we used in the other exercises?) and add a `click` event listener to each.
3. When clicked, we want to remove **only the card that belongs to that button**.
4. Inside the function, find the button's parent (or grandparent) using `this.closest(".fact-card")`.
5. Call `.remove()` on that card.

***Hint:*** You can see the doc on the **[.closest() method](https://developer.mozilla.org/en-US/docs/Web/API/Element/closest)** if you are in doubt on what it does.

**Important:** You must use a standard `function() { ... }` (not an arrow function `=>`) for the event listener, otherwise `this` will not work!





------





**C) We want the card to fade out before we remove it. To do this correctly, we must listen for the CSS transition to finish.**

1. In your JS delete function: Set the card's opacity to 0 (`card.style.opacity = 0`).
2. Add a new event listener to the **card** for the event `"transitionend"`.
3. Inside *that* function, call `card.remove()`.

***Note:*** This makes it so JS waits for the visual fade to complete before deleting the element.



------

