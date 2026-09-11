# Ares CrystalPvP — Fabric 1.21.8

Zaawansowany klient CrystalPvP na Minecraft **1.21.8 (Fabric)** z GUI/HUD w stylu **Aoba**
oraz ponad **80 modułami** (combat / movement / render / utility / client).

## Moduły CrystalPvP (rdzeń)

| Moduł | Co robi |
|---|---|
| **Auto Crystal** | Pełny cykl: wybór celu → kalkulacja obrażeń (własny algorytm wybuchu + pancerz + enchanty + resistance) → wybór najlepszej pozycji → rotacja → cichy switch na kryształ → postawienie → zniszczenie. Delay'e, wallrange, lethal override, force place, anti-suicide, min ratio, face/feet place, render celu i miejsca stawiania. |
| **Auto Anchor** | Fazy: postaw anchor → ładuj glowstonem → wysadź, gdy obrażenia się opłacają. |
| **Anchor Aura** | Agresywna wersja: kilka anchorów naraz, ładowanie i detonacja w pętli. |
| **Auto Totem** | Przenosi totem do offhanda (Always / Health / Smart), powiadomienia. |
| **Offhand** | Trzyma wybrany item w offhandzie (totem / crystal / gapple / shield / obsidian / glowstone / anchor). |
| **Auto Armor** | Zakłada najlepszy pancerz, anti-break, elytra mode. |
| **KillAura** | Cele, cooldown 1.9, shield breaker, auto switch na broń. |
| **Surround / SelfTrap / AutoTrap / HoleFiller** | Obsydianowa defensywa i ofensywa. |
| **AutoCity / AutoWeb / Burrow / AutoAnvil / AutoEXP / Criticals** | Narzędzia do wykańczania. |

## GUI (styl Aoba)

- **ClickGUI** (prawy Shift) — kategorie po lewej, moduły w środku z wyszukiwarką, ustawienia po prawej,
  animacje, scroll, suwaki, przełączniki, dropdowny, color picker (HSV + alpha + rainbow), bindy.
- **Windows GUI** — przesuwane okna per kategoria (ustawienie `ClickGUI → Style → WINDOWS`).
- **HUD Editor** — przeciąganie elementów z przyciąganiem do siatki, skala scroll-em, tło PPM.

Elementy HUD: Watermark, ArrayList, Coordinates, FPS, Ping, Speed, Armor, Totems, Potions,
Keystrokes (WASD), TargetHUD, Compass, Radar, Notifications.

## Instalacja / budowa

1. Zainstaluj Fabric Loader 0.16.14+ dla 1.21.8 i wrzuć `fabric-api` do `mods`.
2. Zbuduj moda:
   ```bash
   ./gradlew build
   ```
   (wymagany **JDK 21**; jeśli nie masz `gradlew`, uruchom `gradle wrapper --gradle-version 8.14.3`)
3. Wynik: `build/libs/Ares-1.0.0.jar` → do katalogu `mods`.

## Komendy

```
/ares toggle <modul>            wlacz / wylacz modul
/ares set <modul> <ustawienie> <wartosc>
/ares bind <modul> <kod klawisza>
/ares friend add|remove|list <nick>
/ares macro add <kod> <akcja>
/ares modules                   lista wszystkich modulow
/ares save                      zapis konfiguracji
/ares gui                       otwiera ClickGUI
```

Klawisz GUI: **Right Shift**. Konfiguracja: `.minecraft/config/ares/` (ustawienia, znajomi, makra).
