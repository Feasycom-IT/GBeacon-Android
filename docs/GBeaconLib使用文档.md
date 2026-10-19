# GBeaconLib 接入文档（AAR 版）

> **交付物**：`GBeaconLib-release.aar` · **库版本**：`1.0.2.4` · **包名**：`com.feasycn` · **最低系统版本**：Android 10（API 29）
> **支持 ABI**：`arm64-v8a`、`armeabi-v7a`
> 本文凡标注"实测"的数据，均来自对交付的 release AAR 的实际解包检查。

---

## 1. 交付内容

交付一个 AAR 文件（实测 `164,936` 字节，SHA-256 `6F5CD6497D92ABB1F9629A7735C1206C738132F6D82027878AA0FE3A8679C0C6`，MD5 `3CC3C8403468BF66B55FFC66C8C58E4D`；每次重新构建哈希会变化，请以实际交付文件为准）。

交付包目录结构：

```
Gbeacon使用示例/
├── README.md                                # 交付说明（中英双语）
├── docs/
│   ├── GBeaconLib使用文档.md                 # 中文接入文档（本文）
│   └── GBeaconLib_Integration_Guide_EN.md   # 英文接入文档
├── sdk/
│   ├── GBeaconLib-release.aar               # SDK 本体（接入方只需要这一个文件）
│   └── SHA256.txt                           # 校验值
└── GBeaconDemo/                             # 演示工程：已接入 AAR、可独立编译，不含 SDK 源码
```

> 接入方只需把 `sdk/GBeaconLib-release.aar` 拷进自己的工程（见 3.1）；`GBeaconDemo` 是一个**不含 SDK 源码**的完整示例工程，可直接用 Android Studio 打开、编译、运行。

AAR 内部结构（实测）：

| 条目 | 大小 | 说明 |
|---|---|---|
| `classes.jar` | 114,922 B | **65 个 class**：`controler` 18、`util` 14、`bean` 14、`encrypted` 8、`service` 7、`ota` 3、`BuildConfig` 1 |
| `jni/arm64-v8a/libFeasyBeaconLibrary.so` | 28,032 B | 原生库（JNI 符号前缀为 `com.feasycn`） |
| `jni/armeabi-v7a/libFeasyBeaconLibrary.so` | 18,572 B | 同上 |
| `AndroidManifest.xml` | 1,220 B | 包名 `com.feasycn`、`minSdkVersion 29`、若干权限声明（**不含 Service**，见 3.2） |
| `proguard.txt` | **0 B（空）** | AAR 未携带混淆规则，**需要接入方自行添加 keep 规则**（见 3.3） |
| `META-INF/com/android/build/gradle/aar-metadata.properties` | 156 B | `minCompileSdk=1`、`minAndroidGradlePluginVersion=1.0.0`、`coreLibraryDesugaringEnabled=false` |
| `R.txt` | 272,409 B | 资源 ID 清单；**AAR 内不含 `res/` 资源文件**（实测 `res/` 条目数 = 0） |

> 说明：AAR 内**不含** Maven POM，因此不会自动带入任何第三方依赖（见 3.1）。

---

## 2. 环境要求

| 项 | 要求 | 依据 |
|---|---|---|
| `minSdk` | **≥ 29** | AAR 内 AndroidManifest 声明 `minSdkVersion 29` |
| `compileSdk` | 无特殊限制 | AAR 元数据 `minCompileSdk=1`（不会像部分 SDK 那样强制高版本 compileSdk） |
| Android Gradle Plugin | 无特殊限制 | `minAndroidGradlePluginVersion=1.0.0` |
| core library desugaring | **不需要** | `coreLibraryDesugaringEnabled=false` |
| ABI | 仅 `arm64-v8a` / `armeabi-v7a` | AAR 内 `jni/` 只有这两个目录（**x86/x86_64 模拟器无法运行**） |
| Kotlin | 需要 `kotlin-stdlib` | 库内有 Kotlin 编译的类（`OTAService`、`com.feasycn.ota.error.*`）；纯 Java 工程需自行加入 `org.jetbrains.kotlin:kotlin-stdlib` |
| 运行环境 | 设备需支持 BLE 且蓝牙已开启 | `uses-permission android.hardware.bluetooth_le`（AAR 已声明） |

---

## 3. 集成步骤

### 3.1 引入 AAR

