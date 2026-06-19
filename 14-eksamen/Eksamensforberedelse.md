
**Eksempler på eksamensspørgsmål og kodeændringer**

---

Jeg har lavet lidt generelle spørgsmål som kan bruges uafhængigt af jeres specifikke løsning, hvis et spørgsmål ikke giver mening så prøv at omformuler eller gå videre til næste.

Et generelt råd er at øve sig i at kunne forklare de centrale elementer af appen: Når man klikker/taster noget i frontenden hvad sker der så i koden? Hvordan gemmes data i databasen? Hvis i f.eks. har implementeret RestClient hvor i koden er det så vi henter geodata, og hvordan vises de? Det er også vigtigt at kunne finde rundt i projektet.

Hvis i efter de 24t. har fundet fejl i jeres app, så må i gerne vise det til eksamen - man må bare ikke pushe det til github.

I kan også bruge AI til at lave flere opgaver (hold fokus på de centrale emner, AI kan nemt ryge af sporet:). I kan også få AI til at få jer til at forklare emnerne (det virker lidt forvirret men det fungerer ok - man kan også efter at have læst op på et emne be' AI om at agere censor eller have en 'Sokratisk samtale'). 

---

JPA, DTOer og databasen
1. Hvad for nogle relationer har du brugt i din app? Hvis du f.eks. har brugt ManyToOne hvordan gemmes det så i databasen?
2. Hvorfor bruger du et repository? Hvad gør det?
3. Forklar hvor og hvordan din Spring app er sat op til at bruge databasen?
4. Hvis du har brugt DTOer, hvorfor har du gjort det og til hvad?

---

Services og RestClient
1. Kan du beskrive de forskellige regler og logik i en af dine serviceklasser?
2. Kan du forklare ift. en metode i din service, hvad kommer der ud og ind?
3. Hvad sker der hvis en sensor sender dårlige data (f.eks. negative eller manglende)?
4. (**BONUS**)Hvis du har implementeret geocoding, hvordan virker det så? 

---

Controllers og dataflow
1. Hvad gør @RequestBody?
2. Hvad gør de forskellige mappings? Hvornår bruger vi @PathVariable og @RequestParam?
3. Hvorfor bruger vi @ResponseStatus i en controller? Og hvad sker der hvis vi ikke bruger det?
4. Vælg en handling i jeres app: Forklar det fulde flow fra brugeren udfører handlingen og hvad der så sker igennem koden.

---

Spring Security (**BONUS**)
1. Hvis du har sat Spring Security op, hvilke valg har du truffet f.eks. omkring login, roller, og beskyttede endpoints?
2. Hvordan gemmer frontenden loginoplysninger? Kan det gøres bedre (HUSK: Forståelsen er det vigtigste)?
3. Hvordan gemmes brugere og password i appen?

---

Frontend
1. Hvad er fetch? Hvordan virker det? Er fetch altid asynkront?
2. Hvis i vælger et element i jeres frontend, kan i forklare hvordan det bliver rendered i koden?
3. Hvad er en event listener og hvad bruger vi den til?
4. Hvorfra kommer dataene til de forskellige elementer i har i jeres frontend?

---

Kodeændringer i backenden
1. Gå ind i en af dine services og ændr' en regel så den gør noget andet.
2. Tilføj et simpelt endpoint der har en eller anden simpel funktion(kald en eksisterende service, eller f.eks. returnerer antal elementer i en eksisterende liste)?
3. Kan vi ændre på noget i en Java Stream(altså så listen den returnerer ændres), eller hvis i ikke bruger streams kan i så omskrive et loop(der laver en liste) til en stream?
4. (**BONUS**)Hvis i har tilføjet Spring Security, kan i så lave om i en regel, så f.eks. brugeren pludselig kan tilgå noget han/hun ikke kunne før?

---

Kodeændringer i frontenden
1. Lav en ny knap på siden. Kan du få knappen til at kalde en eksisterende funktion(f.eks. en der rerender eller clearer noget)?
2. Kan du ændre rækkefølgen på noget i din frontend?
3. Ændre på noget i en tabel, f.eks. rearranger rækkefølgen af kolonner, eller tilføj en kolonne(den kunne f.eks. vise både bredde- og længdegrad).
4. Find et sted hvor du viser noget i en liste og sorter eller filtrer det anderledes. 


