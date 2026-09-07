package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.KrillionInfo
import sh.zachwal.dailygames.results.resultinfo.ParsedResult
import java.time.Instant

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

    @Test
    fun `extracts Krillion mid-range score`() {
        val parsed = mapper.extract(KrillionFixtures.MID, testUser)

        assertThat(parsed.puzzleNumber).isEqualTo(54)
        assertThat(parsed.score).isEqualTo(5)
        assertThat(parsed.resultInfo).isEqualTo(
            KrillionInfo(
                rawScore = 385,
                missCount = 0,
                planktonCount = 0,
                schoolerCount = 2,
                rareCount = 4,
                deepCutCount = 1,
                krillionCount = 0,
            ),
        )
    }

    @Test
    fun `extracts Krillion zero-answer score`() {
        val parsed = mapper.extract(KrillionFixtures.ZERO, testUser)

        assertThat(parsed.score).isEqualTo(0)
        assertThat(parsed.resultInfo).isEqualTo(
            KrillionInfo(
                rawScore = 0,
                missCount = 7,
                planktonCount = 0,
                schoolerCount = 0,
                rareCount = 0,
                deepCutCount = 0,
                krillionCount = 0,
            ),
        )
    }

    @Test
    fun `extracts Krillion score with share link`() {
        val parsed = mapper.extract(KrillionFixtures.WITH_LINK, testUser)

        assertThat(parsed.score).isEqualTo(5)
        assertThat(parsed.shareTextNoLink).doesNotContain("https://")
        assertThat(parsed.resultInfo).isEqualTo(
            KrillionInfo(
                rawScore = 355,
                missCount = 1,
                planktonCount = 0,
                schoolerCount = 1,
                rareCount = 4,
                deepCutCount = 1,
                krillionCount = 0,
            ),
        )
    }

    @Test
    fun `maps Krillion perfect share line`() {
        val parsed = mapper.extract(KrillionFixtures.PERFECT, testUser)
        val result = puzzleResult(parsed)

        assertThat(mapper.shareLine(result)).isEqualTo("${Game.KRILLION.emoji()} Krillion #54 700/700 ${Game.KRILLION.perfectEmoji()}")
    }

    @Test
    fun `maps Krillion mid-range share line`() {
        val parsed = mapper.extract(KrillionFixtures.MID, testUser)
        val result = puzzleResult(parsed)

        assertThat(mapper.shareLine(result)).isEqualTo("${Game.KRILLION.emoji()} Krillion #54 385/700")
    }

    @Test
    fun `maps Krillion partial share line with perfect emoji when krillionCount is at least 1`() {
        val parsed = mapper.extract(KrillionFixtures.PARTIAL_WITH_KRILLION, testUser)
        val result = puzzleResult(parsed)

        assertThat(parsed.resultInfo).isEqualTo(
            KrillionInfo(
                rawScore = 485,
                missCount = 0,
                planktonCount = 0,
                schoolerCount = 2,
                rareCount = 2,
                deepCutCount = 1,
                krillionCount = 2,
            ),
        )
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.KRILLION.emoji()} Krillion #54 485/700 ${Game.KRILLION.perfectEmoji()}")
    }

    private fun puzzleResult(parsed: ParsedResult) = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.KRILLION,
        puzzleNumber = parsed.puzzleNumber,
        puzzleDate = parsed.date,
        instantSubmitted = Instant.now(),
        score = parsed.score,
        shareText = parsed.shareTextNoLink,
        resultInfo = parsed.resultInfo,
    )
}
