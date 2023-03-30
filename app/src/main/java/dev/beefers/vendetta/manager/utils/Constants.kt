package dev.beefers.vendetta.manager.utils

import android.os.Environment

object Constants {

    val TEAM_MEMBERS = listOf(
        TeamMember("Pylix", "Developer - Vendetta", "amsyarasyiq"),
        TeamMember("Kasi", "Developer - Xposed Module", "redstonekasi")
    )

    val VENDETTA_DIR = Environment.getExternalStorageDirectory().resolve("Vendetta")

    val DUMMY_VERSION = DiscordVersion(1,0, DiscordVersion.Type.STABLE)

}

data class TeamMember(
    val name: String,
    val role: String,
    val username: String = name
)