```gradle
// app/build.gradle
android {
    defaultConfig {
        minSdk 29                  // 不能低于 29
        ndk { abiFilters 'arm64-v8a', 'armeabi-v7a' }   // 只支持这两个 ABI
    }
}

dependencies {
    implementation files('libs/GBeaconLib-release.aar')   // 把 aar 放到 app/libs 下
}
```

如果通过 Maven 私服分发，请同时提供带依赖坐标的 POM，例如：

```gradle
implementation 'com.feasycn:gbeaconlib:1.0.2.4@aar'
```

> ⚠️ **AAR 不携带依赖信息**：接入方需自行确认工程里存在 `kotlin-stdlib`（Kotlin 工程默认已有）与 `androidx.annotation`（通常随 appcompat 带入）。库本身在运行期只用到 Android 框架 API 和 `androidx.annotation` 注解（`@Keep`/`@RequiresApi`/`@NonNull`），**不要求** appcompat/material 必须存在。

### 3.2 AndroidManifest 配置（**必读**）

**AAR 会把下面这些权限自动合并进 App**（实测清单）：

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.hardware.bluetooth_le" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_PRIVILEGED" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

**接入方必须自己补充两处：**

**① 声明库内的两个 Service（缺了会导致参数读写（AT 通道）与 OTA 失败，这是最常见的接入问题）**

```xml
<service
    android:name="com.feasycn.service.AtCommandService"
    android:enabled="true"
    android:exported="false" />
<service
    android:name="com.feasycn.service.OTAService"
    android:enabled="true"
    android:exported="false" />
```

库内部会自行 `bindService`，接入方**不需要**手动启动它们。

**② 补充扫描/广播权限（Android 12 / API 31 及以上必需）**

```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
```

**运行时权限**：Android 12+ 必须动态申请 `BLUETOOTH_SCAN` 与 `BLUETOOTH_CONNECT`；Android 11 及以下扫描需要定位权限。库不负责申请权限，**未授权时扫描不会有任何回调**（容易误判为 SDK 故障）。

**如不需要某个自动合并的权限**（例如上架审核或隐私合规要求），可在 App 的 `AndroidManifest.xml` 中移除：

```xml
<manifest xmlns:tools="http://schemas.android.com/tools">
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" tools:node="remove" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" tools:node="remove" />
    <uses-permission android:name="android.permission.BLUETOOTH_PRIVILEGED" tools:node="remove" />
</manifest>
```

### 3.3 混淆规则（**必须自己加**）

AAR 内的 `proguard.txt` 是空的，所以 App 开启 `minifyEnabled true` 时必须自行添加：

```proguard
-keep public class com.feasycn.bean.**{*;}
-keep public class com.feasycn.controler.**{*;}
-keep public class com.feasycn.encrypted.**{*;}
-keep public class com.feasycn.ota.**{*;}
-keep public class com.feasycn.service.**{*;}
-keep public class com.feasycn.util.**{*;}
```

（其中 `controler` 是库的固定拼写，不是笔误。）另外 `com.feasycn.ota.error.*`、`com.feasycn.BuildConfig` 建议一并保留。

### 3.4 打包注意

- 只支持 `arm64-v8a` / `armeabi-v7a`；若 App 需要 x86 包（模拟器调试），请联系厂商提供对应 `.so`。
- 若需排查 native 崩溃，可在打包时保留符号：`android.packagingOptions.jniLibs.keepDebugSymbols += ['**/*.so']`。
- **请勿替换 AAR 内的 `.so`**：原生方法依赖 `Java_com_feasycn_*` 符号，符号前缀不匹配时会抛 `UnsatisfiedLinkError`。

---

## 4. 初始化与最小示例

