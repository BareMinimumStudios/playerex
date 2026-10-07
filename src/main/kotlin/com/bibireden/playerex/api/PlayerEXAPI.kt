package com.bibireden.playerex.api
import com.bibireden.playerex.api.attribute.PlayerEXAttributes
import com.bibireden.playerex.api.attribute.TradeSkillAttributes
import com.bibireden.playerex.api.damage.DamageFunction
import com.bibireden.playerex.api.damage.DamagePredicate
import com.bibireden.playerex.registry.*
import net.minecraft.resources.ResourceLocation

object PlayerEXAPI {
    @JvmField val PRIMARY_ATTRIBUTE_IDS: Collection<ResourceLocation> = PlayerEXAttributes.PRIMARY_ATTRIBUTE_IDS
    @JvmField val TRADE_SKILL_IDS: Collection<ResourceLocation> = TradeSkillAttributes.IDS
    @JvmStatic fun registerDamageModification(predicate: DamagePredicate, function: DamageFunction) = DamageModificationRegistry.register(predicate, function)
    @JvmStatic fun registerRefundCondition(condition: RefundCondition) = RefundConditionRegistry.register(condition)
    @JvmStatic val refundConditions: List<RefundCondition> get() = RefundConditionRegistry.get()
}
