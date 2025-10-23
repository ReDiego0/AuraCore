package org.ReDiego0.auracore.climate.impl

import org.ReDiego0.auracore.climate.Climate
import org.ReDiego0.auracore.climate.ClimateType
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

object FrecuenciaSonica : Climate {
    override val name: String = "Frecuencia Sonica"
    override val papiTag: String = "&bFrecuencia Sonica"
    override val type: ClimateType = ClimateType.HOSTILE
    override val duration: Int = 120

    override fun applyVisuals(world: World) {
        world.setStorm(false)
    }

    override fun applyEffects(player: Player) {
        val globalDuration = Random.nextInt(20, 60)
        val hunger = PotionEffect(
            PotionEffectType.HUNGER,
            globalDuration,
            0,
            true,
            true,
            true,
        )

        val nausea = PotionEffect(
            PotionEffectType.NAUSEA,
            globalDuration,
            0,
            true,
            true,
            true,
        )

        if (Random.nextDouble() < 0.15) {
            player.playSound(player.location,
                Sound.ENTITY_ENDERMAN_STARE,
                1.0f, 1.0f)
        }

        player.world.spawnParticle(
            Particle.NOTE,
            player.location,
            2,
            0.5, 0.5, 0.5,
            0.0, null, true
        )

        if (Random.nextDouble() < 0.15) {
            player.addPotionEffect(nausea)
            player.addPotionEffect(hunger)
        }
    }
}