```kotlin
import com.feasycn.bean.BluetoothDeviceWrapper
import com.feasycn.controler.FscBeaconApiImp
import com.feasycn.controler.FscBeaconCallbacks
import android.bluetooth.*
import java.util.ArrayList

class BeaconClient(context: Context) : FscBeaconCallbacks {

    /** ① 初始化：取单例 → initialize() → setCallbacks()（三步缺一不可） */
    private val api = FscBeaconApiImp.getInstance(context).apply {
        initialize()
        setCallbacks(this@BeaconClient)
    }

    fun isBluetoothEnabled() = api.isBluetoothEnabled
    fun startScan() = api.startScan()                    // 也可 startScan(10_000) 指定扫描时长(ms)
    fun stopScan() = api.stopScan()
    fun connect(device: BluetoothDeviceWrapper) = api.connect(device, "000000")
    fun disconnect() = api.disconnect()

    /** ② 扫描到设备：用 wrapper 里的字段判断设备类型 */
    override fun blePeripheralFound(device: BluetoothDeviceWrapper?, rssi: Int, record: ByteArray?) {
        val d = device ?: return
        when {
            d.iBeacon != null         -> { /* iBeacon 广播 */ }
            d.eddystoneBeacon != null -> { /* Eddystone 广播 */ }
            d.altBeacon != null       -> { /* AltBeacon 广播 */ }
            d.thSensor != null        -> { /* 温湿度传感器 */ }
            d.gSensor != null         -> { /* 重力传感器 */ }
        }
        if (d.feasyBeacon?.module != null) { /* 这是可配置的模块，可连接进去改参数 */ }
    }

    /** ③ 连接进度：0=密码校验中，1=密码正确，2=密码错误 */
    override fun connectProgressUpdate(device: BluetoothDevice?, status: Int) { }

    override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) { /* 可读写了 */ }
    override fun blePeripheralDisconnected(gatt: BluetoothGatt?, device: BluetoothDevice?) { /* 断开 */ }

    /** ④ 设备信息回调（getDeviceInfo 的结果） */
    override fun onDeviceInfoReceived(parameterName: String?, parameter: Any?) { }

    /** ⑤ AT 指令结果：status 0=失败 1=成功 2=完成 3=无需修改 4=超时 */
    override fun atCommandCallBack(command: String?, param: String?, status: String?) { }

    /** ⑥ OTA 进度：status 见 FscBleCentralApi.OTA_STATUS_* */
    override fun otaProgressUpdate(percentage: Int, status: Int) { }

    // 其余回调不需要就留空
    override fun startScan() {}
    override fun stopScan() {}
    override fun servicesFound(gatt: BluetoothGatt?, device: BluetoothDevice?, services: ArrayList<BluetoothGattService>?) {}
    override fun characteristicForService(gatt: BluetoothGatt?, device: BluetoothDevice?, service: BluetoothGattService?, characteristic: BluetoothGattCharacteristic?) {}
    override fun packetReceived(gatt: BluetoothGatt?, device: BluetoothDevice?, service: BluetoothGattService?, ch: BluetoothGattCharacteristic?, strValue: String, hexString: String, rawValue: ByteArray, timestamp: String?) {}
    override fun readResponse(gatt: BluetoothGatt?, device: BluetoothDevice?, service: BluetoothGattService?, ch: BluetoothGattCharacteristic?, strValue: String?, hexString: String?, rawValue: ByteArray?, timestamp: String?) {}
    override fun sendPacketProgress(gatt: BluetoothGatt?, device: BluetoothDevice?, ch: BluetoothGattCharacteristic?, percentage: Int, sendByte: ByteArray?) {}
}
```

> 实践建议：把 SDK 包成一个全局单例（例如 `object BluetoothRepository`），再由它把 SDK 回调分发给各页面，避免每个页面各自持有 `FscBeaconApiImp`。

---

## 5. API 参考

### 5.1 `FscBeaconApi`（主接口）/ `FscBeaconApiImp`（单例实现）

```java
static FscBeaconApiImp getInstance(Context con)
static FscBeaconApiImp getInstance()
```

