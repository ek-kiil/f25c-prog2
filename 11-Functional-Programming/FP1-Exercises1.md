# Øvelse 1: Java Lambdaer

<br>

Lav `Main.java` i et Java-projekt(ikke Spring!) i IntelliJ.

<br>

Vi laver en List vi skal bruge undervejs:

```java
List<Integer> prices = List.of(89, 95, 99, 105, 119, 125);
```



<br>

------

## 1. Indbyggede Interfaces

<br>

I dag bruger vi især to indbyggede interfaces:

- `Function<T, R>` med den abstrakte metode `R apply(T t)`
- `Predicate<T>` med den abstrakte metode `boolean test(T t)`

Helt kort:

- `Function` bruges, når noget skal ændres til noget nyt
- `Predicate` bruges, når du vil teste en regel

<br>Tal om lambdaerne med din sidemand, og diskuter:

- om den passer til `Function` eller `Predicate`

- hvad den gør, og hvad den returnerer

<br>

1. `price -> "Pris: " + price + " kr."`
2. `name -> name.contains(" ")`
3. `price -> price >= 100 && price % 5 == 0`
4. `name -> name.toUpperCase().length()`
5. `price -> price > 500 ? price -50 : price`

<br>

---

## 2. Function og Predicate

<br>

En `Function<T, R>` tager en værdi ind og returnerer en ny værdi.

Eksempel:

```java
Function<Integer, Integer> addTen = price -> price + 10;
```

<br>

Hvis reglen er lidt mere kompleks, kan vi bruge en blok-lambda med `return`:

```java
Function<Integer, Integer> addDelivery = price -> {
    if (price < 250) {
        return price + 50;
    }
    return price;
};
```

<br>

Hvis I har mod på det, kan I også prøve at bruge ternary operator:

```java
Function<Integer, Integer> addDelivery = price -> price < 250 ? price + 50 : price;
```

<br>

En `Predicate<T>` tager én værdi ind og returnerer `true` eller `false`.

Eksempel:

```java
Predicate<Integer> isCheap = price -> price < 100;
```

<br>

## Øvelse 2

<br>

Lav disse fire lambdaer, som lokale variabler i `main` og test de virker:

1. En `Function<Integer, Integer>` der lægger 25 kr. til, men kun hvis prisen er under 100 kr.
2. En `Function<Integer, String>` der gør en pris om til `"budget"`, `"normal"` eller `"premium"`
3. En `Predicate<Integer>` der tester om en pris er studievenlig
4. En `Predicate<String>` der tester om et ingrediens-tag starter med "veg".

<br>

Brug fx disse værdier til nr. 1, 2 og 3:

- `89`
- `100`
- `119`

<br>

Brug fx disse værdier til nr. 4:

- "vegansk"
- "vegetar"
- "kød"

<br>

---

## 3. Hjælper metode

<br>

Nu laver vi metoder, som modtager en lambda og bruger den på en liste.

Skriv metoderne som `static` metoder i klassen, men uden for `main`.

Det kan se sådan her ud:

```java
public class Main {

    // hjælper-metode her

    public static void main(String[] args) {
        // testkode her
    }
}
```

<br>

### 3A. filterPrices

------

Indsæt dette hjælper-metode skelet og lav TODO:

```java
public static List<Integer> filterPrices(List<Integer> prices, Predicate<Integer> rule) {
    List<Integer> result = new ArrayList<>();

    for (Integer price : prices) {
        // TODO
    }

    return result;
}
```

<br>

Inde i løkken skal du bruge `rule.test(price)` for at afgøre, om prisen skal med i resultatlisten.

Hvis `rule.test(price)` giver `true`, skal prisen lægges i `result` med `.add(...)`.

<br>

Brug derefter metoden til at lave:

1. alle priser under 100 kr.
2. alle priser der stadig er studievenlige efter levering

<br>

Et kald kan se sådan her ud:

```java
List<Integer> cheap = filterPrices(prices, p -> p < 100);
```

<br>

Hvis du er i tvivl om, hvad der sker her, så stop op og tænk:

- Hvem går gennem listen?
- Hvem bestemmer reglen?

<br>

<details>
<summary>Hint</summary>
Selve metoden `filterPrices(...)` ved, hvordan man går gennem en liste. Lambdaen, som du sender ind som `rule`, bestemmer bare hvilke værdier der skal beholdes.
</details>

<br>

### 3B. transformPrices

------

<br>

Indsæt dette og lav TODO:

```java
public static List<Integer> transformPrices(List<Integer> prices, Function<Integer, Integer> operation) {
    List<Integer> result = new ArrayList<>();

    for (Integer price : prices) {
        // TODO
    }

    return result;
}
```

<br>

Inde i løkken skal du bruge `operation.apply(price)` for at lave hver pris om, og vi `add`'er den samtidig til resultatlisten.

<br>

Brug derefter metoden til at lave:

1. priser med 20 procent rabat
2. priser rundet op til nærmeste 10 kr.

<br>

<details>
<summary>Hint til nr. 2</summary>
Tænk over heltalsdivision og resten ved division med 10.
</details>


<br>

Hvis du er i tvivl om, hvad der sker her, så stop lige op og tænk:

- Hvem går gennem listen?
- Hvem bestemmer hvordan hver pris ændres?

<br>

<details>
<summary>Hint</summary>
Selve metoden `transformPrices(...)` går gennem alle priserne. Lambdaen, som du sender ind som `operation`, bestemmer hvordan hver enkelt pris bliver ændret.
</details>

<br>

------

## Ekstra opgaver

<br>

### Ekstra 1: Mysterie Funktion

------

<br>

Du får disse data:

```text
89  -> 104
95  -> 110
99  -> 114
105 -> 95
119 -> 109
125 -> 115
```

<br>

Lav den `Function<Integer, Integer>` der passer til alle seks linjer.

Test den bagefter på hele listen `prices` med `transformPrices(...)`.

<br>

<details>
<summary>Hint</summary>
Reglen behandler priser under 100 anderledes end priser fra 100 og op.
</details>

<br>

### Ekstra 2: Lav selv reglerne

------

<br>

Her er vi mere i puzzle genren end det er kode vi normalt skriver, men bare rolig om lidt går vi den anden vej igen med streams! 

Brug listen fra tidligere:

```java
List<Integer> prices = List.of(89, 95, 99, 105, 119, 125);
```

<br>

Du skal selv vælge tre tal:

- `a`
- `b`
- `budget`

der skal følge disse regler:

- `a` og `b` skal være multipla(produkt) af 5
- `a` og `b` skal ligge mellem 5 og 25

<br>

Lav derefter:

```java
Function<Integer, Integer> adjustPrice = p -> p < 100 ? p + a : p - b;
Predicate<Integer> keepPrice = p -> p <= budget && p % 5 == 0;
```

og brug dem her:

```java
List<Integer> result = filterPrices(
        transformPrices(prices, adjustPrice),
        keepPrice
);
```

<br>

Målet er:

1. præcis 3 priser skal være tilbage
2. alle 3 priser skal være delelige med 5
3. summen af de 3 priser skal være så lav som muligt

<br>

<details>
<summary>Hint</summary>
Prøv først at regne på papir, hvilke af tallene der overhovedet kan ende med at blive delelige med 5.
</details>

------

