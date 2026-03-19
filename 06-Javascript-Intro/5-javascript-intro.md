# Introduction to Javascript



## Peer quiz - Javascript WTF

**A) What will this script print? (Bonus question: What if it was String and int in Java?)**

```javascript
const x = "10";
const y = 5;

console.log(x + y);
console.log(x - y);
```

**A)** "105" then Error

**B)** 15 then 5

**C)** "105" then 5

**D)** NaN then NaN



**B) In Java, 0 is a number and false is a boolean. They are never equal. What happens here?**

```javascript
const value = 0;

if (value == false) {
    console.log("Equal");
}
if (value === false) {
    console.log("Strictly Equal");
}
```

**A)** Prints nothing

**B)** Prints "Equal"

**C)** Prints "Strictly Equal"

**D)** Prints both



**C) In Java, missing an argument is a compile error. What happens in JS?**

```javascript
function greet(name) {
    return "Hello " + name;
}

console.log(greet());
```

**A)** Compile Error

**B)** "Hello"

**C)** "Hello null"

**D)** "Hello undefined"



**D) In Java, Collections.sort() sorts numbers numerically (1, 2, 3, 10). Javascript Arrays have a .sort as well. What does it do?**

```javascript
const numbers = [1, 10, 2, 21];
numbers.sort();

console.log(numbers);
```

**A)** [1, 2, 10, 21]

**B)** [1, 10, 2, 21]

**C)** Error

**D)** [21, 10, 2, 1]



**E) In Java, strict types prevents identity crisis. In Javascript, NaN stands for "Not a number", what is its type?**

```javascript
const result = "Hello" / 2; // This results in NaN

console.log(result);
console.log(typeof result);
```

**A)** Error then Error

**B)** NaN then NaN

**C)** NaN then string

**D)** NaN then number



## Execute Javascript from Webstorm

**1. Create an empty project in webstorm**

**2. Create an HTML file**

