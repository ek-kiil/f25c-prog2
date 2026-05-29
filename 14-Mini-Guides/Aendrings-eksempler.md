

### Eksempler på ændringer i koden



**Frontend**

1. Skift en tekst, label eller overskrift i frontenden.
2. Tilføj en tekst eller knap i en eksisterende visning.
3. Vis noget data i frontenden på en anden måde, f.eks. anden tekst eller rækkefølge.
4. Byt rundt på to felter i en visning.
5. Find en liste eller tabel og tilføj et felt, der allerede findes i frontend-dataen, til hver række.

**Sværere**

1. Find en liste eller tabel og skjul elementer med f.eks. bestemt status.
2. Kombiner to eksisterende værdier i et nyt felt, f.eks. #id - status.
3. Lav en knap i liste/tabel, der kalder en eksisterende funktion med rækkens id.
4. Vis antal elementer i en allerede hentet liste.
5. Skift CSS/visning for et element ud fra f.eks. dets status.





**Backend** 

1. Skift en eksisterende fejl eller succesbesked i backenden.
2. Ændr en eksisterende service regel, så færre eller flere elementer returneres
3. Tilføj simpel service regel, der blokerer en bestemt ændring, f.eks. ændring af status.
4. Tilføj lille endpoint, der kalder en eksisterende service metode.

**Sværere** 

1. Tilføj et lille endpoint, der returnerer antal elementer i en eksisterende liste fra service.
2. Tilføj et @PathVariable endpoint, der returnerer en simpel værdi fra en entity ud fra id, f.eks. status.
3. (Ekstra svær)Tilføj lille endpoint der returnerer hvor mange child-entities(many siden) der er knyttet til en parent entity(one siden). 





**Front- & Backend**

1. Ændre stien på et endpoint og opdater så i frontenden.
2. Fjern et felt fra backendens svar og fjern det også fra visningen.
3. Vælg et felt der allerede kommer fra backenden, og vis det et nyt sted eller andet sted i frontend.
4. Ændr på en værdi der kommer fra backenden(f.eks. Status) og opdater frontendens visning.
5. (**Ekstra svær**)Tilføj et simpelt @RequestParam til eksisterende liste endpoint og send parameteren fra frontenden, filtrer i servicen og vis så resultatet.

