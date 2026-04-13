
# Tutorial 1, Chapter 1: Spring Security "Basics"

## Introduction: Project Setup and Default Spring Security

<br>

> **Concept: Auto-Configuration and Default Deny Policy** <br>
When Spring Boot detects `spring-boot-starter-security` on the classpath, it enables a default "Secure by Default" setup. It registers a default `SecurityFilterChain` that applies a universal `anyRequest().authenticated()` rule. This approach is meant to protect security unaware developers - as it is significantly safer to lock down an application and force developers to intentionally open specific parts, rather than having an open application by default.

<br>
We will initialize a small Spring Boot application, add Spring Security dependencies, and without adding anything at this point, observe the framework's default protection.

<br>

**Steps:**

1. Start a Spring Boot project in IntelliJ.
2. We pick the usual suspects: **Maven**, **Java 25**, **Spring Boot 4.x**.
3. Add our dependencies: **Spring Web**, **Spring Security**.
4. Make a Controller package and create `AccountController.java` to act as our target:

Java

```
@RestController
@RequestMapping("/api")
public class AccountController {

    @GetMapping("/public/contact")
    public String getContact() {
        return "Contact us at support@bank.com";
    }

    @GetMapping("/account/myBalance")
    public String getBalance() {
        return "Your balance is $500";
    }

    @GetMapping("/admin/system-logs")
    public String getLogs() {
        return "System OK. All servers running.";
    }
}
```
<br>

**Lets see if it works:**

1. Run our Spring Boot application.
2. Look in the console. Scroll until you see an entry reading: `Using generated security password: [UUID]`. This UUID is Spring's generated password for the default user, copy whats inside the [].
3. Open a web browser and navigate to `http://localhost:8080/api/public/contact`.
4. Put in 'user' and paste the UUID.
5. Before moving on, **discuss:**
   - What happened with our request?
   - If Spring Security had defaulted to `permitAll()` instead of `anyRequest().authenticated()`, what kind of mistake would become more likely?

<br>

------

## Step 1: The In-Memory Sandbox and Cleartext Passwords

<br>

Now we will take control of the authentication process, and Spring's auto-generated console password, by implementing our own users in the servers memory. 

This is not how we would do it in a real application, but we are intentionally doing it today, so we can control one security building block at a time before moving on to using a database as in a real application.

<br>

> **Concept: The SecurityFilterChain and CSRF** <br>
In modern Spring Security, the `SecurityFilterChain` bean is the mandatory way to dictate how web traffic is secured.
>
> Inside this chain, we configure `.csrf(csrf -> csrf.disable())`. CSRF (Cross-Site Request Forgery) is a built-in defense mechanism that defaults to "enabled". When enabled, Spring generates a stateful "Synchronizer Token" and saves it in the server's session memory. It blocks all state-changing HTTP requests (POST, PUT, DELETE) unless that token is sent back.
>
> We disable it for two reasons:
>
> 1. **Short-term:** We are testing with Postman, which does not automatically handle these tokens. Leaving it enabled blocks our testing (unless using the desktop app).
> 2. **Long-term:** We are eventually building a **Stateless** app, where traditional session-based CSRF tokens do not work. Therefore, this setting will remain disabled permanently, and we will later implement a modern, stateless CSRF defense (`SameSite` cookies).

<br>
Instead of Spring's generated HTML login page, we are now testing with HTTP Basic in Postman. 
<br><br>

**Steps:**

1. Create a new package named `config`.
2. Create a class named `SecurityConfig.java`.
3. Add the following code to define your custom security beans:

Java

```
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        var inMemoryManager = new InMemoryUserDetailsManager();
        var user = User.withUsername("alice")
                .password("12345") // Cleartext password
                .authorities("ROLE_USER")
                .build();
        inMemoryManager.createUser(user);
        return inMemoryManager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); 
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
```

<br>

**In the codeblock above we choose to save passwords in plain text, never do this in production. We will change it to encrypted in the next part.**

