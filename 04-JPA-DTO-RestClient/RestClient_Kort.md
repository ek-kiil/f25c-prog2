
# RestClient: kald et eksternt API fra vores Spring app

Guiden viser hvordan en Spring backend kan hente data fra et eksternt API, og hvordan dataene kan bruges i appen igen.

Eksemplet bruger vejrdata fra Open-Meteo:

> Link: https://open-meteo.com/en/docs

Mønstret vi bruger generelt: appen har nogle data, backend bruger dem til et eksternt API-kald, og svaret returneres enten i en response eller gemmes som udvalgte felter på en entity.

---

## 1. Formål

`RestClient` bruges, når din backend skal hente data fra en anden server.

I guiden arbejder vi med at:

- konfigurere en `RestClient`
- mappe JSON-svar til Java `record` classes
- lave en client-service, der henter data fra det eksterne API
- teste clienten med et lille midlertidigt endpoint
- bruge resultatet i en eksisterende service
- vurdere om API-data skal gemmes eller kun returneres

---

## 2. Find stedet i din egen app

Før du laver API-kaldet, skal du finde ud af hvor dataene passer ind i din app.

Start med at spørge:

- Hvilken entity eller request har de data, som det eksterne API skal bruge?
- Hvilken service ejer den forretningslogik, hvor API-dataene giver mening?
- Skal API-data kun vises lige nu, eller skal de gemmes som en del af appens egne data?
- Hvilket endpoint skal frontenden kalde?

**Hvis data kun skal vises her og nu, kan de bare returneres direkte i en response DTO. Hvis data beskriver en entity på et bestemt tidspunkt, kan man gemme udvalgte felter på selve entityen(altså vi beriger selve entityen med dataene fra den eksterne API).**

**Ift. brug i andet projekt:**  
Find den entity, request eller service-metode, hvor appen allerede har de data, som det eksterne API skal bruge.

---

## 3. RestClient bean

Lav en config class:

```java
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient openMeteoRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.open-meteo.com")
                .build();
    }
}
```

Vi samler opsætningen af klienten her, og vi injecter så `RestClient` i den klasse, der skal bruge den.

**Ift. brug i andet projekt:**  
Hvis du bruger et andet API, skal `baseUrl` ændres. Bean-metoden bør også have et navn, der passer til API'et. `openMeteoRestClient` er bare navnet på denne konkrete client.

Eksempel:

```java
@Bean
public RestClient myExternalApiRestClient() {
    return RestClient.builder()
            .baseUrl("https://example.com")
            .build();
}
```

---

## 4. Response records

Open-Meteo returnerer et større JSON-svar. Vi skal kun bruge delen `current`.

Åbn først API-kaldet i browseren. Vores records skal følge den struktur, du ser i JSON-svaret.

Strukturen:

```text
OpenMeteoResponse
└── current
    ├── temperature_2m
    ├── wind_speed_10m
    ├── wind_direction_10m
    └── precipitation
```

Eksempel på JSON:

```json
{
  "current": {
    "temperature_2m": 21.4,
    "wind_speed_10m": 8.3,
    "wind_direction_10m": 240,
    "precipitation": 0.0
  }
}
```

`OpenMeteoResponse` er en wrapper omkring den del af JSON-svaret, vi skal bruge, nemlig `current`.

```java
public record OpenMeteoResponse(CurrentWeather current) {
}
```

`CurrentWeather` matcher felterne inde i `current`.

```java
public record CurrentWeather(
        double temperature_2m,
        double wind_speed_10m,
        int wind_direction_10m,
        double precipitation
) {
}
```

Hvis du mapper direkte til `CurrentWeather`, vil Spring lede efter `temperature_2m` på øverste niveau. Men på øverste niveau ligger kun `current`.

Lav til sidst en DTO med navne, der passer til din egen app:

```java
public record WeatherResult(
        double temperature,
        double windSpeed,
        int windDirection,
        double precipitation
) {
}
```

Response records følger API'ets JSON. `WeatherResult` er den version, resten af appen arbejder med.

**Ift. brug i andet projekt:**  
Lav response records, der matcher det eksterne APIs JSON. `OpenMeteoResponse` og `CurrentWeather` er bare eksempler. Lav derefter en DTO med navne, der giver mening i din egen app.

**Spørgsmål:**  
Hvorfor kan det være en fordel ikke at bruge API'ets response record direkte i resten af appen?

---

## 5. WeatherClient

Lav en service, der kun håndterer kaldet til Open-Meteo.

```java
@Service
public class WeatherClient {

    private final RestClient openMeteoRestClient;

    public WeatherClient(RestClient openMeteoRestClient) {
        this.openMeteoRestClient = openMeteoRestClient;
    }

    public WeatherResult getCurrentWeather(double latitude, double longitude) {
        OpenMeteoResponse response = openMeteoRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", "temperature_2m,wind_speed_10m,wind_direction_10m,precipitation")
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);

        if (response == null || response.current() == null) {
            throw new IllegalStateException("Could not fetch weather data");
        }

        CurrentWeather current = response.current();

        return new WeatherResult(
                current.temperature_2m(),
                current.wind_speed_10m(),
                current.wind_direction_10m(),
                current.precipitation()
        );
    }
}
```

`WeatherClient` henter data. Den står ikke for at gemme entities, opdatere status eller håndtere appens egne regler.

