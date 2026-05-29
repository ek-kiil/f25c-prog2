# RestClient: kald et eksternt API fra vores Spring app



Miniguiden viser, hvordan en Spring backend kan hente data fra et eksternt API, og hvordan vi så bruge dataene i vores egen app.

Det er nyttigt når man gerne i sin app vil kombinere ens egne data med data fra en anden service, f.eks. vejrdata eller andre offentlige API'er.

Eksemplet bruger vejrdata fra Open-Meteo:

> Link: https://open-meteo.com/en/docs

**Det er meningen man kan arbejde videre i projektet vi lavede i JPA mini guiden.** Man kan også bruge et andet Spring projekt, der så selvfølgelig skal have en service hvor de eksterne data giver mening.



## 1. Formål

`RestClient` bruges, når din backend skal hente data fra en anden server. I eksemplet sender brugeren koordinater til din backend, som henter vejrdata fra Open-Meteo.

I guiden arbejder vi med at:

- konfigurere en `RestClient`
- mappe JSON-svar til Java `record` classes
- lave en `WeatherClient`, der henter vejrdata
- teste `WeatherClient` med et lille endpoint
- bruger resultatet i en eksisterende service

**Spørgsmål:**  
Hvorfor kalder vi oftest det eksterne API fra vores backend og ikke frontend?



## 2. RestClient bean

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

Vi kan så samle div. opsætning af klienten her, og vi injecter så `RestClient` i de klasser, der skal bruge den.

**Spørgsmål:**  
Behøver vi lave opsætningen i en config klasse?



## 3. Response records

Open-Meteo returnerer et større JSON-svar. Vi skal kun bruge delen `current`.

Åbn først API-kaldet i browseren. Vores records skal følge den struktur, du ser i JSON-svaret.

Strukturen:

```
OpenMeteoResponse
└── current
    ├── temperature_2m
    ├── wind_speed_10m
    ├── wind_direction_10m
    └── precipitation
```



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

Her laver vi de records, som `WeatherClient` senere skal bruge, når API-svaret mappes med `.body(OpenMeteoResponse.class)`.



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



**Spørgsmål:**  
Hvorfor kan det være en fordel ikke at bruge API'ets response record direkte i resten af appen?



## 4. WeatherClient

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

`WeatherClient` henter data. Den står ikke for at persiste til db'en eller andre ting.

**Spørgsmål:**  
Hvorfor ikke bare bruge `WeatherClien`t til at gemme eller opdatere appen entities?



## 5. Test-endpoint



Vi laver et lille endpoint for at se, om API-kaldet virker.

Endpointet er kun til at teste `WeatherClient` isoleret. I en eksisterende app vil vi oftest bruge `WeatherClient` direkte i en service i stedet.

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



## 6. Brug data i en eksisterende service

I en rigtig app skal `WeatherClient` kaldes der, hvor appen allerede har de data, som det eksterne API skal bruge.

Det kan f.eks. være:

- når en ny entity oprettes
- når en eksisterende entity opdateres
- når en response bygges til frontend

Vi skal ikke lave en ny mini vejr app men bruge `WeatherClient` i den service-metode, hvor dataen giver mening.



**Navnene er placeholders. Brug de classes, der findes i dit projekt.**

```java
@Service
public class ExistingService {

    private final WeatherClient weatherClient;
    private final ExistingRepository existingRepository;

    public ExistingService(
            WeatherClient weatherClient,
            ExistingRepository existingRepository
    ) {
        this.weatherClient = weatherClient;
        this.existingRepository = existingRepository;
    }

    public ExistingResponse create(CreateExistingRequest request) {
        WeatherResult weather = weatherClient.getCurrentWeather(
                request.latitude(),
                request.longitude()
        );

        ExistingEntity entity = new ExistingEntity(
                request.latitude(),
                request.longitude(),
                weather.temperature(),
                weather.windSpeed()
        );

        ExistingEntity saved = existingRepository.save(entity);

        return new ExistingResponse(saved);
    }
}
```


Hvis du allerede har en entity med koordinater, kan samme mønster bruges i en update- eller detail-metode:

```
WeatherResult weather = weatherClient.getCurrentWeather(
        entity.getLatitude(),
        entity.getLongitude()
);
```



Typiske spørgsmål:

- Hvor i service-laget har appen koordinaterne?

- Skal data gemmes, eller kun bruges i en response?

- Hvilke felter fra det eksterne API skal appen faktisk bruge?

  

**Spørgsmål:**  
Hvordan kan du se forskel på kode, der handler om det eksterne API, og kode, der handler om appens egne regler?



## 7. Øvelser



1. **Flyt API-kaldet fra test-endpointet ind i en eksisterende service-metode.**
2. **Skift query parameters, så du henter et andet felt fra Open-Meteo.**
3. **Forklar hvad der sker i .retrieve().body(OpenMeteoResponse.class).**
4. **Forklar hvilke classes du ville ændre, hvis API’et skiftede navn på et JSON-felt.**
