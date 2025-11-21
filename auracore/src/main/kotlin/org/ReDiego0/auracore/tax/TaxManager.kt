package org.ReDiego0.auracore.tax

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownyMessaging
import com.palmergames.bukkit.towny.`object`.WorldCoord
import org.ReDiego0.auracore.Auracore
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitTask
import org.ReDiego0.auracore.generator.GeneratorManager

class TaxManager(private val plugin: Auracore, private val generatorManager: GeneratorManager) {

    private val townData = TownData(plugin)
    private val currencyManager = plugin.currencyManager
    private val prefix = "${ChatColor.AQUA}[${plugin.auraName}] ${ChatColor.GRAY}"
    private var scheduledTask: BukkitTask? = null

    fun scheduleNextTaxCycle(delayTicks: Long) {
        scheduledTask?.cancel()

        scheduledTask = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            runTaxCollectionCycle()
        }, delayTicks)
    }

    fun runTaxCollectionCycle() {
        plugin.logger.info("Iniciando ciclo de cobro de impuestos de ${plugin.auraName} (${plugin.currencyShortName})...")
        val allTowns = TownyAPI.getInstance().towns
        var townsProcessed = 0
        var townsCollapsed = 0

        val baseCostPerGenerator = plugin.taxBaseCostCC
        val costPerChunk = plugin.taxCostPerChunkCC
        val generatorLocations = generatorManager.getAllGenerators().map { it.location.block.location }.toSet()

        for (town in allTowns) {
            if (!town.hasMayor()) continue

            val mayor = town.mayor
            val townBlocksCollection = town.townBlocks
            val chunks = townBlocksCollection.size
            if (chunks == 0) continue

            var generatorCount = 0
            val townWorldCoords = townBlocksCollection.map { it.worldCoord }.toSet()

            for (genLocation in generatorLocations) {
                val genWorldCoord = WorldCoord.parseWorldCoord(genLocation)
                if (townWorldCoords.contains(genWorldCoord)) {
                    generatorCount++
                }
            }
            val cost = (baseCostPerGenerator * generatorCount) + (chunks - 1) * costPerChunk
            val finalCost = cost.coerceAtLeast(0.0)

            if (finalCost <= 0) {
                townsProcessed++
                if (townData.hasAuraCollapsed(town)) {
                    townData.setAuraCollapsed(town, false)
                    townData.setAllProtections(town, false)
                }
                continue
            }

            if (currencyManager.removeBalance(mayor.uuid, finalCost)) {
                townsProcessed++
                val mayorPlayer = mayor.player
                if (mayorPlayer != null && mayorPlayer.isOnline) {
                    mayorPlayer.sendMessage("${prefix}${ChatColor.GREEN}Se ha cobrado exitosamente el impuesto de ${finalCost} ${plugin.currencyShortName}. El ${plugin.auraName} de la ciudad es estable.")
                }
                if (townData.hasAuraCollapsed(town)) {
                    townData.setAuraCollapsed(town, false)
                    townData.setAllProtections(town, false)
                    TownyMessaging.sendPrefixedTownMessage(town, "${prefix}${ChatColor.GREEN}¡El ${plugin.auraName} se ha estabilizado! La deuda de ${finalCost} ${plugin.currencyShortName} ha sido pagada y las protecciones han sido RESTAURADAS.")
                }
            } else {
                townsCollapsed++
                if (!townData.hasAuraCollapsed(town)) {
                    townData.setAuraCollapsed(town, true)
                    townData.setAllProtections(town, true)
                    val mayorPlayer = mayor.player
                    if (mayorPlayer != null && mayorPlayer.isOnline) {
                        mayorPlayer.sendMessage("${prefix}${ChatColor.RED}¡ALERTA! No pudiste pagar el impuesto de ${finalCost} ${plugin.currencyShortName}. Tu saldo es ${currencyManager.getBalance(mayor.uuid)} ${plugin.currencyShortName}.")
                    }
                    TownyMessaging.sendPrefixedTownMessage(town, "${prefix}${ChatColor.DARK_GRAY}¡ALERTA! El alcalde no pudo pagar el impuesto de ${finalCost} ${plugin.currencyShortName}. El ${plugin.auraName} ha colapsado. ¡Las protecciones están DESACTIVADAS!")
                }
            }
        }
        plugin.logger.info("Ciclo de cobro de impuestos de ${plugin.auraName} (${plugin.currencyShortName}) finalizado. Ciudades procesadas: $townsProcessed. Ciudades colapsadas: $townsCollapsed.")

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