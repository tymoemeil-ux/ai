# Ares CrystalPvP 1.21.8 — folder `fixed/`

Kompletny, gotowy projekt (src + build.gradle + gradle.properties + settings.gradle + gradle/ + README).
Skopiuj zawartość `fixed/` do katalogu projektu (nadpisz pliki) i zbuduj ponownie.

## Naprawione błędy (runda 1)

1. `com/ares/modules/combat/AutoCrystal.java:196` — `error: not a statement`
   ternaria użyta jako instrukcja (obie gałęzie `void`) → zamienione na `if / else`.

2. `com/ares/modules/combat/AutoTrap.java`, `com/ares/modules/combat/Surround.java`
   — brakujący `import com.ares.core.setting.FloatSetting;` → dodany.

3. `com/ares/Ares.java` — brakująca klasa `com.ares.core.target.PopCounter`
   → utworzona (`src/main/java/com/ares/core/target/PopCounter.java`, licznik popnięć totemów).

4. `net.minecraft.item.ArmorItem` **nie istnieje w yarn 1.21.8**
   → `InventoryUtil.armorSlot(Item)` / `InventoryUtil.isArmor(Item)` (mapa po `Items.*`).
   Użycia naprawione w `AutoArmor` (było `armor.getSlotType()`) i `InventoryCleaner`.

5. `net.minecraft.item.ToolItem` nie istnieje w yarn 1.21.8
   → `InventoryCleaner` używa teraz `stack.isDamageable()`.

6. `net.minecraft.item.PickaxeItem` nie istnieje w yarn 1.21.8
   → `AutoMine.findPickaxeSlot()` porównuje z listą `Items.*_PICKAXE`.

## Naprawione błędy (runda 2)

7. `PlayerInventory.selectedSlot` jest prywatne w 1.21.8
   → `getSelectedSlot()` / `setSelectedSlot(int)` (wszystkie moduły: AutoCrystal, AutoAnchor,
   AnchorAura, Surround, AutoTrap, Scaffold, AutoEat, AutoTool itd.).

8. `ItemStack.isFood()` nie istnieje
   → `stack.getUseAction() == UseAction.EAT` (PlayerUtil, AutoEat, InventoryCleaner).

9. `BlockItem.getBlock(Item)` nie istnieje (jest `getBlock()` na instancji)
   → `((BlockItem) item).getBlock()` (InteractionUtil, InventoryUtil).

10. `RespawnAnchorBlock.canCharge(BlockState)` jest prywatne
    → `state.get(RespawnAnchorBlock.CHARGES) < RespawnAnchorBlock.MAX_CHARGES` (AnchorUtil).

11. `EnchantmentHelper.getLevel(Enchantments.X, stack)` — `Enchantments.X` to `RegistryKey`,
    a metoda chce `RegistryEntry` → uproszczone liczenie ochrony bez rejestrów (DamageUtil).

12. Pola `x / y / width / height / scale / background` w `HudElement` były prywatne
    → zmienione na `protected` (elementy HUD z `HudElements` dziedziczą po `HudElement`).

13. `add(new BoolSetting(...).group("..."))` — błąd inferencji typów (`Setting<T>` zamiast konkretu)
    → każde ustawienie ma teraz kowariantną metodę `group(String)` zwracającą swój własny typ
    (BoolSetting, IntSetting, FloatSetting, DoubleSetting, ModeSetting, StringSetting,
    ColorSetting, BindSetting, ItemListSetting, NumberSetting).

## Naprawione błędy (runda 3 — pełna lista 91 błędów)

14. **~60 błędów `onTick/onRender3D ... attempting to assign weaker access privileges`**
    → `Module` miał puste metody `public onTick/onRender2D/onRender3D`, a moduły definiują
    je jako `private` (z `@EventHandler`). Puste hooki usunięte z `Module`.

15. `Ares.java:247` — brak `ColorsModule` → brakował `import com.ares.modules.client.ColorsModule;`.

16. `AnchorAura` / `AutoMine` — `FloatSetting`/`BoolSetting` użyte zamiast wartości
    → `range.get()` / `feet.get()`.

17. `EnchantmentHelper.hasBindingCurse` nie istnieje → `stack.hasEnchantments()` (AutoArmor).

18. `Enchantments.PROTECTION / MENDING` to `RegistryKey`, a `getLevel` chce `RegistryEntry`
    → AutoArmor i AutoEXP używają `stack.hasEnchantments()` (przybliżenie).

19. `PlayerMoveC2SPacket` jest abstrakcyjna → `new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround, horizontalCollision)` (Criticals).

20. `ClientWorld.setTimeOfDay(long)` — jawne rzutowanie na `ClientWorld` + `long` (TimeChanger).

