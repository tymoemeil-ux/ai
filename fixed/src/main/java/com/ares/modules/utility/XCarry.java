package com.ares.modules.utility;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;

/**
 * XCarry - dodatkowe sloty (2x2 w ekwipunku).
 *
 * Wymaga przechwytywania pakietu zamykajacego ekwipunek (inaczej serwer wyrzuca
 * przedmioty z pol craftingu). Na ten moment modul jest UKRYTY - wlaczenie go
 * bez obslugi pakietow rozjechaloby ekwipunek.
 */
public final class XCarry extends Module {

    public XCarry() {
        super("XCarry", "Dodatkowe sloty w ekwipunku (wymaga obslugi pakietow)", ModuleCategory.UTILITY);
        setHidden(true);
    }
}
