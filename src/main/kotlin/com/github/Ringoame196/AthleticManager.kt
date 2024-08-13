package com.github.Ringoame196

import com.github.Ringoame196.managers.YmlFileManager
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.random.Random

class AthleticManager(private val plugin: Plugin) {
    private val parquetManager = ParquetManager()
    private val ymlFileManager = YmlFileManager()

    private val setupBlockKey = "setupBlock"
    private val setupBlockListKey = "setupBlockList"
    private val startWorldName = "start_world_name"
    private val startLocationKey = "start_location"
    private val countKey = "count"

    private fun acquisitionPlayerData(player: Player): File {
        val file = File(plugin.dataFolder, "/playerData/${player.name}.yml")
        return file
    }

    fun isRun(player: Player): Boolean { // アスレ中かを返す
        val playerDataFile = acquisitionPlayerData(player)
        return playerDataFile.exists()
    }

    fun isReached(player: Player): Boolean {
        val playerDataFile = acquisitionPlayerData(player)
        val setBlockX = ymlFileManager.acquisitionIntValue(playerDataFile, "$setupBlockKey.x")
        val setBlockY = ymlFileManager.acquisitionIntValue(playerDataFile, "$setupBlockKey.y")
        val setBlockZ = ymlFileManager.acquisitionIntValue(playerDataFile, "$setupBlockKey.z")

        val blockLocation = player.location.add(0.0, -1.0, 0.0).clone().block.location
        return blockLocation.blockX == setBlockX && blockLocation.blockY == setBlockY && blockLocation.blockZ == setBlockZ
    }

    fun isPermitWorld(worldName: String): Boolean {
        val permitWorldName = plugin.config.get("permit_world")
        return permitWorldName == worldName
    }

    fun start(player: Player) {
        val world = player.world
        val worldName = world.name
        if (isRun(player)) {
            player.sendMessage("${ChatColor.RED}既にスタートしています")
        } else if (!isPermitWorld(worldName)) {
            player.sendMessage("${ChatColor.RED}このワールドでは実行することはできません")
        } else {
            player.sendMessage("${ChatColor.AQUA}アスレチックスタート")
            saveStartLocation(player) // スタート座標を保存する
            setBlock(player) // 1つめのブロック設置
        }
    }

    private fun saveStartLocation(player: Player) {
        val playerDataFile = acquisitionPlayerData(player)
        val location = player.location
        val worldName = location.world?.name
        ymlFileManager.setValue(playerDataFile, startWorldName, worldName)
        saveLocation(playerDataFile, location, startLocationKey)
    }

    fun stop(player: Player) {
        if (!isRun(player)) {
            player.sendMessage("${ChatColor.RED}スタートしていません")
        } else {
            val playerDataFile = acquisitionPlayerData(player)
            val count = ymlFileManager.acquisitionIntValue(playerDataFile, countKey)
            player.sendMessage("${ChatColor.GOLD}アスレチック終了\nあなたは ${count}回まで進みました")
            returnStartLocation(player) // スタート位置に戻る
            cleanBlocks(player) // ブロックをきれいにする
            ymlFileManager.delete(playerDataFile) // データファイルを削除する
        }
    }

    private fun returnStartLocation(player: Player) {
        // 座標情報を取得する
        val playerDataFile = acquisitionPlayerData(player)
        val worldName = ymlFileManager.acquisitionStringValue(playerDataFile, startWorldName)
        val x = ymlFileManager.acquisitionDoubleValue(playerDataFile, "$startLocationKey.x")
        val y = ymlFileManager.acquisitionDoubleValue(playerDataFile, "$startLocationKey.y")
        val z = ymlFileManager.acquisitionDoubleValue(playerDataFile, "$startLocationKey.z")
        val world = Bukkit.getWorld(worldName ?: "world")
        val startLocation = Location(world, x, y, z)

        player.teleport(startLocation) // スタート位置にテレポート
    }

    fun setBlock(player: Player) {
        // 座標を生成する
        val playerLocation = player.location.clone()
        val blockLocation = playerLocation.add(0.0, -1.0, 0.0).block.location.clone()
        val randomX = Random.nextInt(-1, 2) // 次のブロックの設置場所を乱数で指定
        val randomY = Random.nextInt(0, 3) // 次のブロックの設置場所を乱数で指定
        val randomZ = Random.nextInt(2, 5) // 次のブロックの設置場所を乱数で指定
        val setupBlockLocation = blockLocation.clone().add(randomX.toDouble(), randomY.toDouble(), randomZ.toDouble())

        saveBlockLocation(player, setupBlockLocation) // ブロックの座標を保存する

        val count = additionCount(player)

        if (randomY <= 1) {
            parquetManager.setBlock(Material.STONE, setupBlockLocation, player)
        } else {
            val ladderLocation = setupBlockLocation.clone().add(0.0, 0.0, -1.0)
            parquetManager.setBlock(Material.STONE, setupBlockLocation, player)
            parquetManager.setBlock(Material.LADDER, ladderLocation, player)
        }

        val particleLocation = setupBlockLocation.clone().add(0.5, 0.0, 0.5)
        staging(player, particleLocation, count)
    }

    private fun saveBlockLocation(player: Player, location: Location) {
        val playerDataFile = acquisitionPlayerData(player)

        // 新しい足場ブロックを保存する
        saveLocation(playerDataFile, location, setupBlockKey)

        // ブロック設置場所のログを保存する
        val listBlockLocation = "x:${location.blockX}y:${location.blockY}z:${location.blockZ}"
        ymlFileManager.addList(playerDataFile, setupBlockListKey, listBlockLocation)
    }

    private fun staging(player: Player, location: Location, count: Int) {
        player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1f) // 音を流す
        player.world.spawnParticle(Particle.HEART, location, 1) // パーティクルを表示
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("${ChatColor.GOLD}$count"))
    }

    private fun cleanBlocks(player: Player) {
        val playerDataFile = acquisitionPlayerData(player)
        val setupBlockList = ymlFileManager.acquisitionListValue(playerDataFile, setupBlockListKey)
        for (setupBlockLocation in setupBlockList) {
            val world = player.world
            val (x, y, z) = parseCoordinates(setupBlockLocation) ?: continue
            val blockLocation = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
            val ladderLocation = blockLocation.clone().add(0.0, 0.0, -1.0)
            returnBlock(blockLocation, player)
            returnBlock(ladderLocation, player)
        }
    }

    private fun returnBlock(location: Location, player: Player) {
        val blockMaterial = location.block.type
        parquetManager.setBlock(blockMaterial, location, player)
    }

    private fun parseCoordinates(input: String?): Triple<Int, Int, Int>? {
        val regex = Regex("""x:(\d+)y:(\d+)z:(\d+)""")
        val matchResult = regex.find(input ?: return null)

        return matchResult?.let {
            val (x, y, z) = it.destructured
            Triple(x.toInt(), y.toInt(), z.toInt())
        }
    }

    private fun saveLocation(playerDataFile: File, location: Location, key: String) {
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ
        ymlFileManager.setValue(playerDataFile, "$key.x", x)
        ymlFileManager.setValue(playerDataFile, "$key.y", y)
        ymlFileManager.setValue(playerDataFile, "$key.z", z)
    }

    private fun additionCount(player: Player): Int {
        val playerDataFile = acquisitionPlayerData(player)
        val count = ymlFileManager.acquisitionIntValue(playerDataFile, countKey) + 1
        ymlFileManager.setValue(playerDataFile, countKey, count)
        return count
    }
}
