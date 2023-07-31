package com.cnting.apm_lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Created by cnting on 2023/7/30
 *
 */
@Dao
interface IssueDao {
    @Insert
    fun insert(issueEntity: IssueEntity)

    @Query("select * from issue")
    suspend fun getAll(): List<IssueEntity>
}