# GBeaconLib Integration Guide (AAR)

> **Deliverable**: `GBeaconLib-release.aar` · **Library version**: `1.0.2.4` · **Namespace**: `com.feasycn` · **Minimum OS**: Android 10 (API 29)
> **Supported ABIs**: `arm64-v8a`, `armeabi-v7a`
> Figures marked "measured" below were obtained by unpacking the delivered release AAR.

---

## 1. Deliverables

One AAR file is delivered (measured: `164,936` bytes; SHA-256 `6F5CD6497D92ABB1F9629A7735C1206C738132F6D82027878AA0FE3A8679C0C6`; MD5 `3CC3C8403468BF66B55FFC66C8C58E4D`. The hash changes whenever the library is rebuilt — always verify against the file you actually received).

Package layout:

```
Gbeacon使用示例/
├── README.md                                # Delivery notes (Chinese + English)
├── docs/
│   ├── GBeaconLib使用文档.md                 # Chinese integration guide
│   └── GBeaconLib_Integration_Guide_EN.md   # English integration guide (this document)
├── sdk/
│   ├── GBeaconLib-release.aar               # The SDK itself (all you need to integrate)
│   └── SHA256.txt                           # Checksum
└── GBeaconDemo/                             # Demo project: AAR-based, builds standalone, no SDK sources
```

> Simply copy `sdk/GBeaconLib-release.aar` into your own project (see 3.1). `GBeaconDemo` is a complete sample project **without SDK sources** that opens, builds and runs in Android Studio.

AAR internal structure (measured):

| Entry | Size | Notes |
|---|---|---|
| `classes.jar` | 114,922 B | **65 classes**: `controler` 18, `util` 14, `bean` 14, `encrypted` 8, `service` 7, `ota` 3, `BuildConfig` 1 |
| `jni/arm64-v8a/libFeasyBeaconLibrary.so` | 28,032 B | Native library (JNI symbol prefix `com.feasycn`) |
| `jni/armeabi-v7a/libFeasyBeaconLibrary.so` | 18,572 B | Same, 32-bit ARM |
| `AndroidManifest.xml` | 1,220 B | Package `com.feasycn`, `minSdkVersion 29`, 10 permissions (**no `<service>`** — see 3.2) |
| `proguard.txt` | **0 B (empty)** | The AAR ships no keep rules; **the app must add them** (see 3.3) |
| `META-INF/com/android/build/gradle/aar-metadata.properties` | 156 B | `minCompileSdk=1`, `minAndroidGradlePluginVersion=1.0.0`, `coreLibraryDesugaringEnabled=false` |
| `R.txt` | 272,409 B | Resource ID list; the AAR contains **no `res/` resources** (measured entry count = 0) |

> Note: the AAR carries **no Maven POM**, therefore it pulls in no third-party dependencies automatically (see 3.1).

---

## 2. Requirements

| Item | Requirement | Basis |
|---|---|---|
| `minSdk` | **≥ 29** | AAR manifest declares `minSdkVersion 29` |
| `compileSdk` | No restriction | AAR metadata `minCompileSdk=1` (does not force a high compileSdk) |
| Android Gradle Plugin | No restriction | `minAndroidGradlePluginVersion=1.0.0` |
| Core library desugaring | **Not required** | `coreLibraryDesugaringEnabled=false` |
| ABI | `arm64-v8a` / `armeabi-v7a` only | `jni/` only contains those two directories (**x86/x86_64 emulators cannot run the SDK**) |
| Kotlin | `kotlin-stdlib` required | The library contains Kotlin-compiled classes (`OTAService`, `com.feasycn.ota.error.*`); pure-Java apps must add `org.jetbrains.kotlin:kotlin-stdlib` |
| Runtime | Device must support BLE with Bluetooth enabled | `uses-permission android.hardware.bluetooth_le` (already declared by the AAR) |

---

## 3. Integration

### 3.1 Add the AAR

```gradle
// app/build.gradle
android {
    defaultConfig {
        minSdk 29                  // must not be lower than 29
        ndk { abiFilters 'arm64-v8a', 'armeabi-v7a' }   // only these two ABIs are supported
    }
}

dependencies {
    implementation files('libs/GBeaconLib-release.aar')   // put the aar in app/libs
}
```

