# SpigotPluginTemplate

## [KotlinSpigotPluginTemplate (クローン元)](https://github.com/sya-ri/KotlinSpigotPluginTemplate)

## 使い方

- `Use this template` を押して、GitHub 上にプロジェクトを作成する。
- 作成したプロジェクトをクローンする。
-  Gradle の `setup` タスクを実行して、初期化する。

## 追加機能
- `setup`タスクを実行時に、README.mdを変更
- ビルド時にプラグインをコピー
- [AutomaticCreatingPluginUpdate](https://github.com/Ringoame196/AutomaticCreatingPluginUpdate)に対応

## plugin.yml

ビルド時に自動生成されます。[build.gradle.kts](build.gradle.kts) の以下の箇所で設定できます。
書き方は https://github.com/Minecrell/plugin-yml の Bukkit kotlin-dsl を見てください。

```kotlin
configure<BukkitPluginDescription> {
    // 内容
}
```

## タスク

### プラグインのビルド `build`

`build/libs` フォルダに `.jar` を生成します。

### テストサーバーの起動 `buildAndLaunchServer`

`:25565` でテストサーバーを起動します。
