package org.ReDiego0.auracore.economy

import org.ReDiego0.auracore.Auracore
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID

class CurrencyCommands(private val plugin: Auracore) : CommandExecutor {
    private val currencyManager = plugin.currencyManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage("${prefix}Este comando solo puede ser usado por un jugador. Los administradores de consola usen /auracc balance <jugador>.")
                return true
            }
            val balance = currencyManager.getBalance(sender.uniqueId)
            sender.sendMessage("${prefix}Tu saldo de CC: ${ChatColor.GREEN}${balance}")
            return true
        }

        when (args[0].lowercase()) {
            "balance", "bal" -> handleBalance(sender, args)
            "pay", "pagar" -> handlePay(sender, args)
            "help", "ayuda" -> handleHelp(sender)
            "ver", "version" -> handleVersion(sender)

            "give", "add", "añadir" -> handleGive(sender, args)
            "set", "establecer" -> handleSet(sender, args)
            "take", "remove", "quitar" -> handleTake(sender, args)

            else -> sender.sendMessage("${prefix}${ChatColor.RED}Comando desconocido. Usa /auracc help.")
        }

        return true
    }

    private fun handleBalance(sender: CommandSender, args: Array<out String>) {
        if (args.size == 1) {
            if (sender !is Player) {
                sender.sendMessage("${prefix}La consola debe especificar un jugador.")
                return
            }
            val balance = currencyManager.getBalance(sender.uniqueId)
            sender.sendMessage("${prefix}Tu saldo de CC: ${ChatColor.GREEN}${balance}")

        } else if (args.size == 2) {
            if (!sender.hasPermission("auracore.admin")) {
                sender.sendMessage("${prefix}${ChatColor.RED}No tienes permiso para ver el saldo de otros.")
                return
            }

            val targetName = args[1]
            val targetUUID = getUUIDFromName(targetName)
            if (targetUUID == null) {
                sender.sendMessage("${prefix}${ChatColor.RED}Jugador '$targetName' no encontrado.")
                return
            }

            val balance = currencyManager.getBalance(targetUUID)
            sender.sendMessage("${prefix}Saldo de $targetName: ${ChatColor.GREEN}${balance}")
        } else {
            sender.sendMessage("${prefix}${ChatColor.RED}Uso: /auracc balance [jugador]")
        }
    }

    private fun handlePay(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("${prefix}Este comando solo puede ser usado por un jugador.")
            return
        }

        if (args.size != 3) {
            sender.sendMessage("${prefix}${ChatColor.RED}Uso: /auracc pay <jugador> <cantidad>")
            return
        }

        val amount = args[2].toDoubleOrNull()
        if (amount == null || amount <= 0) {
            sender.sendMessage("${prefix}${ChatColor.RED}La cantidad debe ser un número positivo.")
            return
        }

        val targetName = args[1]
        val targetPlayer = Bukkit.getPlayer(targetName)
        if (targetPlayer == null || targetPlayer == sender) {
            sender.sendMessage("${prefix}${ChatColor.RED}Jugador no encontrado o no puedes pagarte a ti mismo.")
            return
        }

        if (currencyManager.removeBalance(sender.uniqueId, amount)) {
            currencyManager.addBalance(targetPlayer.uniqueId, amount)
            sender.sendMessage("${prefix}Has enviado ${ChatColor.GREEN}${amount} CC${ChatColor.GRAY} a ${targetPlayer.name}.")
            targetPlayer.sendMessage("${prefix}Has recibido ${ChatColor.GREEN}${amount} CC${ChatColor.GRAY} de ${sender.name}.")
        } else {
            sender.sendMessage("${prefix}${ChatColor.RED}No tienes suficientes CC para hacer eso.")
        }
    }

    private fun handleHelp(sender: CommandSender) {
        sender.sendMessage("${ChatColor.AQUA}--- Ayuda de AuraCore (CC) ---")
        sender.sendMessage("${ChatColor.GREEN}/auracc ${ChatColor.GRAY}- Muestra tu saldo de CC.")
        sender.sendMessage("${ChatColor.GREEN}/auracc pay <jugador> <cantidad> ${ChatColor.GRAY}- Envía CC a otro jugador.")
        sender.sendMessage("${ChatColor.GREEN}/auracc ver ${ChatColor.GRAY}- Muestra la versión del plugin.")
        if (sender.hasPermission("auracore.admin")) {
            sender.sendMessage("${ChatColor.YELLOW}--- Comandos de Admin ---")
            sender.sendMessage("${ChatColor.YELLOW}/auracc balance <jugador> ${ChatColor.GRAY}- Muestra el saldo de un jugador.")
            sender.sendMessage("${ChatColor.YELLOW}/auracc give <jugador> <cantidad> ${ChatColor.GRAY}- Da CC a un jugador.")
            sender.sendMessage("${ChatColor.YELLOW}/auracc set <jugador> <cantidad> ${ChatColor.GRAY}- Establece el saldo de CC de un jugador.")
            sender.sendMessage("${ChatColor.YELLOW}/auracc take <jugador> <cantidad> ${ChatColor.GRAY}- Quita CC a un jugador.")
        }
    }

    private fun handleVersion(sender: CommandSender) {
        sender.sendMessage("${prefix}AuraCore Versión: ${plugin.description.version}")
    }

    private fun handleGive(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("auracore.admin")) {
            sender.sendMessage("${prefix}${ChatColor.RED}No tienes permiso para usar este comando.")
            return
        }

        if (args.size != 3) {
            sender.sendMessage("${prefix}${ChatColor.RED}Uso: /auracc give <jugador> <cantidad>")
            return
        }

        val amount = args[2].toDoubleOrNull()
        if (amount == null || amount <= 0) {
            sender.sendMessage("${prefix}${ChatColor.RED}La cantidad debe ser un número positivo.")
            return
        }

        val targetName = args[1]
        val targetUUID = getUUIDFromName(targetName)
        if (targetUUID == null) {
            sender.sendMessage("${prefix}${ChatColor.RED}Jugador '$targetName' no encontrado.")
            return
        }

        currencyManager.addBalance(targetUUID, amount)
        val newBalance = currencyManager.getBalance(targetUUID)
        sender.sendMessage("${prefix}Has dado ${ChatColor.GREEN}${amount} CC${ChatColor.GRAY} a $targetName. Nuevo saldo: ${ChatColor.GREEN}$newBalance")
        Bukkit.getPlayer(targetUUID)?.sendMessage("${prefix}${ChatColor.GREEN}Has recibido ${amount} CC ${ChatColor.GRAY}de un administrador.")
    }

    private fun handleSet(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("auracore.admin")) {
            sender.sendMessage("${prefix}${ChatColor.RED}No tienes permiso para usar este comando.")
            return
        }

        if (args.size != 3) {
            sender.sendMessage("${prefix}${ChatColor.RED}Uso: /auracc set <jugador> <cantidad>")
            return
        }

        val amount = args[2].toDoubleOrNull()
        if (amount == null || amount < 0) {
            sender.sendMessage("${prefix}${ChatColor.RED}La cantidad debe ser un número positivo (0 o más).")
            return
        }

        val targetName = args[1]
        val targetUUID = getUUIDFromName(targetName)
        if (targetUUID == null) {
            sender.sendMessage("${prefix}${ChatColor.RED}Jugador '$targetName' no encontrado.")
            return
        }

        currencyManager.setBalance(targetUUID, amount)
        sender.sendMessage("${prefix}Has establecido el saldo de $targetName en ${ChatColor.GREEN}${amount} CC.")
        Bukkit.getPlayer(targetUUID)?.sendMessage("${prefix}${ChatColor.YELLOW}Tu saldo de CC ha sido establecido en ${ChatColor.GREEN}${amount} ${ChatColor.YELLOW}por un administrador.")
    }

    private fun handleTake(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("auracore.admin")) {
            sender.sendMessage("${prefix}${ChatColor.RED}No tienes permiso para usar este comando.")
            return
        }

        if (args.size != 3) {
            sender.sendMessage("${prefix}${ChatColor.RED}Uso: /auracc take <jugador> <cantidad>")
            return
        }

        val amount = args[2].toDoubleOrNull()
        if (amount == null || amount <= 0) {
            sender.sendMessage("${prefix}${ChatColor.RED}La cantidad debe ser un número positivo.")
            return
        }

        val targetName = args[1]
        val targetUUID = getUUIDFromName(targetName)
        if (targetUUID == null) {
            sender.sendMessage("${prefix}${ChatColor.RED}Jugador '$targetName' no encontrado.")
            return
        }

        if (currencyManager.removeBalance(targetUUID, amount)) {
            val newBalance = currencyManager.getBalance(targetUUID)
            sender.sendMessage("${prefix}Has quitado ${ChatColor.GREEN}${amount} CC${ChatColor.GRAY} a $targetName. Nuevo saldo: ${ChatColor.GREEN}$newBalance")
            Bukkit.getPlayer(targetUUID)?.sendMessage("${prefix}${ChatColor.RED}Se te han quitado ${amount} CC ${ChatColor.GRAY}por un administrador.")
        } else {
            sender.sendMessage("${prefix}${ChatColor.RED}$targetName no tiene suficientes fondos. Su saldo es ${currencyManager.getBalance(targetUUID)}")
        }
    }

    @Suppress("DEPRECATION")
    private fun getUUIDFromName(playerName: String): UUID? {
        val player = Bukkit.getPlayer(playerName)
        if (player != null) {
            return player.uniqueId
        }
        val offlinePlayer = Bukkit.getOfflinePlayer(playerName)
        return if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline) {
            offlinePlayer.uniqueId
        } else {
            null
        }
    }
}