| 方法（逐字签名） | 说明 |
|---|---|
| `int MAX_BEACONS = 10` | 设备内最多可配置的广播信标数 |
| `boolean isBluetoothEnabled()` | 蓝牙是否已开启（进前台先判断） |
| `boolean initialize()` | 初始化（必须先调用） |
| `void setCallbacks(FscBeaconCallbacks callback)` | 注册回调 |
| `boolean startScan()` / `boolean startScan(int duration)` | 开始扫描（可指定时长 ms） |
| `void stopScan()` | 停止扫描 |
| `boolean connect(BluetoothDeviceWrapper device, String pin)` | 连接并校验 PIN；PIN 非法 / 非本厂商信标 / context 为空时返回 `false` |
| `void disconnect()` | 断开（不允许 GATT 自动重连） |
| `boolean isConnected()` | 是否已连接 |
| `void getDeviceInfo(FeasyBeacon feasyBeacon, boolean queryTxPower)` | 查询设备信息，结果走 `onDeviceInfoReceived` 回调 |
| `void setDeviceName(String deviceName)` | 设备名（`null` → 默认 `Feasycom`） |
| `void setDevicePin(String devicePin)` | PIN（0-999999，非法 → 默认 `0000`） |
| `void setBroadcastInterval(String intervalTime)` | 广播间隔 ms（100-1000，非法 → 150） |
| `void setDeviceTxPower(String txPower)` | 模块发射功率（非法 → 7） |
| `void setBroadcastTxPower(String index, int p)` / `(String index, String p)` | 指定索引信标的 TX 功率 |
| `void setBroadcastPHY(String index, int p)` | 指定索引信标的 PHY |
| `void setGSensorConfig(String broadcastInterval, String broadcastDuration)` / `(..., String threshold)` | 重力传感器触发广播（间隔 / 持续 / 阈值） |
| `void setKeyConfig(String broadcastInterval, String broadcastDuration)` | 按键触发广播 |
| `void setLed(String ledTime)` | LED 指示时长（0-999999） |
| `boolean isBeaconInfoFull()` | 设备内信标列表是否已满 |
| `boolean addBeaconInfo(BeaconBean beaconBean)` | 添加广播信标（成功后索引自动重排） |
| `boolean deleteBeaconInfo(String index)` | 按索引删除（1..MAX_BEACONS） |
| `boolean updateBeaconInfo(BeaconBean beaconBean, String index)` | 按索引更新 |
| `void saveBeaconInfo()` | **保存到模块端**（名称/TX 功率/广播间隔/信标列表都靠它落盘） |
| `void beaconBroadcastParameters()` | 查询 / 同步广播参数 |
| `void saveBatch(CommandBean commandBean)` | 批量下发参数 |
| `void setFirmwareKey(String command)` | 查询 / 设置固件密钥 |
| `void setAppKey(String key)` | 设置应用密钥 |
| `void restore()` | 恢复出厂设置 |
| `void ota()` | 进入 OTA 模式 |
| `void setTlm(boolean isTlm)` | 是否开启 TLM 广播 |
| `void setBroadcastExtensions(boolean isBroadcastExtensions)` | 是否开启广播扩展 |
| `boolean startOtaUpdate(byte[] dfuFile, boolean reset)` | 开始空中升级（`reset`=升级后是否恢复出厂） |
| `DfuFileInfo checkDfuFile(byte[] dfuFile)` | 解析 DFU 文件（型号 / 版本 / 引导） |

### 5.2 `FscBleCentralApi`（底层 BLE / AT 通道）/ `FscBleCentralApiImp`

```java
static FscBleCentralApiImp getInstance()
static FscBleCentralApiImp getInstance(Context context)
static FscBleCentralApiImp getInstance(Activity activity)
```

| 方法 | 说明 |
|---|---|
| `boolean initialize()` / `boolean isBtEnabled()` | 初始化 / 蓝牙是否开启 |
| `boolean startScan()` / `boolean startScan(int duration)` / `void stopScan()` | 扫描控制 |
| `boolean connect(String deviceAddress, String pin)` | **按地址**连接（`FscBeaconApi.connect` 内部使用） |
| `void disconnect()` / `void close()` | 断开 / 释放 |
| `void setCallbacks(FscBleCentralCallbacks callback)` | 数据 + OTA 回调 |
| `void setSDKDataCallbacks(FscBleCentralCallbacks callback)` | 仅替换数据回调 |
| `void setOTACallbacks(FscBleCentralCallbacks callback)` | 仅替换 OTA 回调 |
| `FscBleCentralCallbacks getCallbacks()` | 取当前回调 |
| `boolean isConnected()` / `BluetoothDevice getDevice()` | 连接状态 / 当前设备 |
| `void sendATCommand(Set<String> command)` | 直接发送 AT 指令（如 `AT+NAME=Feasycom`） |
| `boolean send(byte[] data)` / `void sendCommand(byte[] data)` / `void stopSend()` | 发送原始数据 / 入队发送 / 停止发送 |
| `BluetoothGattCharacteristic getWriteCharacteristic()` | 当前写特征（高级用法） |
| `boolean startOTA(byte[] dfuFile, boolean reset)` | 开始 OTA |
| `DfuFileInfo checkDfuFile(byte[] dfuFile)` | 解析 DFU 文件 |
| `static boolean EN_AUTO_QUERY` | 默认 `false`；置 `true` 时改参数前自动先查询一次，避免重复写入 |
| `static boolean EN_AUTO_VERIFY` | 默认 `false`；置 `true` 时改完参数自动回读校验是否写入成功 |

