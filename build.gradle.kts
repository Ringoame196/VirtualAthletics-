import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask.JarUrl
import groovy.lang.Closure
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import java.net.HttpURLConnection
import java.net.URL

plugins {
    kotlin("jvm") version "1.6.10"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.ben-manes.versions") version "0.41.0"
    id("com.palantir.git-version") version "0.12.3"
    id("dev.s7a.gradle.minecraft.server") version "1.2.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.jmailen.kotlinter") version "3.8.0"
}

val gitVersion: Closure<String> by extra

val pluginVersion: String by project.ext

repositories {
    mavenCentral()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://oss.sonatype.org/content/groups/public/")
    maven(url = "https://repo.dmulloy2.net/repository/public/")
}

val shadowImplementation: Configuration by configurations.creating
configurations["implementation"].extendsFrom(shadowImplementation)

dependencies {
    shadowImplementation(kotlin("stdlib"))
    compileOnly("org.spigotmc:spigot-api:$pluginVersion-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
}

configure<BukkitPluginDescription> {
    main = "com.github.Ringoame196.Main"
    version = gitVersion()
    apiVersion = "1." + pluginVersion.split(".")[1]
    commands {
        register("virtualathletic") {
            description = "アスレチックをスタート・ストップするコマンド"
            usage = "/virtualathletic <start,stop>"
        }
    }
    depend = listOf("ProtocolLib")
}

tasks.withType<ShadowJar> {
    configurations = listOf(shadowImplementation)
    archiveClassifier.set("")
    relocate("kotlin", "com.github.Ringoame196.libs.kotlin")
    relocate("org.intellij.lang.annotations", "com.github.Ringoame196.libs.org.intellij.lang.annotations")
    relocate("org.jetbrains.annotations", "com.github.Ringoame196.libs.org.jetbrains.annotations")
}

tasks.named("build") {
    dependsOn("shadowJar")
    // プラグインを特定のパスへ自動コピー
    val copyFilePath = "D:/デスクトップ/Twitterサーバー/plugins" // コピー先のフォルダーパス
    val copyFile = File(copyFilePath)
    if (copyFile.exists() && copyFile.isDirectory) {
        doFirst {
            copy {
                from(buildDir.resolve("libs/${project.name}.jar"))
                into(copyFile)
            }
        }
        doLast { // AutomaticCreatingPluginUpdate連携
            // APIリクエストを行う
            val port = 25585
            val apiUrl = "http://localhost:$port/plugin?name=${project.name}"
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connect()

                // レスポンスコードを確認
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    println("API Response: $response")
                } else {
                    println("Failed to get response: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error during API request: ${e.message}")
            } finally {
                connection.disconnect()
            }
        }
    }
}

task<LaunchMinecraftServerTask>("buildAndLaunchServer") {
    dependsOn("build")
    doFirst {
        copy {
            from(buildDir.resolve("libs/${project.name}.jar"))
            into(buildDir.resolve("MinecraftServer/plugins"))
        }
    }

    jarUrl.set(JarUrl.Paper(pluginVersion))
    jarName.set("server.jar")
    serverDirectory.set(buildDir.resolve("MinecraftServer"))
    nogui.set(true)
    agreeEula.set(true)
}

task<SetupTask>("setup")