<br>

**Lets see if it works:**

1. Restart the Spring application. Check the console logs, can you still find the `[UUID]`?.
2. Open Postman. Use either the desktop app (best imo) or set the agent to 'Browser' in the very lower right corner. Create a new `GET` request targeting `http://localhost:8080/api/account/myBalance`.
3. Leave the request as **No Auth** and send it once. It should fail because the endpoint is still protected.
4. Under the "Authorization" tab in Postman, select "Basic Auth" from the dropdown menu.
5. Enter the username `alice` and a wrong password first. What happens?
6. Now enter the correct password `12345`. Send the request again. What do we get back now?
7. In pairs, **discuss** these two questions:
   - What have we taken control over now?
   - If this exact setup accidentally reached production, which choice would be the most dangerous and why: storing users in our code(to get them in memory), using cleartext passwords, or disabling CSRF?

<br>

------

## Step 2: Moving to Database Users

<br>
We are now going to replace the in-memory sandbox and users with a real database and cryptographic hashing. We will create the users through our Spring application.

<br>


> **Concept: The Authentication Contract** <br>
The `AuthenticationManager` has no idea that we are about to delete the `InMemoryUserDetailsManager`. It only knows that it needs a generic `UserDetailsService`. By building `CustomUserDetailsService` (our database adapter), Spring uses that implementation and just continues working - **this is the power of interfaces.**
>
> Also, we will significantly harden the application by replacing `NoOpPasswordEncoder` with `BCryptPasswordEncoder`. BCrypt is a cryptographic hashing function. When a user registers, we hash their password and save the scramble. When they log in, Spring hashes their login attempt and compares the two scrambles.

<br>

**Steps:**

1. Before we move to database users, we need to add the database dependencies and configure a datasource.

1. In MySQL Workbench or DataGrip, create an empty database called `springsec_tutorial1`.

1. Open `pom.xml` and add these dependencies:

XML

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

1. Refresh Maven so IntelliJ downloads the new dependencies.

1. Open `application.properties` and add:

Properties

```
spring.datasource.url=jdbc:mysql://localhost:3306/springsec_tutorial1
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

Replace `YOUR_DB_USERNAME` and `YOUR_DB_PASSWORD` with your own MySQL credentials.

<br>

1. Create an `AppUser` Entity:

Java

```
@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role; 

    // TODO: Getters and Setters
}
```

<br>

1. Create an `UserRepository`:

Java

```
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
```

<br>

1. Open `SecurityConfig.java` and **delete** the entire `userDetailsService()` bean method you wrote in Step 1. We remove the old in-memory version so we are not accidentally testing two different user sources.
2. In `SecurityConfig.java`, update the PasswordEncoder bean to use BCrypt:

<br>

Java

```
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
```

<br>

1. Create the Database Adapter service `CustomUserDetailsService.java`:

<br>

Java

```
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPassword()) // Must be a BCrypt hash in the DB
                .authorities(appUser.getRole())
                .build();
    }
}
```

<br>

1. Create a simple `AuthController.java` and inject both the Repository and the Encoder:

<br>

Java

```
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        AppUser newUser = new AppUser();
        newUser.setEmail(request.get("email"));
        
        // Hash the password before saving!
        String hashedPwd = passwordEncoder.encode(request.get("password"));
        newUser.setPassword(hashedPwd);
        
        newUser.setRole("ROLE_USER"); // Default role
        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully");
    }
}
```

<br>

2. Open `SecurityConfig.java` and update your filter chain so `/api/auth/register` is permitted without authentication by adding `.requestMatchers("/api/auth/register").permitAll()` like this:

Java
```
@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
```

<br>

For now, this endpoint is made simple. We are focusing on the security flow, not full validation.

<br>

**Lets check it out:**

1. Restart your application. Verify in your Database GUI (e.g., MySQL Workbench) that the `users` table exists.

2. Try logging in one last time with the old Step 1 credentials (`alice` / `12345`). It should no longer work, because we are no longer loading users from memory.
3. In Postman, send a `POST` request to `http://localhost:8080/api/auth/register` with JSON body: `{"email": "customer@bank.com", "password": "12345"}`.
4. Send a second `POST` request to the same endpoint with JSON body: `{"email": "admin@bank.com", "password": "password"}`.
5. Check your database. You should now see both users saved, and neither password should be stored as plain text. They should both be BCrypt hashes.
6. Notice that both users were created with the default role `ROLE_USER`.
7. In your database, run this SQL:


