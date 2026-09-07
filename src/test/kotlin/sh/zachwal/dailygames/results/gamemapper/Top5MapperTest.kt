package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import java.time.Instant

class Top5MapperTest : GameMapperContractTest() {
    override val mapper = Top5Mapper()
    override val fixtures = Top5Fixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val top5Result = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.TOP5,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 5,
        shareText = "share text",
        resultInfo = Top5Info(numGuesses = 5, numCorrect = 5, isPerfect = true),
    )

    @Test
    fun `extracts Top5 info`() {
        val result = mapper.extract(Top5Fixtures.WITH_MISSES, testUser)

        assertThat(result.puzzleNumber).isEqualTo(171)
        assertThat(result.score).isEqualTo(3)
        assertThat(result.game).isEqualTo(Game.TOP5)
        assertThat(result.resultInfo).isInstanceOf(Top5Info::class.java)
        val top5Info = result.info<Top5Info>()
        assertThat(top5Info.numGuesses).isEqualTo(8)
        assertThat(top5Info.numCorrect).isEqualTo(3)
        assertThat(top5Info.isPerfect).isFalse()
    }

    @Test
    fun `extracts Top5 info with all 5 correct but misses`() {
        val result = mapper.extract(Top5Fixtures.ALL_5_WITH_MISSES, testUser)

        assertThat(result.puzzleNumber).isEqualTo(170)
        assertThat(result.score).isEqualTo(6)
        val top5Info = result.info<Top5Info>()
        assertThat(top5Info.numGuesses).isEqualTo(9)
        assertThat(top5Info.numCorrect).isEqualTo(5)
        assertThat(top5Info.isPerfect).isFalse()
    }

    @Test
    fun `extracts Top5 info with no misses`() {
        val result = mapper.extract(Top5Fixtures.NO_MISSES, testUser)

        assertThat(result.puzzleNumber).isEqualTo(169)
        assertThat(result.score).isEqualTo(10)
        val top5Info = result.info<Top5Info>()
        assertThat(top5Info.numGuesses).isEqualTo(5)
        assertThat(top5Info.numCorrect).isEqualTo(5)
        assertThat(top5Info.isPerfect).isFalse()
    }

    @Test
    fun `extracts Top5 info with perfect score`() {
        val result = mapper.extract(Top5Fixtures.PERFECT, testUser)

        assertThat(result.puzzleNumber).isEqualTo(169)
        assertThat(result.score).isEqualTo(10)
        val top5Info = result.info<Top5Info>()
        assertThat(top5Info.numGuesses).isEqualTo(5)
        assertThat(top5Info.numCorrect).isEqualTo(5)
        assertThat(top5Info.isPerfect).isTrue()
    }

    @Test
    fun `maps top5 perfect`() {
        assertThat(mapper.shareLine(top5Result)).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 5/5 🌈")
    }

    @Test
    fun `maps top5 five correct, no misses, but not perfect`() {
        val result = top5Result.copy(resultInfo = top5Result.info<Top5Info>().copy(isPerfect = false))
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 5/5")
    }

    @Test
    fun `maps top5 with five correct but some misses`() {
        val result = top5Result.copy(
            resultInfo = top5Result.info<Top5Info>().copy(numCorrect = 5, numGuesses = 6, isPerfect = false),
        )
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 5/5 (1 wrong)")
    }

    @Test
    fun `maps top5 with misses`() {
        val result = top5Result.copy(
            resultInfo = top5Result.info<Top5Info>().copy(numCorrect = 4, numGuesses = 10, isPerfect = false),
        )
        assertThat(mapper.shareLine(result)).isEqualTo("${Game.TOP5.emoji()} Top 5 #123 4/5")
    }
}
