import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.time.LocalDate

/**
 * GitHub アカウントを使って プロジェクトをセットアップする
 */
open class SetupTask : DefaultTask() {
    @TaskAction
    fun action() {
        val projectDir = project.projectDir
        val repository = try {
            FileRepositoryBuilder.create(projectDir.resolve(".git"))
        } catch (ex: IOException) {
            error("リポジトリが見つかりませんでした")
        }
        val git = Git(repository)
        val remoteList = git.remoteList().call()
        val uri = remoteList.flatMap { it.urIs }.firstOrNull { it.host == "github.com" } ?: error("GitHub のプッシュ先が見つかりませんでした")
        val rawAccount = "/?([^/]*)/?".toRegex().find(uri.path)?.groupValues?.get(1) ?: error("アカウント名が見つかりませんでした (${uri.path})")
        val account = rawAccount.replace('-', '_')
        val groupId = "com.github.$account"
        val srcDirPath = "src/main/kotlin/com/github/$account"
        val srcDir = projectDir.resolve(srcDirPath).apply(File::mkdirs)
        srcDir.resolve("Main.kt").writeText(
            """
                package $groupId

                import org.bukkit.plugin.java.JavaPlugin

                class Main : JavaPlugin() {
                    override fun onEnable() {
                        super.onEnable()
                        val plugin = this
                        server.pluginManager.registerEvents(Events(), plugin)
                        // val command = getCommand("command")
                        // command!!.setExecutor(Command())
                        // command.tabCompleter = TabCompleter()
                    }
                }
                
            """.trimIndent()
        )
        val eventDir = projectDir.resolve("$srcDirPath/events").apply(File::mkdirs)
        eventDir.resolve("Events.kt").writeText(
            """
                package $groupId

                import org.bukkit.event.Listener
                import org.bukkit.plugin.Plugin

                class Events:Listener
                
            """.trimIndent()
        )
        val commandDir = projectDir.resolve("$srcDirPath/commands").apply(File::mkdirs)
        commandDir.resolve("Command.kt").writeText(
            """
                package $groupId

                import org.bukkit.command.Command
                import org.bukkit.command.CommandExecutor
                import org.bukkit.command.CommandSender
                import org.bukkit.event.Listener

                class Command:CommandExecutor {
                    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                        TODO("Not yet implemented")
                    }
                }
                
            """.trimIndent()
        )
        
        commandDir.resolve("TabCompleter.kt").writeText(
            """
                package $groupId
                
                import org.bukkit.command.Command
                import org.bukkit.command.CommandSender
                import org.bukkit.command.TabCompleter

                class TabCompleter : TabCompleter {
                    override fun onTabComplete(commandSender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
                        TODO("Not yet implemented")
                    }
                }
                
            """.trimIndent()
        )

        projectDir.resolve("src/main/resources/").apply(File::mkdirs) // resources生成

        val buildScript = projectDir.resolve("build.gradle.kts")
        buildScript.writeText(buildScript.readText().replace("@group@", groupId))

        val minecraftVersion  = project.findProperty("pluginVersion").toString()
        val projectName = project.name
        projectDir.resolve("README.md").writeText(
            """
                # $projectName
                
                ## プラグイン説明
                
                ## コマンド
                
                ## 使い方
                
                ## configファイル
                
                ## 開発環境
                - Minecraft Version : $minecraftVersion
                - Kotlin Version : 1.6.10
                
                ## プロジェクト情報
                - プロジェクトパス : ${uri.path}
                - 開発者名 : $account
                - 開発開始日 : ${LocalDate.now()}

            """.trimIndent()
        )
    }
}