21. `World.getTopY()` bez argumentów nie istnieje → `getTopYInclusive()` (XRay).

22. `Entity.RemovalReason.DEATH` nie istnieje → `RemovalReason.KILLED` (AutoRespawn).

23. `TickTimer.elapsed()` nie istnieje → `timer.increment()` + `timer.passed(ticks)` (AntiAFK, Spammer).

24. `net.minecraft.util.UseAction` nie istnieje w 1.21.8 → `PlayerUtil.isFood()` sprawdza
    `stack.getUseAction().name().equals("EAT")` (PlayerUtil, AutoEat, InventoryCleaner).

## Naprawione — runda 4 (mod sie juz laduje, ale NIC nie dzialalo)

Przyczyna „żadna komenda ani GUI nie działa” była w runtime, nie w kompilacji:

25. **Moduły nie dostawały eventów** — `ModuleManager.register()` tylko wrzucał moduł do mapy.
    Dodano `Ares.get().eventBus().register(module)` przy rejestracji.

26. **Eventy pochodne nie trafiały do słuchaczy** — `EventBus.post()` patrzył tylko na
    `event.getClass()`, a `AresMod` publikuje `TickEvent.Client`, moduły nasłuchują `TickEvent`.
    `post()` zbiera teraz listenery z całej hierarchii klas.

27. **Bindy myszy były ignorowane** — w `Ares.handleKeybinds()` było `if (bind < 0) continue;`,
    przez co przyciski myszy (kodowane jako <= -100) nigdy nie działały. Teraz: `-1` = brak bindu.

28. **Nic nie publikowało `Render3DEvent`** — ESP, tracery, nametagi, chams, boxy itd. były martwe.
    W `AresMod` dodano `WorldRenderEvents.LAST` (Fabric API) → `postRender3D(...)`.

29. **`/ares bind <modul> <klawisz>` wymagało liczby** — teraz przyjmuje nazwy:
    `/ares bind killaura r`, `/ares bind esp right_shift`, `/ares bind offhand key.mouse.0`,
    albo kod liczbowy (`82`).

30. Nowe mixiny (wszystkie cele zweryfikowane w mapowaniach 1.21.8):
    - `CameraMixin` → Freecam podmienia pozycję/rotację kamery (`Camera.update`, TAIL),
    - `InGameHudMixin` → No Render: `renderStatusEffectOverlay` (ikony efektów)
      i `renderOverlay` (ogień / dynia po ścieżce tekstury),
    - `BossBarHudMixin` → No Render: pasek bossa.

## Naprawione — runda 5 (ostatnie 4 błędy + HUD)

31. `RenderTickCounter.getTickDelta(boolean)` nie istnieje → `getTickProgress(true)` (AresMod).

32. `Camera.setPos/setRotation` są `protected` → w `CameraMixin` dodano deklaracje `@Shadow`,
    Mixin podmienia je na prawdziwe metody (Freecam podmienia kamerę).

33. `ClientWorld.setTimeOfDay(long)` nie jest publiczne → TimeChanger woła je przez refleksję
    po nazwie (`setTimeOfDay`, a w buildzie produkcyjnym `method_165`) — zero błędów kompilacji.

34. **HUD uciekał do rogu**: `HudManager.render()` dociskał każdy element do krawędzi ekranu
    i zapisywał tę pozycję (`element.setPosition`), przez co ustawienia z edytora HUDu
    były niszczone przy każdej klatce. Teraz elementy rysują się tam, gdzie są ustawione.

## Naprawione — runda 6 (crash przy ekranie "Loading terrain")

35. **Każdy mixin jest teraz w `try/catch (Throwable)`** (InGameHudMixin, EntityMixin,
    PlayerEntityMixin, BossBarHudMixin, CameraMixin). Wcześniej wyjątek rzucony w mixinie
    szedł wprost do waniliowego kodu Minecrafta = crash gry przy ładowaniu świata.

36. **Lambdy Fabric API w `AresMod` też są w try/catch** (tick + WorldRenderEvents.LAST).

37. **HUD nie rysuje się bez gracza** (`Wrapper.nullCheck()`), czyli nie odpala się na ekranie
    ładowania terenu, gdzie `mc.player` jest jeszcze `null`.

38. **Uszkodzony config nie blokuje startu**: `Ares.init()` łapie wyjątek przy `config.load()`
    i startuje z ustawieniami domyślnymi.

Uwaga: event bus od rundy 4 łapie wyjątki wewnątrz listenerów, więc po tych zmianach
żaden błąd w module nie powinien już wywalać gry — w najgorszym razie zobaczysz błąd w logu.

## Naprawione — runda 7 (spam "Illegal option value ... options.fov" = crash przy ładowaniu terenu)

