package com.github.Ringoame196

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.Plugin

class PlayerDeathEvent(private val plugin: Plugin) : Listener {
    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val athleticManager = AthleticManager(plugin)
        val player = e.entity

        if (athleticManager.isRun(player)) {
            athleticManager.stop(player) // ストップ処理を実行する
        }
    }
}
