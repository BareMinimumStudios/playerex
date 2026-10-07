# Save compatibility

Back up worlds before updating versions. This port preserves its 1.21.1 attachment/component schemas; it does not import 1.20.1 CCA/Endec progression or legacy PlayerEx/RelicEx item NBT.

## Player and chunk state

Canonical server-owned player fields are `level`, `skill_points`, `refundable_points`, `allocations`, and `level_up_notified`. Allocations are integer counts keyed by attribute resource ID. Runtime modifiers are transient and rebuilt from saved counts. `level` defaults to 0 for earlier development saves. Payload protocol remains **3**; 5.0.0 changes no codec or packet shape.

Chunk fields are `negation_factor` and `last_recovery_game_time`. Recovery uses server game time and is lazy; zero means no active recovery anchor. No wall-clock interpretation or per-chunk ticking component is introduced.

Native attachment copy-on-death is retained; configured `resetOnDeath` explicitly resets progression. Logout/reconnect, respawn, dimension transfer and world restart must also be checked with real connected clients.

Vanilla owns Health NBT. PlayerEx captures loaded health transiently and restores it once after login attribute reconciliation; no separate persisted health field exists.

## Item components

`playerex:item_progression` contains optional `level` (0–10000), `experience`, `times_broken` and `broken`; missing state defaults to an intact level-zero item. Counters are nonnegative. Item level commands reset only level/XP and preserve all other component fields.

`playerex:dragon_stone_users` is an optional distinct UUID list, bounded to 4096, recording server-authorized reset confirmation for that stone.

`playerex:relic` contains rarity 0–6 and one to five distinct attribute rolls (ID, finite nonnegative amount bounded to 1000000, multiply flag). Missing state rolls once server-side. Unknown optional IDs remain saved but inactive. Stack copies, supported anvil infusion and repair preserve components. Crafting repair is rejected where it would discard relic rolls.

Ten school allocations and admin trade allocations use the existing allocation map; no new persistent field is added. Reset retention keeps whole points, truncating fractional level/allocation counts and rounding skill/refund counters.

## External state

Remnant owns the offline ledger and its existing snapshot codec. Mana Attributes owns Fabric mana persistence; PlayerEx only tracks transient casting reservations. Delayed/interrupted casting across disconnect/restart still needs connected-client verification. NeoForge mana integration is intentionally absent.

Gameplay config retains existing flat keys; Fzzy display groups do not serialize. Client palette migration replaces only the complete original stock palette and preserves custom colors. Do not delete existing configs to install 5.0.0.
