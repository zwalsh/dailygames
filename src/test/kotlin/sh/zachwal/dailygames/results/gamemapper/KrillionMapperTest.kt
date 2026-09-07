package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.results.resultinfo.KrillionInfo

class KrillionMapperTest : GameMapperContractTest() {
    override val mapper = KrillionMapper()
    override val fixtures = KrillionFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    @Test
    fun `extracts Krillion perfect score`() {
        val parsed = mapper.extract(KrillionFixtures.PERFECT, testUser)

        assertThat(parsed.game).isEqualTo(Game.KRILLION)
        assertThat(parsed.puzzleNumber).isEqualTo(54)
        assertThat(parsed.date).isNull()
        assertThat(parsed.score).isEqualTo(10)
        assertThat(parsed.resultInfo).isEqualTo(
            KrillionInfo(
                rawScore = 700,
                missCount = 0,
                planktonCount = 0,
                schoolerCount = 0,
                rareCount = 0,
                deepCutCount = 0,
                krillionCount = 7,
            ),
        )
    }
}
