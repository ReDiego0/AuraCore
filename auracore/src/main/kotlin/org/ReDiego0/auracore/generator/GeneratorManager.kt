package org.ReDiego0.auracore.generator

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.TownyMessaging
import eu.decentsoftware.holograms.api.DHAPI
import org.ReDiego0.auracore.Auracore
import org.ReDiego0.auracore.anomaly.HologramHelper
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class GeneratorManager(
    private val plugin: Auracore,
    private val generationAmount: Double,
    private val generationIntervalMillis: Long
) {

    private val generatorsFile: File = plugin.dataFolder.resolve("generators.yml")
    private lateinit var generatorsConfig: YamlConfiguration
    private val activeGenerators: ConcurrentHashMap<UUID, GeneratorData> = ConcurrentHashMap()

    private val currencyManager = plugin.currencyManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"

    fun loadGenerators() {
        if (!generatorsFile.exists()) {
            try {
                generatorsFile.parentFile.mkdirs()
                generatorsFile.createNewFile()
                plugin.logger.info("Creando nuevo archivo generators.yml.")
            } catch (e: IOException) {
                plugin.logger.severe("¡No se pudo crear generators.yml! ${e.message}")
                return
            }
        }
        generatorsConfig = YamlConfiguration.loadConfiguration(generatorsFile)
        loadActiveGeneratorsFromConfig()
        plugin.logger.info("${activeGenerators.size} generadores activos cargados desde generators.yml.")
        startGeneratorCheckTimer()
        startHologramUpdateTimer()
    }

    private fun loadActiveGeneratorsFromConfig() {
        activeGenerators.clear()
        generatorsConfig.getConfigurationSection("generators")?.getKeys(false)?.forEach { key ->
            try {
                val id = UUID.fromString(key)
                val path = "generators.$key"
                val worldName = generatorsConfig.getString("$path.world")
                val x = generatorsConfig.getDouble("$path.x")
                val y = generatorsConfig.getDouble("$path.y")
                val z = generatorsConfig.getDouble("$path.z")
                val nextGen = generatorsConfig.getLong("$path.nextGenerationTime", 0L)
                val hologramName = generatorsConfig.getString("$path.hologramName")

                val world = Bukkit.getWorld(worldName ?: "world")
                if (world != null) {
                    val location = Location(world, x, y, z)
                    val data = GeneratorData(id, location, nextGen, hologramName)
                    activeGenerators[id] = data
                } else {
                    plugin.logger.warning("Mundo '$worldName' no encontrado para el generador $id. Omitiendo.")
                }
            } catch (e: Exception) {
                plugin.logger.warning("Error al cargar el generador con ID $key: ${e.message}")
            }
        }
    }

    fun saveGenerators() {
        try {
            generatorsConfig.set("generators", null)
            activeGenerators.forEach { (id, data) ->
                val path = "generators.$id"
                generatorsConfig.set("$path.world", data.location.world?.name)
                generatorsConfig.set("$path.x", data.location.x)
                generatorsConfig.set("$path.y", data.location.y)
                generatorsConfig.set("$path.z", data.location.z)
                generatorsConfig.set("$path.nextGenerationTime", data.nextGenerationTime)
                generatorsConfig.set("$path.hologramName", data.hologramName)
            }
            generatorsConfig.save(generatorsFile)
        } catch (e: IOException) {
            plugin.logger.severe("¡No se pudo guardar generators.yml! ${e.message}")
        }
    }

    fun addGenerator(location: Location): GeneratorData? {
        val id = UUID.randomUUID()
        val hologramName = "generator_${id.toString().substring(0, 8)}"
        val firstNextGenTime = System.currentTimeMillis() + generationIntervalMillis
        val data = GeneratorData(id, location.block.location, firstNextGenTime, hologramName)

        activeGenerators[id] = data
        saveGenerators()
        plugin.logger.info("Generador $id añadido en ${data.location}. Próxima generación en ${generationIntervalMillis / 1000 / 60} minutos.")
        createOrUpdateHologram(data)
        return data
    }

    fun removeGenerator(id: UUID): Boolean {
        val removedData = activeGenerators.remove(id)
        if (removedData != null) {
            removedData.hologramName?.let { HologramHelper.deleteAnomalyHologram(it) }
            saveGenerators()
            plugin.logger.info("Generador $id removido.")
            return true
        }
        return false
    }

    fun getGeneratorById(id: UUID): GeneratorData? {
        return activeGenerators[id]
    }

    fun getGeneratorAtLocation(location: Location): GeneratorData? {
        val blockLoc = location.block.location
        return activeGenerators.values.find { it.location == blockLoc }
    }

    fun getAllGenerators(): Collection<GeneratorData> {
        return activeGenerators.values
    }

    private fun startGeneratorCheckTimer() {
        val checkIntervalTicks = 1200L

        object : BukkitRunnable() {
            override fun run() {
                checkGeneratorsAndRunCycles()
            }
        }.runTaskTimer(plugin, 0L, checkIntervalTicks)
    }

    private fun startHologramUpdateTimer() {
        val updateIntervalTicks = 100L

        object : BukkitRunnable() {
            override fun run() {
                updateAllHolograms()
            }
        }.runTaskTimer(plugin, 0L, updateIntervalTicks)
    }

    private fun checkGeneratorsAndRunCycles() {
        val currentTime = System.currentTimeMillis()

        val generatorsToCheck = activeGenerators.values.toList()

        generatorsToCheck.forEach { data ->
            if (currentTime >= data.nextGenerationTime) {
                runSingleGeneratorCycle(data)
            }
        }
    }

    private fun runSingleGeneratorCycle(data: GeneratorData) {
        val townyAPI = TownyAPI.getInstance()
        val location = data.location
        val townBlock = try { townyAPI.getTownBlock(location) } catch (e: Exception) { null }

        var generationSuccessful = false

        if (townBlock != null && townBlock.hasTown()) {
            try {
                val town = townBlock.town
                if (town.hasMayor()) {
                    val mayor = town.mayor
                    val success = currencyManager.addBalance(mayor.uuid, generationAmount)

                    if (success) {
                        generationSuccessful = true // Marcar como exitoso para actualizar la hora
                        mayor.player?.sendMessage("$prefix${ChatColor.GREEN}Has recibido ${generationAmount} CC del generador en (${location.blockX}, ${location.blockY}, ${location.blockZ}).")
                        TownyMessaging.sendPrefixedTownMessage(town, "$prefix${ChatColor.AQUA}El generador en (${location.blockX}, ${location.blockY}, ${location.blockZ}) ha producido ${generationAmount} CC para el alcalde.")
                    } else {
                        plugin.logger.warning("Error al depositar CC al alcalde ${mayor.name} para el generador ${data.id}")
                    }
                }
            } catch (e: Exception) {
                plugin.logger.warning("Error procesando generación para ${data.id}: ${e.message}")
            }
        }

        val newNextGenerationTime = System.currentTimeMillis() + generationIntervalMillis
        data.nextGenerationTime = newNextGenerationTime
        saveGenerators()

        createOrUpdateHologram(data)
    }

    private fun updateAllHolograms() {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            activeGenerators.values.forEach { data ->
                createOrUpdateHologram(data)
            }
        })
    }

    fun createOrUpdateHologram(data: GeneratorData) {
        val location = data.location
        val townyAPI = TownyAPI.getInstance()
        val townBlock = townyAPI.getTownBlock(location)
        val ownerName = try {
            if (townBlock != null && townBlock.hasTown()) townBlock.town.name else "${ChatColor.GRAY}Nadie"
        } catch (e: Exception) { "${ChatColor.GRAY}Nadie" }

        val remainingMillis = data.nextGenerationTime - System.currentTimeMillis()

        val timeString = if (remainingMillis <= 0) {
            "${ChatColor.GREEN}¡Generando!"
        } else {
            val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            buildString {
                append(ChatColor.YELLOW)
                if (hours > 0) append("${hours}h ")
                if (hours > 0 || minutes > 0) append("${minutes}m ")
            }
        }

        val hologramName = data.hologramName ?: return
        var hologram = DHAPI.getHologram(hologramName)

        val lines = listOf(
            "${ChatColor.AQUA}--- Generador de Aura ---",
            "${ChatColor.GRAY}Controlado por: ${ChatColor.WHITE}$ownerName",
            "${ChatColor.GRAY}Próxima Generación: $timeString",
            "#ICON: BEACON"
        )

        if (hologram == null) {
            val adjustedLocation = location.clone().add(0.5, 2.5, 0.5)
            hologram = DHAPI.createHologram(hologramName, adjustedLocation, false, lines)
            if (hologram != null) {
                hologram.enable()
            } else {
                plugin.logger.warning("No se pudo crear el holograma para el generador ${data.id}")
            }
        } else {
            DHAPI.setHologramLines(hologram, lines)
        }
    }
}