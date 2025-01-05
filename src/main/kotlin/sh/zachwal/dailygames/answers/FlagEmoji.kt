package sh.zachwal.dailygames.answers

import java.util.Locale

// https://stackoverflow.com/a/42235254 implementation in Kotlin
fun Locale.flagEmoji(): String {
    val flagOffset = 0x1F1E6
    val asciiOffset = 0x41

    val firstChar = Character.codePointAt(country, 0) - asciiOffset + flagOffset
    val secondChar = Character.codePointAt(country, 1) - asciiOffset + flagOffset

    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}