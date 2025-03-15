package sh.zachwal.dailygames.answers

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.net.http.HttpClient
import java.time.LocalDate

class FlagleAnswerServiceTest {

    private val service = FlagleAnswerService()

    @Test
    fun `returns the correct result for 2025-01-07`() {
        val puzzle = Puzzle(Game.FLAGLE, 1051, LocalDate.of(2025, 1, 7))

        val answer = service.answerForPuzzle(puzzle)

        assertThat(answer).isEqualTo("Bolivia \uD83C\uDDE7\uD83C\uDDF4")
    }

    @Disabled
    @Test
    fun `pull flagle answers`() {
        val dates = LocalDate.of(2025, 3, 31)..LocalDate.of(2026, 12, 31)

        val answers = answers(dates.start, dates.endInclusive)
        val file = java.io.File("flagle-${LocalDate.now()}.csv")
        file.printWriter().use { out ->
            out.println("date,answer")
            answers.forEach { (date, answer) ->
                out.println("$date,$answer")
            }
        }
    }

    private fun answers(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, String> {
        val map = mutableMapOf<LocalDate, String>()
        var date = startDate
        while (date <= endDate) {
            Thread.sleep(1000) // Don't hit the server too hard
            val answer = answerForDate(date)
            if (answer == null) {
                println("Error: $date")
                date = date.plusDays(1)
                continue
            }
            map[date] = answer
            date = date.plusDays(1)
        }
        return map
    }

    private fun answerForDate(date: LocalDate): String? {
        // fetch url
        val url = url(date)
        val client = HttpClient.newHttpClient()

        println("Fetching for date $date: $url")
        val response = client.send(
            java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .build(),
            java.net.http.HttpResponse.BodyHandlers.ofString()
        )

        return if (response.statusCode() == 200) {
            val body = response.body()
            val countryCode = body.substringAfter("\"countryCode\":\"").substringBefore("\"")
            println("Answer for $date: $countryCode")
            countryCode
        } else {
            println("Error: ${response.statusCode()}")
            null
        }
    }

    private fun url(date: LocalDate): String {
        return "https://teuteuf-dashboard-assets.pages.dev/data/flagle/games/${date.year}/$date.json"
    }
}
