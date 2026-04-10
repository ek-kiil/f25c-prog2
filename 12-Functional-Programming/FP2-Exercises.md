# Øvelse F2: Java Streams

<br>

Vi laver et helt almindeligt Java-projekt i IntelliJ.

I dag arbejder vi med data fra en rejseapp. Chefen vil gerne have bygget bestemte søgninger, rapporter og dashboards.

<br>

Læg denne `record` i `Main`-klassen, men uden for `main` metoden:

```java
record FlightBooking(
        String customer,
        String airline,
        String destination,
        String country,
        int price,
        int distanceKm,
        int totalMinutes,
        int checkedBags,
        int delayMinutes,
        boolean flexibleTicket,
        boolean refundable,
        boolean weekendTrip,
        double rating
) {}
```

<br>

Læg denne liste under `main` metoden:

```java
List<FlightBooking> bookings = List.of(
        new FlightBooking("Maja Lund", "SAS", "Barcelona", "Spain", 1499, 1750, 210, 1, 15, false, false, true, 4.3),
        new FlightBooking("Tariq Nasser", "Norwegian", "Berlin", "Germany", 699, 355, 70, 0, 0, false, false, false, 4.1),
        new FlightBooking("Fatima Saleh", "Lufthansa", "Rome", "Italy", 1899, 1530, 180, 1, 20, true, true, true, 4.6),
        new FlightBooking("Emil Ravn", "KLM", "Lisbon", "Portugal", 1399, 2470, 320, 1, 55, false, false, false, 3.9),
        new FlightBooking("Nora Qvist", "Ryanair", "London", "UK", 499, 955, 125, 0, 10, false, false, true, 3.7),
        new FlightBooking("Aisha Rahman", "SAS", "Paris", "France", 2599, 1025, 110, 2, 0, true, true, false, 4.8),
        new FlightBooking("Clara Bjerre", "Norwegian", "Barcelona", "Spain", 899, 1750, 215, 1, 5, true, false, true, 4.4),
        new FlightBooking("Malik Okoye", "Turkish Airlines", "Istanbul", "Turkey", 1699, 2020, 220, 1, 35, false, true, false, 4.2),
        new FlightBooking("Freja Madsen", "KLM", "Tokyo", "Japan", 8299, 8700, 940, 2, 30, true, true, false, 4.9),
        new FlightBooking("Luca Ferri", "Lufthansa", "New York", "USA", 5499, 6200, 680, 1, 90, true, true, false, 4.5),
        new FlightBooking("Sara Holst", "SAS", "Oslo", "Norway", 799, 480, 75, 0, 0, true, false, false, 4.0),
        new FlightBooking("Victor Tran", "Norwegian", "Alicante", "Spain", 1099, 2150, 230, 1, 25, false, false, true, 4.2),
        new FlightBooking("Maja Lund", "Turkish Airlines", "Bangkok", "Thailand", 4799, 8600, 760, 1, 45, false, true, false, 4.4),
        new FlightBooking("Tariq Nasser", "SAS", "Amsterdam", "Netherlands", 999, 620, 95, 0, 5, true, false, true, 4.1),
        new FlightBooking("Fatima Saleh", "KLM", "Cape Town", "South Africa", 6899, 10300, 980, 2, 70, true, true, false, 4.7),
        new FlightBooking("Sara Holst", "SAS", "Brussels", "Belgium", 899, 765, 100, 0, 0, true, false, true, 4.2),
        new FlightBooking("Clara Bjerre", "Norwegian", "Prague", "Czech Republic", 949, 635, 95, 0, 5, false, false, true, 4.3),
        new FlightBooking("Aisha Rahman", "Lufthansa", "Rome", "Italy", 1499, 1530, 175, 1, 5, true, true, true, 4.5),
        new FlightBooking("Malik Okoye", "Lufthansa", "Madrid", "Spain", 1299, 2070, 225, 1, 5, false, false, true, 4.2),
        new FlightBooking("Nora Qvist", "Norwegian", "Rome", "Italy", 1299, 1530, 190, 1, 10, true, false, true, 4.5),
        new FlightBooking("Alma Nguyen", "KLM", "Barcelona", "Spain", 1299, 1750, 205, 1, 10, true, false, true, 4.3),
        new FlightBooking("Victor Tran", "Turkish Airlines", "Athens", "Greece", 1599, 2130, 210, 1, 20, false, true, true, 4.5),
        new FlightBooking("Lea Mikkelsen", "SAS", "Amsterdam", "Netherlands", 949, 620, 100, 0, 0, true, false, true, 4.2),
        new FlightBooking("Yusuf Karim", "SAS", "Valencia", "Spain", 1399, 2050, 220, 1, 10, true, false, true, 4.4),
        new FlightBooking("Emil Ravn", "Ryanair", "Milan", "Italy", 699, 1160, 120, 0, 25, false, false, true, 3.8),
        new FlightBooking("Freja Madsen", "KLM", "Vienna", "Austria", 999, 870, 100, 0, 0, true, false, true, 4.1),
        new FlightBooking("Sara Holst", "SAS", "Paris", "France", 1199, 1025, 115, 1, 15, true, true, false, 4.2),
        new FlightBooking("Clara Bjerre", "Turkish Airlines", "Istanbul", "Turkey", 1499, 2020, 215, 1, 10, false, true, true, 4.4),
        new FlightBooking("Aisha Rahman", "KLM", "Amsterdam", "Netherlands", 1049, 620, 105, 0, 10, true, false, true, 4.3)
);
```

