package com.downnotice.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class IncidentStatus {
    OPERATIONAL,
    DEGRADED,
    DOWN,
    ERROR,
    RESOLVED,
    SCHEDULED,
    UNKNOWN;

    companion object {
        fun color(status: IncidentStatus): StatusColor = when (status) {
            OPERATIONAL, RESOLVED, SCHEDULED -> StatusColor.GREEN
            DEGRADED -> StatusColor.YELLOW
            DOWN -> StatusColor.RED
            ERROR -> StatusColor.BLACK
            UNKNOWN -> StatusColor.YELLOW
        }
    }
}

enum class StatusColor { GREEN, YELLOW, RED, BLACK }
