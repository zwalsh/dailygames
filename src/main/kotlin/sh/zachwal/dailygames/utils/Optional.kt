package sh.zachwal.dailygames.utils

import java.util.Optional

fun <T> Optional<T>.orNull(): T? {
    return if (this.isPresent) this.get() else null
}
