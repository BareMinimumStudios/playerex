# Commands

The original 1.20.1 command tree is restored. Every `/playerex` command requires operator permission level 2. Player targets accept names and single-player selectors such as `@s`; target commands also work from the server console.

```text
/playerex level get <player>
/playerex level add <player> [amount]
/playerex reset <player> [retain]
/playerex reset @all [amount]
/playerex skill <id> get <player>
/playerex skill <id> add <player> [amount]
/playerex refund get <player>
/playerex refund add <player> [amount]
/playerex refund skill <id> <player> <amount>
/playerex armor level reset
/playerex armor level set <amount>
/playerex weapon level reset
/playerex weapon level set <amount>
```

Optional grant amounts default to 1. Level/skill grants are administrative: they do not charge the target's vanilla XP or skill points, and stop at the attribute cap. Level grants still award configured skill points. Primary, available spell-school and trade attribute IDs are suggested.

Reset defaults to retaining 0%. `retain` is a percentage from 0 to 100. The historical `reset @all [amount]` argument also means a retained percentage, bounded to 0–100, for online players. The 1.21.1 schema stores whole allocation counts: fractional retained levels/allocations truncate, while skill/refund points use the original rounding rule.

Refund point grants are capped by allocated points plus registered refund conditions. A negative refund grant removes points, stopping at zero. Skill refunds require enough owned allocation and refund points. Trade skills remain admin/API attributes; this does not add automatic trade XP or make them spendable through the GUI.

Armor/weapon controls operate on the executing player's held item and reset its leveling XP to zero. Item levels are bounded to the existing component range 0–10000. Other item data and broken-item state are preserved. The console must use a player execution context for held-item commands.

Temporary port commands (`status`, `grant-refund`, `item-xp`, and the old short-form leveling/allocation commands) are removed.

Examples:

```text
/playerex level add Poke 5
/playerex skill playerex:strength add Poke 3
/playerex refund add Poke 2
/playerex refund skill playerex:strength Poke 2
/playerex reset Poke 50
```
