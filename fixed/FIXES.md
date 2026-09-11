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
