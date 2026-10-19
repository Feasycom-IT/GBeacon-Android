package com.glowbird.gbeacon.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.glowbird.gbeacon.logic.model.DeviceInfo

/**
 * 管理数据库中 DeviceInfo 实体的 DAO 接口。
 */

@Dao
interface DeviceDao {

    /**
     * 将多个 DeviceInfo 对象插入数据库。
     * @param deviceInfoList 要添加的 DeviceInfo 对象列表。
     */
    @Insert
    fun addDevices(deviceInfoList: List<DeviceInfo>)

    /**
     * 更新数据库中的单个 DeviceInfo 对象。
     * @param deviceInfo 要更新的 DeviceInfo 对象。
     */
    @Update
    fun updateDevice(deviceInfo: DeviceInfo)

    @Update
    fun updateDevice(deviceInfoList: List<DeviceInfo>)

    /**
     * 将单个 DeviceInfo 对象插入数据库。
     * @param deviceInfo 要插入的 DeviceInfo 对象。
     */
    @Insert
    fun insertDevice(deviceInfo: DeviceInfo)

    /**
     * 按名称查询数据库中的 DeviceInfo 对象。
     * @param name 要查询的 DeviceInfo 的名称。
     * @return 查询到的 DeviceInfo 对象，如果未找到则返回 null。
     */
    @Query("SELECT * FROM DeviceInfo WHERE name = :name")
    fun queryDeviceByName(name: String): DeviceInfo?

    /**
     * 按编号查询数据库中的 DeviceInfo 对象。
     * @param number 要查询的 DeviceInfo 的编号。
     * @return 查询到的 DeviceInfo 对象，如果未找到则返回 null。
     */
    @Query("SELECT * FROM DeviceInfo WHERE number = :number")
    fun queryDeviceByNumber(number: Int): DeviceInfo?

    /**
     * 按设备类型查询 DeviceInfo 对象的编号列表。
     * @param deviceType 用于过滤查询的设备类型。
     * @return 整数数组，表示 DeviceInfo 对象的编号。
     */
    @Query("SELECT number FROM DeviceInfo WHERE deviceType = :deviceType")
    fun queryAllBeaconByDeviceType(deviceType: Int): IntArray

    /**
     * 删除数据库中的所有 DeviceInfo 记录
     */
    @Query("DELETE FROM DeviceInfo")
    fun deleteAllBeacons()
}