<br>

Læg også disse hjælpermetoder i `Main`-klassen, men uden for `main` metoden:

```java
static int actualTravelMinutes(FlightBooking booking) {
    return booking.totalMinutes() + booking.delayMinutes();
}

static double pricePer100Km(FlightBooking booking) {
    return booking.price() * 100.0 / booking.distanceKm();
}
```

<br>

## Forslag til struktur

Det bliver hurtigt forvirrende, hvis alle resultater bliver printet uden overskrift.

En enkel måde at holde styr på output er at:

- gemme hvert resultat i en variabel med et tydeligt navn
- printe en lille overskrift før resultatet

Det kan fx se sådan her ud:

```java
List<String> result1A = bookings.stream()
        // stream her
        .toList();

System.out.println("\n--- Øvelse 1A ---");
System.out.println(result1A);
```

Hvis du vil, kan du bruge samme mønster hele vejen:

<br>

---

## 1. Sortering

> **Koncept: `sorted(...)`**
>
> Brug `sorted(...)`, når du vil have streamens elementer i en bestemt rækkefølge.
>
> De mest almindelige mønstre er:
>
> - `Comparator.comparingInt(...)` når du sorterer på et `int`
> - `Comparator.comparingDouble(...)` når du sorterer på et `double`
> - `thenComparing...(...)` når du vil have en ekstra sorteringsregel
> - `reversed()` når du vil vende rækkefølgen om
>
> Eksempler:
>
> ```java
> bookings.stream()
>         .sorted(Comparator.comparingInt(FlightBooking::price))
> ```
>
> ```java
> bookings.stream()
>         .sorted(Comparator.comparingInt(Main::actualTravelMinutes)
>                 .thenComparingInt(FlightBooking::price))
> ```

<br>

> **Koncept: method references**
>
> De her to betyder det samme:
>
> ```java
> booking -> booking.price()
> FlightBooking::price
> ```
>
> De her to betyder også det samme:
>
> ```java
> booking -> actualTravelMinutes(booking)
> Main::actualTravelMinutes
> ```
>

<br>

## Øvelse 1A

Find alle weekendture til højst 1500 kr.

Sorter dem efter laveste pris først.

Map dem bagefter til tekst som fx:

```text
London - Ryanair - 499 kr.
```

<details>
<summary>Hint</summary>
Start fx sådan her:

