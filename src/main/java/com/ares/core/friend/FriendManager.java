package com.ares.core.friend;

import com.ares.Ares;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;

/** Lista znajomych z zapisem do pliku JSON. */
public final class FriendManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<String> names = new ArrayList<>();
    private final List<UUID> uuids = new ArrayList<>();
    private Path file;

    public FriendManager() {
    }

    public void init(Path directory) {
        this.file = directory.resolve("friends.json");
        load();
    }

    public boolean add(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        if (names.contains(normalized)) return false;
        names.add(normalized);

        UUID uuid = uuidOf(name);
        if (uuid != null && !uuids.contains(uuid)) uuids.add(uuid);

        save();
        return true;
    }

    public boolean remove(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        UUID uuid = uuidOf(name);
        boolean removed = names.remove(normalized);
        if (uuid != null) uuids.remove(uuid);
        if (removed) save();
        return removed;
    }

    public boolean isFriend(String name) {
        return name != null && names.contains(name.toLowerCase(Locale.ROOT));
    }

    public boolean isFriend(PlayerEntity player) {
        if (player == null) return false;
        if (uuids.contains(player.getUuid())) return true;
        return isFriend(player.getName().getString());
    }

    public boolean isFriend(UUID uuid) {
        return uuid != null && uuids.contains(uuid);
    }

    public List<String> all() {
        return new ArrayList<>(names);
    }

    public void clear() {
        names.clear();
        uuids.clear();
        save();
    }

    /** Probuje wyciagnac UUID gracza z listy graczy na serwerze. */
    private UUID uuidOf(String name) {
        if (Ares.get().mc() == null) return null;
        ClientPlayNetworkHandler handler = Ares.get().mc().getNetworkHandler();
        if (handler == null) return null;
        for (PlayerListEntry entry : handler.getPlayerList()) {
            if (entry.getProfile() != null && entry.getProfile().getName() != null
                    && entry.getProfile().getName().equalsIgnoreCase(name)) {
                return entry.getProfile().getId();
            }
        }
        return null;
    }

    public void save() {
        if (file == null) return;
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(names, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void load() {
        if (file == null || !Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            List<String> loaded = GSON.fromJson(reader, List.class);
            if (loaded == null) return;
            names.clear();
            for (String name : loaded) {
                if (name != null) names.add(name.toLowerCase(Locale.ROOT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
