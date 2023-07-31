package com.cnting.apm_lib.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Created by cnting on 2023/7/30
 *
 */
@Entity(tableName = "issue")
@Parcelize
data class IssueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: Int?,
    val tag: String?,
    val key: String?,
    val time: Long?,
    val content: String?,
) : Parcelable