package com.github.Ringoame196

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin

class PlayerJoinEvent(private val plugin: Plugin) : Listener {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val athleticManager = AthleticManager(plugin)
        val player = e.player

        if (athleticManager.isRun(player)) {
            athleticManager.stop(player) // ストップ処理を実行する
        }
    }
}
