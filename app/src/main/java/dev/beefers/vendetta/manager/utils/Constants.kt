package dev.beefers.vendetta.manager.utils

import android.os.Environment

object Constants {

    val TEAM_MEMBERS = listOf(
        TeamMember("Pylix", "Developer - Vendetta", "amsyarasyiq"),
        TeamMember("Kasi", "Developer - Xposed Module", "redstonekasi")
    )

    val VENDETTA_DIR = Environment.getExternalStorageDirectory().resolve("Vendetta")

}

data class TeamMember(
    val name: String,
    val role: String,
    val username: String = name
)