# Øvelse 2: Java Streams

<br>

Brug denne record:

```java
record Pizza(
        String name,
        int price,
        boolean vegetarian,
        boolean glutenFree,
        boolean containsNuts,
        int deliveryMinutes,
        double rating
) {}
```

<br>

Brug denne liste:

```java
List<Pizza> pizzas = List.of(
        new Pizza("Margherita", 89, true, false, false, 18, 4.4),
        new Pizza("Pepperoni", 105, false, false, false, 22, 4.6),
        new Pizza("Quattro Formaggi", 119, true, false, false, 28, 4.7),
        new Pizza("Pesto Verde", 112, true, false, true, 21, 4.5),
        new Pizza("Vegetariana GF", 124, true, true, false, 26, 4.2),
        new Pizza("Hawaii", 99, false, false, false, 20, 3.8),
        new Pizza("Capricciosa", 115, false, false, false, 25, 4.3),
        new Pizza("Marinara", 79, true, true, false, 17, 4.0)
);
```

<br>

---

## 1. Stream

Kig på den her stream:

```java
List<String> result = pizzas.stream()
        .filter(p -> !p.containsNuts())
        .sorted(Comparator.comparingInt(p -> p.deliveryMinutes()))
        .limit(3)
        .map(p -> p.name() + " (" + p.deliveryMinutes() + " min)")
        .toList();
```

## Øvelse 1

<br>

Diskuter med din sideperson:

1. hvad der kommer ind i streamen
2. hvad `filter(...)` gør her
3. hvad `sorted(...)` gør her
4. hvad `limit(3)` gør her
5. hvad `map(...)` gør her
6. hvad typen er lige før `map(...)`
7. hvad typen er til sidst
8. hvordan første element i resultatet kommer til at se ud

<br>

---

## 2. Slutpris

<br>

Kunden vil have levering, vi tilføjer en hjælpe metode.

Brug denne regel:

- pizzaer under 100 kr. får 25 kr. i levering
- pizzaer fra 100 kr. og op får gratis levering

```java
static int finalPrice(Pizza p) {
    // TODO
}
```

<br>

## Øvelse 2

<br>

Lav en stream, der finder pizzaer hvor:

- slutprisen er højst 120 kr. (brug vores hjælpemetode)
- rating er mindst 4.2

<br>

Sorter dem efter:

1. laveste slutpris først
2. ved samme slutpris: højeste rating først

<br>

Arbejd først med `Pizza`-objekterne i streamen.

Brug så `map(...)` til sidst til at lave en `List<String>`.

Hver streng kan fx se sådan ud:

```text
Margherita - slutpris 114 kr. - rating 4.4
```

<details>
<summary>Hint</summary>
Det er nemmere at sortere og filtrere på `Pizza` først og først bagefter mappe til tekst.
</details>



<details>
<summary>Ekstra hint til map-delen</summary>

```java
.map(p -> p.name() + " - slutpris " + finalPrice(p) + " kr. - rating " + p.rating())
```

</details>

<br>

---

## 3. En besværlig kunde

Nora vil gerne have en pizza som:

- er vegetarisk
- ikke indeholder nødder
- koster højst 120 kr.

Hvis der er flere muligheder, vil hun helst have den bedst ratede først.

Ved samme rating vil hun have den hurtigste levering først.

<br>

## Øvelse 3

Lav en stream, der bygger Nora's liste.

Vi arbejder først med `Pizza`-objekterne i streamen.

Brug så `map(...)` til sidst til at lave en `List<String>`.

Her kan hver pizza mappes til en tekst, som fx:

- navn
- pris
- rating
- leveringstid

Afslut med `.toList()` og print listen bagefter.

<br>

<details>
<summary>Hint</summary>
Du skal bruge mindst to `filter(...)` og én `sorted(...)`.
</details>


<details>
<summary>Ekstra hint til map-delen</summary>

```java
List<String> result = pizzas.stream()
        // filter og sorted her
        .map(p -> p.name() + " - " + p.price() + " kr.")
        .toList();

System.out.println(result);
```

</details>

<br>

---

## 4. Flere besværlige kunder

<br>

En kunde siger:

"Jeg vil gerne have noget der er rimeligt hurtigt, rimeligt billigt og rimeligt godt."

Vi kan fx oversætte det til disse krav:

- slutpris højst 120 kr.
- levering højst 22 minutter
- rating mindst 4.5

<br>

## Øvelse 4

<br>

Nu bliver det en tand mere kringlet.

Vi skal lave en stream, der finder alle pizzaer som opfylder **mindst to ud af de tre krav**.

Sorter dem bagefter sådan:

1. højeste rating først
2. ved samme rating: laveste slutpris først

<br>

Arbejd først med `Pizza`-objekterne i streamen.

Brug så `map(...)` til sidst til at lave en `List<String>`.

Hver streng kan fx indeholde:

- navn
- slutpris
- leveringstid
- rating

Afslut med `.toList()` og print listen bagefter.

<br>

<details>
<summary>Hint</summary>
Hvis tre betingelser hedder `a`, `b` og `c`, så betyder "mindst to" at mindst én af disse er sand:
`a && b`, `a && c`, `b && c`.
</details>


<details>
<summary>Ekstra hint til map-delen</summary>

```java
List<String> result = pizzas.stream()
        // filter og sorted her
        .map(p -> p.name() + " - " + finalPrice(p) + " kr. - " + p.rating())
        .toList();

System.out.println(result);
```

</details>

<br>

---

## Ekstra opgaver

<br>

### Ekstra 1: Vi laver en anbefaling

<br>

Lav din egen stream, der finder tre pizzaer du ville anbefale til en bestemt kunde.

Du vælger kundens præferencer.

Vælg mindst tre kundekrav baseret på felterne:

- vegetarian
- glutenFree
- containsNuts
- deliveryMinutes
- rating
- finalPrice

Din stream skal bruge mindst tre af disse trin:

- `filter`
- `sorted`
- `map`
- `limit`

Resultatet kan være en `List<String>`.

<br>

------

### Ekstra 2: Pizza i balance

<br>

Nu skal vi ikke bare filtrere på en regel.

Nu skal hver pizza have en score.

Giv et point for hvert af disse kriterier som pizzaen opfylder:

- slutpris `<= 120`
- levering `<= 22` minutter
- rating `>= 4.5`
- ingen nødder

<br>

Lav først en hjælper-metode:

```java
static int score(Pizza p) {
    // TODO
}
```

<br>

Lav derefter en stream, der:

1. beholder pizzaer med score mindst 2
2. sorterer efter højeste score først
3. ved samme score sorterer efter laveste slutpris
4. ved samme slutpris sorterer efter hurtigste levering
5. tager top 3

<br>

Afslut med at mappe til en `List<String>` og print den.

Hver streng kan fx indeholde:

- navn
- score
- slutpris
- leveringstid

<br>

<details>
<summary>Hint</summary>
Det er ofte lettest at lave `score(...)` som en almindelig hjælper-metode først og derefter bruge den flere steder i streamen.
</details>


------