```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'admin@bank.com';
COMMIT;
```
This makes admin@bank.com our admin user for the next step.

8. Open Postman. Create a new `GET` request targeting `http://localhost:8080/api/account/myBalance`.
9. Under the "Authorization" tab in Postman, select "Basic Auth" and enter `customer@bank.com` and `12345`. Send the request.
10. It should now succeed in loading the user from the database and verifying the BCrypt-hashed password.

<br>

------

## Pair Discussion/Research #1

**Format:** 15-20 minutes in pairs, later we discuss it in plenum.

Look at the BCrypt hash stored in your database for the user you just registered. It will look something like `$2a$10$...`.

1. Investigate and discuss with each other how hashing is different from encryption. Why do we use hashing for storing passwords, and is it also ok to use normal encryption?
2. Imagine a hacker steals a website's user database and gets thousands of password hashes. How do salts change that attack? What becomes harder for the attacker compared with a system without salts?

------

<br>

## Step 3: The Security Filter Chain and Roles

<br>

Define route-specific access rules based on user privileges and visualize the filter proxy.

In Step 2, we made it so users can authenticate. Now we decide what different users are allowed to access.

**Concept: The Filter Chain** <br>
Spring Security is fundamentally a series of Servlet Filters that intercept incoming HTTP requests before they reach your Controllers. The `SecurityFilterChain` bean allows us to define the exact rules of these filters. When a request targets `/api/admin/system-logs`, it passes through the authentication filters and hits the `FilterSecurityInterceptor`. The interceptor extracts the user's `GrantedAuthorities` and compares them against the `.hasRole("ADMIN")` rule. If they match, the request proceeds.

**Steps:**

1. Open your `application.properties` file and add this line to enable filter logging:`logging.level.org.springframework.security.web.FilterChainProxy=TRACE`
2. Open `SecurityConfig.java`. Update the `filterChain` bean to map our bank endpoints:

<br>

Java

```
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**", "/api/auth/**").permitAll()
                .requestMatchers("/api/account/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
```

<br>

**Lets go:**

1. Restart the server. Open Postman. Change Authorization to "No Auth".
2. Send a `GET` to `http://localhost:8080/api/public/contact`. You should get `200 OK` because this route is explicitly opened with `permitAll()`.
3. Send a `GET` to `http://localhost:8080/api/admin/system-logs`. You should get `401 Unauthorized`.
4. Change to "Basic Auth", enter `customer@bank.com` and `12345`, and send the same request again. You should get `403 Forbidden`.
5. Now change to `admin@bank.com` and `password`, and send the same request again. You should get `200 OK`.
6. Compare the two authenticated users: both are logged in, but only the admin user is allowed through. Now we have authorization, not just authentication.
8. Look at your IDE console. Thanks to the logging property we added, you will see a long list of 10+ internal filters Spring executed to authorize our request, very exciting ;)

<br>

------

## Pair Discussion #2

**Format:** 10-15 minutes in pairs, class plenum round-up later.

**The Prompt:** In Step 3, we experienced HTTP status codes blocking our access. If you try to access the logs without logging in, you get a `401 Unauthorized`. However, if you log in as the normal user (`customer@bank.com`) and try to access the admin logs, Spring returns a `403 Forbidden`.

1. Discuss and investigate with your partner the difference between HTTP 401 vs HTTP 403.
2. Why do many high-security enterprise applications configure their Spring Security filters to intentionally lie and return a 404 Not Found for unauthorized admin endpoints, rather than a 403?

