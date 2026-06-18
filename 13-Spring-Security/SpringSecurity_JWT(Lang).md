# Guide: Spring Security med JWT

Denne guide viser implementering af en udvidet Spring Security-opsætning.

Vi bruger JWT-login, hvor backend laver en JWT og sender den til browseren som en `HttpOnly` cookie.
Brugere gemmes i databasen, kodeord hashes med BCrypt, secret sættes som environment variabel og adgang styres med roller som `USER`, `OPERATOR` og `ADMIN`.
CSRF er slået til, fordi vores JWT ligger i en cookie.
Guiden tager udgangspunkt i Spring Boot 4 / Spring Security 7.

---

## 1. Roller

Start med at vælge roller, der passer til jeres projekt.

Det kunne f.eks. være:

- `USER`: en almindelig bruger.
- `OPERATOR`: en medarbejder der arbejder i systemet.
- `ADMIN`: administrator der kan ændre i systemet.

I databasen gemmer vi rollerne som:

```text
ROLE_USER
ROLE_OPERATOR
ROLE_ADMIN
```

I controllerne skriver vi stadig:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Spring oversætter selv `hasRole('ADMIN')` til `ROLE_ADMIN`.

---

## 2. Tilføj dependencies

Tilføj Spring Security i `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Tilføj også JWT dependencies:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

Genindlæs Maven bagefter.

Tilføj dette i `application.properties`:

```properties
security.jwt.secret=${JWT_SECRET}
security.jwt.expiration-ms=7200000
```

Så skal `JWT_SECRET` sættes som environment variable i jeres run configuration.

Den skal være mindst 32 tegn lang, f.eks.:

```text
this-is-a-secret-key-with-at-least-32-chars
```

I IntelliJ kan den sættes sådan her:

```text
Run -> Edit Configurations -> Modify Options -> Environment variables 
JWT_SECRET=this-is-a-secret-key-with-at-least-32-chars
```

Hvis appen ikke starter og siger `Could not resolve placeholder 'JWT_SECRET'`, mangler environment variablen.


---

## 3. Brugere i databasen

Det her forudsætter, at projektet allerede bruger JPA og har en database sat op, f.eks. MySQL eller H2.

Opret en `AppUser` entity:

```java
package your.package.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    // Getters and setters
}
```

Opret repository:

```java
package your.package.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import your.package.model.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
```

Opret `CustomUserDetailsService`.
Det er den klasse Spring Security bruger, når den skal finde en bruger ved login:

```java
package your.package.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import your.package.model.AppUser;
import your.package.repository.UserRepository;

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
                .password(appUser.getPassword())
                .authorities(appUser.getRole())
                .build();
    }
}
```

Fordi vi bruger `.authorities(appUser.getRole())`, skal rollerne gemmes som `ROLE_USER`, `ROLE_OPERATOR` og `ROLE_ADMIN`.

---

## 4. JWT helper

Nu laver vi en klasse, der kan oprette og validere JWTs.

Opret `JwtUtil` i en package der f.eks. hedder `security`:

```java
package your.package.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

JWT'en indeholder brugerens email og udløbstid.
Den indeholder ikke passwordet.
Rollerne henter vi fra databasen i filteret.

---

## 5. JWT filter

Spring Security læser ikke vores `JWT` cookie automatisk.
Derfor laver vi et filter.

Filteret kigger efter `JWT` cookien, validerer tokenet og sætter brugeren ind i Spring Security.

Opret `JwtAuthenticationFilter`:

```java
package your.package.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = getJwtFromCookies(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtUtil.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | AuthenticationException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("JWT".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
```

Når filteret virker, kan Spring bruge JWT'en som login på hvert request.

I filteret henter vi brugeren fra databasen på hvert authenticated request.
Det koster et DB-opslag, men gør at ændrede roller og slettede brugere slår igennem med det samme.
Alternativt kunne roller gemmes i JWT'en, men så ville de først ændre sig, når tokenet udløber.

---

## 6. `SecurityConfig`

Opret en package `config` og lav `SecurityConfig`.

```text
.../config/SecurityConfig.java
```

Tilføj:

