package sh.zachwal.dailygames.results

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.GeoGridInfo

const val BANDLE_PERFECT = """
Bandle #941 1/6
🟩⬜⬜⬜⬜⬜
Found: 1/1 (100%)
#Bandle #Heardle #Wordle 

https://bandle.app/
"""

const val BANDLE_ZERO = """
Bandle #941 x/6
🟥🟥🟥🟥🟥🟥
Found: 0/1 (0%)
#Bandle #Heardle #Wordle 

https://bandle.app/
"""

const val BANDLE_FOUR = """
Bandle #941 4/6
🟨🟥🟨🟩⬜⬜
Found: 1/1 (100%)
#Bandle #Heardle #Wordle 

https://bandle.app/
"""

const val BANDLE_SKIP = """
Bandle #941 x/6
⬛⬛⬛⬛⬛⬛
Found: 0/1 (0%)
#Bandle #Heardle #Wordle 

https://bandle.app/
"""

class ShareTextParserBandleTest {
    private val parser = ShareTextParser()

    @Test
    fun `matches bandle`() {
        assertThat(parser.identifyGame(BANDLE_PERFECT)).isEqualTo(Game.BANDLE)
        assertThat(parser.identifyGame(BANDLE_ZERO)).isEqualTo(Game.BANDLE)
        assertThat(parser.identifyGame(BANDLE_FOUR)).isEqualTo(Game.BANDLE)
        assertThat(parser.identifyGame(BANDLE_SKIP)).isEqualTo(Game.BANDLE)
    }

}
