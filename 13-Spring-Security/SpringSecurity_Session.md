
# Guide: Spring Security

Sidste gang lavede vi den helt store version, her gør vi så vores API hurtigt har:

- login
- roller
- beskyttede endpoints
- forskellige rettigheder for forskellige brugere

Guiden er lavet så den kan tilpasses til ethvert Spring/JS projekt. Hvis i vil øve lidt så kan i f.eks. tilføje det til den eksamenscase vi lavede i maj.

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

Nu er vores endpoints låst ned ligesom sidste gang, så skal vi lave regler for hvad der skal låses op.

---

## 3. `SecurityConfig`

Opret en ny package `config` i projektet(ligesom når vi laver en package til controllers).

Heri oprettes klassen `SecurityConfig`:

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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
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
    public UserDetailsService users(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build();

        UserDetails operator = User.withUsername("operator")
                .password(passwordEncoder.encode("operator123"))
                .roles("OPERATOR")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, operator, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Status på sikkerheden:**

- `/api/public/**` og h2 konsolen er åben for alle pga. .permitAll().
- `/`, `/index.html`, `/app.js` og `/style.css` er åbne, **tilpas til jeres egne frontend-filer**. Bruger I fx `/main.js` eller `/css/**` skal de også åbnes.
- Alle andre endpoints kræver login.
- Brugerne ligger i memory for at holde det simpelt.
- Passwords hashes med BCrypt.
- `/api/login` bruges til login fra frontenden.
- `/api/logout` bruges til logout fra frontenden.
- Når login virker, laver Spring en session og browseren får en `JSESSIONID` cookie.
- Hvis man kalder et protected endpoint uden login, får man `401` i stedet for en redirect.
- CSRF er slået fra.

Vi tager nogle valg her ift. at balancere sikkerhed med simplicitet; på den ene side bruger vi session fremfor local storage, det er automatisk HttpOnly og ondsindet Javascript kan altså ikke læse vores cookie(XSS). Vi hasher også kodeord med BCrypt.

For at gøre det simplere gemmer vi brugerne i memory, og hardcoder credentials for at teste vores setup - **i en rigtig app med brugere ville vi lægge brugere og roller i databasen**. 
Vi slår også CSRF fra for at gøre `fetch`-kaldene nemmere - **i produktion ville vi slå CSRF til og så konfigurere det i frontenden**. 

Valgene er som vi snakkede om i klassen en balancegang, det vigtigste er at vi er bevidste om hvad vi gør.

---

## 4. Beskyt controller-metoder

Fordi vi satte `@EnableMethodSecurity` på vores SecurityConfig kan vi nu i controlleren sætte 'rolle-regler' direkte på metoderne, hvis vi f.eks. mener at kun `ADMIN` må slette en ordre så sættes `@PreAuthorize("hasRole('ADMIN')")` som her på `deleteOrder` metoden. 

Her er eksempler på metoder med regler, **de skal så tilpasses til de metoder/regler vi ønsker i vores specifikke case:**:

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

**OBS Ift. jeres specifikke projekt: Brug lidt tid på at kunne forklare hvilke roller der må bruge hvilke funktioner.**

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

---

## 6. Test i frontend

Guiden tager udgangspunkt i vi allerede har en JS frontend.

**OBS: Hvis jeres frontend ikke ligger i Spring projektet men kører separat, så flyt enten filerne ind i Spring projektet(nemmest!), ellers så gå først til afsnit 8. om CORS. Når CORS er sat op, så vend tilbage hertil og fortsæt.**


Login-koden nedenfor skal sættes ind i jeres JS-fil, f.eks. `app.js`.

Vi bruger `/api/login` og `/api/logout`, fordi det er de adresser vi har sat i `SecurityConfig`.

Selve login-funktionen kan se sådan her ud:

```js
async function login(username, password) {
    const response = await fetch("/api/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({
            username: username,
            password: password
        })
    });

    return response.ok;
}
```

Hvis login virker, returnerer backend `200 OK`. Hvis login fejler, returnerer backend `401 Unauthorized`.

For at koble det på frontenden skal I bruge:

- to inputfelter til username/password
- en login-knap der kalder `login(...)`
- et sted at vise fejlbeskeder

```html
<input id="username">
<input id="password" type="password">
<button id="loginButton">Login</button>
<p id="message"></p>
```

Eksempel:

```js
document.querySelector("#loginButton").addEventListener("click", async () => {
    const username = document.querySelector("#username").value;
    const password = document.querySelector("#password").value;

    const loginOk = await login(username, password);

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

**OBS: `loadInitialData()` er kun et eksempelnavn. Brug navnet på den funktion I allerede bruger til at hente data og rendere siden, så det første data først hentes efter login. Hvis I ikke har sådan en funktion, kan linjen fjernes.**


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

- `user` / `user123`
- `operator` / `operator123`
- `admin` / `admin123`

Så kan man se direkte i browseren, at nogle knapper virker for nogle roller, men giver `403 Forbidden` for andre.

Nu kom vi i mål, godt klaret!

---

## 7. Spørgsmål

- Kan du forklare forskellen på authentication og authorization?
- Hvilken kode giver det med ingen login?
- Hvilken kode giver det med forkert rolle?
- Kan du forklare hvad hver rolle i din app har lov til?
- Kan du forklare session login?

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
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> response.setStatus(401))
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
        username: username,
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
