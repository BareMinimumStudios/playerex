package com.bibireden.playerex.compat.mana;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/** Fabric-only adapter for the verified Spell Engine 1.10.9 and Mana Attributes 2.9.1 APIs. */
public final class FabricSpellMana {
    private static final Set<String> MORE_RPG_RUNES = Set.of("more_rpg_classes:aqua_stone", "more_rpg_classes:terra_stone", "more_rpg_classes:storm_stone", "more_rpg_classes:nature_stone");
    private static final ThreadLocal<Frame> CURRENT = new ThreadLocal<>();
    private static final Map<Object, Reservation> PAID_CHANNELS = new WeakHashMap<>();
    private static final Map<Player, Batch> BATCHES = new WeakHashMap<>();
    private static final ClassValue<Map<String, Field>> FIELDS = new ClassValue<>() {
        @Override protected Map<String, Field> computeValue(Class<?> type) {
            var fields = new HashMap<String, Field>();
            for (var field : type.getFields()) fields.put(field.getName(), field);
            return fields;
        }
    };
    private static final Method GET_MANA, ADD_MANA, SATISFIED, ITEM, SUCCESS, CAST_PROCESS, PROCESS_SPELL;
    private static final Constructor<?> RESULT;
    static {
        try {
            var mana = Class.forName("com.github.theredbrain.manaattributes.entity.ManaUsingEntity");
            GET_MANA = mana.getMethod("manaattributes$getMana");
            ADD_MANA = mana.getMethod("manaattributes$addMana", float.class);
            var result = Class.forName("net.spell_engine.internals.cost.Ammo$Result");
            SATISFIED = result.getMethod("satisfied");
            ITEM = result.getMethod("item");
            RESULT = result.getConstructors()[0];
            SUCCESS = Class.forName("net.spell_engine.internals.SpellExecution$DeliveryCompletion").getMethod("success");
            CAST_PROCESS = Class.forName("net.spell_engine.internals.casting.SpellCaster$Entity").getMethod("getSpellCastProcess");
            PROCESS_SPELL = Class.forName("net.spell_engine.internals.casting.SpellCast$Process").getMethod("spell");
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("Unsupported Spell Engine/Mana Attributes mana API", error);
        }
    }

    public static final class Frame {
        final Player player;
        final Object spell;
        final Object channel;
        float cost;
        Frame(Player player, Object spell, Object channel) {
            this.player = player; this.spell = spell; this.channel = channel;
        }
    }
    private static final class Batch {
        final long tick;
        final Map<Object, Reservation> spells = new HashMap<>();
        Batch(long tick) { this.tick = tick; }
    }
    private static final class Reservation {
        final WeakReference<Player> player;
        final float cost;
        int pending;
        boolean committed;
        boolean refunded;
        Reservation(Player player, float cost) { this.player = new WeakReference<>(player); this.cost = cost; }
        void settle(boolean success) {
            pending--;
            committed |= success;
            if (pending == 0 && !committed && !refunded) {
                refunded = true;
                var owner = player.get();
                if (owner != null) addMana(owner, cost);
            }
        }
    }

    public static Frame begin(Object caster, Object entry, Object action) {
        var previous = CURRENT.get();
        var player = (Player) caster;
        var spell = ((Holder<?>) entry).value();
        var process = !action.toString().equals("TRIGGER") ? matchingProcess(player, spell) : null;
        CURRENT.set(new Frame(player, spell, process));
        return previous;
    }
    public static void end(Frame previous) {
        if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
    }

    public static Object ammo(Object caster, Object spell, Object original) {
        var frame = CURRENT.get();
        if (frame != null && frame.spell == spell) frame.cost = 0;
        if ((boolean) invoke(SATISFIED, original)) return original;
        var item = field(field(spell, "cost"), "item");
        var id = item == null ? null : field(item, "id");
        // Substitute Runes and the four More RPG Library rune stones; other reagents stay native.
        if (!(id instanceof String text)) return original;
        var rune = text.startsWith("#") ? text.substring(1) : text;
        if (!rune.startsWith("runes:") && !MORE_RPG_RUNES.contains(rune)) return original;
        if (!(boolean) field(item, "consume")) return original;
        var player = (Player) caster;
        var cost = cost(spell, ((Number) field(item, "amount")).intValue());
        var available = mana(player);
        var authority = frame;
        if (authority == null && player instanceof net.minecraft.server.level.ServerPlayer) {
            authority = new Frame(player, spell, matchingProcess(player, spell));
        }
        var required = authority != null && reservation(authority) != null ? 0 : cost;
        if (!Float.isFinite(cost) || cost < 0 || !Float.isFinite(available) || available < required) return original;
        if (frame != null && frame.spell == spell) frame.cost = cost;
        try { return RESULT.newInstance(true, invoke(ITEM, original), 0, List.of()); }
        catch (ReflectiveOperationException error) { throw incompatible(error); }
    }