```java
package your.package.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import your.package.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                        .spa()
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/app.js", "/style.css", "/js/**", "/css/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

Her er valgene vi har truffet:

- Vi bruger JWT, så session er sat til `STATELESS`.
- JWT'en ligger i en `HttpOnly` cookie.
- CSRF er slået til med Spring Securitys SPA-setup.
- `XSRF-TOKEN` må gerne læses af JavaScript.
- `JWT` må ikke læses af JavaScript.
- `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/auth/csrf`, `/api/public/**` og `/error` er åbne.
- Alle andre endpoints kræver login.

Hvis I ikke bruger H2, kan H2-linjerne slettes.

---

## 7. Register, login, logout og `/me`

Opret `AuthController`.

```java
package your.package.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import your.package.model.AppUser;
import your.package.repository.UserRepository;
import your.package.security.JwtUtil;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password required"));
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already exists"));
        }

        AppUser newUser = new AppUser();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("ROLE_USER");

        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of("message", "User registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password required"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            ResponseCookie jwtCookie = ResponseCookie.from("JWT", token)
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(Duration.ofMillis(jwtUtil.getExpirationMs()))
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(Map.of("message", "Logged in"));

        } catch (AuthenticationException exception) {
            return ResponseEntity.status(401).body(Map.of("message", "Wrong login"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cleanCookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "email", authentication.getName(),
                "roles", authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}
```

Når en bruger registrerer sig, får brugeren rollen `ROLE_USER`.
Hvis en bruger skal være `OPERATOR` eller `ADMIN`, kan rollen ændres direkte i databasen:

```sql
UPDATE app_users SET role = 'ROLE_ADMIN' WHERE email = 'admin@test.dk';
UPDATE app_users SET role = 'ROLE_OPERATOR' WHERE email = 'operator@test.dk';
```

Det er fint til vores brug, i en større app ville man lave et helt admin setup.

`secure(false)` bruges fordi vi typisk kører på `http://localhost`.
I produktion skal cookies normalt bruge `secure(true)`.

---

## 8. Beskyt controller-metoder

Fordi vi satte `@EnableMethodSecurity` på `SecurityConfig`, kan vi bruge `@PreAuthorize` direkte på controller-metoder.

Eksempel:

```java
// Use IntelliJ for imports

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PreAuthorize("hasAnyRole('USER', 'OPERATOR', 'ADMIN')")
    @GetMapping
    public List<String> getOrders() {
        return List.of("Order 1", "Order 2");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public String createOrder() {
        return "Order created";
    }

    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @PutMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id) {
        return "Order " + id + " updated";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        return "Order " + id + " deleted";
    }
}
```

Brug `hasRole(...)` når der er én rolle.
Brug `hasAnyRole(...)` når flere roller må bruge samme funktion.

Frontend må gerne skjule knapper baseret på roller, men den rigtige beskyttelse skal ligge i backend.

---

## 9. Lav et public endpoint

Det er praktisk at have et endpoint, som kan testes uden login.

```java
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/status")
    public String status() {
        return "API is running";
    }
}
```

Test: `GET http://localhost:8080/api/public/status`

Vi forventer: `200 OK`.

---

## 10. Frontend

Guiden tager udgangspunkt i, at I allerede har en vanilla JS frontend.

Hvis frontenden ligger i Spring Boot-projektet, kan I bruge relative URLs som `/api/auth/login`.
Hvis frontenden kører separat, så gå først til afsnit 11 om CORS.

Læg gerne auth-koden i en separat fil, f.eks.:

```text
static/js/auth.js
```

### CSRF helper

Før alle `POST`, `PUT` og `DELETE` requests skal frontenden sende CSRF-token med.
Brug derfor `csrfFetch(...)` til alle `POST`, `PUT` og `DELETE` kald; ellers mangler `X-XSRF-TOKEN`, og Spring svarer med `403`.

```js
const API_URL = "";

async function loadCsrfToken() {
    await fetch(`${API_URL}/api/auth/csrf`, {
        credentials: "include"
    });
}

function getCookie(name) {
    const row = document.cookie
        .split("; ")
        .find(row => row.startsWith(name + "="));

    return row ? decodeURIComponent(row.substring(name.length + 1)) : null;
}

function csrfHeader() {
    const token = getCookie("XSRF-TOKEN");
    return token ? { "X-XSRF-TOKEN": token } : {};
}

async function csrfFetch(url, options = {}) {
    await loadCsrfToken();

    return fetch(`${API_URL}${url}`, {
        ...options,
        credentials: "include",
        headers: {
            ...(options.headers || {}),
            ...csrfHeader()
        }
    });
}
```

`XSRF-TOKEN` må godt læses af JavaScript.
Det er kun CSRF-tokenet.
JWT'en ligger i `JWT` cookien, og den er `HttpOnly`.

### Register

```js
async function register(email, password) {
    const response = await csrfFetch("/api/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: email,
            password: password
        })
    });

    return response.ok;
}
```

Register opretter en almindelig `ROLE_USER`.

### Login

```js
async function login(email, password) {
    const response = await csrfFetch("/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: email,
            password: password
        })
    });

    return response.ok;
}
```

Hvis login virker, sætter backend en `JWT` cookie.
Frontend skal ikke gemme tokenet selv.

### Hent current user

Efter en browser-refresh mister frontenden sin JavaScript-state.
Derfor spørger vi backend hvem der er logget ind:

```js
async function getCurrentUser() {
    const response = await fetch(`${API_URL}/api/auth/me`, {
        credentials: "include"
    });

    if (response.status === 401) {
        return null;
    }

    return await response.json();
}
```

Brug den når appen starter:

```js
const currentUser = await getCurrentUser();

if (currentUser) {
    loadInitialData(); // Erstat med jeres egen init/load/render funktion
} else {
    showLogin(); // Erstat med jeres egen login-visning
}
```

Frontend kan bruge `currentUser.roles` til at skjule knapper.
Backend skal stadig beskytte endpoints med `@PreAuthorize`.

### Logout

```js
async function logout() {
    await csrfFetch("/api/auth/logout", {
        method: "POST"
    });
}
```

Logout sletter JWT-cookien i browseren.

### Protected requests

Almindelige `GET` requests kan se sådan her ud:

```js
async function getOrders() {
    const response = await fetch(`${API_URL}/api/orders`, {
        credentials: "include"
    });

    if (response.status === 401) {
        showLogin();
        return;
    }

    if (response.status === 403) {
        alert("Du har ikke adgang");
        return;
    }

    return await response.json();
}
```

`POST`, `PUT` og `DELETE` skal bruge `csrfFetch(...)`.

---

## 11. Ekstra: separat frontend og CORS

**Hvis frontend ligger i Spring Boot-projektet, kan I springe det her afsnit over.**

Hvis frontend kører separat, skal backend tillade requests fra frontendens adresse.

Tilføj disse imports i `SecurityConfig`:

```java
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
```

Tilføj `.cors(Customizer.withDefaults())` i `securityFilterChain`:

```java
return http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .spa()
        )
        // resten af SecurityConfig fortsætter som før
        .build();
```

Tilføj også denne bean i `SecurityConfig`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

Hvis jeres frontend kører på en anden adresse, skal den rettes her:

```java
config.setAllowedOrigins(List.of("http://localhost:5173"));
```

I frontenden skal `API_URL` ændres:

```js
const API_URL = "http://localhost:8080";
```

Og alle `fetch`-kald skal bruge:

```js
credentials: "include"
```

Brug helst samme hostname hele vejen, altså bland ikke `localhost` og `127.0.0.1`.

Hvis frontend og backend ligger på helt forskellige domæner i produktion, skal cookie-indstillingerne også justeres.
Til lokale projekter er ovenstående normalt nok.

---

## 12. Status på sikkerheden

- Brugere ligger i databasen.
- Kodeord bliver hashet med BCrypt.
- JWT ligger i en `HttpOnly` cookie.
- CSRF er slået til.
- Roller styres med `@PreAuthorize`.
- `/api/auth/me` kan bruges til at gendanne login-state efter browser-refresh.

---

## 13. Spørgsmål

- Forklar forskellen på authentication og authorization.
- Hvornår får vi hhv. `401` og `403`?
- Hvad er en JWT, og hvorfor ligger den i en `HttpOnly` cookie?
- Hvorfor har vi både en JWT-cookie og et CSRF-token?
- Hvad bruger frontenden `/api/auth/me` til?