**接口常量**

| 常量 | 值 | 含义 |
|---|---|---|
| `FscBleCentralApi.OTA_STATUS_BEGIN` | 110 | OTA 开始 |
| `FscBleCentralApi.OTA_STATUS_PROCESSING` | 121 | OTA 进行中 |
| `FscBleCentralApi.OTA_STATUS_FINISH` | 10086 | OTA 结束 |
| `FscBleCentralApi.PACKAGE_SEND_FINISH` | 100 | 软件包发送完成 |
| `FscBleCentralApi.FIFO_SEND_FINISH` | 101 | FIFO 发送完成 |

### 5.3 回调接口（三层继承，注册时用 `FscBeaconCallbacks`）

```java
FscCallbacks
   └── FscBleCentralCallbacks
          └── FscBeaconCallbacks
```

**`FscCallbacks`**

| 方法 | 触发时机 |
|---|---|
| `void startScan()` / `void stopScan()` | 调用 `startScan()` / `stopScan()` 后 |
| `void connectProgressUpdate(BluetoothDevice device, int status)` | 连接进度：`0` 密码校验中、`1` 密码正确、`2` 密码错误（另有 `3` 超时，见 `CommandBean.PASSWORD_*`） |

**`FscBleCentralCallbacks`（含上表）**

| 方法 | 触发时机 |
|---|---|
| `void blePeripheralFound(BluetoothDeviceWrapper device, int rssi, byte[] record)` | 扫描到设备（`record` 为原始广播数据） |
| `void blePeripheralConnected(BluetoothGatt gatt, BluetoothDevice device)` | 连接成功 |
| `void blePeripheralDisconnected(BluetoothGatt gatt, BluetoothDevice device)` | 连接断开 |
| `void servicesFound(BluetoothGatt gatt, BluetoothDevice device, ArrayList<BluetoothGattService> services)` | 发现服务 |
| `void characteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic)` | 发现特征 |
| `void packetReceived(gatt, device, service, ch, String strValue, String hexString, byte[] rawValue, String timestamp)` | 收到数据包 |
| `void readResponse(gatt, device, service, ch, String strValue, String hexString, byte[] rawValue, String timestamp)` | 读到数据 |
| `void sendPacketProgress(gatt, device, ch, int percentage, byte[] sendByte)` | 发送进度 |
| `void atCommandCallBack(String command, String param, String status)` | AT 指令结果（查询命令会带回 `param`） |
| `void otaProgressUpdate(int percentage, int status)` | OTA 进度与状态 |

**`FscBeaconCallbacks`（含上表）**

| 方法 | 触发时机 |
|---|---|
| `void onDeviceInfoReceived(String parameterName, Object parameter)` | 读到设备参数（`getDeviceInfo` 的结果）。**厂商提示：不要在这里做耗时操作，否则会影响后续查询** |

---

## 6. 典型流程

### 6.1 扫描

```kotlin
if (!api.isBluetoothEnabled) { /* 提示用户开启蓝牙 */ }
api.startScan()                 // 或 api.startScan(10_000)

override fun blePeripheralFound(device: BluetoothDeviceWrapper?, rssi: Int, record: ByteArray?) {
    val d = device ?: return
    if (d.feasyBeacon?.module != null) addToList(d)   // 只收可配置模块
}
api.stopScan()
```

`BluetoothDeviceWrapper` 中 `iBeacon` / `eddystoneBeacon` / `altBeacon` 三个字段互斥（设置其中一个会清空其余），传感器数据（`thSensor` / `gSensor`）是附加信息。

### 6.2 连接与 PIN

```kotlin
api.connect(deviceWrapper, "000000")   // PIN 0-999999，默认 "0000"
```

连接过程通过 `connectProgressUpdate(device, status)` 返回 `0/1/2`；读写参数前用 `isConnected()` 确认已连接。

### 6.3 读取设备信息

```kotlin
api.getDeviceInfo(device.feasyBeacon, true)   // true = 同时查询 TX 功率

override fun onDeviceInfoReceived(parameterName: String?, parameter: Any?) { /* 参数名 → 值 */ }
```

随后可从 `FeasyBeacon` 读取：`deviceName`、`deviceAddress`、`module`（型号）、`version`（固件版本）、`battery`（电量）、`connectable`、`gSensor`、`keyConfig`、`buzzer`、`led`、`nfc`、`longRange`、`encryptionWay`。

