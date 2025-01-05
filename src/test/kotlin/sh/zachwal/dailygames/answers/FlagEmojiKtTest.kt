package sh.zachwal.dailygames.answers

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.Locale

class FlagEmojiKtTest {

    @Test
    fun `returns correct flag for Ghana`() {
        assertThat(Locale("en", "GH").flagEmoji()).isEqualTo("\uD83C\uDDEC\uD83C\uDDED")
    }

    @Test
    fun `returns correct flag for US`() {
        assertThat(Locale("en", "US").flagEmoji()).isEqualTo("\uD83C\uDDFA\uD83C\uDDF8")
    }
}
