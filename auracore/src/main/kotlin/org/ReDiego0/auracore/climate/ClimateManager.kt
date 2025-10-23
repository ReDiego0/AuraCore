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
        val isClimateHostile = activeClimate.type == ClimateType.HOSTILE

        for (player in plugin.server.onlinePlayers) {
            val townBlock = try {
                townyAPI.getTownBlock(player.location)
            } catch (e: Exception) {
                null
            }

            val isInWilderness = (townBlock == null)
            val shouldBeAffected = isInWilderness && isClimateHostile

            if (shouldBeAffected) {
                activeClimate.applyEffects(player)
            }

            val playerUUID = player.uniqueId
            val currentBar = activeBossBars[playerUUID]

            if (shouldBeAffected) {
                val barTitle = "&c[PELIGRO] &l${activeClimate.name}]"
                if (currentBar == null) {
                    val newBar = Bukkit.createBossBar(barTitle, BarColor.RED, BarStyle.SOLID)
                    newBar.addPlayer(player)
                    activeBossBars[playerUUID] = newBar
                } else {
                    currentBar.setTitle(barTitle)
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