### 6.4 修改参数

```kotlin
api.setDeviceName("GB21G-001")
api.setDevicePin("123456")
api.setBroadcastInterval("200")     // 100-1000 ms
api.setDeviceTxPower("7")
api.setGSensorConfig("100", "5")
api.setKeyConfig("100", "5")
api.setLed("10")
api.saveBeaconInfo()                // ★ 必须调用才会真正写入模块
```

### 6.5 多信标广播管理（最多 10 个）

```kotlin
val bean = BeaconBean("1", "iBeacon").apply {
    uuid = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825"
    major = "10001"; minor = "10002"
    txpower = "2"; phy = "0"; interval = 1000
    enable = true; connectable = true
}

if (api.isBeaconInfoFull) { /* 先删一个 */ }
api.addBeaconInfo(bean)               // 添加（索引自动重排）
api.updateBeaconInfo(bean, "1")       // 按索引更新
api.deleteBeaconInfo("1")             // 按索引删除
api.beaconBroadcastParameters()       // 同步 / 查询广播参数
api.saveBeaconInfo()                  // 落盘
```

> `BeaconBean.beaconType` 的取值由设备固件协议决定；厂商示例中常见 `iBeacon`、`Eddystone_uid`、`Eddystone_url`、`altBeacon`、`eddy-uid`、`eddy-url`、`eddy-tlm`、`aoaBeacon`。接入前请与厂商确认目标型号支持的取值。

### 6.6 批量配置

```kotlin
val cmd = CommandBean()
cmd.setCommand("AT+NAME=GB21G-002")
cmd.setCommand("AT+ADVIN=200")
api.saveBatch(cmd)         // cmd.getCommand() 可取出待下发指令集合 Set<String>
```

### 6.7 OTA / DFU

```kotlin
// 1) 选择固件文件并解析
val info: DfuFileInfo = api.checkDfuFile(dfuBytes)
info.versonStart; info.verson_soft_end; info.bootloader; info.type_model

// 2) 建议先断开普通连接再升级
api.disconnect()
api.startOtaUpdate(dfuBytes, /* reset = */ false)

override fun otaProgressUpdate(percentage: Int, status: Int) {
    when (status) {
        FscBleCentralApi.OTA_STATUS_BEGIN      -> { /* 110 开始 */ }
        FscBleCentralApi.OTA_STATUS_PROCESSING -> { /* 121 进行中，percentage 为进度 */ }
        FscBleCentralApi.OTA_STATUS_FINISH     -> { /* 10086 完成 */ }
    }
}
```

升级由库内 `com.feasycn.service.OTAService` 完成（XMODEM 分包），**前提是 App 已声明该 Service（见 3.2）**。传输异常类型位于 `com.feasycn.ota.error`（`CException`、`SendingCanceledException`、`SendingEndedException`）。

### 6.8 固件密钥与加密校验

```kotlin
api.setFirmwareKey("AT+KEY=...")   // 查询 / 设置固件密钥
api.setAppKey("xxxx")              // 设置应用密钥
```

```java
// com.feasycn.encrypted.*
static FscEncryptApi getInstance()
byte[]  getEncrypted()                                  // 通用加密参数
byte[]  getBeaconEncrypted(String address, String pin)  // 指定设备的加密参数
boolean verifyEncrypted(byte[] encrypted)               // 校验加密参数

static EncryptInfo create(String address, String pin, String encryptAlgorithm)
static native EncryptInfo createRandom(String encryptAlgorithm)
boolean isUsingUniversalAlgorithm(); boolean isUsingBeaconAlgorithm()
String getEncryptAlgorithm(); String getPassword(); String getRandomNumber()
// EncryptAlgorithm.Beacon / .Universal: parseRandomNumber(String) / randomNumberMatches(EncryptInfo, String)
```

### 6.9 恢复出厂

```kotlin
api.restore()
```

---

## 7. 数据模型（`com.feasycn.bean`）