**Ift. brug i andet projekt:**  
**OBS:** Skift *class-navn*, *metode-navn*, *path*, *query parameters* og *DTO'er*, så de passer til det API, du kalder. Hvis du ikke henter vejrdata, skal `WeatherClient` og `WeatherResult` også have andre navne.

**Spørgsmål:**  
Hvorfor bruger vi ikke også `WeatherClient` til at gemme og opdatere appens entities?

---

## 6. Midlertidigt test-endpoint

Vi kan lave et midlertidigt endpoint for at se, det er bare for at teste om API-kaldet virker.

Når du har testet, at clienten virker, har det ingen funktion, da **det rigtige API-kald sker i appens eksisterende service**.

```java
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherClient weatherClient;

    public WeatherController(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @GetMapping
    public WeatherResult getWeather(@RequestParam double lat, @RequestParam double lon) {
        return weatherClient.getCurrentWeather(lat, lon);
    }
}
```

Test:

```http
GET http://localhost:8080/api/weather?lat=34.05&lon=-118.25
```

Hvis kaldet fejler, så tjek først den eksterne URL direkte:

```http
GET https://api.open-meteo.com/v1/forecast?latitude=34.05&longitude=-118.25&current=temperature_2m,wind_speed_10m,wind_direction_10m,precipitation
```

---

**OBS:** Eksemplerne her bruger den eksamenscase vi gennemgik for noget tid siden(ligger version på Github hvis i ikke selv har et projekt), og altså SirenAlerts-navne som `Siren` og `Fire`, men de skal kun ses som konkrete eksempler. 

**I en anden app udskifter du dem med de entities, services og endpoints, der findes i dit eget projekt.**

I SirenAlerts-eksemplet har appen koordinater på `Siren` og `Fire`. Det giver to forskellige brugssituationer:

- En sirene har faste koordinater. Hvis frontend skal vise vejret lige nu ved sirenen, kan vi hente vejret og returnere det direkte.
- En brand har koordinater og et oprettelsestidspunkt. Hvis vejret skal beskrive situationen ved oprettelsen, kan vi gemme udvalgte vejrdata på `Fire`.

## 7. Eksempel A: Vis API-data i en response

Brug denne løsning, hvis API-data kun skal vises her og nu.

I SirenAlerts har en `Siren` allerede koordinater i databasen. Frontend behøver derfor ikke sende koordinaterne. Frontend kan sende sirenens `id`, og backend kan selv hente sirenen og bruge dens koordinater.

```java
@Service
public class SirenService {

    private final SirenRepository sirenRepository;
    private final WeatherClient weatherClient;

    public SirenService(SirenRepository sirenRepository, WeatherClient weatherClient) {
        this.sirenRepository = sirenRepository;
        this.weatherClient = weatherClient;
    }

    public WeatherResult getWeatherForSiren(Long id) {
        Siren siren = sirenRepository.findById(id)
                .orElseThrow();

        return weatherClient.getCurrentWeather(
                siren.getLatitude(),
                siren.getLongitude()
        );
    }
}
```

Controlleren kan kalde service-metoden:

```java
@RestController
@RequestMapping("/sirens")
public class SirenController {

    private final SirenService sirenService;

    public SirenController(SirenService sirenService) {
        this.sirenService = sirenService;
    }

    @GetMapping("/{id}/weather")
    public WeatherResult getWeatherForSiren(@PathVariable Long id) {
        return sirenService.getWeatherForSiren(id);
    }
}
```

Her gemmes vejret ikke i databasen. Det hentes, når frontend spørger efter det.

**Ift. brug i andet projekt:** 
Udskift `Siren`, `SirenRepository`, `getWeatherForSiren` og endpointet med den entity og feature, der findes i dit projekt. Mønstret er, at servicen først henter appens egne data og derefter bruger dem til API-kaldet.

---

## 8. Eksempel B: Gem API-data som snapshot på en entity

Brug denne løsning, hvis API-data beskriver situationen på det tidspunkt, hvor en entity bliver oprettet.

I SirenAlerts kan en `Fire` oprettes med koordinater. Hvis vejret ved oprettelsen er relevant senere, kan vi hente vejret og gemme udvalgte felter på `Fire`.

Eksemplet forudsætter, at `Fire` har felter som `temperature` og `windSpeed`.

```java
@Transactional
public FireResponse createFire(FireRequest request) {
    WeatherResult weather = weatherClient.getCurrentWeather(
            request.latitude(),
            request.longitude()
    );

    Fire fire = new Fire(
            request.latitude(),
            request.longitude(),
            LocalDateTime.now(),
            false
    );

    fire.setTemperature(weather.temperature());
    fire.setWindSpeed(weather.windSpeed());

    Fire saved = fireRepository.save(fire);

    return toResponse(saved);
}
```

Her gemmer vi ikke hele API-svaret. Vi gemmer kun de felter, som appen faktisk skal bruge.

**Ift. brug i andet projekt:** 
Udskift `Fire`, `FireRequest`, `FireResponse` og felterne med den entity, request og response, der giver mening i dit projekt. Tilføj kun felter på entityen, hvis dataene faktisk skal gemmes som en del af appens data.

Om API-data skal gemmes eller kun returneres afhænger af, hvad dataene betyder i appen.
- Hvis API-data kun skal vises her og nu, så returner dem i en response.
- Hvis API-data beskriver en entity på oprettelsestidspunktet, så gem de relevante værdier på entityen.
- Gem ikke hele API-svaret, medmindre du har en grund til det.