If you distribute it through a private Maven repository, publish it together with a POM carrying the dependency coordinates, e.g.:

```gradle
implementation 'com.feasycn:gbeaconlib:1.0.2.4@aar'
```

> ⚠️ **The AAR carries no dependency metadata**: make sure your project provides `kotlin-stdlib` (any Kotlin project already does) and `androidx.annotation` (normally pulled in by appcompat). At runtime the library only uses Android framework APIs plus `androidx.annotation` annotations (`@Keep` / `@RequiresApi` / `@NonNull`); appcompat/material are **not** mandatory for the library itself.

### 3.2 AndroidManifest configuration (**must read**)

**The AAR automatically merges the following permissions into your app** (measured list):

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

**You must add two things yourself:**

**① Declare the library's two services** (missing them breaks AT-channel parameter read/write and OTA — this is the most common integration problem)

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

The library binds them internally (`bindService`); you do **not** start them manually.

**② Add the scan/broadcast permissions required on Android 12 / API 31+**

```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
```

**Runtime permissions**: on Android 12+ you must request `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT` at runtime; on Android 11 and below scanning requires location permission. The library does not request permissions for you — **without the grant, scanning produces no callback at all** (easily mistaken for an SDK failure).

**To remove an auto-merged permission** (e.g. for store review or privacy compliance), strip it in your app manifest:

```xml
<manifest xmlns:tools="http://schemas.android.com/tools">
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" tools:node="remove" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" tools:node="remove" />
    <uses-permission android:name="android.permission.BLUETOOTH_PRIVILEGED" tools:node="remove" />
</manifest>
```

### 3.3 ProGuard / R8 rules (**you must add them**)

The AAR's `proguard.txt` is empty, so if your app enables `minifyEnabled true` you must add:

```proguard
-keep public class com.feasycn.bean.**{*;}
-keep public class com.feasycn.controler.**{*;}
-keep public class com.feasycn.encrypted.**{*;}
-keep public class com.feasycn.ota.**{*;}
-keep public class com.feasycn.service.**{*;}
-keep public class com.feasycn.util.**{*;}
```