    public static boolean deliver(Object caster, Object entry, Consumer<Object> completion,
                                  Function<Consumer<Object>, Boolean> original) {
        var frame = CURRENT.get();
        if (frame == null || frame.player != caster || frame.spell != ((Holder<?>) entry).value()
                || frame.cost == 0 || frame.player.level().isClientSide()) return original.apply(completion);
        var shared = reservation(frame);
        if (shared == null) {
            var available = mana(frame.player);
            if (!Float.isFinite(available) || available < frame.cost) return false;
            // Debit before delivery, including scheduling, so another spell cannot spend the same mana.
            addMana(frame.player, -frame.cost);
            shared = new Reservation(frame.player, frame.cost);
            remember(frame, shared);
        }
        final var charge = shared;
        charge.pending++;
        var settled = new boolean[]{false};
        Consumer<Object> callback = result -> {
            if (settled[0]) return;
            settled[0] = true;
            charge.settle((boolean) invoke(SUCCESS, result));
            if (completion != null) completion.accept(result);
        };
        try {
            var delivered = original.apply(callback);
            // AREA deliberately returns false even when its delayed delivery was scheduled.
            var area = field(field(frame.spell, "target"), "type").toString().equals("AREA");
            var delayed = ((Number) field(field(frame.spell, "deliver"), "delay")).intValue() > 0;
            if (!delivered && !settled[0] && !(area && delayed)) {
                settled[0] = true;
                charge.settle(false);
            }
            return delivered;
        } catch (RuntimeException error) {
            if (!settled[0]) charge.settle(false);
            throw error;
        }
    }

    private static Reservation reservation(Frame frame) {
        var channel = frame.channel == null ? null : PAID_CHANNELS.get(frame.channel);
        if (channel != null && !channel.refunded) return channel;
        if (!(boolean) field(field(frame.spell, "cost"), "batching")) return null;
        var batch = BATCHES.get(frame.player);
        var result = batch != null && batch.tick == frame.player.level().getGameTime() ? batch.spells.get(frame.spell) : null;
        return result != null && !result.refunded ? result : null;
    }
    private static void remember(Frame frame, Reservation reservation) {
        if (frame.channel != null) PAID_CHANNELS.put(frame.channel, reservation);
        if ((boolean) field(field(frame.spell, "cost"), "batching")) {
            var tick = frame.player.level().getGameTime();
            var batch = BATCHES.get(frame.player);
            if (batch == null || batch.tick != tick) { batch = new Batch(tick); BATCHES.put(frame.player, batch); }
            batch.spells.put(frame.spell, reservation);
        }
    }

    private static float cost(Object spell, int runeCount) {
        if (runeCount < 1) return Float.NaN;
        var override = ((SpellManaCost) field(spell, "cost")).playerex$manaCost();
        if (override != -1f) return override <= 100000f ? override : Float.NaN;
        double coefficient = 0;
        var impacts = field(spell, "impacts");
        if (impacts instanceof List<?> list) for (var impact : list) {
            var action = field(impact, "action");
            for (var kind : List.of("damage", "heal")) {
                var value = field(action, kind);
                if (value != null) coefficient += ((Number) field(value, "spell_power_coefficient")).doubleValue();
            }
        }
        int launches = 1;
        var delivery = field(spell, "deliver");
        for (var kind : List.of("projectile", "meteor")) {
            var launch = field(delivery, kind);
            if (launch != null) launches = Math.max(launches,
                    1 + Math.min(1000, Math.max(0, ((Number) field(field(launch, "launch_properties"), "extra_launch_count")).intValue())));
        }
        var result = 20d * Math.max(1d, coefficient) * launches * Math.max(1, runeCount);
        return Double.isFinite(result) && result <= 100000d ? (float) result : Float.NaN;
    }
    private static float mana(Player player) { return ((Number) invoke(GET_MANA, player)).floatValue(); }
    private static Object matchingProcess(Player player, Object spell) {
        var process = invoke(CAST_PROCESS, player);
        return process != null && ((Holder<?>) invoke(PROCESS_SPELL, process)).value() == spell ? process : null;
    }
    private static void addMana(Player player, float amount) { invoke(ADD_MANA, player, amount); }
    private static Object field(Object owner, String name) {
        try { return FIELDS.get(owner.getClass()).get(name).get(owner); }
        catch (ReflectiveOperationException | NullPointerException error) { throw incompatible(error); }
    }
    private static Object invoke(Method method, Object owner, Object... args) {
        try { return method.invoke(owner, args); }
        catch (ReflectiveOperationException error) { throw incompatible(error); }
    }
    private static IllegalStateException incompatible(Exception error) {
        return new IllegalStateException("Unsupported Spell Engine/Mana Attributes mana API", error);
    }
}
