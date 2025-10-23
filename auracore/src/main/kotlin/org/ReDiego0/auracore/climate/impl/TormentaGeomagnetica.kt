package org.ReDiego0.auracore.climate.impl

import org.ReDiego0.auracore.climate.Climate
import org.ReDiego0.auracore.climate.ClimateType
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

object TormentaGeomagnetica : Climate {

    override val name: String = "Tormenta Geomagnética"
    override val papiTag: String = "&cTormenta Geomagnética"
    override val type: ClimateType = ClimateType.HOSTILE
    override val duration: Int = 120
    override fun applyVisuals(world: World) {
        world.setStorm(true)
        world.isThundering = true
    }

    override fun applyEffects(player: Player) {
        val fatigue = PotionEffect(
            PotionEffectType.MINING_FATIGUE,
            duration,
            1,
            true,
            true,
            true,
        )
        player.addPotionEffect(fatigue)
        if (Random.nextDouble() < 0.10) {
            player.damage(1.0)
        }
    }
}