(`controler` is the library's actual spelling, not a typo.) Keeping `com.feasycn.BuildConfig` as well is recommended.

### 3.4 Packaging notes

- Only `arm64-v8a` / `armeabi-v7a` are supported. If you need x86 builds (emulator debugging), contact the vendor for a matching `.so`.
- To debug native crashes, keep symbols when packaging: `android.packagingOptions.jniLibs.keepDebugSymbols += ['**/*.so']`.
- **Never replace the `.so` inside the AAR**: the native methods rely on `Java_com_feasycn_*` symbols; a mismatched prefix throws `UnsatisfiedLinkError`.

---

## 4. Initialization and minimal example

```kotlin
import com.feasycn.bean.BluetoothDeviceWrapper
import com.feasycn.controler.FscBeaconApiImp
import com.feasycn.controler.FscBeaconCallbacks
import android.bluetooth.*
import java.util.ArrayList

class BeaconClient(context: Context) : FscBeaconCallbacks {

    /** ① Initialize: get the singleton -> initialize() -> setCallbacks() (all three are required) */
    private val api = FscBeaconApiImp.getInstance(context).apply {
        initialize()
        setCallbacks(this@BeaconClient)
    }

    fun isBluetoothEnabled() = api.isBluetoothEnabled
    fun startScan() = api.startScan()                    // or startScan(10_000) for a 10 s scan
    fun stopScan() = api.stopScan()
    fun connect(device: BluetoothDeviceWrapper) = api.connect(device, "000000")
    fun disconnect() = api.disconnect()

    /** ② Device found: decide the device type from the wrapper fields */
    override fun blePeripheralFound(device: BluetoothDeviceWrapper?, rssi: Int, record: ByteArray?) {
        val d = device ?: return
        when {
            d.iBeacon != null         -> { /* iBeacon advertisement */ }
            d.eddystoneBeacon != null -> { /* Eddystone advertisement */ }
            d.altBeacon != null       -> { /* AltBeacon advertisement */ }
            d.thSensor != null        -> { /* temperature/humidity sensor */ }
            d.gSensor != null         -> { /* G-sensor */ }
        }
        if (d.feasyBeacon?.module != null) { /* configurable module: connect to change parameters */ }
    }

    /** ③ Connection progress: 0 = checking PIN, 1 = correct PIN, 2 = wrong PIN */
    override fun connectProgressUpdate(device: BluetoothDevice?, status: Int) { }

    override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) { /* ready to read/write */ }
    override fun blePeripheralDisconnected(gatt: BluetoothGatt?, device: BluetoothDevice?) { /* disconnected */ }

    /** ④ Device information callback (result of getDeviceInfo) */
    override fun onDeviceInfoReceived(parameterName: String?, parameter: Any?) { }

    /** ⑤ AT command result: status 0=failed 1=success 2=finished 3=no change needed 4=timeout */
    override fun atCommandCallBack(command: String?, param: String?, status: String?) { }

    /** ⑥ OTA progress: see FscBleCentralApi.OTA_STATUS_* */
    override fun otaProgressUpdate(percentage: Int, status: Int) { }

    // Leave the remaining callbacks empty if unused
    override fun startScan() {}
    override fun stopScan() {}
    override fun servicesFound(gatt: BluetoothGatt?, device: BluetoothDevice?, services: ArrayList<BluetoothGattService>?) {}
    override fun characteristicForService(gatt: BluetoothGatt?, device: BluetoothDevice?, service: BluetoothGattService?, characteristic: BluetoothGattCharacteristic?) {}
    override fun packetReceived(gatt: BluetoothGatt?, device: BluetoothDevice?, service: BluetoothGattService?, ch: BluetoothGattCharacteristic?, strValue: String, hexString: String, rawValue: ByteArray, timestamp: String?) {}
    override fun readResponse(gatt: BluetoothGatt?, device: BluetoothDevice?, service: BluetoothGattService?, ch: BluetoothGattCharacteristic?, strValue: String?, hexString: String?, rawValue: ByteArray?, timestamp: String?) {}
    override fun sendPacketProgress(gatt: BluetoothGatt?, device: BluetoothDevice?, ch: BluetoothGattCharacteristic?, percentage: Int, sendByte: ByteArray?) {}
}
```

> Recommended practice: wrap the SDK in a single app-wide singleton (e.g. `object BluetoothRepository`) and let it dispatch SDK callbacks to your screens, instead of holding `FscBeaconApiImp` in every screen.

---

## 5. API reference

### 5.1 `FscBeaconApi` (main interface) / `FscBeaconApiImp` (singleton implementation)

```java
static FscBeaconApiImp getInstance(Context con)
static FscBeaconApiImp getInstance()
```

| Method (exact signature) | Description |
|---|---|
| `int MAX_BEACONS = 10` | Maximum number of broadcast beacons configurable in one device |
| `boolean isBluetoothEnabled()` | Whether Bluetooth is on (check when entering the foreground) |
| `boolean initialize()` | Initialize (must be called first) |
| `void setCallbacks(FscBeaconCallbacks callback)` | Register callbacks |
| `boolean startScan()` / `boolean startScan(int duration)` | Start scanning (optional duration in ms) |
| `void stopScan()` | Stop scanning |
| `boolean connect(BluetoothDeviceWrapper device, String pin)` | Connect and verify the PIN; returns `false` for an invalid PIN, a non-vendor beacon, or a null context |
| `void disconnect()` | Disconnect (GATT auto-reconnect disabled) |
| `boolean isConnected()` | Connection state |
| `void getDeviceInfo(FeasyBeacon feasyBeacon, boolean queryTxPower)` | Query device information; the result arrives via `onDeviceInfoReceived` |
| `void setDeviceName(String deviceName)` | Device name (`null` → default `Feasycom`) |
| `void setDevicePin(String devicePin)` | PIN (0-999999; invalid → default `0000`) |
| `void setBroadcastInterval(String intervalTime)` | Advertising interval in ms (100-1000; invalid → 150) |
| `void setDeviceTxPower(String txPower)` | Module TX power (invalid → 7) |
| `void setBroadcastTxPower(String index, int p)` / `(String index, String p)` | TX power of the beacon at the given index |
| `void setBroadcastPHY(String index, int p)` | PHY of the beacon at the given index |
| `void setGSensorConfig(String broadcastInterval, String broadcastDuration)` / `(..., String threshold)` | G-sensor triggered advertising (interval / duration / threshold) |
| `void setKeyConfig(String broadcastInterval, String broadcastDuration)` | Button-triggered advertising |
| `void setLed(String ledTime)` | LED indication duration (0-999999) |
| `boolean isBeaconInfoFull()` | Whether the on-device beacon list is full |
| `boolean addBeaconInfo(BeaconBean beaconBean)` | Add a broadcast beacon (indices are re-ordered afterwards) |
| `boolean deleteBeaconInfo(String index)` | Delete by index (1..MAX_BEACONS) |
| `boolean updateBeaconInfo(BeaconBean beaconBean, String index)` | Update by index |
| `void saveBeaconInfo()` | **Persist to the module** (name / TX power / interval / beacon list all need it) |
| `void beaconBroadcastParameters()` | Query / synchronize advertising parameters |
| `void saveBatch(CommandBean commandBean)` | Send a batch of parameters |
| `void setFirmwareKey(String command)` | Query / set the firmware key |
| `void setAppKey(String key)` | Set the application key |
| `void restore()` | Factory reset |
| `void ota()` | Enter OTA mode |
| `void setTlm(boolean isTlm)` | Enable/disable TLM advertising |
| `void setBroadcastExtensions(boolean isBroadcastExtensions)` | Enable/disable advertising extensions |
| `boolean startOtaUpdate(byte[] dfuFile, boolean reset)` | Start over-the-air update (`reset` = factory reset after the update) |
| `DfuFileInfo checkDfuFile(byte[] dfuFile)` | Parse a DFU file (model / version / bootloader) |

### 5.2 `FscBleCentralApi` (low-level BLE / AT channel) / `FscBleCentralApiImp`

```java
static FscBleCentralApiImp getInstance()
static FscBleCentralApiImp getInstance(Context context)
static FscBleCentralApiImp getInstance(Activity activity)
```

| Method | Description |
|---|---|
| `boolean initialize()` / `boolean isBtEnabled()` | Initialize / whether Bluetooth is on |
| `boolean startScan()` / `boolean startScan(int duration)` / `void stopScan()` | Scan control |
| `boolean connect(String deviceAddress, String pin)` | Connect **by address** (used internally by `FscBeaconApi.connect`) |
| `void disconnect()` / `void close()` | Disconnect / release |
| `void setCallbacks(FscBleCentralCallbacks callback)` | Data + OTA callbacks |
| `void setSDKDataCallbacks(FscBleCentralCallbacks callback)` | Replace only the data callbacks |
| `void setOTACallbacks(FscBleCentralCallbacks callback)` | Replace only the OTA callbacks |
| `FscBleCentralCallbacks getCallbacks()` | Current callbacks |
| `boolean isConnected()` / `BluetoothDevice getDevice()` | Connection state / current device |
| `void sendATCommand(Set<String> command)` | Send raw AT commands (e.g. `AT+NAME=Feasycom`) |
| `boolean send(byte[] data)` / `void sendCommand(byte[] data)` / `void stopSend()` | Send raw data / enqueue / stop sending |
| `BluetoothGattCharacteristic getWriteCharacteristic()` | Current write characteristic (advanced use) |
| `boolean startOTA(byte[] dfuFile, boolean reset)` | Start OTA |
| `DfuFileInfo checkDfuFile(byte[] dfuFile)` | Parse a DFU file |
| `static boolean EN_AUTO_QUERY` | Default `false`; when `true`, parameters are queried before being written, avoiding duplicate writes |
| `static boolean EN_AUTO_VERIFY` | Default `false`; when `true`, parameters are read back after writing to verify success |

**Interface constants**

| Constant | Value | Meaning |
|---|---|---|
| `FscBleCentralApi.OTA_STATUS_BEGIN` | 110 | OTA started |
| `FscBleCentralApi.OTA_STATUS_PROCESSING` | 121 | OTA in progress |
| `FscBleCentralApi.OTA_STATUS_FINISH` | 10086 | OTA finished |
| `FscBleCentralApi.PACKAGE_SEND_FINISH` | 100 | Package transfer finished |
| `FscBleCentralApi.FIFO_SEND_FINISH` | 101 | FIFO transfer finished |

### 5.3 Callback interfaces (three-level inheritance; register with `FscBeaconCallbacks`)

```java
FscCallbacks
   └── FscBleCentralCallbacks
          └── FscBeaconCallbacks
```

**`FscCallbacks`**

| Method | Fired when |
|---|---|
| `void startScan()` / `void stopScan()` | After `startScan()` / `stopScan()` |
| `void connectProgressUpdate(BluetoothDevice device, int status)` | Connection progress: `0` checking PIN, `1` PIN correct, `2` PIN wrong (`3` = timeout, see `CommandBean.PASSWORD_*`) |

**`FscBleCentralCallbacks` (includes the above)**

| Method | Fired when |
|---|---|
| `void blePeripheralFound(BluetoothDeviceWrapper device, int rssi, byte[] record)` | A device is found (`record` = raw advertising data) |
| `void blePeripheralConnected(BluetoothGatt gatt, BluetoothDevice device)` | Connected |
| `void blePeripheralDisconnected(BluetoothGatt gatt, BluetoothDevice device)` | Disconnected |
| `void servicesFound(BluetoothGatt gatt, BluetoothDevice device, ArrayList<BluetoothGattService> services)` | Services discovered |
| `void characteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic)` | Characteristic discovered |
| `void packetReceived(gatt, device, service, ch, String strValue, String hexString, byte[] rawValue, String timestamp)` | A packet was received |
| `void readResponse(gatt, device, service, ch, String strValue, String hexString, byte[] rawValue, String timestamp)` | A read completed |
| `void sendPacketProgress(gatt, device, ch, int percentage, byte[] sendByte)` | Send progress |
| `void atCommandCallBack(String command, String param, String status)` | AT command result (query commands return `param`) |
| `void otaProgressUpdate(int percentage, int status)` | OTA progress and status |

**`FscBeaconCallbacks` (includes the above)**

| Method | Fired when |
|---|---|
| `void onDeviceInfoReceived(String parameterName, Object parameter)` | A device parameter was read (result of `getDeviceInfo`). **Vendor note: do not do heavy work here — it slows down subsequent queries.** |

---

## 6. Typical flows

### 6.1 Scanning

```kotlin
if (!api.isBluetoothEnabled) { /* ask the user to enable Bluetooth */ }
api.startScan()                 // or api.startScan(10_000)

override fun blePeripheralFound(device: BluetoothDeviceWrapper?, rssi: Int, record: ByteArray?) {
    val d = device ?: return
    if (d.feasyBeacon?.module != null) addToList(d)   // keep only configurable modules
}
api.stopScan()
```

In `BluetoothDeviceWrapper` the fields `iBeacon` / `eddystoneBeacon` / `altBeacon` are mutually exclusive (setting one clears the others); sensor data (`thSensor` / `gSensor`) is extra information.

### 6.2 Connecting and PIN

```kotlin
api.connect(deviceWrapper, "000000")   // PIN 0-999999, default "0000"
```

Progress comes back through `connectProgressUpdate(device, status)` as `0/1/2`; confirm `isConnected()` before reading or writing parameters.

### 6.3 Reading device information

```kotlin
api.getDeviceInfo(device.feasyBeacon, true)   // true = also query TX power

override fun onDeviceInfoReceived(parameterName: String?, parameter: Any?) { /* parameter name -> value */ }
```

`FeasyBeacon` then exposes: `deviceName`, `deviceAddress`, `module` (model), `version` (firmware), `battery`, `connectable`, `gSensor`, `keyConfig`, `buzzer`, `led`, `nfc`, `longRange`, `encryptionWay`.

### 6.4 Modifying parameters

```kotlin
api.setDeviceName("GB21G-001")
api.setDevicePin("123456")
api.setBroadcastInterval("200")     // 100-1000 ms
api.setDeviceTxPower("7")
api.setGSensorConfig("100", "5")
api.setKeyConfig("100", "5")
api.setLed("10")
api.saveBeaconInfo()                // ★ required, otherwise nothing is written to the module
```

### 6.5 Managing multiple advertised beacons (up to 10)

```kotlin
val bean = BeaconBean("1", "iBeacon").apply {
    uuid = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825"
    major = "10001"; minor = "10002"
    txpower = "2"; phy = "0"; interval = 1000
    enable = true; connectable = true
}

if (api.isBeaconInfoFull) { /* delete one first */ }
api.addBeaconInfo(bean)               // add (indices are re-ordered)
api.updateBeaconInfo(bean, "1")       // update by index
api.deleteBeaconInfo("1")             // delete by index
api.beaconBroadcastParameters()       // synchronize / query advertising parameters
api.saveBeaconInfo()                  // persist
```

> The valid values of `BeaconBean.beaconType` are defined by the device firmware protocol; vendor samples commonly use `iBeacon`, `Eddystone_uid`, `Eddystone_url`, `altBeacon`, `eddy-uid`, `eddy-url`, `eddy-tlm`, `aoaBeacon`. Confirm the supported values for your target model with the vendor before integrating.

### 6.6 Batch configuration

```kotlin
val cmd = CommandBean()
cmd.setCommand("AT+NAME=GB21G-002")
cmd.setCommand("AT+ADVIN=200")
api.saveBatch(cmd)         // cmd.getCommand() returns the pending Set<String> of commands
```

### 6.7 OTA / DFU

```kotlin
// 1) Pick a firmware file and parse it
val info: DfuFileInfo = api.checkDfuFile(dfuBytes)
info.versonStart; info.verson_soft_end; info.bootloader; info.type_model

// 2) Recommended: disconnect the normal connection first
api.disconnect()
api.startOtaUpdate(dfuBytes, /* reset = */ false)

override fun otaProgressUpdate(percentage: Int, status: Int) {
    when (status) {
        FscBleCentralApi.OTA_STATUS_BEGIN      -> { /* 110 started */ }
        FscBleCentralApi.OTA_STATUS_PROCESSING -> { /* 121 in progress, percentage is the progress */ }
        FscBleCentralApi.OTA_STATUS_FINISH     -> { /* 10086 finished */ }
    }
}
```

The update runs inside the library's `com.feasycn.service.OTAService` (XMODEM packetization) — **which requires the service declaration in your manifest (see 3.2)**. Transfer exceptions live in `com.feasycn.ota.error` (`CException`, `SendingCanceledException`, `SendingEndedException`).

### 6.8 Firmware key and encryption verification

```kotlin
api.setFirmwareKey("AT+KEY=...")   // query / set the firmware key
api.setAppKey("xxxx")              // set the application key
```

```java
// com.feasycn.encrypted.*
static FscEncryptApi getInstance()
byte[]  getEncrypted()                                  // generic encrypted parameters
byte[]  getBeaconEncrypted(String address, String pin)  // encrypted parameters for a given device
boolean verifyEncrypted(byte[] encrypted)               // verify encrypted parameters

static EncryptInfo create(String address, String pin, String encryptAlgorithm)
static native EncryptInfo createRandom(String encryptAlgorithm)
boolean isUsingUniversalAlgorithm(); boolean isUsingBeaconAlgorithm()
String getEncryptAlgorithm(); String getPassword(); String getRandomNumber()
// EncryptAlgorithm.Beacon / .Universal: parseRandomNumber(String) / randomNumberMatches(EncryptInfo, String)
```

### 6.9 Factory reset

```kotlin
api.restore()
```

---

## 7. Data models (`com.feasycn.bean`)

| Class | Purpose | Key fields / methods |
|---|---|---|
| `BluetoothDeviceWrapper` | Scan result wrapper | `address` `name` `rssi` `timestampNanos`; `iBeacon` `eddystoneBeacon` `altBeacon` `feasyBeacon` `thSensor` `gSensor` `completeLocalName`; constants `EDDYSTONE_TYPE=0x16`, `ALTBEACON_TYPE=0xFF`, `COMPLETE_LOCAL_NAME=0x09` |
| `FeasyBeacon` | Module information | `getDeviceName` `getDeviceAddress` `getModule` `getVersion` `getBattery` `getConnectable` `getEncryptionWay` `getKeyConfig` `getGSensor` `getBuzzer` `getLed` `getNfc` `isLongRange` |
| `IBeacon` | iBeacon advertisement | `getCompany` `getType` `getDataLength` `getUuid` `getMajor` `getMinor` `getRssi` |
| `AltBeacon` | AltBeacon advertisement | `getAltBeaconRssi` `getBeaconId` `getManufacturerId` `getReservedId` `getFeature` |
| `EddystoneBeacon` | Eddystone advertisement | `getFrameTypeString` `getFrameTypeHex` `getEddystoneRssiOrVersion` `getUrl` `getNameSpace` `getInstance` `getReserved` `getBatteryVoltage` `getTemperature` `getAdvertisementsCount` `getTimeSincePowerUp` |
| `THSensor` | Temperature/humidity advertisement | `getTemperature` `getHumidity` `getBattery` `getName` |
| `GSensor` | G-sensor advertisement | `getXAxis` `getYAxis` `getZAxis` |
| `BeaconBean` | Beacon to **write into the device** | `index` `beaconType` `uuid` `major` `minor` `url` `nameSpace` `instance` `reserved` `id1/id2/id3` `manufacturerId` `manufacturerReserved` `rssi` `connectable` `enable` `version` `interval` (default 1000) `txpower` (default `"2"`) `phy` (default `"0"`) `parentName` |
| `CommandBean` | Batch AT commands | `setCommand(String)` / `getCommand(): Set<String>`; constants in 9.1 |
| `DfuFileInfo` | DFU parsing result | Public fields `bootloader` `versonStart` `verson_soft_end` `type_model` |
| `EncryptInfo` / `EncryptAlgorithm` | Encrypted parameters | See 6.8 |

Exceptions: `com.feasycn.ota.error.CException`, `SendingCanceledException`, `SendingEndedException`.

---

## 8. Utilities (`com.feasycn.util`, all static)

| Class | Main capabilities |
|---|---|
| `IBeaconUtil` / `AltBeaconUtil` / `EddystoneBeaconUtil` | `fromScanData(...)`: parse advertising data into the matching bean (useful with your own scanning flow; the SDK scan already parses it) |
| `FeasyBeaconUtil` | `moduleAdapter(String)` model number → name, `getSPPAddrByBLEAddr(String)` derive the SPP address from the BLE address, `getVersionByFileName` / `getModelByFileName` parse version / model from the firmware file name |
| `FileUtil` | byte/hex conversion (`bytesToHexString`, `hexStringToByte`…), file I/O (`readFileToByte`, `getFileAbsolutePath`), `getDfuFileInformation(byte[])`, `getModelName(int)`, `needsReconnect(byte[])`, `serviceFilter(String)`, send FIFO (`dataQueue`/`poll`/`offer`), `versionConv` |
| `FeasycomUtil` | `checkLength(String,int,boolean)` (native), `setBroadcastInfo(String, ArrayList<BeaconBean>)` build advertising data, `getBroadcastHex(int, ArrayList<BeaconBean>)` build the advertising hex for a given index, `hexStringToByteArray(String)` |
| `TeaCode` | TEA encryption native wrapper (internally `System.loadLibrary("FeasyBeaconLibrary")`): `encrypt_bitstream` / `decrypt_bitstream` / `feasycom_decryption` / `getDfuFileInformation` |
| `CRC16` | Checksum computation (used by OTA) |
| `StringUtil` | String helpers (`getType(String)` etc.) |
| `SharedPreferencesUtil` | `getStr/getInt/getLong/getBoolean`, `putStr/putInt/putLong/putBoolean`, `removeData` |
| `NetworkUtil` | `feedback(String feedbackContent, String feedbackType, String email, Context, FeedbackCallback)` feedback reporting; `FeedbackCallback{ onSuccess(String) / onFailure(String) / onNetworkError(String) }` |
| `ToastUtil` | `show(Context, String)` |

---

## 9. Constants quick reference

### 9.1 `CommandBean` (AT commands and result codes)

| Constant | Value / meaning |
|---|---|
| `COMMAND_HEADER` | `"AT+"` |
| `COMMAND_NAME` / `COMMAND_LENAME` / `COMMAND_ADVIN` / `COMMAND_TX_POWER` / `COMMAND_PIN` | `NAME` / `LENAME` / `ADVIN` / `TXPOWER` / `PIN` |
| `COMMAND_GSCFG` / `COMMAND_KEYCFG` / `COMMAND_LED` / `COMMAND_BUZ` | G-sensor / button / LED / buzzer |
| `COMMAND_BADVDATA` / `COMMAND_BADVPARM` / `COMMAND_TLM` / `COMMAND_ADVEXT` | advertising data / advertising parameters / TLM / advertising extensions |
| `COMMAND_OTA` / `COMMAND_END` / `COMMAND_RAP` / `COMMAND_VERSION` / `COMMAND_BWMODE` | OTA / end / reset / version / broadcast mode |
| `DEFALUT_DEVICE_NAME` / `DEFALUT_PIN` | `"Feasycom"` / `"0000"` |
| Command results | `COMMAND_FAILED="0"`, `COMMAND_SUCCESSFUL="1"`, `COMMAND_FINISH="2"`, `COMMAND_NO_NEED="3"`, `COMMAND_TIME_OUT="4"` |
| PIN verification | `PASSWORD_CHECK=0`, `PASSWORD_SUCCESSFUL=1`, `PASSWORD_FAILED=2`, `PASSWORD_TIME_OUT=3` |

### 9.2 OTA / send status: see the constant table in 5.2

---

## 10. Notes and FAQ

1. **No scan callback at all** → in 99% of cases a permission problem: Android 12+ needs the runtime grants `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`; Android 11 and below need location permission. Also verify Bluetooth is enabled (`isBluetoothEnabled`).
2. **Parameter read/write or OTA unresponsive/failing** → check that your `AndroidManifest.xml` declares `com.feasycn.service.AtCommandService` and `com.feasycn.service.OTAService`.
3. **`UnsatisfiedLinkError`** → ① only unsupported ABIs were packaged (the library is arm64-v8a / armeabi-v7a only); ② the native library inside the AAR was replaced by another `.so`; ③ the packaging tool stripped `libFeasyBeaconLibrary.so` (check `packagingOptions` / `jniLibs`).
4. **Release build throws `NoSuchMethodError` / callbacks never fire** → the app enables obfuscation without the keep rules from 3.3.
5. **`connect()` returns false or `connectProgressUpdate` reports 2** → the PIN is outside 0-999999, the PIN is wrong, or the target is not a connectable vendor beacon (no `feasyBeacon.module` in the advertisement).
6. **Parameters do not take effect after `saveBeaconInfo()`** → every `setXxx()` only queues the value; you must call `saveBeaconInfo()` (or `saveBatch`) to write it into the module, and the connection must still be alive.
7. **Unexpected beacon count / deleting the wrong one** → a device holds at most `MAX_BEACONS` (10); indices are re-ordered after every add/delete, so deletion and updates must use the **re-ordered** index.
8. **Queries slow down after `onDeviceInfoReceived` fires** → do not do heavy work in that callback (explicitly noted by the vendor).
9. **Permission compliance** → the AAR merges `SYSTEM_ALERT_WINDOW`, `READ_EXTERNAL_STORAGE`, `BLUETOOTH_PRIVILEGED`, `ACCESS_LOCATION_EXTRA_COMMANDS` and others into your app; if store review or privacy policy does not allow them, remove them with `tools:node="remove"` as shown in 3.2.
10. **Emulator debugging** → x86/x86_64 images cannot load this library (no matching `.so`); use a real device or an arm image.
11. **Callback thread** → the callback thread is not specified; if you update UI directly from a callback, switch to the main thread first.

---

## 11. Version history

| Library version | Notes |
|---|---|
| `1.0.2.4` | Current release (namespace `com.feasycn`, AAR ships `arm64-v8a` / `armeabi-v7a` native libraries) |

> For additional ABIs, a different namespace, or source-level customization, please contact the vendor.
