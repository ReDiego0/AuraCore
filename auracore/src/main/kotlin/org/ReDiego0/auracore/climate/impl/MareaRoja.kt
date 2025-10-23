package org.ReDiego0.auracore.climate.impl

import org.ReDiego0.auracore.climate.Climate
import org.ReDiego0.auracore.climate.ClimateType
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable
import org.bukkit.Color
import kotlin.random.Random

object MareaRoja : Climate {
    override val name: String = "Marea Roja"
    override val papiTag: String = "&cMarea Roja"
    override val duration: Int = 120
    override val type: ClimateType = ClimateType.HOSTILE

    override fun applyVisuals(world: World) {
        world.setStorm(true)
        world.isThundering = false
    }
    override fun applyEffects(player: Player) {
        if (Random.nextInt(3) == 0) {

        val armorContents = player.inventory.armorContents
        for (i in armorContents.indices) {
            val item = armorContents[i]
            if (item != null && item.type != Material.AIR) {
                val meta = item.itemMeta
                if (meta is Damageable) {
                    meta.damage = meta.damage + 1
                    if (meta.damage >= item.type.maxDurability) {
                        armorContents[i] = null
                    } else {
                        item.itemMeta = meta
                        armorContents[i] = item
                    }
                }
            }
        }
        player.inventory.armorContents = armorContents

        val mainHand = player.inventory.itemInMainHand
        if (mainHand.type != Material.AIR) {
            val meta = mainHand.itemMeta
            if (meta is Damageable) {
                meta.damage = meta.damage + 1
                if (meta.damage >= mainHand.type.maxDurability) {
                    player.inventory.setItemInMainHand(null)
                } else {
                    mainHand.itemMeta = meta
                }
             }
            }
        player.updateInventory()
        }

        player.world.spawnParticle(
            Particle.CRIMSON_SPORE,
            player.location,
            300,
            10.0, 5.0, 5.0,
            0.0, null, true,
        )
} }