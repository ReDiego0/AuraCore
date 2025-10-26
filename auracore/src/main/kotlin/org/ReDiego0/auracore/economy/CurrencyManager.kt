package org.ReDiego0.auracore.economy

import org.ReDiego0.auracore.Auracore
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CurrencyManager(private val plugin: Auracore, private val balancesFile: File) {
    private lateinit var balancesConfig: FileConfiguration

    private val onlinePlayerBalances = ConcurrentHashMap<UUID, Double>()

    fun getBalance(uuid: UUID): Double {
        return onlinePlayerBalances[uuid] ?: getBalanceOffline(uuid)
    }
    fun setBalance(uuid: UUID, amount: Double) {
        val finalAmount = amount.coerceAtLeast(0.0)
        if (onlinePlayerBalances.containsKey(uuid)) {
            onlinePlayerBalances[uuid] = finalAmount
        }
        balancesConfig.set(uuid.toString(), finalAmount)
    }

    fun addBalance(uuid: UUID, amount: Double) : Boolean {
        if (amount <= 0) return false
        val currentBalance = getBalance(uuid)
        setBalance(uuid, currentBalance + amount)
        return true
    }

    fun removeBalance(uuid: UUID, amount: Double): Boolean {
        if (amount <= 0) return true
        val currentBalance = getBalance(uuid)

        return if (currentBalance >= amount) {
            setBalance(uuid, currentBalance - amount)
            true
        } else {
            false
        }
    }

    fun loadPlayer(uuid: UUID) {
        val balance = balancesConfig.getDouble(uuid.toString(), 0.0)
        onlinePlayerBalances[uuid] = balance
    }

    fun unloadPlayer(uuid: UUID) {
        val balance = onlinePlayerBalances.remove(uuid)
        if (balance != null) {
            balancesConfig.set(uuid.toString(), balance)
        }
    }

    fun loadBalances() {
        if (!balancesFile.exists()) {
            try {
                balancesFile.parentFile.mkdirs()
                balancesFile.createNewFile()
                plugin.logger.info("Creando nuevo archivo balances.yml.")
            } catch (e: IOException) {
                plugin.logger.severe("¡No se pudo crear balances.yml! ${e.message}")
            }
        }
        balancesConfig = YamlConfiguration.loadConfiguration(balancesFile)
        plugin.logger.info("Saldos de CC cargados desde balances.yml.")
    }

    fun saveBalances() {
        try {
            onlinePlayerBalances.forEach { (uuid, balance) ->
                balancesConfig.set(uuid.toString(), balance)
            }
            balancesConfig.save(balancesFile)
            plugin.logger.info("Saldos de CC guardados en balances.yml.")
        } catch (e: IOException) {
            plugin.logger.severe("¡No se pudo guardar balances.yml! ${e.message}")
        }
    }

    private fun getBalanceOffline(uuid: UUID): Double {
        return balancesConfig.getDouble(uuid.toString(), 0.0)
    }
}