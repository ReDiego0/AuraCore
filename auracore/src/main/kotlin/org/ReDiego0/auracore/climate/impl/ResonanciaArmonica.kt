package org.ReDiego0.auracore.climate.impl

import org.ReDiego0.auracore.climate.Climate
import org.ReDiego0.auracore.climate.ClimateType
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object ResonanciaArmonica : Climate {
    override val name: String = "Resonancia Armonica"
    override val papiTag: String = "&bResonancia Armonica"
    override val type: ClimateType = ClimateType.BENEFICIAL
    override val duration: Int = 120

    override fun applyVisuals(world: World) {
        world.setStorm(false)
    }

    override fun applyEffects(player: Player) {
        val haste = PotionEffect(
            PotionEffectType.HASTE,
            duration,
            2,
            false,
            false,
            true,
        )
        val speed = PotionEffect(
            PotionEffectType.SPEED,
            duration,
            1,
            false,
            true,
            true,
        )

        player.world.spawnParticle(
            Particle.GLOW,
            player.location,
            15,
            0.5, 0.5, 0.5,
            0.0, null, true
        )

        player.addPotionEffect(haste)
        player.addPotionEffect(speed)
    }
}