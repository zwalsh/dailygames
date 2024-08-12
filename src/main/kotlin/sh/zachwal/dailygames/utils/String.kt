package sh.zachwal.dailygames.utils

fun String.toSentenceCase(): String {
    return lowercase().replaceFirstChar { it.uppercase() }
}
