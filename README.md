# GBeacon Android App（应用层源码）

GBeacon 蓝牙信标配置 App 的**应用层源码**。蓝牙协议与设备操作能力由 **GBeaconLib** 提供，本仓库**不包含 GBeaconLib 的源码**，只以 AAR 形式引用：

```
gbeacon/libs/GBeaconLib-release.aar
```

> Application-layer source of the GBeacon beacon configuration app.
> The BLE/protocol layer is provided by **GBeaconLib** as a prebuilt AAR — its source is **not** included in this repository.

---

## 下载安装包 / Download APK

| 版本 | 文件 | 大小 | SHA-256 |
|---|---|---|---|
| v1.0.2.5 | [apk/GBeacon-v1.0.2.5.apk](apk/GBeacon-v1.0.2.5.apk) | 20.33 MB | `3003293AD8FBF16208E4A76C0806DE3F927E55EB37D03BE902433153694CD413` |

- 安装包由**官方签名证书**（CN=GlowBird）签名，非调试包，可直接安装/覆盖升级；
- 适用系统：**Android 10 及以上**（minSdk 29）；
- 说明：本仓库源码版本为 **1.0.2.6**，该 APK 为已发布的 **1.0.2.5** 正式包；
  如需 1.0.2.6 的包，请用 Android Studio 自行编译（见下方“编译 / Build”）。
## 1. 引入 SDK / Add the SDK

1. 把 `GBeaconLib-release.aar` 放到 `gbeacon/libs/`（仓库已内置一份）
2. 依赖已配置好，`gbeacon/build.gradle` 中：

```groovy
implementation files('libs/GBeaconLib-release.aar')
implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')
```

3. AAR 的 SHA-256（请核对，防止被替换）：

```
6F5CD6497D92ABB1F9629A7735C1206C738132F6D82027878AA0FE3A8679C0C6
```

## 2. 编译 / Build

环境要求：**Android Studio（JDK 17）**，Android SDK **API 36**，minSdk 29。

```bash
./gradlew :gbeacon:assembleDebug
```

> ⚠️ 本仓库**已移除签名配置**（`signingConfigs` 中的 keystore 与密码属于内部资源）。
> 需要签名包时，请在自己的机器上配置：`signingConfigs { release { ... } }`，
> 且**不要**把 keystore 文件和密码提交到公开仓库。
> Signing config has been removed on purpose. Configure your own keystore locally and never commit it.

## 3. 应用信息 / App info

| 项 | 值 |
|---|---|
| applicationId | `com.glowbird.gbeacon` |
| versionName | 1.0.2.6 |
| versionCode | 1026 |
| minSdk / targetSdk | 29 / 36 |

## 4. GBeaconLib 使用文档 / SDK documents

| 语言 | 文档 |
|---|---|
| 中文 | [docs/GBeaconLib使用文档.md](docs/GBeaconLib使用文档.md) |
| English | [docs/GBeaconLib_Integration_Guide_EN.md](docs/GBeaconLib_Integration_Guide_EN.md) |

## 5. 目录结构 / Layout

```
.
├── build.gradle                 # 根构建脚本
├── settings.gradle              # 只包含 :gbeacon
├── gradle/wrapper/              # Gradle Wrapper
├── gbeacon/                     # 应用层（Application module）
│   ├── build.gradle             # 依赖 GBeaconLib-release.aar
│   ├── libs/GBeaconLib-release.aar
│   └── src/main/...
└── docs/                        # GBeaconLib 使用文档（中/英）
```

## 6. 说明 / Notes

- 本仓库只发布**对外资源**（应用层源码 + SDK 使用文档 + AAR），不含 GBeaconLib 源码、不含签名密钥。
- This repository contains only public resources: app-layer source, SDK docs and the prebuilt AAR.