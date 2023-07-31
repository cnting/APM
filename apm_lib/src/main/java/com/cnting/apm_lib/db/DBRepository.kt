package com.cnting.apm_lib.db

import android.os.SystemClock
import android.util.Log
import com.cnting.apm_lib.report.Issue

/**
 * Created by cnting on 2023/7/31
 *
 */
class DBRepository(private val db: APMDatabase) {
    fun saveIssue(issue: Issue) {
        db.issueDao().insert(
            IssueEntity(
                type = issue.type,
                tag = issue.tag,
                key = issue.key,
                time = System.currentTimeMillis(),
                content = issue.content.toString()
            )
        )
    }

    suspend fun getAllIssue(): List<IssueEntity> {
        return db.issueDao().getAll()
    }
}