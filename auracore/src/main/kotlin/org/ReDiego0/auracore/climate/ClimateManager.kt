package org.ReDiego0.auracore.climate

import com.palmergames.bukkit.towny.TownyAPI
import org.ReDiego0.auracore.Auracore
import org.ReDiego0.auracore.climate.impl.*
import org.ReDiego0.auracore.tax.TownData
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

class ClimateManager(private val plugin: Auracore) : Listener {
    private val worldAllowedClimates: Map<String, List<Climate>> = mapOf(
        "Elysium" to listOf(PazAtmosferica, FlujoVital, ResonanciaArmonica),
        "Tartaros" to listOf(MareaRoja, TormentaGeomagnetica, PazAtmosferica),
        "Krimera" to listOf(TormentaGeomagnetica, PulsoEstatico),
        "world" to listOf(PazAtmosferica)
    )

    private val activeWorldClimates: MutableMap<String, Climate> = mutableMapOf()
    private val worldClimateStartTimes: MutableMap<String, Long> = mutableMapOf()

    private val activeBossBars: MutableMap<UUID, BossBar> = mutableMapOf()
    private val townData = TownData(plugin)
    private var climateDurationTicks: Long = 36000L

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
        climateDurationTicks = plugin.config.getLong("climate.changeInterval", 36000L)
        pickNewClimates()
    }

    fun startClimateTimer(changeInterval: Long) {
        climateDurationTicks = changeInterval
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            pickNewClimates()
        }, changeInterval, changeInterval)
    }

    fun startEffectApplicator(checkInterval: Long) {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            applyClimateEffectsToPlayers()
        }, 0L, checkInterval)
    }

    private fun pickNewClimates() {
        val currentTime = System.currentTimeMillis()
        for ((worldName, allowedList) in worldAllowedClimates) {
            val world = Bukkit.getWorld(worldName) ?: continue
            val newClimate = allowedList.random()

            activeWorldClimates[worldName] = newClimate
            worldClimateStartTimes[worldName] = currentTime

            newClimate.applyVisuals(world)

            announceClimateChange(world, newClimate)
        }
    }

    private fun announceClimateChange(world: World, climate: Climate) {
        val msg = when (climate.type) {
            ClimateType.HOSTILE -> "§c[ALERTA] El sistema atmosférico en ${world.name} detecta: §l${climate.name}§c."
            ClimateType.NEUTRAL -> "§a[SISTEMA] Atmósfera estable en ${world.name}: §l${climate.name}§a."
            ClimateType.BENEFICIAL -> "§e[EVENTO] Flujo de energía positivo en ${world.name}: §l${climate.name}§e."
        }
        world.players.forEach { it.sendMessage(msg) }
    }

    private fun applyClimateEffectsToPlayers() {
        val townyAPI = TownyAPI.getInstance()
        val totalDurationMs = climateDurationTicks * 50L

        for (player in plugin.server.onlinePlayers) {
            val worldName = player.world.name
            val currentClimate = activeWorldClimates[worldName]

            if (currentClimate == null) {
                removeBossBar(player)
                continue
            }

            val startTime = worldClimateStartTimes[worldName] ?: System.currentTimeMillis()
            val elapsedTime = System.currentTimeMillis() - startTime
            val progress = (1.0 - (elapsedTime.toDouble() / totalDurationMs.toDouble())).coerceIn(0.0, 1.0)

            val townBlock = try { townyAPI.getTownBlock(player.location) } catch (e: Exception) { null }
            val isInWilderness = (townBlock == null)
            var isInCollapsedTown = false

            if (!isInWilderness) {
                try {
                    if (townBlock!!.hasTown()) {
                        if (townData.hasAuraCollapsed(townBlock.town)) {
                            isInCollapsedTown = true
                        }
                    }
                } catch (e: Exception) { }
            }

            val shouldBeAffected = when (currentClimate.type) {
                ClimateType.HOSTILE -> isInWilderness || isInCollapsedTown
                ClimateType.BENEFICIAL -> isInWilderness || isInCollapsedTown
                ClimateType.NEUTRAL -> false
            }

            if (shouldBeAffected) {
                currentClimate.applyEffects(player)
            }

            updateBossBar(player, currentClimate, progress, shouldBeAffected)
        }
    }

    private fun updateBossBar(player: org.bukkit.entity.Player, climate: Climate, progress: Double, isAffected: Boolean) {
        val playerUUID = player.uniqueId
        if (climate.type == ClimateType.NEUTRAL) {
            removeBossBar(player)
            return
        }

        val (barTitle, barColor) = when (climate.type) {
            ClimateType.HOSTILE -> "§c☠ CLIMA: §l${climate.name}" to BarColor.RED
            ClimateType.BENEFICIAL -> "§e★ CLIMA: §l${climate.name}" to BarColor.GREEN
            else -> "" to BarColor.WHITE
        }

        var bar = activeBossBars[playerUUID]
        if (bar == null) {
            bar = Bukkit.createBossBar(barTitle, barColor, BarStyle.SOLID)
            bar.addPlayer(player)
            activeBossBars[playerUUID] = bar
        }

        bar.setTitle(barTitle)
        bar.color = barColor
        bar.progress = progress
        bar.style = if (isAffected) BarStyle.SOLID else BarStyle.SEGMENTED_6
    }

    private fun removeBossBar(player: org.bukkit.entity.Player) {
        activeBossBars.remove(player.uniqueId)?.removePlayer(player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        removeBossBar(event.player)
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        plugin.server.scheduler.runTask(plugin, Runnable {
            removeBossBar(event.player)
        })
    }

    fun shutdown() {
        activeBossBars.values.forEach { it.removeAll() }
        activeBossBars.clear()
        activeWorldClimates.clear()
        worldClimateStartTimes.clear()
    }

    fun getClimateForWorld(worldName: String): Climate? {
        return activeWorldClimates[worldName]
    }
}