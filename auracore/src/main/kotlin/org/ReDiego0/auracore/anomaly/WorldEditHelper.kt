package org.ReDiego0.auracore.anomaly // O org.ReDiego0.auracore.helpers

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.world.block.BlockType
import com.sk89q.worldedit.world.block.BlockTypes
import org.ReDiego0.auracore.Auracore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import java.io.File
import java.io.FileInputStream

object WorldEditHelper {

    fun pasteSchematic(plugin: Auracore, schematicName: String, location: Location, callback: (Boolean, Location?) -> Unit) {
        val schematicFile = File(plugin.dataFolder.parentFile, "FastAsyncWorldEdit/schematics/$schematicName.schem")

        if (!schematicFile.exists()) {
            plugin.logger.warning("Schematic no encontrado: ${schematicFile.path}")
            callback(false, null)
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                val weWorld = BukkitAdapter.adapt(location.world)
                val format: ClipboardFormat? = ClipboardFormats.findByFile(schematicFile)
                val reader: ClipboardReader? = format?.getReader(FileInputStream(schematicFile))
                val clipboard: Clipboard? = reader?.read()

                if (clipboard == null) {
                    plugin.logger.warning("No se pudo cargar el schematic: ${schematicFile.path}")
                    Bukkit.getScheduler().runTask(plugin, Runnable { callback(false, null) })
                    return@Runnable
                }

                val targetVector = BukkitAdapter.asBlockVector(location)
                var markerLocation: Location? = null

                WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
                    val operation = ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(targetVector)
                        .ignoreAirBlocks(false)
                        .build()

                    Operations.complete(operation)

                    markerLocation = findAndRemoveMarkerBlock(editSession, clipboard, targetVector, Material.SPONGE)
                }

                reader.close()

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    callback(true, markerLocation)
                })

            } catch (e: Exception) {
                plugin.logger.severe("Error al pegar schematic '$schematicName': ${e.message}")
                e.printStackTrace()
                Bukkit.getScheduler().runTask(plugin, Runnable { callback(false, null) })
            }
        })
    }

    private fun findAndRemoveMarkerBlock(
        editSession: EditSession,
        clipboard: Clipboard,
        pasteLocation: BlockVector3,
        markerMaterial: Material
    ): Location? {
        val weMarkerType: BlockType? = BukkitAdapter.asBlockType(markerMaterial)
        if (weMarkerType == null) return null

        var foundLocation: Location? = null
        val origin = clipboard.origin
        val region = clipboard.region

        for (vec in region) {
            val block = clipboard.getBlock(vec)
            if (block.blockType == weMarkerType) {
                val worldVector = vec.subtract(origin).add(pasteLocation)

                val bukkitWorld = BukkitAdapter.adapt(editSession.world)
                val bukkitLocation = Location(bukkitWorld, worldVector.x().toDouble(), worldVector.y().toDouble(), worldVector.z().toDouble())
                editSession.setBlock(worldVector, BlockTypes.AIR?.defaultState)
                foundLocation = bukkitLocation
                break
            }
        }
        return foundLocation
    }
}