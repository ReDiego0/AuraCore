package org.ReDiego0.auracore.tax

import com.palmergames.bukkit.towny.TownyEconomyHandler
import com.palmergames.bukkit.towny.event.TownPreClaimEvent
import org.ReDiego0.auracore.Auracore
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ClaimCostListener(private val plugin: Auracore) : Listener {

    private val currencyManager = plugin.currencyManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onTownPreClaim(event: TownPreClaimEvent) {
        if (event.isCancelled) return

        val player = event.player ?: return
        val town = event.town ?: return
        val townBlock = event.townBlock ?: return

        if (!town.hasMayor()) {
            player.sendMessage("${prefix}${ChatColor.RED}Tu ciudad necesita un alcalde para poder reclamar territorios.")
            event.isCancelled = true
            return
        }
        val mayor = town.mayor

        val isOutpost = townBlock.isOutpost

        val costCC: Double
        val costMP: Double

        if (isOutpost) {
            costCC = plugin.claimOutpostCostCC
            costMP = plugin.claimOutpostCostMP
        } else {
            costCC = plugin.claimNormalCostCC
            costMP = plugin.claimNormalCostMP
        }

        if (costCC <= 0 && costMP <= 0) {
            return
        }

        val mayorBalanceCC = currencyManager.getBalance(mayor.uuid)

        val townBalanceMP = try {
            TownyEconomyHandler.getBalance(town.name, town.world)
        } catch (e: Exception) {
            plugin.logger.warning("Error al obtener saldo MP de ${town.name}: ${e.message}")
            0.0
        }

        var message = ""
        var canAfford = true

        if (mayorBalanceCC < costCC) {
            message += "${ChatColor.RED}El alcalde (${mayor.name}) necesita ${String.format("%.2f", costCC - mayorBalanceCC)} CC más. "
            canAfford = false
        }
        if (townBalanceMP < costMP) {
            message += "${ChatColor.RED}El banco de la ciudad necesita ${String.format("%.2f", costMP - townBalanceMP)} MP más."
            canAfford = false
        }

        if (canAfford) {
            var chargedCC = false
            var chargedMP = false

            if (costCC > 0) {
                if (currencyManager.removeBalance(mayor.uuid, costCC)) {
                    chargedCC = true
                } else {
                    player.sendMessage("${prefix}${ChatColor.RED}Error inesperado al cobrar ${costCC} CC al alcalde (Saldo actual: $mayorBalanceCC).")
                    event.isCancelled = true
                    return
                }
            } else {
                chargedCC = true
            }

            if (costMP > 0) {
                try {
                    val success = TownyEconomyHandler.subtract(town.name, costMP, town.world)
                    if (success) {
                        chargedMP = true
                    } else {
                        player.sendMessage("${prefix}${ChatColor.RED}El banco de la ciudad no tiene suficientes fondos (${costMP} MP).")
                        event.isCancelled = true
                        if (chargedCC) {
                            currencyManager.addBalance(mayor.uuid, costCC)
                        }
                        return
                    }
                } catch (e: Exception) {
                    player.sendMessage("${prefix}${ChatColor.RED}Error al cobrar ${costMP} MP del banco de la ciudad.")
                    plugin.logger.warning("Error de economía al cobrar a ${town.name}: ${e.javaClass.simpleName} - ${e.message}")
                    event.isCancelled = true
                    if (chargedCC) {
                        currencyManager.addBalance(mayor.uuid, costCC)
                    }
                    return
                }
            } else {
                chargedMP = true
            }

            if (chargedCC && chargedMP) {
                var successMessage = "${prefix}${ChatColor.GREEN}Chunk reclamado exitosamente."
                if (costCC > 0) successMessage += " Costo: ${ChatColor.AQUA}${costCC} CC ${ChatColor.GRAY}(Alcalde)"
                if (costMP > 0) successMessage += " ${ChatColor.GOLD}${costMP} MP ${ChatColor.GRAY}(Ciudad)"
                player.sendMessage(successMessage)
            }

        } else {
            event.isCancelled = true
            player.sendMessage("${prefix}${ChatColor.RED}No se puede reclamar el chunk:")
            player.sendMessage(message)
        }
    }}