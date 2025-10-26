package org.ReDiego0.auracore.generator

import org.ReDiego0.auracore.Auracore
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GeneratorCommands(private val plugin: Auracore) : CommandExecutor {

    private val generatorManager = plugin.generatorManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"

    companion object {
        val GENERATOR_BLOCK_MATERIAL = Material.BEACON
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("auracore.admin")) {
            sender.sendMessage("${ChatColor.RED}No tienes permiso para usar este comando.")
            return true
        }

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "place", "colocar" -> handlePlace(sender)
            "remove", "remover", "eliminar" -> handleRemove(sender)
            "list", "listar" -> handleList(sender)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handlePlace(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage("${prefix}Este comando solo puede ser ejecutado por un jugador.")
            return
        }

        val targetBlock = sender.getTargetBlockExact(5)
        if (targetBlock == null || targetBlock.type.isAir) {
            sender.sendMessage("${prefix}${ChatColor.RED}Debes estar mirando el bloque exacto donde quieres colocar el generador (máx. 5 bloques).")
            return
        }

        val location = targetBlock.location
        val existingGenerator = generatorManager.getGeneratorAtLocation(location)
        if (existingGenerator != null) {
            sender.sendMessage("${prefix}${ChatColor.RED}Ya existe un generador en esta ubicación (ID: ${existingGenerator.id}).")
            return
        }

        location.block.type = GENERATOR_BLOCK_MATERIAL
        val generatorData = generatorManager.addGenerator(location)

        if (generatorData != null) {
            sender.sendMessage("${prefix}${ChatColor.GREEN}Generador ${generatorData.id} colocado exitosamente en (${location.blockX}, ${location.blockY}, ${location.blockZ}).")
        } else {
            sender.sendMessage("${prefix}${ChatColor.RED}Error al guardar los datos del generador.")
            location.block.type = Material.AIR
        }
    }

    private fun handleRemove(sender: CommandSender) {
        if (sender !is Player && sender !is org.bukkit.command.ConsoleCommandSender) {
            sender.sendMessage("${prefix}${ChatColor.RED}Debes ser un jugador mirando el generador o usar la consola con el ID.")
            return
        }

        var generatorToRemove: GeneratorData? = null

        if (sender is Player) {
            val targetBlock = sender.getTargetBlockExact(5)
            if (targetBlock != null && targetBlock.type == GENERATOR_BLOCK_MATERIAL) {
                generatorToRemove = generatorManager.getGeneratorAtLocation(targetBlock.location)
                if (generatorToRemove == null) {
                    sender.sendMessage("${prefix}${ChatColor.RED}Este bloque (${GENERATOR_BLOCK_MATERIAL.name}) no es un generador registrado.")
                    return
                }
            }
        }

        if (generatorToRemove == null && sender is org.bukkit.command.ConsoleCommandSender) {
            sender.sendMessage("${prefix}${ChatColor.RED}La consola debe especificar un ID (próximamente). Por ahora, un jugador debe mirar el bloque.")
            return
        }


        if (generatorToRemove == null) {
            sender.sendMessage("${prefix}${ChatColor.RED}No estás mirando un bloque generador (${GENERATOR_BLOCK_MATERIAL.name}) o no se encontró un generador registrado aquí.")
            return
        }


        generatorToRemove.location.block.type = Material.AIR

        if (generatorManager.removeGenerator(generatorToRemove.id)) {
            sender.sendMessage("${prefix}${ChatColor.GREEN}Generador ${generatorToRemove.id} removido exitosamente.")
        } else {
            sender.sendMessage("${prefix}${ChatColor.RED}Error al remover el generador del archivo.")
            generatorToRemove.location.block.type = GENERATOR_BLOCK_MATERIAL
        }
    }

    private fun handleList(sender: CommandSender) {
        val generators = generatorManager.getAllGenerators()
        if (generators.isEmpty()) {
            sender.sendMessage("${prefix}No hay generadores activos.")
            return
        }
        sender.sendMessage("${prefix}--- Generadores Activos (${generators.size}) ---")
        generators.forEach { data ->
            val loc = data.location
            sender.sendMessage("${ChatColor.GRAY}- ID: ${ChatColor.AQUA}${data.id}")
            sender.sendMessage("${ChatColor.GRAY}  Loc: ${ChatColor.WHITE}${loc.world?.name}, ${loc.blockX}, ${loc.blockY}, ${loc.blockZ}")
        }
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${ChatColor.AQUA}--- Ayuda de Generadores AuraCore ---")
        sender.sendMessage("${ChatColor.GREEN}/gcc place ${ChatColor.GRAY}- Coloca un generador en el bloque que miras.")
        sender.sendMessage("${ChatColor.GREEN}/gcc remove ${ChatColor.GRAY}- Elimina el generador que miras.")
        sender.sendMessage("${ChatColor.GREEN}/gcc list ${ChatColor.GRAY}- Muestra los generadores activos.")
    }
}