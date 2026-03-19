------

# Tutorial: Validation & Exception Handling

In this tutorial, we will harden our validation-starter API. Currently, it allows users to save blank names, invalid emails, and duplicate data. It also crashes with ugly "500 Internal Server Error" messages when things go wrong.

We will fix this by implementing:

1. **Input Validation:** Rejecting bad data before it reaches our logic.
2. **Global Exception Handling:** Catching errors and returning clean, standardized JSON responses (RFC 7807 `ProblemDetail`).
3. **Defensive Programming:** Checking for existing data before saving to prevent database conflicts.



------



## Part 1: Input Validation

We use **Data Transfer Objects (DTOs)** to define exactly what data we accept. By adding validation annotations here, we ensure that our "Entity" objects (Database tables) only ever contain valid data.

### Concept: Bean Validation

Java provides standard annotations in the `jakarta.validation.constraints` package to define rules.

- `@NotBlank`: The string cannot be null or empty.
- `@Size(min=X, max=Y)`: Enforces string length.
- `@Email`: Enforces valid email format (e.g., `user@example.com`).
- `@NotNull`: The object cannot be null.

### Exercise 1: Validate User and Todo Requests

We have defined the records `CreateUserRequest` and `CreateTodoRequest`, but they currently accept anything. Your task is to restrict them.

**Instructions:**

1. Open `src/main/java/com/example/validationstarter/dto/CreateUserRequest.java`.
2. Add `@NotBlank` and `@Size(min = 3)` to the `name` field.
3. Add `@NotBlank` and `@Email` to the `email` field.
4. Open `src/main/java/com/example/validationstarter/dto/CreateTodoRequest.java`.
5. Add `@NotBlank` and `@Size(min = 3)` to the `title` field.
6. Add `@NotNull` to the `userId` field (Integer is an object, so we use NotNull).



**Code Context:**

Java

```
// src/main/java/com/example/validationstarter/dto/CreateUserRequest.java

// TODO: Import validation constraints (NotBlank, Size, Email)

public record CreateUserRequest(
    
    // TODO: Add annotation to ensure name is not blank
    // TODO: Add annotation to ensure name is at least 3 characters long
    String name,
    
    // TODO: Add annotation to ensure email is not blank
    // TODO: Add annotation to ensure this is a valid email format
    String email
) {}
```



------



## Part 2: Activating Validation in the Controller

Adding annotations to the DTO is not enough. You must explicitly tell Spring Boot to check them when a request arrives.

### Concept: The `@Valid` Annotation

When Spring Boot receives a request, it maps the JSON to your DTO. If you add the `@Valid` annotation to the method parameter, Spring will run all the checks you defined in Part 1. If any check fails, it throws a `MethodArgumentNotValidException`.

### Exercise 2: Enable Validation

**Instructions:**

1. Open `UserController.java` and `TodoController.java`.
2. Locate the `create` methods (`@PostMapping`).
3. Add the `@Valid` annotation before the `@RequestBody` parameter.



**Code Context:**

Java

```
// src/main/java/com/example/validationstarter/controller/UserController.java

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // TODO: Add the @Valid annotation to the request parameter to enable validation
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        User user = new User(request.name(), request.email());
        return new UserResponse(userService.create(user));
    }
```

> **Hint:** It should look like `public UserResponse createUser(@Valid @RequestBody CreateUserRequest request)`.



------



## Part 3: Global Exception Handling

If we stop now, a validation error will return a generic "400 Bad Request" with no details, or a "500 Error" for other issues. We need to intercept these errors and return a structured JSON response.

### Concept: `ProblemDetail` and `@RestControllerAdvice`

- **`@RestControllerAdvice`**: A class that "watches" all Controllers. If an exception is thrown anywhere, this class can catch it.
- **`ProblemDetail`**: A standardized Java object (introduced in Spring Boot 3) that represents errors in a format compliant with RFC 7807. It includes fields like `type`, `title`, `status`, `detail`, and `instance`.

### Exercise 3: Implement the Global Handler

If we don't catch exceptions manually, the API will return a generic "500 Internal Server Error" which is unhelpful for the client. We need to intercept these errors and return a structured JSON response using the **ProblemDetail** format (RFC 7807).

#### Concept: `@RestControllerAdvice`

- **`@RestControllerAdvice`**: This annotation marks a class as a global interceptor for exceptions thrown by any Controller.
- **`ProblemDetail`**: A standard way to represent problem details in HTTP APIs. It allows us to specify the status code, a title, and specific details about what went wrong.

#### Instructions:

1. Open `src/main/java/com/example/validationstarter/exception/GlobalExceptionHandler.java`.
2. **Global Setup**: Add the missing `@RestControllerAdvice` annotation to the class.
3. **Task 1 (Not Found)**: Correct the HTTP status to `404 (NOT_FOUND)`, set a clear title, and pass the exception message into the detail field.
4. **Task 2 (Conflict)**: Correct the HTTP status to `409 (CONFLICT)` for cases where data (like an email) already exists.
5. **Task 3 (Validation)**: Correct the HTTP status to `400 (BAD_REQUEST)`. Use `setProperty` to attach the map of field errors so the client knows exactly which inputs failed validation.

> **Implementation Hints:**
>
> - To set the main heading of the error, use `problemDetail.setTitle("Your Title Here")`.
> - To set the specific error message, use `problemDetail.setDetail(e.getMessage())`.
> - To attach extra data (like the `errors` map), use `problemDetail.setProperty("key", object)`.

------



#### Code Context:

Java

    // src/main/java/com/example/validationstarter/exception/GlobalExceptionHandler.java
    
    // TODO: Add annotation to make this a global exception handler (@RestControllerAdvice)
    public class GlobalExceptionHandler {
    
        // 1. Handle Resource Not Found (404)
        @ExceptionHandler(ResourceNotFoundException.class)
        public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException e) {
            // TODO: Update the status to 404 (NOT_FOUND)
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    
            // TODO: Set the title to "Resource Not Found"
            // TODO: Set the detail to the exception message (e.getMessage())
    
            return problemDetail;
        }
    
        // 2. Handle User Already Exists (409)
        @ExceptionHandler(ResourceAlreadyExistsException.class)
        public ProblemDetail handleUserAlreadyExistsException(ResourceAlreadyExistsException e) {
            // TODO: Update the status to 409 (CONFLICT)
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    
            // TODO: Set the title to "User Already Exists"
            // TODO: Set the detail to the exception message
    
            return problemDetail;
        }
    
        // 3. Handle Validation Errors (400)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
            // Extracts errors into a Map<Field, Message>
            Map<String, String> errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existing, replacement) -> existing
                    ));
    
            // TODO: Update the status to 400 (BAD_REQUEST)
            ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    
            // TODO: Set the title to "Validation Failed"
            // TODO: Set the "errors" property (problemDetail.setProperty("errors", errors))
    
            return problemDetail;
        }
    }


------



## Part 4: Service Layer Business Logic

Finally, we need to implement the logic that actually triggers these exceptions. The Service layer is responsible for enforcing business rules (e.g., "No duplicate emails").

### Concept: Defensive Programming

Before performing an action (like `save` or `delete`), we check if the conditions are met.

- **Existence Check:** Before creating a Todo for User 99, check if User 99 exists.
- **Uniqueness Check:** Before creating a User with email `a@b.com`, check if that email is already taken.

### Exercise 4: Implement Logic in Services

**Instructions:**

1. Open `UserService.java`.
   - In `create()`: Check `userRepository.existsByEmail()`. If true, throw `ResourceAlreadyExistsException`.
   - In `findById()`: Use `.orElseThrow()` to throw `ResourceNotFoundException` if the user is missing.
2. Open `TodoService.java`.
   - In `create()`: Check if the `userId` exists (using `userRepository`). If not, throw `ResourceNotFoundException`.
   - In `create()`: Check if the `title` exists (using `todoRepository`). If true, throw `ResourceAlreadyExistsException`.
   - In `delete()`: Check if the ID exists before deleting. If not, throw `ResourceNotFoundException`.



**Code Context (UserService):**

Java

```
// src/main/java/com/example/validationstarter/service/UserService.java

    public User create(User user) {
        // TODO: Check if a user with this email already exists using userRepository.existsByEmail()
        // If it does, throw ResourceAlreadyExistsException.
        
        return userRepository.save(user);
    }
```



**Code Context (TodoService):**

Java

```
// src/main/java/com/example/validationstarter/service/TodoService.java

    public Todo create(Todo todo) {
        // TODO: Check if the userId exists in userRepository. If not, throw ResourceNotFoundException.
        
        // TODO: Check if a todo with this title already exists. If yes, throw ResourceAlreadyExistsException.

        return todoRepository.save(todo);
    }
```

> **Hint:** The `existsBy...` methods return a `boolean`. You just need a simple `if` statement.



------



### Verification

Once you have finished:

1. Run the application.
2. Try to create a User with a blank name -> Should get **400 Bad Request** with details.
3. Try to create a User with the same email twice -> Should get **409 Conflict**.
4. Try to create a Todo for `userId: 9999` -> Should get **404 Not Found**.