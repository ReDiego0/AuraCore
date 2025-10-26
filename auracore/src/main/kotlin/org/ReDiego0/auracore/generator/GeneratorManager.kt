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

class GeneratorManager(private val plugin: Auracore) {

    private val generatorsFile: File = plugin.dataFolder.resolve("generators.yml")
    private lateinit var generatorsConfig: YamlConfiguration
    private val activeGenerators: ConcurrentHashMap<UUID, GeneratorData> = ConcurrentHashMap()

    private val currencyManager = plugin.currencyManager
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"

    companion object {
        //val GENERATION_INTERVAL_MS = TimeUnit.HOURS.toMillis(8)
        val GENERATION_INTERVAL_MS = TimeUnit.SECONDS.toMillis(30) // Para pruebas
        const val GENERATION_AMOUNT = 120.0
    }

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
        startGenerationTimer()
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
                val lastGen = generatorsConfig.getLong("$path.lastGenerationTime", 0L)
                val hologramName = generatorsConfig.getString("$path.hologramName")

                val world = Bukkit.getWorld(worldName ?: "world")
                if (world != null) {
                    val location = Location(world, x, y, z)
                    val data = GeneratorData(id, location, lastGen, hologramName)
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
                generatorsConfig.set("$path.lastGenerationTime", data.lastGenerationTime)
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
        val data = GeneratorData(id, location.block.location, 0L, hologramName)

        activeGenerators[id] = data
        saveGenerators()
        plugin.logger.info("Generador $id añadido en ${data.location}.")
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

    private fun startGenerationTimer() {
        val checkIntervalTicks = 20L * 60L * 5L

        object : BukkitRunnable() {
            override fun run() {
                runGenerationCycle()
            }
        }.runTaskTimerAsynchronously(plugin, 0L, checkIntervalTicks)
    }

    private fun startHologramUpdateTimer() {
        val updateIntervalTicks = 20L * 30L

        object : BukkitRunnable() {
            override fun run() {
                updateAllHolograms()
            }
        }.runTaskTimer(plugin, 0L, updateIntervalTicks)
    }

    private fun runGenerationCycle() {
        val currentTime = System.currentTimeMillis()
        val townyAPI = TownyAPI.getInstance()

        activeGenerators.values.forEach { data ->
            if (currentTime - data.lastGenerationTime >= GENERATION_INTERVAL_MS) {

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    val location = data.location
                    val townBlock = townyAPI.getTownBlock(location)

                    if (townBlock != null && townBlock.hasTown()) {
                        try {
                            val town = townBlock.town
                            if (town.hasMayor()) {
                                val mayor = town.mayor
                                currencyManager.addBalance(mayor.uuid, GENERATION_AMOUNT)
                                data.lastGenerationTime = currentTime
                                saveGenerators()

                                mayor.player?.sendMessage("$prefix${ChatColor.GREEN}Has recibido ${GENERATION_AMOUNT} CC del generador en (${location.blockX}, ${location.blockY}, ${location.blockZ}).")
                                TownyMessaging.sendPrefixedTownMessage(town, "$prefix${ChatColor.AQUA}El generador en (${location.blockX}, ${location.blockY}, ${location.blockZ}) ha producido ${GENERATION_AMOUNT} CC para el alcalde.")

                                createOrUpdateHologram(data)

                            }
                        } catch (e: Exception) {
                            plugin.logger.warning("Error procesando generación para ${data.id}: ${e.message}")
                        }
                    }
                })
            }
        }
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

        val nextGenerationTime = data.lastGenerationTime + GENERATION_INTERVAL_MS
        val remainingMillis = nextGenerationTime - System.currentTimeMillis()
        val timeString = if (remainingMillis <= 0) {
            "${ChatColor.GREEN}¡Listo!"
        } else {
            val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
            "${ChatColor.YELLOW}${hours}h ${minutes}m"
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
            val adjustedLocation = location.clone().add(0.5, 1.5, 0.5)
            hologram = DHAPI.createHologram(hologramName, adjustedLocation, false, lines)
            if (hologram != null) {
                data.hologramName = hologramName
                saveGenerators()
                hologram.enable()
            } else {
                plugin.logger.warning("No se pudo crear el holograma para el generador ${data.id}")
            }
        } else {
            DHAPI.setHologramLines(hologram, lines)
        }
    }
}