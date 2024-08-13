package com.github.Ringoame196.managers

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class YmlFileManager {
    private fun acquisitionYml(file: File): YamlConfiguration {
        return YamlConfiguration.loadConfiguration(file)
    }
    fun acquisitionIntValue(file: File, key: String): Int {
        val ymlFile = acquisitionYml(file)
        return ymlFile.getInt(key)
    }
    fun acquisitionDoubleValue(file: File, key: String): Double {
        val ymlFile = acquisitionYml(file)
        return ymlFile.getDouble(key)
    }
    fun acquisitionStringValue(file: File, key: String): String? {
        val ymlFile = acquisitionYml(file)
        return ymlFile.getString(key)
    }
    fun acquisitionListValue(file: File, key: String): MutableList<String> {
        val ymlFile = acquisitionYml(file)
        return ymlFile.getStringList(key).toMutableList() // 指定されたキーのリストを取得し、MutableListに変換して返す
    }

    fun addList(file: File, key: String, value: String) {
        val ymlFile = acquisitionYml(file)
        val list = acquisitionListValue(file, key) // 既存のリストを取得
        list.add(value) // リストに新しい値を追加
        ymlFile.set(key, list) // リストをYAMLファイルに保存
        ymlFile.save(file) // ファイルを保存
    }
    fun setValue(file: File, key: String, value: Any?) {
        val ymlFile = acquisitionYml(file)
        ymlFile.set(key, value)
        ymlFile.save(file)
    }
    fun delete(file: File) {
        file.delete()
    }
}