| 类 | 用途 | 关键字段 / 方法 |
|---|---|---|
| `BluetoothDeviceWrapper` | 扫描结果包装 | `address` `name` `rssi` `timestampNanos`；`iBeacon` `eddystoneBeacon` `altBeacon` `feasyBeacon` `thSensor` `gSensor` `completeLocalName`；常量 `EDDYSTONE_TYPE=0x16`、`ALTBEACON_TYPE=0xFF`、`COMPLETE_LOCAL_NAME=0x09` |
| `FeasyBeacon` | 模块信息 | `getDeviceName` `getDeviceAddress` `getModule` `getVersion` `getBattery` `getConnectable` `getEncryptionWay` `getKeyConfig` `getGSensor` `getBuzzer` `getLed` `getNfc` `isLongRange` |
| `IBeacon` | iBeacon 广播 | `getCompany` `getType` `getDataLength` `getUuid` `getMajor` `getMinor` `getRssi` |
| `AltBeacon` | AltBeacon 广播 | `getAltBeaconRssi` `getBeaconId` `getManufacturerId` `getReservedId` `getFeature` |
| `EddystoneBeacon` | Eddystone 广播 | `getFrameTypeString` `getFrameTypeHex` `getEddystoneRssiOrVersion` `getUrl` `getNameSpace` `getInstance` `getReserved` `getBatteryVoltage` `getTemperature` `getAdvertisementsCount` `getTimeSincePowerUp` |
| `THSensor` | 温湿度传感器广播 | `getTemperature` `getHumidity` `getBattery` `getName` |
| `GSensor` | 重力传感器广播 | `getXAxis` `getYAxis` `getZAxis` |
| `BeaconBean` | **要写入设备**的广播信标 | `index` `beaconType` `uuid` `major` `minor` `url` `nameSpace` `instance` `reserved` `id1/id2/id3` `manufacturerId` `manufacturerReserved` `rssi` `connectable` `enable` `version` `interval`(默认 1000) `txpower`(默认 `"2"`) `phy`(默认 `"0"`) `parentName` |
| `CommandBean` | 批量 AT 指令 | `setCommand(String)` / `getCommand(): Set<String>`；常量见 9.1 |
| `DfuFileInfo` | DFU 文件解析结果 | 公有字段 `bootloader` `versonStart` `verson_soft_end` `type_model` |
| `EncryptInfo` / `EncryptAlgorithm` | 加密参数 | 见 6.8 |

异常类：`com.feasycn.ota.error.CException`、`SendingCanceledException`、`SendingEndedException`。

---

## 8. 工具类（`com.feasycn.util`，均为静态方法）

| 类 | 主要能力 |
|---|---|
| `IBeaconUtil` / `AltBeaconUtil` / `EddystoneBeaconUtil` | `fromScanData(...)`：把广播数据解析成对应 Bean（自建扫描流程时可用；SDK 扫描已自动解析） |
| `FeasyBeaconUtil` | `moduleAdapter(String)` 模块号→名称、`getSPPAddrByBLEAddr(String)` 由 BLE 地址推算 SPP 地址、`getVersionByFileName` / `getModelByFileName` 从固件文件名解析版本 / 型号 |
| `FileUtil` | 字节与十六进制互转（`bytesToHexString`、`hexStringToByte`…）、文件读写（`readFileToByte`、`getFileAbsolutePath`）、`getDfuFileInformation(byte[])`、`getModelName(int)`、`needsReconnect(byte[])`、`serviceFilter(String)`、发送 FIFO（`dataQueue`/`poll`/`offer`）、`versionConv` |
| `FeasycomUtil` | `checkLength(String,int,boolean)`（native）、`setBroadcastInfo(String, ArrayList<BeaconBean>)` 生成广播数据、`getBroadcastHex(int, ArrayList<BeaconBean>)` 生成指定索引的广播十六进制、`hexStringToByteArray(String)` |
| `TeaCode` | TEA 加解密 native 封装（内部 `System.loadLibrary("FeasyBeaconLibrary")`）：`encrypt_bitstream` / `decrypt_bitstream` / `feasycom_decryption` / `getDfuFileInformation` |
| `CRC16` | 校验计算（OTA 使用） |
| `StringUtil` | 字符串工具（`getType(String)` 等） |
| `SharedPreferencesUtil` | `getStr/getInt/getLong/getBoolean`、`putStr/putInt/putLong/putBoolean`、`removeData` |
| `NetworkUtil` | `feedback(String feedbackContent, String feedbackType, String email, Context, FeedbackCallback)` 意见反馈上报；`FeedbackCallback{ onSuccess(String) / onFailure(String) / onNetworkError(String) }` |
| `ToastUtil` | `show(Context, String)` |

---

## 9. 常量速查

### 9.1 `CommandBean`（AT 指令与结果码）

