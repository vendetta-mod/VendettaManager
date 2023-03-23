package dev.beefers.vendetta.manager.utils

object Constants {

    val TEAM_MEMBERS = listOf(
        TeamMember("Pylix", "Developer - Vendetta", "amsyarasyiq"),
        TeamMember("Kasi", "Developer - Xposed Module", "redstonekasi")
    )

}

data class TeamMember(
    val name: String,
    val role: String,
    val username: String = name
)