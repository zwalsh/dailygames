package sh.zachwal.dailygames.shared_html

import kotlinx.html.Tag

/**
 * Some data that can be rendered as HTML within some parent [Tag] of type [T].
 */
abstract class HTMLView<T : Tag> {
    fun renderIn(t: T) = this.apply {
        t.render()
    }

    internal abstract fun T.render()
}