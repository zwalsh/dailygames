package sh.zachwal.dailygames.leaderboard

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.PuzzleResult
import sh.zachwal.dailygames.results.resultinfo.BandleInfo
import sh.zachwal.dailygames.results.resultinfo.BracketCityInfo
import sh.zachwal.dailygames.results.resultinfo.FlagleInfo
import sh.zachwal.dailygames.results.resultinfo.FramedInfo
import sh.zachwal.dailygames.results.resultinfo.GeoGridInfo
import sh.zachwal.dailygames.results.resultinfo.GeocirclesInfo
import sh.zachwal.dailygames.results.resultinfo.PinpointInfo
import sh.zachwal.dailygames.results.resultinfo.Top5Info
import sh.zachwal.dailygames.results.resultinfo.TradleInfo
import sh.zachwal.dailygames.results.resultinfo.WorldleInfo
import java.time.Instant

class PointCalculatorTest {
    private val calculator = PointCalculator()

    private val worldleResult = PuzzleResult(
        id = 1L,
        userId = 1L,
        game = Game.WORLDLE,
        puzzleNumber = 30,
        instantSubmitted = Instant.now(),
        puzzleDate = null,
        score = 5,
        shareText = "",
        resultInfo = WorldleInfo(
            percentage = 100
        ),
    )

    @Test
    fun `returns 7 minus score for worldle result`() {
        assertThat(calculator.calculatePoints(worldleResult)).isEqualTo(2)
    }

    private val flagleResult = worldleResult.copy(
        game = Game.FLAGLE,
        score = 6,
        resultInfo = FlagleInfo
    )

    @Test
    fun `returns 7 minus score for flagle result`() {
        assertThat(calculator.calculatePoints(flagleResult)).isEqualTo(1)
    }

    private val tradleResult = worldleResult.copy(
        game = Game.TRADLE,
        score = 7,
        resultInfo = TradleInfo,
    )

    @Test
    fun `returns 7 minus score for tradle result`() {
        assertThat(calculator.calculatePoints(tradleResult)).isEqualTo(0)
    }

    private val bandleResult = worldleResult.copy(
        game = Game.BANDLE,
        score = 5,
        resultInfo = BandleInfo(
            numSkips = 0,
            numCorrectBand = 1,
            numIncorrect = 3
        ),
    )

    @Test
    fun `returns 7 minus score for bandle result`() {
        assertThat(calculator.calculatePoints(bandleResult)).isEqualTo(2)
    }

    private val top5Result = worldleResult.copy(
        game = Game.TOP5,
        score = 5,
        resultInfo = Top5Info(
            numGuesses = 0,
            numCorrect = 0,
            isPerfect = false,
        ),
    )

    @Test
    fun `returns score directly for top5 result`() {
        assertThat(calculator.calculatePoints(top5Result)).isEqualTo(5)
    }

    private val pinpointResult = worldleResult.copy(
        game = Game.PINPOINT,
        score = 2,
        resultInfo = PinpointInfo,
    )

    @Test
    fun `returns 6 minus score for pinpoint result`() {
        assertThat(calculator.calculatePoints(pinpointResult)).isEqualTo(4)
    }

    private val geocirclesResult = worldleResult.copy(
        game = Game.GEOCIRCLES,
        score = 8,
        resultInfo = GeocirclesInfo,
    )

    @Test
    fun `returns score directly for geocircles`() {
        assertThat(calculator.calculatePoints(geocirclesResult)).isEqualTo(8)
    }

    private val framedResult = worldleResult.copy(
        game = Game.FRAMED,
        score = 4,
        resultInfo = FramedInfo,
    )

    @Test
    fun `returns 7 minus score for framed result`() {
        assertThat(calculator.calculatePoints(framedResult)).isEqualTo(3)
    }

    private val geoGridResult = worldleResult.copy(
        game = Game.GEOGRID,
        score = 9,
        resultInfo = GeoGridInfo(
            score = 123.3,
            rank = 3618,
            rankOutOf = 11718,
            numCorrect = 9,
        ),
    )

    @Test
    fun `returns score directly for geogrid`() {
        assertThat(calculator.calculatePoints(geoGridResult)).isEqualTo(9)
    }

    @Test
    fun `returns 0 for framed result with 7 incorrect guesses`() {
        val result = framedResult.copy(score = 7)
        assertThat(calculator.calculatePoints(result)).isEqualTo(0)
    }

    private val bracketCityResult = worldleResult.copy(
        game = Game.BRACKET_CITY,
        resultInfo = BracketCityInfo(
            rank = "Power Broker",
            rankEmoji = "💼",
            wrongGuesses = 1,
            peeks = 0,
            answersRevealed = 0,
            totalScore = 98.0,
            grid = ""
        )
    )

    @Test
    fun `calculates points for bracket city by dividing score by 10 and rounding down`() {
        val result100 = bracketCityResult.copy(score = 100)
        assertThat(calculator.calculatePoints(result100)).isEqualTo(10)

        val result99 = bracketCityResult.copy(score = 99)
        assertThat(calculator.calculatePoints(result99)).isEqualTo(9)

        val result90 = bracketCityResult.copy(score = 90)
        assertThat(calculator.calculatePoints(result90)).isEqualTo(9)

        val result89 = bracketCityResult.copy(score = 89)
        assertThat(calculator.calculatePoints(result89)).isEqualTo(8)

        val result0 = bracketCityResult.copy(score = 0)
        assertThat(calculator.calculatePoints(result0)).isEqualTo(0)
    }

    @Test
    fun `returns max score for simple games`() {
        assertThat(calculator.maxPoints(worldleResult)).isEqualTo(6)
        assertThat(calculator.maxPoints(flagleResult)).isEqualTo(6)
        assertThat(calculator.maxPoints(tradleResult)).isEqualTo(6)
        assertThat(calculator.maxPoints(top5Result)).isEqualTo(10)
        assertThat(calculator.maxPoints(pinpointResult)).isEqualTo(5)
        assertThat(calculator.maxPoints(geocirclesResult)).isEqualTo(10)
        assertThat(calculator.maxPoints(framedResult)).isEqualTo(6)
        assertThat(calculator.maxPoints(geoGridResult)).isEqualTo(9)
        assertThat(calculator.maxPoints(bandleResult)).isEqualTo(6)
    }
}
