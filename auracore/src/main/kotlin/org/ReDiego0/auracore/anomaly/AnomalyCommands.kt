package org.ReDiego0.auracore.anomaly

import org.ReDiego0.auracore.Auracore
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class AnomalyCommands(private val plugin: Auracore) : CommandExecutor {

    private val anomalyManager = plugin.anomalyManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("auracore.admin")) {
            sender.sendMessage("${ChatColor.RED}No tienes permiso para usar este comando.")
            return true
        }

        if (args.isEmpty() || args[0].lowercase() != "anomalia") {
            sendHelp(sender)
            return true
        }

        if (args.size < 2) {
            sendHelp(sender)
            return true
        }

        when (args[1].lowercase()) {
            "place", "colocar" -> handlePlace(sender, args)
            "remove", "remover", "eliminar" -> handleRemove(sender, args)
            "list", "listar" -> handleList(sender)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handlePlace(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("${prefix}Este comando solo puede ser ejecutado por un jugador.")
            return
        }
        if (args.size != 3) {
            sender.sendMessage("${prefix}${ChatColor.RED}Uso: /auracore anomalia place <nombreSchematic>")
            return
        }

        val schematicName = args[2]
        val location = sender.location

        sender.sendMessage("${prefix}Intentando colocar anomalía '$schematicName' en tu ubicación...")

        WorldEditHelper.pasteSchematic(plugin, schematicName, location) { success, markerLocation ->
            if (success && markerLocation != null) {
                val anomalyData = anomalyManager.addAnomaly(markerLocation, schematicName)
                if (anomalyData != null) {
                    HologramHelper.createAnomalyHologram(plugin, anomalyData.hologramName, markerLocation)
                    sender.sendMessage("${prefix}${ChatColor.GREEN}¡Anomalía colocada exitosamente! ID: ${anomalyData.id}")
                } else {
                    sender.sendMessage("${prefix}${ChatColor.RED}Error al guardar los datos de la anomalía.")
                }
            } else if (success && markerLocation == null) {
                sender.sendMessage("${prefix}${ChatColor.RED}Schematic pegado, ¡pero no se encontró el bloque marcador (Esponja)!")
            }
            else {
                sender.sendMessage("${prefix}${ChatColor.RED}Error al pegar el schematic '$schematicName'. Revisa la consola.")
            }
        }
    }

    private fun handleRemove(sender: CommandSender, args: Array<out String>) {
        if (args.size != 3) {
            sender.sendMessage("${prefix}${ChatColor.RED}Uso: /auracore anomalia remove <ID>")
            return
        }

        val idString = args[2]
        val anomalyId = try {
            UUID.fromString(idString)
        } catch (e: IllegalArgumentException) {
            sender.sendMessage("${prefix}${ChatColor.RED}ID inválido.")
            return
        }

        val anomalyData = anomalyManager.getAnomalyById(anomalyId)
        if (anomalyData == null) {
            sender.sendMessage("${prefix}${ChatColor.RED}No se encontró ninguna anomalía con ese ID.")
            return
        }

        HologramHelper.deleteAnomalyHologram(anomalyData.hologramName)

        if (anomalyManager.removeAnomaly(anomalyId)) {
            sender.sendMessage("${prefix}${ChatColor.GREEN}Anomalía ${anomalyId} removida exitosamente.")
            sender.sendMessage("${prefix}${ChatColor.YELLOW}Nota: La estructura física no ha sido removida automáticamente.")
        } else {
            sender.sendMessage("${prefix}${ChatColor.RED}Error al remover la anomalía del archivo.")
        }
    }

    private fun handleList(sender: CommandSender) {
        val anomalies = anomalyManager.getAllAnomalies()
        if (anomalies.isEmpty()) {
            sender.sendMessage("${prefix}No hay anomalías activas.")
            return
        }
        sender.sendMessage("${prefix}--- Anomalías Activas (${anomalies.size}) ---")
        anomalies.forEach { data ->
            val loc = data.location
            sender.sendMessage("${ChatColor.GRAY}- ID: ${ChatColor.AQUA}${data.id}")
            sender.sendMessage("${ChatColor.GRAY}  Schem: ${ChatColor.WHITE}${data.schematicName}")
            sender.sendMessage("${ChatColor.GRAY}  Loc: ${ChatColor.WHITE}${loc.world?.name}, ${loc.blockX}, ${loc.blockY}, ${loc.blockZ}")
        }
    }


    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${ChatColor.AQUA}--- Ayuda de Administración de AuraCore ---")
        sender.sendMessage("${ChatColor.GREEN}/auracore anomalia place <schematic> ${ChatColor.GRAY}- Coloca una anomalía en tu ubicación.")
        sender.sendMessage("${ChatColor.GREEN}/auracore anomalia remove <ID> ${ChatColor.GRAY}- Elimina una anomalía por su ID.")
        sender.sendMessage("${ChatColor.GREEN}/auracore anomalia list ${ChatColor.GRAY}- Muestra las anomalías activas.")
    }

}