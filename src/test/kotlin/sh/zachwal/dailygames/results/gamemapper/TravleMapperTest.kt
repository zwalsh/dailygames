package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.TravleInfo
import java.time.Instant

class TravleMapperTest : GameMapperContractTest() {
    override val mapper = TravleMapper()
    override val fixtures = TravleFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val travleResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.TRAVLE,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 0,
        shareText = "share text",
        resultInfo = TravleInfo(
            numGuesses = 6,
            numIncorrect = 0,
            numPerfect = 6,
            numHints = 0,
        ),
    )

    @Test
    fun `extracts Travle info when perfect`() {
        val result = mapper.extract(TravleFixtures.PERFECT, testUser)

        assertThat(result.puzzleNumber).isEqualTo(607)
        assertThat(result.score).isEqualTo(0)
        assertThat(result.shareTextNoLink).isEqualTo(
            """
                #travle #607 +0 (Perfect)
                ✅✅✅✅✅✅✅
            """.trimIndent(),
        )
        assertThat(result.game).isEqualTo(Game.TRAVLE)
        assertThat(result.resultInfo).isInstanceOf(TravleInfo::class.java)

        val travleInfo = result.info<TravleInfo>()
        assertThat(travleInfo.numPerfect).isEqualTo(7)
        assertThat(travleInfo.numIncorrect).isEqualTo(0)
        assertThat(travleInfo.numGuesses).isEqualTo(7)
        assertThat(travleInfo.numHints).isEqualTo(0)
    }

    @Test
    fun `extracts Travle info when score is not perfect`() {
        val result = mapper.extract(TravleFixtures.PLUS_0, testUser)

        assertThat(result.puzzleNumber).isEqualTo(607)
        assertThat(result.score).isEqualTo(0)
        val travleInfo = result.info<TravleInfo>()
        assertThat(travleInfo.numPerfect).isEqualTo(6)
        assertThat(travleInfo.numIncorrect).isEqualTo(0)
        assertThat(travleInfo.numGuesses).isEqualTo(7)
        assertThat(travleInfo.numHints).isEqualTo(0)
    }

    @Test
    fun `extracts Travle info when hints are used`() {
        val result = mapper.extract(TravleFixtures.WITH_HINT, testUser)

        assertThat(result.puzzleNumber).isEqualTo(606)
        assertThat(result.score).isEqualTo(2)
        val travleInfo = result.info<TravleInfo>()
        assertThat(travleInfo.numPerfect).isEqualTo(3)
        assertThat(travleInfo.numIncorrect).isEqualTo(2)
        assertThat(travleInfo.numGuesses).isEqualTo(6)
        assertThat(travleInfo.numHints).isEqualTo(1)
    }

    @Test
    fun `extracts Travle info when did not finish`() {
        val result = mapper.extract(TravleFixtures.THREE_AWAY, testUser)

        assertThat(result.puzzleNumber).isEqualTo(614)
        assertThat(result.score).isEqualTo(-3)
        val travleInfo = result.info<TravleInfo>()
        assertThat(travleInfo.numPerfect).isEqualTo(1)
        assertThat(travleInfo.numIncorrect).isEqualTo(8)
        assertThat(travleInfo.numGuesses).isEqualTo(9)
        assertThat(travleInfo.numHints).isEqualTo(0)
    }

    @Test
    fun `maps travle perfect`() {
        assertThat(mapper.shareLine(travleResult)).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +0 ${Game.TRAVLE.perfectEmoji()}")
    }

    @Test
    fun `maps travle plus zero non-perfect`() {
        val result = travleResult.copy(resultInfo = travleResult.info<TravleInfo>().copy(numPerfect = 5))
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +0")
    }

    @Test
    fun `maps travle plus one`() {
        val result = travleResult.copy(
            score = 1,
            resultInfo = travleResult.info<TravleInfo>().copy(numGuesses = 7, numPerfect = 6, numIncorrect = 1),
        )
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +1")
    }

    @Test
    fun `maps travle with hints`() {
        val result = travleResult.copy(
            score = 1,
            resultInfo = travleResult.info<TravleInfo>().copy(numGuesses = 7, numPerfect = 6, numIncorrect = 1, numHints = 2),
        )
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 +1 (2 hints)")
    }

    @Test
    fun `maps travle with negative score`() {
        val result = travleResult.copy(
            score = -1,
            resultInfo = travleResult.info<TravleInfo>().copy(numGuesses = 10, numPerfect = 6, numIncorrect = 4),
        )
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 (1 away)")
    }

    @Test
    fun `maps travle with negative score and one hint`() {
        val result = travleResult.copy(
            score = -1,
            resultInfo = travleResult.info<TravleInfo>().copy(numGuesses = 10, numPerfect = 6, numIncorrect = 4, numHints = 1),
        )
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.TRAVLE.emoji()} Travle #123 (1 away) (1 hint)")
    }
}