```java
List<String> result1A = bookings.stream()
        .filter(...)
        .sorted(...)
        .map(...)
        .toList();

System.out.println("\n--- Øvelse 1A ---");
System.out.println(result1A);
```

Fyld selv reglerne ind i `filter(...)`, `sorted(...)` og `map(...)`.

</details>

<details>
<summary>Ekstra hint til `map(...)`</summary>
I `map(...)` skal du lave hver booking om til én tekstlinje.

Det kan fx se sådan her ud:

```java
.map(b -> b.destination() + " - " + b.airline() + " - " + b.price() + " kr.")
```

Den samme ide kan genbruges senere i arket:

- tag de felter I vil vise
- byg en læsbar streng af dem
</details>

<br>

## Øvelse 1B

Find alle ture på mindst 1500 km.

Sorter dem efter kortest faktisk rejsetid først.

Ved samme rejsetid skal den billigste komme først.

Brug hjælpermetoden `actualTravelMinutes(...)`.

<details>
<summary>Hint</summary>
Du kan bruge hjælpermetoden på begge måder:

```java
b -> actualTravelMinutes(b)
Main::actualTravelMinutes
  
</details>
```

<br>

## Øvelse 1C

Find de 5 bedst ratede ture.

Ved samme rating skal den korteste faktiske rejsetid komme først.

Map til tekst, så det bliver nemt at læse bagefter.

<details>
<summary>Hint</summary>
Du får brug for:

- `Comparator.comparingDouble(...)`
- `reversed()`
- `thenComparingInt(...)`
- `limit(5)`
</details>

<br>

---

## 2. Aggregating og numeriske streams

> **Koncept: aggregering**
>
> Når vi aggregerer, samler vi mange værdier til ét resultat.
>
> Typiske eksempler:
>
> - hvor mange bookinger der findes
> - samlet omsætning
> - gennemsnitlig forsinkelse
> - højeste eller laveste værdi
>
> De mest almindelige værktøjer er:
>
> - `count()` tæller elementer og returnerer `long`
> - `sum()` lægger tal sammen
> - `average()` finder et gennemsnit
> - `min()` og `max()` finder mindste eller største værdi

<br>

## Øvelse 2A

Hvor mange bookinger har en faktisk rejsetid på højst 240 minutter?

Brug `count()`.

<details>
<summary>Hint</summary>
Resultatet kan gemmes i en `long`, fordi `count()` returnerer `long`.
</details>

<br>

> **Koncept: numeriske streams**
>
> Når du vil regne på tal, er det tit smart at skifte fra en almindelig stream til en numerisk stream.
>
> Det gør vi med fx:
>
> - `mapToInt(...)`
> - `mapToDouble(...)`
>
> Så får du adgang til metoder som:
>
> - `sum()`
> - `average()`
> - `max()`
> - `min()`

<br>

## Øvelse 2B

Hvor meget omsætning kommer der fra fleksible billetter?

<details>
<summary>Hint</summary>
Filtrér først til de relevante bookinger.

Gør streamen numerisk med `mapToInt(...)`.

Resultatet kan gemmes i en `int`.

Husk at afslutte streamen med `.sum()`.
</details>

<br>

> **Koncept: Optional**
>
> `average()` og `max()` kan give et tomt resultat, hvis streamen er tom.
>
> Derfor får man en optional type tilbage.
>
> Hvis du bare vil have et tal ud, kan du bruge `.orElse(0)`.

<br>

## Øvelse 2C

Find den gennemsnitlige forsinkelse for ture til `Spain`.

Brug `average()`.

<br>

## Øvelse 2D

Find den højeste pris pr. 100 km blandt alle ture over 1000 km.

Brug hjælpermetoden `pricePer100Km(...)`.

<details>
<summary>Hint</summary>
Start med at filtrere til ture over 1000 km.

Du kan fx bruge hjælpermetoden enten med lambda eller som method ref.:

```java
Main::pricePer100Km
b -> pricePer100Km(b)
```

Brug derefter `mapToDouble(...)` og til sidst `max()`.
</details>

<br>

---

## 3. groupingBy

> **Koncept: `Collectors.groupingBy(...)`**
>
> `groupingBy(...)` lægger elementer i grupper efter en nøgle.
>
> Den funktion du sender ind, bestemmer hvilken "kasse" hvert element havner i.
>
> Eksempler:
>
> - samme flyselskab i samme gruppe
> - samme land i samme gruppe
> - samme destination i samme gruppe
>
> Der er to meget almindelige måder at bruge `groupingBy(...)` på:
>
> 1. Kun gruppering:
>
> ```java
> Map<String, List<FlightBooking>> result = bookings.stream()
>         .collect(Collectors.groupingBy(FlightBooking::airline));
> ```
>
> 2. Gruppering plus en collector, som regner videre på hver gruppe:
>
> ```java
> Map<String, Long> result = bookings.stream()
>         .collect(Collectors.groupingBy(
>                 FlightBooking::airline,
>                 Collectors.counting()
>         ));
> ```
>
> I den anden version siger vi både:
>
> - hvordan der skal grupperes
> - hvad hver gruppe skal ende som

<br>

## Øvelse 3A

Lav et `Map<String, Long>` med antal bookinger pr. flyselskab.

<details>
<summary>Hint</summary>
Brug `Collectors.groupingBy(...)`.

Første argument er nøglen, altså flyselskabet.

Andet argument er `Collectors.counting()`.
</details>

<br>

## Øvelse 3B

Lav et `Map<String, Integer>` med samlet omsætning pr. flyselskab.

<details>
<summary>Hint</summary>
Brug samme mønster som i 3A, men byt `Collectors.counting()` ud med `Collectors.summingInt(...)`.
</details>

<br>

## Øvelse 3C

Lav et `Map<String, Double>` med gennemsnitlig faktisk rejsetid pr. land.

<details>
<summary>Hint</summary>
Brug `Collectors.groupingBy(...)` sammen med `Collectors.averagingInt(...)`.

Inde i `averagingInt(...)` kan du enten bruge lambda eller method reference:

```java
Main::actualTravelMinutes
booking -> actualTravelMinutes(booking)
```
</details>

<br>

## Øvelse 3D

Lav et `Map<String, Double>` med gennemsnitlig rating pr. flyselskab.

<details>
<summary>Hint</summary>
Brug `Collectors.groupingBy(...)` sammen med `Collectors.averagingDouble(FlightBooking::rating)`.
</details>

<br>

---

## 4. Sortering efter grouping

> **Koncept: stream over et `Map`**
>
> Når du har lavet et `Map`, kan du bagefter streame over `entrySet()`.
>
> Så streamer du ikke længere over bookinger, men over grupperne.
>
> Hvis du fx har:
>
> ```java
> Map<String, Integer> revenueByAirline
> ```
>
> så er hver `entry` én gruppe:
>
> - `entry.getKey()` er fx `"SAS"`
> - `entry.getValue()` er fx `5896`
>
> Det er godt til leaderboards og rapporter.
>
> Eksempel:
>
> ```java
> revenueByAirline.entrySet().stream()
>         .map(entry -> entry.getKey() + " - " + entry.getValue())
>         .toList();
> ```

<br>

## Øvelse 4A

Lav en top 3 over flyselskaber med højest omsætning.

Resultatet må gerne være en `List<String>`.

Hver streng kan fx se sådan ud:

```text
KLM - 19944 kr.
```

<details>
<summary>Hint</summary>
Brug det Map vi har lavet med omsætning pr. flyselskab.

Stream derefter over `entrySet()`, sorter efter value i faldende rækkefølge, og brug `limit(3)`.
</details>

<br>

## Øvelse 4B

Lav en liste over lande sorteret efter højeste gennemsnitlige rating først.

