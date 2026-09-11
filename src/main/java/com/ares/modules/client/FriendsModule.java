package com.ares.modules.client;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;

/** Ustawienia listy znajomych. */
public final class FriendsModule extends Module {

    private final BoolSetting attackFriends = add(new BoolSetting("Protect Friends", "Chron znajomych", true).group("General"));
    private final BoolSetting showInEsp = add(new BoolSetting("Show In ESP", "Oznaczaj znajomych w ESP", true).group("General"));

    public FriendsModule() {
        super("Friends", "Zarzadzanie lista znajomych", ModuleCategory.CLIENT);
    }

    public boolean protect() {
        return isEnabled() && attackFriends.get();
    }

    public boolean showInEsp() {
        return showInEsp.get();
    }
}
