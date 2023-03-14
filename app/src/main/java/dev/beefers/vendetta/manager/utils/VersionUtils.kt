package dev.beefers.vendetta.manager.utils

data class DiscordVersion(
    val major: Int,
    val minor: Int,
    val type: Type
) {

    enum class Type {
        STABLE,
        BETA,
        ALPHA
    }

    companion object {

        fun fromVersionCode(string: String): DiscordVersion? = with(string) {
            if (length < 4) return@with null
            if (toIntOrNull() == null) return@with null
            val codeReversed = toCharArray().reversed().joinToString("")
            val typeInt = codeReversed[2].toString().toInt()
            val type = Type.values().getOrNull(typeInt) ?: return@with null
            DiscordVersion(
                codeReversed.slice(3..codeReversed.lastIndex).toInt(),
                codeReversed.substring(0, 2).toInt(),
                type
            )
        }

    }

}