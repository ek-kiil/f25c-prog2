
# Guide: Spring Security

Dette er den lidt kortere version af at implementere SpringSecurity i jeres eget projekt.

Vi bruger session-login hvor Spring laver en server-side session, og browseren så får en JSESSIONID cookie.
Brugere gemmes i databasen, kodeord hashes med BCrypt, og adgang styres med roller. 



Guiden er lavet så den kan tilpasses til ethvert Spring/JS projekt. Hvis i vil øve lidt så kan i f.eks. tilføje det til den eksamenscase vi lavede eller et andet projekt.

---

## 1. Roller

Start med at vælge nogle roller, der passer til din specifikke case. 
Navnene er ikke så vigtige, det vigtige er at hver rolle får adgang til de funktioner de skal bruge.

Det kunne f.eks. være:

- `USER`: en bruger, kunde, eller deltager.
- `OPERATOR`: medarbejder der arbejder i systemet.
- `ADMIN`: administrator der kan ændre i systemet.

---

## 2. Tilføj Spring Security

Tilføj i `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Genindlæs Maven(under Maven og så 'update-pilene' i venstre hjørne).

Nu er vores endpoints låst ned ligesom sidst, så skal der laves regler for hvad der skal låses op.

---

## 3. `SecurityConfig` og brugere i databasen

Opret en ny package `config` i projektet(ligesom når vi laver en package til controllers).

Heri oprettes klassen `SecurityConfig`.

```text
.../config/SecurityConfig.java
```

Tilføj:

<details>
<summary>Klik her for imports!</summary>

```java
package your.package.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
```

</details>

```java

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/app.js", "/style.css").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // Kun til udvikling - fjern i produktion
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response.setStatus(403))
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler((request, response, authentication) -> response.setStatus(200))
                        .failureHandler((request, response, exception) -> response.setStatus(401))
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200))
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

Nu skal Spring Security vide, hvordan den henter brugere fra databasen.

Det her forudsætter, at projektet allerede bruger JPA og har en database sat op, f.eks. MySQL eller H2.


Opret en `AppUser` entity, den kan bare ligge med jeres andre entities:

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

    // Getters og setters
}
```

Opret derefter et repository:

```java
package your.package.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import your.package.model.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
```

Nu laver vi den klasse, Spring Security bruger til at finde brugeren ved login:

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

Fordi vi bruger `.authorities(appUser.getRole())`, skal rollerne gemmes som `ROLE_USER`, `ROLE_OPERATOR` og `ROLE_ADMIN` i databasen.

I controllerne skriver vi stadig:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Til sidst opretter vi testbrugere, så vi har noget at logge ind med.

Opret f.eks. `TestUserSeeder` i `config`:

```java
package your.package.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import your.package.model.AppUser;
import your.package.repository.UserRepository;

@Configuration
public class TestUserSeeder {

