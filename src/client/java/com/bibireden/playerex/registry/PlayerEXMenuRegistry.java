package com.bibireden.playerex.registry;

import com.bibireden.playerex.ui.components.MenuComponent;
import com.bibireden.playerex.ui.PlayerEXScreen;
import kotlin.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to register {@link MenuComponent}'s to the {@link PlayerEXScreen}.
 * This allows you to build a UI layer with PlayerEX
 * and access component data for the {@link LocalPlayer}.
 */
public final class PlayerEXMenuRegistry {
    @NotNull
    private static final List<Pair<ResourceLocation, Class<? extends MenuComponent>>> ENTRIES = new ArrayList<>();

    @NotNull
    private static final HashMap<String, Integer> PRIORITY_ORDER = new HashMap<>();

    /**
     * Registers a {@link MenuComponent} to the registry,
     * which will be applied to the {@link PlayerEXScreen} as a page.
     */
    public static void register(ResourceLocation id, @NotNull Class<? extends MenuComponent> menu) {
        Integer position = PRIORITY_ORDER.get(id.getNamespace());
        if (position != null) {
            for (int i = 0; i < ENTRIES.size(); i++) {
                ResourceLocation entryId = ENTRIES.get(i).getFirst();
                if (PRIORITY_ORDER.get(entryId.getNamespace()) >= position) {
                    position = i + 1;
                }
            }
            ENTRIES.add(position, new Pair<>(id, menu));
        }
        else {
            ENTRIES.add(new Pair<>(id, menu));
        }
    }

    @NotNull
    public static List<Pair<ResourceLocation, Class<? extends MenuComponent>>> get() {
        return ENTRIES;
    }

    @NotNull
    public static List<ResourceLocation> getIds() {
        return ENTRIES.stream().map(Pair::component1).toList();
    }

    @NotNull
    public static List<Class<? extends MenuComponent>> getDefs() {
        return ENTRIES.stream().map(Pair::component2).collect(Collectors.toUnmodifiableList());
    }

    static {
        PRIORITY_ORDER.put("playerex", 0);
        PRIORITY_ORDER.put("relicex", 1);
        PRIORITY_ORDER.put("wizardex", 2);
    }
}
