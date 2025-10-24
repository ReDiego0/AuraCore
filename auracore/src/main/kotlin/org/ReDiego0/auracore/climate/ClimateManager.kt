package org.ReDiego0.auracore.climate

import com.palmergames.bukkit.towny.TownyAPI
import org.ReDiego0.auracore.Auracore
import org.ReDiego0.auracore.climate.impl.FlujoVital
import org.ReDiego0.auracore.climate.impl.FrecuenciaSonica
import org.ReDiego0.auracore.climate.impl.MareaRoja
import org.ReDiego0.auracore.climate.impl.PazAtmosferica
import org.ReDiego0.auracore.climate.impl.PulsoEstatico
import org.ReDiego0.auracore.climate.impl.ResonanciaArmonica
import org.ReDiego0.auracore.climate.impl.TormentaGeomagnetica
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.event.EventHandler
import java.util.UUID
import org.bukkit.event.Listener
import org.ReDiego0.auracore.tax.TownData

class ClimateManager(private val plugin: Auracore) : Listener {
    private val allClimates: List<Climate> = listOf(
        PazAtmosferica,
        TormentaGeomagnetica,
        MareaRoja,
        FrecuenciaSonica,
        FlujoVital,
        PulsoEstatico,
        ResonanciaArmonica
    )

    var activeClimate: Climate = PazAtmosferica
        private set

    private val activeBossBars: MutableMap<UUID, BossBar> = mutableMapOf()

    private val townData = TownData(plugin)
    
    private var climateStartTime: Long = 0L
    private var climateDurationTicks: Long = 0L
    
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
    
    fun startClimateTimer(changeInterval: Long) {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            pickNewClimate()
        }, 0L, changeInterval)
    }

    fun startEffectApplicator(checkInterval: Long) {
        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            applyClimateEffectsToPlayers()
        }, 0L, checkInterval)
    }

    private fun pickNewClimate() {
        val newClimate = allClimates.random()
        activeClimate = newClimate
        
        climateStartTime = System.currentTimeMillis()
        climateDurationTicks = plugin.config.getLong("climate.changeInterval", 36000L)

        if (newClimate.type == ClimateType.HOSTILE) {
            Bukkit.broadcastMessage("§c[DEBUG] El Aura se ha vuelto inestable. Se detecta: §l${newClimate.name}§c.")
        } else if (newClimate.type == ClimateType.NEUTRAL) {
            Bukkit.broadcastMessage("§a[DEBUG] El Campo de Aura se ha estabilizado. El clima es: §l${newClimate.name}§a.")
        } else if (newClimate.type == ClimateType.BENEFICIAL) {
            Bukkit.broadcastMessage("§e[DEBUG] El Campo de Aura se ha vuelto favorable. Se manifiesta: §l${newClimate.name}§e.")
        }

        val mainWorld = plugin.server.worlds.firstOrNull()
        mainWorld?.let {
            newClimate.applyVisuals(it)
        }
    }

    private fun applyClimateEffectsToPlayers() {
        val townyAPI = TownyAPI.getInstance()

        // Calcular el progreso del clima (de 1.0 a 0.0)
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - climateStartTime
        val totalDuration = climateDurationTicks * 50L
        val progress = if (totalDuration > 0) {
            1.0 - (elapsedTime.toDouble() / totalDuration.toDouble())
        } else {
            1.0
        }
        val clampedProgress = progress.coerceIn(0.0, 1.0)

        for (player in plugin.server.onlinePlayers) {
            val townBlock = try {
                townyAPI.getTownBlock(player.location)
            } catch (e: Exception) {
                null
            }

            val isInWilderness = (townBlock == null)
            var isInCollapsedTown = false

            if (!isInWilderness) {
                try {
                    if (townBlock!!.hasTown()) {
                        val town = townBlock.town
                        if (townData.hasAuraCollapsed(town)) {
                            isInCollapsedTown = true
                        }
                    }
                } catch (e: Exception) {
                }
            }

            val shouldBeAffected = when (activeClimate.type) {
                ClimateType.HOSTILE -> isInWilderness || isInCollapsedTown
                ClimateType.BENEFICIAL -> isInWilderness || isInCollapsedTown
                ClimateType.NEUTRAL -> false
            }

            if (shouldBeAffected) {
                activeClimate.applyEffects(player)
            }

            val playerUUID = player.uniqueId
            val currentBar = activeBossBars[playerUUID]

            // Mostrar barra según el tipo de clima
            if (shouldBeAffected && activeClimate.type != ClimateType.NEUTRAL) {
                val (barTitle, barColor) = when (activeClimate.type) {
                    ClimateType.HOSTILE -> "§c[PELIGRO] §l${activeClimate.name}" to BarColor.RED
                    ClimateType.BENEFICIAL -> "§a[BENEFICIO] §l${activeClimate.name}" to BarColor.GREEN
                    else -> "" to BarColor.WHITE
                }
                
                if (currentBar == null) {
                    val newBar = Bukkit.createBossBar(barTitle, barColor, BarStyle.SOLID)
                    newBar.progress = clampedProgress
                    newBar.addPlayer(player)
                    activeBossBars[playerUUID] = newBar
                } else {
                    currentBar.setTitle(barTitle)
                    currentBar.color = barColor
                    currentBar.progress = clampedProgress
                    if (!currentBar.players.contains(player)) {
                        currentBar.addPlayer(player)
                    }
                }
            } else {
                if (currentBar != null) {
                    currentBar.removePlayer(player)
                    activeBossBars.remove(playerUUID)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: org.bukkit.event.player.PlayerQuitEvent) {
        activeBossBars.remove(event.player.uniqueId)
    }

    fun shutdown() {
        activeBossBars.values.forEach {
            bar -> bar.removeAll()
        }
        activeBossBars.clear()
        println("[AuraCore] Bossbars cleaned.")
    }
}