<br>

------

# Chapter 2: Stateless Apps and HttpOnly JWT

## Step 4: The Stateless Problem

<br>

So far, Spring has been quietly creating a server-side session after every successful login. But stateful sessions have severe limitations for modern REST APIs - they hinder scalability and cause complex frontend configurations for CORS and CSRF. In this step, we are going to disable server-side sessions to prepare for a pure stateless setup.

<br>

**Steps:**

1. Open `SecurityConfig.java`.
2. Insert the `sessionManagement` line into your `filterChain`:

<br>

Java

```
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Add this line below to disable sessions completely:
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**", "/api/auth/**").permitAll()
                .requestMatchers("/api/account/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
```

<br>

**Lets check it out:**

1. Restart the server and open Postman.
2. Send a successful Basic Auth request to the protected logs endpoint.
3. Click the **"Headers"** tab in the *response pane* at the bottom of Postman.
4. Verify that there is **no** `Set-Cookie` header returning a `JSESSIONID`. The server has completely forgotten your login the millisecond the response was sent.
5. Now change the same request to **No Auth** and send it again.
6. You should get `401 Unauthorized`, because the previous successful request did not create a login session that Spring could reuse.

<br>

------

## Step 5: Generating a Token, XSS & CSRF

<br>

We will do so that we can create JSON Web Tokens and an endpoint to issue them securely.

In Step 4, every request had to include credentials again. Now we replace that with a token the client can send on later requests.

<br>

> **Concept: Cryptographic Trust, XSS, and SameSite CSRF Defense** <br>
A JSON Web Token (JWT) solves the stateless scaling problem by shifting the storage burden to the client. The Signature is generated by hashing the Header and Payload using a secret key only the backend possesses.
>
> **Note:** Most tutorials will tell you to place the JWT in the HTTP `Authorization` header and save it in the browser's `localStorage`. As `localStorage` is accessible via JavaScript, making it vulnerable to Cross-Site Scripting (XSS), we are instead placing the token inside an `HttpOnly` cookie. This hides the cookie from JavaScript.

> However, using cookies makes us vulnerable to Cross-Site Request Forgery (CSRF). Because our API is stateless, we cannot use Spring's default session-based CSRF tokens. Instead, we use the modern `SameSite=Strict` cookie attribute. This tells the browser: *"Never attach this cookie to an outgoing request if the request was initiated from a different domain."*

<br>

A JWT is not secret just because it looks complicated. Its payload can usually be read. The important part is that the signature stops attackers from changing it without the secret key.

<br>

**Steps:**

1. Open `pom.xml` and add the jjwt dependencies:

XML

```
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

<br>

1. Open `application.properties` and add this line:

<br>

```
security.jwt.secret=${JWT_SECRET}
```

<br>

1. Look at the top right of your IDE, next to the green Play button. Click the dropdown showing your application's name and select Edit Configurations.

2. Make sure your Spring Boot app is selected in the left panel. On the right, look for a field named Environment variables. If you don't see it, click the blue Modify options link near the top and check "Environment variables" to unhide it.

3. Inject the Secret: Click the small document icon at the far right of the Environment variables field. Click the + button to add a new row.

Name: `JWT_SECRET`

Value: `8fK29sLmQ4pX7vNcT2rY5uZaB9wHdE3q`

Click OK, and then Apply.

<br>

This keeps the JWT secret out of your source code (and off Github later;) while still letting Spring inject it into the application.

<br>

1. Create `JwtUtil.java`:

Java

```
@Component
public class JwtUtil {

    private final String secretKey;
    private final long expirationTime = 86400000; // 1 day

