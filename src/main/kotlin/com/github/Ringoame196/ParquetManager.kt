package com.github.Ringoame196

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.WrappedBlockData
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException

class ParquetManager {
    private val protocolManager = ProtocolLibrary.getProtocolManager()
    fun setBlock(id: Material, location: Location, player: Player) {
        // パケットコンテナを作成
        val packetContainer = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE)

        // 座標を取り出す
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ

        // パケットのフィールドへ書き込み
        packetContainer.blockPositionModifier.write(0, BlockPosition(x, y, z))
        packetContainer.blockData.write(0, WrappedBlockData.createData(id))

        // 送信
        try {
            protocolManager.sendServerPacket(player, packetContainer)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}
