package sh.zachwal.dailygames.results.gamemapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import java.time.Instant

class GeocirclesMapperTest : GameMapperContractTest() {
    override val mapper = GeocirclesMapper()
    override val fixtures = GeocirclesFixtures.ALL
    private val testUser = User(id = 1L, username = "test", hashedPassword = "hash")

    private val geocirclesResult = PuzzleResult(
        id = 1,
        userId = 1,
        game = Game.GEOCIRCLES,
        puzzleNumber = 123,
        puzzleDate = null,
        instantSubmitted = Instant.now(),
        score = 10,
        shareText = "share text",
        resultInfo = GeocirclesInfo,
    )

    @Test
    fun `extracts geocircles`() {
        val info = mapper.extract(GeocirclesFixtures.PERFECT, testUser)
        assertThat(info.puzzleNumber).isEqualTo(55)
        assertThat(info.score).isEqualTo(10)
    }

    @Test
    fun `extracts geocircles 0 points`() {
        val info = mapper.extract(GeocirclesFixtures.ZERO_POINTS, testUser)
        assertThat(info.score).isEqualTo(0)
    }

    @Test
    fun `extracts geocircles did not finish`() {
        val info = mapper.extract(GeocirclesFixtures.DNF, testUser)
        assertThat(info.score).isEqualTo(4)
    }

    @Test
    fun `extracts geocircles with lives left`() {
        val info = mapper.extract(GeocirclesFixtures.LIVES_LEFT, testUser)
        assertThat(info.score).isEqualTo(7)
    }

    @Test
    fun `sets correct game info`() {
        val info = mapper.extract(GeocirclesFixtures.PERFECT, testUser)
        assertThat(info.resultInfo).isEqualTo(GeocirclesInfo)
    }

    @Test
    fun `maps geocircles line perfect`() {
        assertThat(mapper.shareLine(geocirclesResult)).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 5/5 🎯")
    }

    @Test
    fun `maps geocircles line lives left`() {
        assertThat(mapper.shareLine(geocirclesResult.copy(score = 8))).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 5/5 (2 wrong)")
    }

    @Test
    fun `maps geocircles line some right`() {
        assertThat(mapper.shareLine(geocirclesResult.copy(score = 4))).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 4/5")
    }

    @Test
    fun `maps geocircles line none right`() {
        assertThat(mapper.shareLine(geocirclesResult.copy(score = 0))).isEqualTo("${Game.GEOCIRCLES.emoji()} Geocircles #123 0/5")
    }
}