    public JwtUtil(@Value("${security.jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

<br>

1. Update `AuthController.java` to issue the secure cookie via the `/login` endpoint. We use `ResponseCookie` to access the modern security flags:

<br>

Notice that this time we are not using Basic Auth in Postman. We are sending credentials to our own `/login` endpoint in the request body.


Java

```
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // Keep your @PostMapping("/register") code here...

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.get("email"), request.get("password"))
        );

        String token = jwtUtil.generateToken(auth.getName());

        // Using Spring's ResponseCookie to access the SameSite flag
        ResponseCookie jwtCookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .secure(false) // Must be set to true in production (requires HTTPS)
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Strict") // Used here as our stateless CSRF defense
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body("Login successful");
    }
}
```

<br>

1. **Important Setup:** To allow `AuthenticationManager` to be injected into your controller, you must expose it as a Bean in `SecurityConfig.java`:

<br>

Java

```
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
```

<br>

**Lets see if it works:**

1. Restart the application after adding the `JWT_SECRET` environment variable.
2. In Postman, create a new `POST` request to `http://localhost:8080/api/auth/login`. Set Authorization to "No Auth". Set the Body to "raw" and "JSON", providing `{"email": "admin@bank.com", "password": "password"}`.
3. Send the request. Look at the Postman **"Cookies"** tab in the bottom response pane.
4. You should see the `JWT` cookie present. Notice that the `HttpOnly` column reads `true`.
5. Copy the token value string, go to `jwt.io` in your browser, and paste it in the debugger. You should be able to read the payload claims in plain text.
6. The readable payload does not mean forgeable token. Without the secret key, an attacker still cannot create a valid modified token with a trusted signature.

<br>

------

## Pair Discussion/research #3

**Format:** 15-20+ minutes in pairs, plenum discussion later.

**The Prompt:** We have looked at XSS and CSRF in relation to JWT cookies.

1. One of you explains XSS to the other, and the other explains CSRF. Focus on how the attack works in practice, not just a definition.
2. Discuss which attack scenario feels more dangerous or more realistic for this kind of application, and try to explain why.

<br>

------

## Step 6: The Custom Authentication Filter

Now we will attempt to intercept incoming requests and map the JWT cookie to the Spring Security Context, so Spring can learn how to read the cookie on later requests.

<br>

> **Concept: The Stateless Interceptor** <br>
Because we disabled standard sessions and hid our token inside a custom cookie, the default Spring Security filters will not recognize our authenticated users. We must build our own filter.
>
> This filter has two distinct jobs: **1. The Extraction:** It scans the incoming HTTP request for the `JWT` cookie, extracts the string, and uses `jwtUtil` to verify the cryptographic signature. **2. The Context Injection:** If valid, it pulls the user from the database adapter, builds an `Authentication` object, and manually forces it into the `SecurityContextHolder`. This tells Spring to treat the user as authenticated for the duration of this single request.

<br>

**Steps:**

1. Create `JwtAuthenticationFilter.java` extending `OncePerRequestFilter`:

<br>

Java

```
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // JOB 1: EXTRACTION
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        // JOB 2: CONTEXT INJECTION
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continue the chain
        filterChain.doFilter(request, response);
    }
}
```

1. Open `SecurityConfig.java`. Replace your current `filterChain` with this version. Notice that `.httpBasic(...)` is now gone. From this point on, protected endpoints must be reached through the JWT cookie and our custom filter.

Java

```
// Inside SecurityConfig.java
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**", "/api/auth/**").permitAll() 
                .requestMatchers("/api/account/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // Add this line to execute our filter before Spring's default authentication:
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); 
        return http.build();
    }
```

<br>

**Lets check our filter:**

1. In Postman, hit the `/login` endpoint one more time to ensure the token is fresh and sitting in Postman's cookie jar.
2. Send a `GET` request to your protected endpoint (`/api/admin/system-logs`).
3. **Crucial Step:** Ensure the "Authorization" tab in Postman is set to **"No Auth"**. We are no longer using Basic Auth.
4. The request should succeed (`200 OK`). This time the success comes from the JWT cookie, not from sending username and password again.
5. The client sends the cookie, the filter validates it, and Spring rebuilds the authenticated user for this single request.

<br>

------

## Step 7: Completing our setup (CORS)

<br>

Now we will configure Cross-Origin Resource Sharing to allow authorized frontend applications to communicate with our API. 

Our authentication flow now works, but a browser frontend on another origin will still be blocked until we configure CORS.

<br>

> **Concept: CORS and Credentials** In a modern architecture operating across different domains (e.g., a React frontend running on port 3000, Spring running on 8080), browsers enforce the Same-Origin Policy. They will block requests between these ports, so we must configure CORS.
>
> **Note:** because we are using cookies for our JWT, we must explicitly configure `setAllowCredentials(true)`. If this is false, the browser will refuse to attach the `HttpOnly` cookie when crossing origins, breaking the entire stateless security flow.

<br>

Earlier requests worked in Postman. This section must be tested in a browser, because CORS is enforced by browsers, not by Postman.

<br>

**Steps:**

1. Open `SecurityConfig.java`.
2. Add the `.cors()` configuration to your filter chain and define the rules:

Java

```
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Added CORS
            .csrf(csrf -> csrf.disable()) 
            // ... the rest of your stateless and filter configurations ...
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); 
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // The Frontend URL
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Required to allow cookies across origins
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
```

<br>

Because we use `setAllowCredentials(true)`, the allowed origin must be explicit. We cannot use `*` here.

<br>

**Lets test:** 

Testing CORS requires an actual browser context running on a different port (not Postman).

1. Open a new Terminal/Command Prompt on your computer.
2. Create an empty temporary folder: `mkdir temp-frontend` and navigate into it: `cd temp-frontend`.
3. Start a simple local web server on port 3000. If you have Python installed, run: `python3 -m http.server 3000`. If you have Node.js, run: `npx serve -p 3000`.
4. Open your web browser and go to `http://localhost:3000`.
5. Press `F12` to open the Browser Developer Tools and navigate to the **"Console"** tab.
6. Paste the following JavaScript code into the console to simulate a frontend login request:

<br>

JavaScript

```
fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'admin@bank.com', password: 'password' })
})
.then(response => console.log('Login Status:', response.status));
```

<br>

1. Press Enter. If configured correctly, it will print `Login Status: 200`. In the DevTools "Network" tab, you will see the browser handled the preflight (`OPTIONS`) request, accepted the `JWT` cookie securely, and successfully logged you into your Spring API across origins.
2. Now paste this second fetch into the browser console:

<br>

JavaScript

```
fetch('http://localhost:8080/api/admin/system-logs', {
    method: 'GET',
    credentials: 'include'
})
.then(response => response.text().then(body => console.log(response.status, body)));
```

<br>

3. If configured correctly, the protected request should now succeed because the browser includes the JWT cookie on the cross-origin request.

<br>

------

## Step 8: The "Who Am I" Endpoint

<br>

Use an endpoint to check what Spring Security sees for the current user.

We can now use the JWT cookie to access protected endpoints. In this step, we check what Spring Security actually sees for the current user.

<br>

> **Concept: The Authentication Object** We have talked about the `SecurityContextHolder`. This endpoint returns the current `Authentication` object so we can see what Spring Security knows about the user who made the request based solely on their JWT cookie.

<br>

**Step:**

1. Open `AuthController.java` and add this final endpoint:

Java

```
    @GetMapping("/me")
    public ResponseEntity<?> whoAmI() {
        // Retrieve the current user from the Security Context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(auth);
    }
```

<br>

**Final check:**

1. Restart the server.
2. In Postman, ensure you are logged in (so you have the JWT cookie).
3. Send a `GET` request to `http://localhost:8080/api/auth/me`.
4. Look at the JSON response. You will see a massive data dump. This is the `UsernamePasswordAuthenticationToken` object. You will be able to clearly identify your `principal` (username), your `authorities` (roles), and verify that `authenticated: true`.
5. This response shows that Spring accepted the JWT, identified the user, and loaded the user's roles for this request. 

<br>

**You made it to the end of the tutorial, good job!**
------

