package org.ReDiego0.auracore.tax

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownyMessaging
import org.ReDiego0.auracore.Auracore
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitTask

class TaxManager(private val plugin: Auracore) {

    private val townData = TownData(plugin)
    private val currencyManager = plugin.currencyManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"
    private var scheduledTask: BukkitTask? = null

    fun scheduleNextTaxCycle(delayTicks: Long) {
        scheduledTask?.cancel()

        scheduledTask = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            runTaxCollectionCycle()
        }, delayTicks)
    }

    fun runTaxCollectionCycle() {
        plugin.logger.info("Iniciando ciclo de cobro de impuestos de Aura (CC)...")
        val allTowns = TownyAPI.getInstance().towns
        var townsProcessed = 0
        var townsCollapsed = 0

        val baseCost = plugin.taxBaseCostCC
        val costPerChunk = plugin.taxCostPerChunkCC

        for (town in allTowns) {
            if (!town.hasMayor()) continue

            val mayor = town.mayor
            val chunks = town.numTownBlocks
            if (chunks <= 1) continue

            val cost = baseCost + (chunks - 1) * costPerChunk

            if (currencyManager.removeBalance(mayor.uuid, cost)) {
                townsProcessed++
                val mayorPlayer = mayor.player
                if (mayorPlayer != null && mayorPlayer.isOnline) {
                    mayorPlayer.sendMessage("${prefix}${ChatColor.GREEN}Se ha cobrado exitosamente el impuesto de ${cost} CC. El Aura de la ciudad es estable.")
                }
                if (townData.hasAuraCollapsed(town)) {
                    townData.setAuraCollapsed(town, false)
                    townData.setAllProtections(town, false)
                    TownyMessaging.sendPrefixedTownMessage(town, "${prefix}${ChatColor.GREEN}¡El Aura se ha estabilizado! La deuda de ${cost} CC ha sido pagada y las protecciones han sido RESTAURADAS.")
                }
            } else {
                townsCollapsed++
                if (!townData.hasAuraCollapsed(town)) {
                    townData.setAuraCollapsed(town, true)
                    townData.setAllProtections(town, true)
                    val mayorPlayer = mayor.player
                    if (mayorPlayer != null && mayorPlayer.isOnline) {
                        mayorPlayer.sendMessage("${prefix}${ChatColor.RED}¡ALERTA! No pudiste pagar el impuesto de ${cost} CC. Tu saldo es ${currencyManager.getBalance(mayor.uuid)}.")
                    }
                    TownyMessaging.sendPrefixedTownMessage(town, "${prefix}${ChatColor.DARK_GRAY}¡ALERTA! El alcalde no pudo pagar el impuesto de ${cost} CC. El Aura ha colapso. ¡Las protecciones están DESACTIVADAS!")
                }
            }
        }
        plugin.logger.info("Ciclo de cobro de impuestos de Aura (CC) finalizado. Ciudades procesadas: $townsProcessed. Ciudades colapsadas: $townsCollapsed.")

        val currentTimeMillis = System.currentTimeMillis()
        val nextTime = currentTimeMillis + (plugin.taxIntervalTicks * 50L)
        plugin.config.set("internal.next-tax-time-millis", nextTime)
        plugin.saveConfig()
        plugin.nextTaxTimeMillis = nextTime

        plugin.logger.info("Próximo ciclo de impuestos reprogramado para ${plugin.taxIntervalTicks / 20 / 3600} horas.")
        scheduleNextTaxCycle(plugin.taxIntervalTicks)
    }

    fun cancelScheduledTask() {
        scheduledTask?.cancel()
        scheduledTask = null
    }
}