| 常量 | 值 / 含义 |
|---|---|
| `COMMAND_HEADER` | `"AT+"` |
| `COMMAND_NAME` / `COMMAND_LENAME` / `COMMAND_ADVIN` / `COMMAND_TX_POWER` / `COMMAND_PIN` | `NAME` / `LENAME` / `ADVIN` / `TXPOWER` / `PIN` |
| `COMMAND_GSCFG` / `COMMAND_KEYCFG` / `COMMAND_LED` / `COMMAND_BUZ` | 重力传感器 / 按键 / LED / 蜂鸣器 |
| `COMMAND_BADVDATA` / `COMMAND_BADVPARM` / `COMMAND_TLM` / `COMMAND_ADVEXT` | 广播数据 / 广播参数 / TLM / 广播扩展 |
| `COMMAND_OTA` / `COMMAND_END` / `COMMAND_RAP` / `COMMAND_VERSION` / `COMMAND_BWMODE` | OTA / 结束 / 复位 / 版本 / 广播模式 |
| `DEFALUT_DEVICE_NAME` / `DEFALUT_PIN` | `"Feasycom"` / `"0000"` |
| 指令结果 | `COMMAND_FAILED="0"`、`COMMAND_SUCCESSFUL="1"`、`COMMAND_FINISH="2"`、`COMMAND_NO_NEED="3"`、`COMMAND_TIME_OUT="4"` |
| 密码校验 | `PASSWORD_CHECK=0`、`PASSWORD_SUCCESSFUL=1`、`PASSWORD_FAILED=2`、`PASSWORD_TIME_OUT=3` |

### 9.2 OTA / 发送状态：见 5.2 常量表

---

## 10. 注意事项与常见问题

1. **扫描没有任何回调** → 99% 是权限问题：Android 12+ 需要运行时授予 `BLUETOOTH_SCAN`、`BLUETOOTH_CONNECT`；Android 11 及以下需要定位权限。另外确认蓝牙已开启（`isBluetoothEnabled`）。
2. **参数读写 / OTA 无响应或失败** → 检查 App 的 `AndroidManifest.xml` 是否声明了 `com.feasycn.service.AtCommandService` 与 `com.feasycn.service.OTAService`。
3. **`UnsatisfiedLinkError`** → ① 只打包了不支持的 ABI（库仅 arm64-v8a / armeabi-v7a）；② 用其它 `.so` 替换了 AAR 内的原生库；③ 打包工具裁剪掉了 `libFeasyBeaconLibrary.so`（检查 `packagingOptions` / `jniLibs` 配置）。
4. **Release 包报 `NoSuchMethodError` / 回调不触发** → App 开了混淆但没加 3.3 的 keep 规则。
5. **`connect()` 返回 false 或 `connectProgressUpdate` 返回 2** → PIN 不在 0-999999、PIN 错误，或目标不是本厂商可连接信标（广播里没有 `feasyBeacon.module`）。
6. **`saveBeaconInfo()` 之后参数没生效** → 所有 `setXxx()` 只是下发，必须调用 `saveBeaconInfo()`（批量用 `saveBatch`）才会写入模块；另外确认连接未断开。
7. **信标数量异常 / 删错** → 设备内最多 `MAX_BEACONS`（10）个；增删改后索引会自动重排，删除与更新必须使用**重排后的索引**。
8. **`onDeviceInfoReceived` 触发后查询变慢** → 该回调里不要做耗时操作（官方注释明确提醒）。
9. **权限合规** → AAR 会向 App 合并 `SYSTEM_ALERT_WINDOW`、`READ_EXTERNAL_STORAGE`、`BLUETOOTH_PRIVILEGED`、`ACCESS_LOCATION_EXTRA_COMMANDS` 等权限；如上架审核或隐私合规不允许，用 3.2 的 `tools:node="remove"` 移除。
10. **模拟器调试** → x86/x86_64 镜像无法加载本库（无对应 `.so`），请用真机或 arm 镜像。
11. **回调线程** → 库回调线程未在文档中约定；如需直接更新 UI，建议在回调里切回主线程后再操作 View。

---

## 11. 版本记录

| 库版本 | 说明 |
|---|---|
| `1.0.2.4` | 当前交付版本（包名 `com.feasycn`，AAR 内含 `arm64-v8a` / `armeabi-v7a` 原生库） |

> 如需其它 ABI、其它包名或源码级定制，请联系厂商。
