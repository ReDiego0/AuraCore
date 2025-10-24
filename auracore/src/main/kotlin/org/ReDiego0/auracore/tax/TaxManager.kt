package org.ReDiego0.auracore.tax

import com.palmergames.bukkit.towny.TownyAPI
import org.ReDiego0.auracore.Auracore
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitRunnable
import com.palmergames.bukkit.towny.TownyMessaging

class TaxManager(private val plugin: Auracore) {
    private val townData = TownData(plugin)
    private val currencyManager = plugin.currencyManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"

    companion object {
        private const val BASE_CC_COST = 10.0
        private const val CC_PER_CHUNK = 2.0
    }

    fun startTaxTimer(taxIntervalTicks: Long) {
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                    chargeAllTownsTax()
                })
            }
        }.runTaskTimer(plugin, 0L, taxIntervalTicks)
    }

    private fun chargeAllTownsTax() {
        plugin.logger.info("Iniciando ciclo de cobro de impuestos de Aura (CC)...")
        val allTowns = TownyAPI.getInstance().towns
        var townsProcessed = 0
        var townsCollapsed = 0

        for (town in allTowns) {
            if (!town.hasMayor()) continue

            val mayor = town.mayor
            val chunks = town.numTownBlocks
            if (chunks <= 1) continue

            val cost = BASE_CC_COST + (chunks - 1) * CC_PER_CHUNK
            if (currencyManager.removeBalance(mayor.uuid, cost)) {
                val cost = BASE_CC_COST + (chunks - 1) * CC_PER_CHUNK
                if (currencyManager.removeBalance(mayor.uuid, cost)) {
                    townsProcessed++

                    if (townData.hasAuraCollapsed(town)) {
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            townData.setAuraCollapsed(town, false)
                            townData.setAllProtections(town, true)
                            TownyMessaging.sendPrefixedTownMessage(town, "${prefix}${ChatColor.GREEN}¡El Aura se ha estabilizado! La deuda de ${cost} CC ha sido pagada y las protecciones han sido RESTAURADAS.")
                        })
                    }
                } else {
                    townsCollapsed++

                    if (!townData.hasAuraCollapsed(town)) {
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            townData.setAuraCollapsed(town, true)
                            townData.setAllProtections(town, false)
                            val mayorPlayer = mayor.player
                            if (mayorPlayer != null && mayorPlayer.isOnline) {
                                mayorPlayer.sendMessage("${prefix}${ChatColor.RED}¡ALERTA! No pudiste pagar el impuesto de ${cost} CC. Tu saldo es ${currencyManager.getBalance(mayor.uuid)}.")
                            }
                            TownyMessaging.sendPrefixedTownMessage(town, "${prefix}${ChatColor.RED}¡ALERTA! El alcalde no pudo pagar el impuesto de ${cost} CC. El Aura ha colapsado. ¡Las protecciones están DESACTIVADAS!")
                        })
                    }
                }
            }
}}}