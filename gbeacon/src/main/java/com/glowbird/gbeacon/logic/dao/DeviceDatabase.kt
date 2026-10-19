package com.glowbird.gbeacon.logic.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.glowbird.gbeacon.logic.model.DeviceInfo

@Database(version = 2, entities = [DeviceInfo::class], exportSchema = false)
abstract class DeviceDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao

    companion object {
        @Volatile
        private var instance: DeviceDatabase? = null

        /**
         * 获取数据库实例，使用双重检查锁定机制确保线程安全
         * @param context 上下文对象，用于创建数据库实例
         * @return DeviceDatabase 实例
         */
        fun getDatabase(context: Context): DeviceDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DeviceDatabase::class.java,
                    context.packageName + "_app_database"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }

}