package sh.zachwal.dailygames.chat.api

data class ChatResponse(
    val username: String,
    val displayTime: String,
    val text: String,
)
