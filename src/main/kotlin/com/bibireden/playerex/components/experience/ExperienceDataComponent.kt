package com.bibireden.playerex.components.experience

import com.bibireden.playerex.PlayerEX
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.chunk.ChunkAccess
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class ExperienceDataComponent(
    val chunk: ChunkAccess,
    private var _ticks: Int = 0,
    private var _restorativeForceTicks: Int = PlayerEX.CONFIG.restorativeForceTicks,
    private var _restorativeForceMultiplier: Int = PlayerEX.CONFIG.restorativeForceMultiplier,
    private var _expNegationFactor: Float = 1.0F,
    private var _expNegationMultiplier: Int = PlayerEX.CONFIG.expNegationFactor
) : IExperienceDataComponent {
    override fun updateExperienceNegationFactor(amount: Int): Boolean {
        if (Random.nextFloat() > this._expNegationFactor) return true;

        val dynamicMultiplier = this._expNegationMultiplier + ((1.0F - this._expNegationMultiplier) * (1.0F - (0.1F * amount)))
        this._expNegationFactor = max(this._expNegationFactor * dynamicMultiplier, 0.0F)
        this.chunk.isUnsaved = true
        return false
    }

    override fun resetExperienceNegationFactor() { this._expNegationMultiplier = 1 }

    override fun readFromNbt(tag: CompoundTag) {
        this._expNegationFactor = tag.getFloat("exp_factor")
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putFloat("exp_factor", this._expNegationFactor)
    }

    override fun serverTick() {
        if (this._expNegationFactor == 1.0F) return
        if (this._ticks < this._restorativeForceTicks) this._ticks++
        else {
            this._ticks = 0
            this._expNegationFactor = min(this._expNegationFactor * this._restorativeForceMultiplier, 1.0F)
            this.chunk.isUnsaved = true
        }
    }
}