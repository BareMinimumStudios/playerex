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
    private static final ArrayList<Pair<ResourceLocation, Class<? extends MenuComponent>>> ENTRIES = new ArrayList<>();

    @NotNull
    private static final HashMap<String, Integer> PRIORITY_ORDER = new HashMap<>();

    /**
     * Registers a {@link MenuComponent} to the registry,
     * which will be applied to the {@link PlayerEXScreen} as a page.
     */
    public static void register(ResourceLocation id, @NotNull Class<? extends MenuComponent> menu) {
        Pair<ResourceLocation, Class<? extends MenuComponent>> pair = new Pair<>(id, menu);
        Integer insertingPriority = PRIORITY_ORDER.get(pair.getFirst().toString());

        if (!ENTRIES.isEmpty()) {
            for (int i = 0, size = ENTRIES.size(); i < size; i++) {
                Pair<ResourceLocation, Class<? extends MenuComponent>> entry = ENTRIES.get(i);
                Integer priority = PRIORITY_ORDER.get(entry.getFirst().toString());
                if (priority > insertingPriority) {
                    ENTRIES.add(i, pair);
                    return;
                }
            }
        }
        ENTRIES.add(pair);
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
