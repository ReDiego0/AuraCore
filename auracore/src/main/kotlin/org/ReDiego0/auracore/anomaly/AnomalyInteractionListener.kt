package org.ReDiego0.auracore.anomaly

import com.palmergames.bukkit.towny.TownyAPI
import eu.decentsoftware.holograms.event.HologramClickEvent
import org.ReDiego0.auracore.Auracore
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.UUID
import kotlin.random.Random

class AnomalyInteractionListener(
    private val plugin: Auracore,
    private val minReward: Double,
    private val maxReward: Double,
    private val bonusChance: Double,
    private val bonusAmount: Double
) : Listener {

    private val anomalyManager = plugin.anomalyManager
    private val currencyManager = plugin.currencyManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"
    private val hologramPrefix = "anomaly_"

    @EventHandler
    fun onHologramClick(event: HologramClickEvent) {
        val player = event.player
        val hologram = event.hologram
        val hologramName = hologram.name

        if (!hologramName.startsWith(hologramPrefix)) {
            return
        }

        val anomalyIdString = hologramName.substringAfter(hologramPrefix)
        val anomalyId = try {
            UUID.fromString(anomalyIdString)
        } catch (e: IllegalArgumentException) {
            plugin.logger.warning("Nombre de holograma inválido detectado: $hologramName (UUID: $anomalyIdString)")
            return
        }

        val anomalyData = anomalyManager.getAnomalyById(anomalyId)
        if (anomalyData == null) {
            player.sendMessage("$prefix${ChatColor.YELLOW}Esta anomalía parece haberse disipado.")
            HologramHelper.deleteAnomalyHologram(hologramName)
            return
        }

        val resident = TownyAPI.getInstance().getResident(player.uniqueId)
        if (resident == null || !resident.hasTown()) {
            player.sendMessage("$prefix${ChatColor.RED}Necesitas pertenecer a una ciudad para canalizar esta energía.")
            player.playSound(player.location, Sound.BLOCK_LEVER_CLICK, 0.5f, 0.5f)
            return
        }

        val town = resident.townOrNull
        if (town == null || !town.hasMayor()) {
            player.sendMessage("$prefix${ChatColor.RED}Tu ciudad no tiene un alcalde designado para recibir la energía.")
            return
        }
        val mayor = town.mayor

        val randomValue = Random.nextDouble()
        val amount = when {
            randomValue < bonusChance -> bonusAmount
            else -> Random.nextDouble(minReward, maxReward + 0.1) // Añadir 0.1 para incluir el maximo
        }.toInt().toDouble() // Convertir a Int para redondear y luego a Double

        val success = currencyManager.addBalance(mayor.uuid, amount)
        if (!success) {
            plugin.logger.warning("Error al depositar CC al alcalde ${mayor.name} desde la anomalía ${anomalyData.id}")
            player.sendMessage("$prefix${ChatColor.RED}Hubo un error al canalizar la energía. Contacta a un administrador.")
            return // No eliminar la anomalía si el pago falló
        }


        if (amount == bonusAmount) {
            player.sendMessage("$prefix${ChatColor.GOLD}${ChatColor.BOLD}¡FLUJO MASIVO DETECTADO! ${ChatColor.YELLOW}Has canalizado ${ChatColor.GREEN}${amount} CC ${ChatColor.YELLOW}para tu alcalde, ${mayor.name}!")
            player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f)
            player.playSound(player.location, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f)
        } else {
            player.sendMessage("$prefix${ChatColor.YELLOW}Has canalizado ${ChatColor.GREEN}${amount} CC ${ChatColor.YELLOW}para tu alcalde, ${mayor.name}.")
            player.playSound(player.location, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f)
        }

        val mayorPlayer = mayor.player
        if (mayorPlayer != null && mayorPlayer.isOnline) {
            mayorPlayer.sendMessage("$prefix${ChatColor.YELLOW}Has recibido ${ChatColor.GREEN}${amount} CC ${ChatColor.YELLOW}canalizados por ${player.name} desde una anomalía.")
        }

        HologramHelper.deleteAnomalyHologram(hologramName)
        anomalyManager.removeAnomaly(anomalyId)
    }
}