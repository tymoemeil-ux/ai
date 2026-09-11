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
