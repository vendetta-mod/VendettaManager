package dev.beefers.vendetta.manager.installer.step

import androidx.annotation.StringRes
import dev.beefers.vendetta.manager.R

enum class StepGroup(@StringRes val nameRes: Int) {
    DL(R.string.group_download),
    PATCHING(R.string.group_patch),
    INSTALLING(R.string.group_installing)
}