Map til tekst som fx:

```text
Japan - 4.9
```

<details>
<summary>Hint</summary>
Lav først et map med gennemsnitlig rating pr. land.

Start derefter et **nyt stream** med `map.entrySet().stream()`.

Sorter til sidst efter gruppens værdi.
</details>

<br>

## Øvelse 4C

Find det flyselskab med lavest gennemsnitlig forsinkelse blandt de flyselskaber, der har mindst 2 bookinger.

<details>
<summary>Hint</summary>
Lav den gerne i to trin:

1. lav først et `Map<String, List<FlightBooking>>`
2. start derefter et nyt stream over `entrySet()`
3. behold kun grupper med mindst 2 bookinger
4. regn gennemsnitlig forsinkelse ud og find den bedste gruppe
</details>

<br>

---

## 5. reduce

> **Koncept: `reduce(...)`**
>
> `reduce(...)` folder mange værdier sammen til et resultat.
>
> Du kan tænke på den som:
>
> - en startværdi
> - og en regel for hvordan det gamle resultat kombineres med det næste element
>
> Til almindelige summer er `sum()` ofte den nemmeste løsning.
>
> `reduce(...)` er især god, når du vil:
>
> - lægge noget sammen på en mere fri måde
> - bygge et samlet resultat trin for trin
>
> Eksempel med en startværdi:
>
> ```java
> int total = bookings.stream()
>         .map(FlightBooking::delayMinutes)
>         .reduce(0, Integer::sum);
> ```
>
> Eksempel uden startværdi:
>
> ```java
> Optional<String> result = bookings.stream()
>         .map(FlightBooking::destination)
>         .limit(3)
>         .reduce((a, b) -> a + ", " + b);
> ```

<br>

## Øvelse 5A

Find den samlede forsinkelse på tværs af alle bookinger ved hjælp af `reduce(...)`.

<details>
<summary>Hint</summary>
Du skal først bruge `.map(...)` for at trække forsinkelsen ud som tal.

Derefter kan du bruge `.reduce(0, Integer::sum)`.
</details>

<br>

## Øvelse 5B

Find det samlede antal indcheckede tasker for alle refunderbare bookinger ved hjælp af `reduce(...)`.

<details>
<summary>Hint</summary>
Tænk i tre trin:

1. filtrér til de refunderbare bookinger
2. map til antal tasker
3. brug `reduce(0, Integer::sum)`

Resultatet kan gemmes i en `int`.
</details>

<br>

---

## 6. Avancerede Streams (Optional)

> **Koncept: kombiner flere greb**
>
> I virkelige systemer lyder opgaver sjældent:
>
> - "brug `groupingBy(...)`"
> - "brug `sorted(...)`"
>
> I stedet kommer de som spørgsmål fra fx forskellige afdelinger i et firma.
>
> Vi løser dem ved at kombinere flere stream-greb i træk.
> 
> En god arbejdsgang er tit:
> 
> 1. vælg de relevante bookinger
> 2. regn eventuelle hjælpetal ud
> 3. grupper hvis der er brug for grupper
> 4. aggreger
> 5. sorter
> 6. map til noget læsbart

<br>

## Øvelse 6A

Product vil lave en "weekend leaderboard" for flyselskaber.

Tag kun weekendture med faktisk rejsetid på højst 240 minutter.

Gruppér dem efter flyselskab og lav en `List<String>` med:

- flyselskab
- antal ture
- gennemsnitlig rating
- gennemsnitlig pris pr. 100 km

Sorter bagefter listen sådan:

1. højeste gennemsnitlige rating først
2. ved samme rating: laveste gennemsnitlige pris pr. 100 km først

<details>
<summary>Hint</summary>
Start med at filtrere til de relevante weekendture.

Lav derefter et `Map<String, List<FlightBooking>>`.

Stream bagefter over grupperne og regn antal ture, gennemsnitlig rating og gennemsnitlig pris pr. 100 km ud for hver airline.
</details>

