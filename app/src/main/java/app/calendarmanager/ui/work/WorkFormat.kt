package app.calendarmanager.ui.work

import androidx.annotation.StringRes
import app.calendarmanager.R
import app.calendarmanager.data.local.entity.WorkEventKind

@StringRes fun WorkEventKind.label() = when (this) {
    WorkEventKind.WEEKLY -> R.string.work_kind_weekly
    WorkEventKind.ONE_TIME -> R.string.work_kind_one_time
    WorkEventKind.UNEXPECTED -> R.string.work_kind_unexpected
}
