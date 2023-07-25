package com.cnting.apm_lib.report

/**
 * Created by cnting on 2023/7/25
 *
 */
class IssuePublisher {

}

interface OnIssueDetectListener {
    fun onDetectIssue(issue: Issue)
}