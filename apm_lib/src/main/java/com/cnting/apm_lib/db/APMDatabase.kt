package com.cnting.apm_lib.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Created by cnting on 2023/7/30
 *
 */
@Database(entities = [IssueEntity::class], version = 1)
abstract class APMDatabase : RoomDatabase() {
    abstract fun issueDao(): IssueDao
}