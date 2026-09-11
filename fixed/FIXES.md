# Ares CrystalPvP 1.21.8 — folder `fixed/`

Ten folder zawiera **cały, działający projekt** (src + build.gradle + gradle.properties + settings.gradle)
z już naniesionymi poprawkami. Żeby zbudować: skopiuj zawartość `fixed/` do katalogu projektu
(nadpisz pliki) i uruchom `gradlew build` / „Rebuild Project” w IntelliJ.

## Naprawione błędy kompilacji

1. **`src/main/java/com/ares/modules/combat/AutoCrystal.java:196` — `error: not a statement`**

   Było (niedozwolone — ternaria użyta jako instrukcja, obie gałęzie `void`):

   ```java
   if (sword != -1) switchMode.get() == SwitchMode.NORMAL ? InventoryUtil.selectSlot(sword) : InventoryUtil.selectSilently(sword);
   ```

   Jest:

   ```java
   if (sword != -1) {
       if (switchMode.get() == SwitchMode.NORMAL) InventoryUtil.selectSlot(sword);
       else InventoryUtil.selectSilently(sword);
   }
   ```

Jeśli wyskoczą kolejne błędy — wklej log, naprawiam od ręki.