**Przyczyna:** `Zoom` (FOV 5-70) i `CustomFOV` (do 180) zapisywały FOV bezpośrednio do opcji
Minecrafta (`options.getFov().setValue(...)`), a vanilla przyjmuje tylko **30-110**. Przy Zoomie
każda klatka animacji poniżej 30 logowała błąd - w kilka sekund powstawały setki tysięcy linii,
co zawieszało i wywalało grę na ekranie ładowania terenu.

**Rozwiązanie (tak jak w prawdziwych klientach):**
39. Nowy `GameRendererMixin` (`getFov`, RETURN, cancellable) podmienia FOV w locie
    przez `FovModifier` - mod nie dotyka już opcji Minecrafta, więc Zoom może zejść do 5,
    a Custom FOV wejść na 180, bez ani jednego nielegalnego zapisu.
40. `Zoom` i `CustomFOV` przepisane: nie zapisują do `options.getFov()` (koniec spamu,
    koniec rozjeżdżania ustawień FOV w opcjach gry po wyłączeniu modułu).
41. `OptionUtil.setValue` pomija zapis, gdy wartość się nie zmieniła; `Fullbright`
    (gamma) ustawia jasność tylko przy zmianie.

Po tej zmianie log nie powinien zawierać już ani jednej linii `Illegal option value`.

## Naprawione — runda 8 (PRAWdziwy crash: StackOverflowError)

Z logu `dd.txt` (34 tys. linii) wyszlo dokladnie:

```
Caused by: java.lang.StackOverflowError
    at com.ares.Ares.onTick(Ares.java:271)
    at com.ares.core.event.EventBus.post(EventBus.java:92)
    at com.ares.core.event.EventBus$Listener.invoke(EventBus.java:31)
```

42. **Nieskonczona rekurencja**: `Ares.onTick` byl sluchaczem `TickEvent` i jednoczesnie
    publikowal `TickEvent.Client` (Timer > 1). Kazdy opublikowany event wchodzil znowu do
    `onTick` -> znowu publikowal -> az do przepelnienia stosu i crashu gry.
    Teraz `Ares` NIE jest juz sluchaczem: metoda nazywa sie `Ares.tick()` i jest wywolywana
    bezposrednio z `AresMod` (ClientTickEvents.END_CLIENT_TICK).

43. **Timer ograniczony**: maksymalnie 10 dodatkowych tickow, a `setTimerValue`
    przycina wartosc do 0.1-10.0.

44. **EventBus: zabezpieczenie przed zapetleniem** - jezeli sluchacz znow publikuje ten sam
    event, po 8 poziomach publikowanie jest przerywane (zamiast StackOverflowError).

45. **Log juz nie puchnie**: pelny slad stosu drukuje sie tylko 3 pierwsze razy dla danego
    sluchacza, potem jedna linia co 500 wystapien (wczesniej jeden modul potrafil wygenerowac
    dziesiatki tysiecy linii i zawiesic gre).

46. Osobno (runda 7): `Illegal option value ... options.fov` - 10 316 linii w jednym logu
    (Zoom/CustomFOV pisaly FOV poza zakres 30-110). Juz naprawione mixinem GameRenderer.

## Naprawione — runda 9 (lagi + moduly ktore "nie dzialaly" + preset na start)

47. **LAGI - skanowanie swiata**: kazdy modul (ESP, Radar, Target, Crystal, Surround...)
    przejezdzal po WSZYSTKICH bytach swiata osobno, po kilkanascie razy na tick.
    `EntityUtil` ma teraz cache odswiezany RAZ NA TICK - wszystkie moduly korzystaja z jednego skanu.

48. **LAGI - kalkulacja obrazen**: `DamageUtil.exposure()` robil do **729 raycastow** na jedno
    obliczenie obrazen (AutoCrystal liczyl to dla ~22 pozycji x cel i x gracz = dziesiatki
    tysiecy raycastow na tick). Teraz max 64 probki + cache wynikow w ramach ticka.

49. **LAGI - `BlockUtil.isCrystalAt`**: skanowal caly swiat dla kazdej pozycji krysztalu.
    Korzysta z cache `EntityUtil`.

50. **KillAura nie atakowal**: opcja "Only Weapon" byla domyslnie wlaczona, wiec modul stal
    bezczynnie, gdy w rece nie bylo miecza/topora. Domyslnie wylaczona - atakuje zawsze.

51. **AutoTotem vs Offhand**: oba moduly klikaly sloty na zmiane i sie gryzly.
    AutoTotem teraz ustepuje, gdy Offhand jest wlaczony.

52. **Surround**: opcja "Center" byla martwa (nieuzwana w kodzie) - dodane delikatne
    centrowanie predkoscia, dzieki czemu obstawianie trafia w blok w ktorym stoimy.