![image-20250128101629131](https://github.com/ek-kiil/f25c-prog2/blob/main/assets/image-20250128101629131.png)

**3. A standard HTML file looks like this**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

</body>
</html>
```

**4. JavaScript can be written inside a script tag**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

  <script>
  	console.log("Hello World");
  </script>
</body>
</html>
```

**5. Open the HTML file within a browser**

![image-20250128103331754](https://github.com/ek-kiil/f25c-prog2/blob/main/assets/image-20250128103331754.png)

**6. Open the browser console**

Right click the page > Inspect 

- Mac: CMD + shift + C
- Windows: Ctrl + shift + C

![image-20250128103543301](https://github.com/ek-kiil/f25c-prog2/blob/main/assets/image-20250128103543301.png)

**7. ???**

![image-20250128103724468](https://github.com/ek-kiil/f25c-prog2/blob/main/assets/image-20250128103724468.png)

**8. Profit**



## Exercises: Execute basic JavaScript

**Most developers refer to an external script outside the HTML file such as this:**

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>

  <script src="index.js"></script>
</body>
</html>
```

**index.js**

```javascript
console.log("Hello World")
```

**This will yield the same result**



A) Implement the above and ensure that it works 

B) Add the following "use strict"- above your JavaScript code

- Read the following article: https://www.freecodecamp.org/news/how-to-use-strict-mode-in-javascript/

**index.js**

```javascript
"use strict"
console.log("Hello World")
```



# JavaScript syntax

### JavaScript variables

```javascript
//A constant that cannot be changed
const name = "Johnny";

//A function declaration
function print(nameArgument){
	//A variable that only exists within the method scope
  let nameFromArgument = nameArgument;
  console.log(nameFromArgument);
}

//This will not work
console.log(nameFromArgument);
```

### JavaScript types

```javascript
const string = "hey"; 				//String
const number = 12; 						//Number
const guests = ["Nicklas", "Jarl", "Bob", "Alice"]; //List
const nothing = null; 				//null
const undefinied = undefined; // Not instantiated
const bool = true; 						//Boolean

//Object
const person = {
  firstName:"John",
  lastName:"Mogensen"
}

let x;       // Now x is undefined
x = 5;       // Now x is a Number
x = "John";  // Now x is a String
```



# JavaScript control flow

```javascript
// Conditional Statement
function checkNumber(num) {
  if (num > 0) {
    console.log("The number is positive.");
  } else if (num < 0) {
    console.log("The number is negative.");
  } else {
    console.log("The number is zero.");
  }
}

// Loop Example: Counting down
function countdown(start) {
  console.log("Countdown:");
  while (start >= 0) {
    console.log(start);
    start--;
  }
}

//Javascript foreach loop
const numbers = [1, 2, 3, 4, 5];

// Using forEach to log each number doubled
numbers.forEach((number) => {
  console.log(number * 2);
});

// Output:
// 2
// 4
// 6
// 8
// 10
```



## Control flow exercises

**A)** Write a program that checks the length of a `password`string from the user using prompt.

The program will print:

- `Strong password` if the length is greater than 12
- `Medium password` it the length is between 8 and 12 (inclusive)
- `Weak password` if the length is less than 8

**Hint:** `prompt("Write your prompt here")` returns the input as a string.



**Requirements:**

- The password checker has its own <u>function</u>: `checkPassword` and returns one of the three strings



**B)** Write a program that takes a date in the following format: `MONTH/DAY/YEAR` fx `10/24/2022`. It should return a date in the following format: `DAY-MONTH-YEAR` fx `24-10-2022`

**NOTE:** Treat the date as a string for this exercise. Use string manipulation methods like `.substring()` or `.split()`.



**C)** Write a program that asks the user for input two times: `message` and `numberOfTimesToLog`

When calling a function it should log out the `message` the amount of times specified in `numberOfTimesToLog`

Here is an example of the output 👇

```javascript
logString('hello', 3);
// hello
// hello
// hello
```



**D)** Peter from the HR department wants us to send out a couple of emails to some recepients. The emails are  in a weird data format: `benjamin@gmail.com|peter@gmail.com|hans@gmail.com|ahmad@gmail.com|sana@gmail.com|virgeen@gmail.com|mohammed@gmail.com`

```javascript
//email-list
const emailsFromPeter = "benjamin@gmail.com|peter@gmail.com|hans@gmail.com|ahmad@gmail.com|sana@gmail.com|virgeen@gmail.com|mohammed@gmail.com"
```

- Format the list such that every e-mail is in an array
  - **Hint:** Use the [split function](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/split)

```javascript
// This function emulates sending emails to receipients
function sendEmailTo(recepient) {
	// But really it only logs out a string
	console.log('email sent to ' + recepient);
}
```



**E) Advanced**

Write a function that receives an array of cpr numbers.

The function will move all correct cpr numbers to an array and return it:

- To find the rules of a cpr number answer the questions:

  - How long should a cpr number be?

  - What are the boundaries/limits of the first 2 numbers?

  - What are the boundaries/limits of the middle 2 numbers?

  - What are the boundaries/limits of the last 2 numbers?
- A CPR number also has the symbol "-".

```javascript
// In the following example number 1 & 6 should be accepted

const strings = [
  "121256-7890",
  "987654-3210",
  "1004890123",
  "112233-44552",
  "111244-556611",
  "150690-3131",
  "150690-3152asd",     
];
```

***Hint:*** You can [cast/convert](https://dev.to/sanchithasr/7-ways-to-convert-a-string-to-number-in-javascript-4l) strings to numbers.

***Hint:*** use the `.split` method and look up `iterating an array js for loop` on google.



# Objects

```javascript
const pokemon = {
    name:"Pikachu",
    type: "Electric",
    generation: 1,
    hasEvolution: true,
    makeSound: function(){
        console.log("Pika pikachu");
    }
}

//Will log out "Pikachu"
console.log(pokemon.name);

//Will log out "Pika pikachi"
pokemon.makeSound();
```



## Object exercises

**A)** Fix the syntax issues with your partner

```javascript
const kitten = {
    fur colour: "orange",
    age "23"
};

const laptop =
    brand: "Lenovo"
    ram "5GB"
}

const phone = {
    operating system "iOS",
    hasStylus: true,
    megapixels 12
    "batteryLife": "24 hours"
```



**B)** Write a program with the following requirements:

1. Create a student object with the following properties:
   - `name` (a string)
   - `grades` (a nested object) which has `math`, `english`, and `science` (numbers).
   - A function `getAverageGrade()` that returns the average of those three grades.
2. Print the `math` grade from the nested `grades` object.
3. Call the `getAverageGrade()` method and print the result.

**Hint:** To access a property like `math` inside a function belonging to the same object, you must use `this.math`.



**C) Advanced** Write a function that counts the frequency of characters in a string using an object as data structure:

```javascript
console.log(getCharacterFrequencies('happy'));
/*
{
  characters: [
    {
      character: 'a',
      count: 1
    },
    {
      character: 'h',
      count: 1
    },
    {
      character: 'p',
      count: 2
    },
    {
      character: 'y',
      count: 1
    }
  ], length: 5
}
*/
```