    @Bean
    CommandLineRunner createTestUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("user@test.dk").isEmpty()) {
                AppUser user = new AppUser();
                user.setEmail("user@test.dk");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setRole("ROLE_USER");
                userRepository.save(user);
            }

            if (userRepository.findByEmail("operator@test.dk").isEmpty()) {
                AppUser operator = new AppUser();
                operator.setEmail("operator@test.dk");
                operator.setPassword(passwordEncoder.encode("operator123"));
                operator.setRole("ROLE_OPERATOR");
                userRepository.save(operator);
            }

            if (userRepository.findByEmail("admin@test.dk").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setEmail("admin@test.dk");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
            }
        };
    }
}
```

Vi tjekker først om brugeren findes, så der ikke oprettes dubletter hver gang appen starter.

**Status på sikkerheden:**

- `/api/public/**` og H2-konsollen er åbne for alle pga. `.permitAll()`.
- `/`, `/index.html`, `/app.js` og `/style.css` er åbne, **OBS: tilpas til jeres egne frontend-filer**. Bruger I fx `/main.js` eller `/css/**` skal de også åbnes.
- Alle andre endpoints kræver login.
- Brugerne hentes fra databasen.
- Kodeord bliver hashet med BCrypt.
- Når login virker, laver Spring en session og browseren får en `JSESSIONID` cookie.
- CSRF er slået fra.


Vi tager nogle valg her ift. at balancere sikkerhed med simplicitet; på den ene side bruger vi session fremfor local storage, det er automatisk HttpOnly og ondsindet Javascript kan altså ikke læse vores cookie(XSS). Vi hasher også kodeord med BCrypt.

For at gøre det simpelt opretter vi testbrugere med `CommandLineRunner`. I en større app ville brugere typisk blive oprettet gennem et admin- eller register-flow.
Vi slår også CSRF fra for at gøre `fetch`-kaldene nemmere - **i produktion ville vi slå CSRF til og så konfigurere det i frontenden**. 

Valgene er som vi har snakket om en balancegang, det vigtige er vi er bevidste om hvad vi gør.

---

## 4. Beskyt controller-metoder

Fordi vi satte `@EnableMethodSecurity` på vores SecurityConfig kan vi nu i controlleren sætte 'rolle-regler' direkte på metoderne, hvis vi f.eks. mener at kun `ADMIN` må slette en ordre så sættes `@PreAuthorize("hasRole('ADMIN')")` som her på `deleteOrder` metoden. 

**OBS: Her er eksempler lavet med `/api/orders`; i skal så selvfølgelig brug jeres egne endpoints og lave jeres egne regler ift. hvem der må tilgå hvad:**

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

Så vi bruger `"hasRole(...)"` når vi har en rolle, og `"hasAnyRole(...)"` når der er flere roller.
Vi kan også bruge `@PreAuthorize("isAuthenticated()")`, så vælges alle brugere, der er logget ind.

**OBS Ift. jeres specifikke projekt: Prøv at forklare hvilke roller der må bruge hvilke funktioner.**

---

## 5. Lav et public endpoint

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

Det virker, fordi vi åbnede `/api/public/**` i `SecurityConfig`.

Det er også praktisk med et endpoint, hvor frontenden kan tjekke, om man allerede er logget ind. Fordi `JSESSIONID`-cookien er `HttpOnly`, kan JavaScript ikke selv læse den, så vi spørger backend i stedet.

```java
// Use IntelliJ for imports

@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}
```

`/api/me` ligger ikke i `.permitAll()`, så den kræver login. Er man logget ind, får man `200 OK` med email og roller; ellers `401 Unauthorized`. Det kan frontenden bruge til at vise den rigtige side, når man genindlæser.

---

## 6. Test i frontend

Guiden tager udgangspunkt i vi allerede har en JS frontend.

**OBS: Hvis jeres frontend ikke ligger i Spring projektet men kører separat, så flyt enten filerne ind i Spring projektet, ellers så gå først til afsnit 8. om CORS. Når CORS er sat op, så vend tilbage hertil og fortsæt.**


Login-koden nedenfor skal sættes ind i jeres JS-fil, f.eks. `app.js` (eller i en separat js fil til security kald).

Vi bruger `/api/login` og `/api/logout`, fordi det er de adresser vi har sat i `SecurityConfig`.

Selve login-funktionen kan se sådan her ud.
Spring Security forventer at feltet hedder `username`, men værdien vi sender ind er brugerens email.

```js
async function login(email, password) {
    const response = await fetch("/api/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({
            username: email,
            password: password
        })
    });

    return response.ok;
}
```

Hvis login virker, returnerer backend `200 OK`. Hvis login fejler, returnerer backend `401 Unauthorized`.

For at koble det på frontenden skal I bruge:

- to inputfelter til email/password
- en login-knap der kalder `login(email, password)`
- et sted at vise fejlbeskeder

```html
<input id="email">
<input id="password" type="password">
<button id="loginButton">Login</button>
<p id="message"></p>
```

Eksempel:

```js
document.querySelector("#loginButton").addEventListener("click", async () => {
    const email = document.querySelector("#email").value;
    const password = document.querySelector("#password").value;

    const loginOk = await login(email, password);

    if (loginOk) {
        document.querySelector("#message").textContent = "Du er logget ind";
        document.querySelector("#loginButton").disabled = true;

        loadInitialData(); // TODO: Denne funktion erstattes med jeres egen funktion der henter data og renderer siden
    } else {
        document.querySelector("#message").textContent = "Forkert login";
        document.querySelector("#password").value = "";
    }
});
```

**OBS: `loadInitialData()` er kun et eksempelnavn. Brug navnet på den funktion I allerede bruger til at hente data og rendere siden som fx init(), loadData(), renderPage() eller whatever i har kaldt den, så det første data først hentes efter login.**


Det vigtige nu er, at frontenden ikke selv skal gemme passwordet. Spring opretter en session, og browseren gemmer en `JSESSIONID` cookie.

Det er en `HttpOnly` cookie, så JavaScript kan ikke læse den. Browseren sender den automatisk med på de næste requests.

Derfor skal jeres almindelige `fetch`-kald ikke laves om sålænge frontenden ligger i samme Spring-projekt(hvis ikke så gå ned til CORS sektion). Når login virker, bruger browseren sessionen automatisk.

Logout kan kalde:

```js
await fetch("/api/logout", {
    method: "POST"
});
```

Test jeres frontend med:

- `user@test.dk` / `user123`
- `operator@test.dk` / `operator123`
- `admin@test.dk` / `admin123`

Så kan man se direkte i browseren, at nogle knapper virker for nogle roller, men giver `403 Forbidden` for andre.

Nu kom vi i mål, godt klaret!

---

## 7. Spørgsmål

- Forklar forskellen på authentication og authorization?
- Hvornår får vi hhv. 401 og 403?
- Forklar hvad de forskellige roller i din app har lov til?
- Forklar session login? Hvorfor bruger vi det?
- Kan du forklare hvordan Spring henter brugeren fra databasen?

---

## 8. Ekstra: separat frontend og CORS

**Hvis frontend ligger i jeres Spring Boot-projekt, kan I springe det her afsnit over!**

Hvis frontend kører separat, så skal backend tillade requests fra frontendens adresse. **Derfor skal vi enten flytte vores frontend ind i Spring projektet eller konfigurere CORS:**

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
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index.html", "/app.js", "/style.css").permitAll()
                    .requestMatchers("/h2-console/**").permitAll() // Kun til udvikling - fjern i produktion
                    .requestMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
                    .accessDeniedHandler((request, response, accessDeniedException) -> response.setStatus(403))
            )
            .formLogin(form -> form
                    .loginProcessingUrl("/api/login")
                    .successHandler((request, response, authentication) -> response.setStatus(200))
                    .failureHandler((request, response, exception) -> response.setStatus(401))
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/api/logout")
                    .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200))
            )
            .build();
}
```

Tilføj også denne bean i `SecurityConfig`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:5173"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Content-Type"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**Her bruger vi port 5173, hvis jeres frontend kører på en anden adresse så ret til den rigtige:**

```java
config.setAllowedOrigins(List.of("http://localhost:5173"));
```

Brug helst samme hostname hele vejen, fx `localhost` til både frontend og backend. Bland ikke `localhost` og `127.0.0.1`, hvis session-cookien driller.

**I en separat frontend skal alle `fetch` kalde backendens fulde adresse og bruge `credentials: "include"`.**

Login:

```js
await fetch("http://localhost:8080/api/login", {
    method: "POST",
    credentials: "include",
    headers: {
        "Content-Type": "application/x-www-form-urlencoded"
    },
    body: new URLSearchParams({
        username: email,
        password: password
    })
});
```

Protected request:

```js
await fetch("http://localhost:8080/api/orders", {
    credentials: "include"
});
```

Logout:

```js
await fetch("http://localhost:8080/api/logout", {
    method: "POST",
    credentials: "include"
});
```

**Husk at genstarte jeres Spring app, og fortsæt fra afsnit 6.**