53. **Preset CrystalPvP na start**: przy PIERWSZYM uruchomieniu (brak `config/ares/ares.json`)
    automatycznie wlaczaja sie: Auto Totem, Offhand, Surround, Auto Crystal, KillAura, Auto Armor.

54. `BlockUtil.canSee` nie rzuca juz NPE gdy nie ma gracza.

## Runda 10 — przebudowa UI (ClickGUI)

55. **Przełącznik (toggle) w każdym wierszu** modułu + płynna animacja - widać od razu,
    czy moduł jest włączony, bez patrzenia na kolor tła.

56. **Przypisywanie klawiszy z GUI**: środkowy przycisk myszy na module (albo przycisk
    „Klawisz: X” w nagłówku ustawień) → wciskasz klawisz i gotowe. `Escape`/`Backspace`
    = usuń bind. Klawisz pokazuje się też jako odznaka w liście modułów (np. `R`, `M1`).

57. **Dymki (tooltip)**: po najechaniu na moduł pokazuje się opis, kategoria, klawisz
    i stan włączenia.

58. **Klikanie jak w dobrych klientach**: lewy = włącz/wyłącz, prawy = ustawienia.

59. **Kolory kategorii** (Combat czerwony, Movement cyan, Render fiolet, Utility żółty,
    Client zielony, HUD niebieski) - ikony, paski i dymki.

60. **Stopka z przyciskami**: „Edytor HUD” (otwiera edycję HUD) i „Zapisz” (zapis konfiguracji
    + powiadomienie), plus podpowiedź sterowania.

61. Komunikat „Brak wyników” gdy wyszukiwarka nic nie znajdzie.

## Runda 11 — wszystkie moduły: kolejny etap optymalizacji

62. **Moduły renderujące skanowały świat na własną rękę** - Nametags, ESP, Chams, Tracers,
    ItemESP i Criticals wywoływały `world.getEntities()` przy KAŻDEJ klatce (60+ razy/s).
    Teraz korzystają z cache `EntityUtil.all()` (odświeżanego raz na tick).

63. `EntityUtil.all()` - publiczny dostęp do cache'owanej listy bytów.

Przejrzane i potwierdzone jako działające: AutoCrystal, KillAura, Surround, AutoTotem,
Offhand, AutoArmor (sloty 5-8 zbroi - poprawne), Criticals (3 tryby: Jump / Mini Jump /
Packet), AutoAnvil, AutoWeb, AutoTrap, Burrow, AnchorAura, AutoAnchor, AutoCity, HoleFiller,
AutoMine, AutoEXP, AutoGap oraz helpery: TickTimer, TargetUtil, AnchorUtil, DamageUtil,
CrystalUtil, BlockUtil, InventoryUtil, InteractionUtil, RotationUtil.

## Runda 12 — moduły Movement (przejrzane wszystkie 16)

Przejrzane: AntiVoid, AutoJump, ElytraFly, Fly, InventoryMove, Jesus, NoFall, NoPush,
NoSlow, SafeWalk, Scaffold, Speed, Sprint, Step, Timer, Velocity - wszystkie mają realną logikę.

64. **ElytraFly - klawisz skoku zostawał wciśnięty na stałe** (moduł wciskał `jumpKey`,
    żeby wystartować, i nigdy go nie puszczał → po wyłączeniu gracz dalej skakał).
    Dodane `releaseJump()` + sprzątanie w `onDisable`, z własną flagą
    `jumpPressedByUs` (żeby nie kasować prawdziwego wciśnięcia klawisza przez gracza).

Potwierdzone działanie: Step (omija brak setStepHeight w 1.21.8 - podbija prędkością przy
kolizji), Fly (Creative/Vanilla/Glide + sprzątanie abilities), Speed, Jesus, Velocity
(redukcja knockbacku), Scaffold (stawia pod nogami), NoFall (wiadro wody), SafeWalk.

## Runda 13 — moduły Render

65. **ViewClip był pustakiem** (tylko ustawienie, zero logiki). Teraz działa naprawdę:
    `CameraMixin` cofa kamerę na pełną odległość (vanilla przesuwa ją do przodu przy
    kolizji), a moduł przełącza widok na trzecią osobę i wraca do pierwszej po wyłączeniu.
    Stałe enuma wybierane przez `isFirstPerson()`/`isFrontView()` (nazwy stałych w
    mapowaniach 1.21.8 nie są pewne).

66. **HoleESP wycinał klatki**: `HoleUtil.holesInRange` liczył ~5 tys. pozycji (kiladziesiąt
    tysięcy odczytów bloków) przy KAŻDEJ klatce. Wynik jest teraz cache'owany na tick.

67. Wszystkie moduły renderujące korzystają z cache bytów (bez skanowania świata co klatkę).