<details>
<summary>Ekstra hint</summary>
Det bliver hurtigt rodet at regne 3 forskellige ting ud direkte inde i én `.map(...)`.

Du må gerne lave en hjælpermetode, fx:

```java
static String formatAirlineStats(String airline, List<FlightBooking> trips)
```

og derefter kalde den sådan her:

```java
.map(entry -> formatAirlineStats(entry.getKey(), entry.getValue()))
```
</details>

<br>

## Øvelse 6B

Marketing vil finde kunder til en loyalitetskampagne.

Find alle kunder der:

- har rejst til mindst 2 forskellige lande
- har en gennemsnitlig rating på mindst 4.0
- har samlet forsinkelse på højst 60 minutter

Sorter dem efter lavest samlet forbrug først.

Map til tekst som fx:

```text
Tariq Nasser - 2 lande - 1698 kr. - samlet forsinkelse 5 min
```

<details>
<summary>Hint</summary>
Den her er oplagt at løse ved først at gruppere til `Map<String, List<FlightBooking>>`.

Regn derefter fx antal lande, gennemsnitlig rating, samlet forsinkelse og samlet forbrug ud for hver kundegruppe.
</details>

<details>
<summary>Ekstra hint</summary>
Når du skal finde antal forskellige lande for én kunde, kan du fx:

1. stream over kundegruppens bookinger
2. map til land
3. brug `distinct()`
4. brug `count()`
</details>

<details>
<summary>Ekstra hint 2</summary>
Hvis din `.map(...)` bliver meget lang, må du gerne flytte formatteringen ud i en hjælpermetode, fx:

```java
static String formatCustomerStats(String customer, List<FlightBooking> trips)
```
</details>

<br>

## Øvelse 6C (The God Stream;)

Product vil lave en premium-liste over de bedste flyselskaber til city breaks.

Tag kun weekendture med faktisk rejsetid på højst 240 minutter.

For hvert flyselskab skal du:

- sortere de relevante ture efter højeste rating først
- ved samme rating: laveste faktiske rejsetid først
- ved samme rejsetid: laveste pris først
- tage kun de 3 bedste ture

Brug derefter kun disse 3 ture til at regne:

- gennemsnitlig rating
- samlet pris
- gennemsnitlig pris pr. 100 km

Returnér kun flyselskaber med mindst 3 kvalificerende ture.

Sorter bagefter resultatet efter:

1. højeste gennemsnitlige rating først
2. ved samme rating: laveste gennemsnitlige pris pr. 100 km først
3. ved samme pris: laveste samlede pris først

Map til tekst som fx:

```text
Lufthansa - top 3 city breaks - rating 4.43 - 94.95 pr. 100 km - samlet pris 4697 kr.
```

<details>
<summary>Hint</summary>
Den her bliver lettest, hvis du først grupperer til `Map<String, List<FlightBooking>>`.

For hver airlinegruppe kan du så:

1. filtrere til de relevante ture
2. sortere listen
3. tage de 3 bedste
4. regne videre på de 3
</details>

<details>
<summary>Ekstra hint</summary>
Du behøver ikke løse den som ét gigantisk stream-udtryk.

Det er fint at lave opgaven i to trin:

- først et map med grupper
- derefter et stream over grupperne
</details>

<details>
<summary>Ekstra hint 2</summary>
Hvis koden bliver meget tung inde i `.map(...)`, må du også her gerne bruge en hjælpermetode til at formatere resultatet for én airlinegruppe.
</details>

<br>

---

## 7. Egen Stream (Optional)

<br>

Hvis nogen skulle blive færdige så lav jeres egen 'Smart søgning' for rejseappen.

Den skal kombinere mindst:

- en hjælpermetode
- en sortering
- en gruppering eller reduce

Eksempler på ideer:

- bedste city break
- mest stabile flyselskab
- bedste value-for-money destination
- kunder der burde få rabatkode
