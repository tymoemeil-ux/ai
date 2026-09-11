package com.ares.modules.utility;

import com.ares.core.module.Module;
import com.ares.core.module.ModuleCategory;
import com.ares.core.setting.BoolSetting;
import com.ares.core.setting.StringSetting;

/** Chat Suffix - dodaje tekst na koncu kazdej wiadomosci. */
public final class ChatSuffix extends Module {

    private final StringSetting suffix = add(new StringSetting("Suffix", "Tekst dodawany do wiadomosci", " | Ares").group("General"));
    private final BoolSetting onlyCommands = add(new BoolSetting("Skip Commands", "Nie dodawaj do komend", true).group("General"));

    public ChatSuffix() {
        super("Chat Suffix", "Dopisek do wiadomosci na czacie", ModuleCategory.UTILITY);
    }

    public String apply(String message) {
        if (!isEnabled()) return message;
        if (onlyCommands.get() && message.startsWith("/")) return message;
        return message + suffix.get();
    }
}
