package sh.zachwal.dailygames.answers

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorldleAnswerService @Inject constructor() : GameAnswerService(Game.WORLDLE) {

    private val worldleAnswers: Map<LocalDate, Locale> = javaClass.getResource("/worldle.csv")
        ?.readText()
        ?.split("\n")
        ?.filter { it.contains(",") }
        ?.associate { line ->
            // date is a String of format 2022-07-07
            val (date, country) = line.split(",")
            LocalDate.parse(date) to Locale("", country.uppercase().trim())
        }
        ?: throw IllegalStateException("Could not load worldle answers")

    override fun answer(puzzle: Puzzle): String? {
        return puzzle.date // Worldle Puzzles include the date
            ?.let { worldleAnswers[it] }
            ?.let { "${it.displayCountry} ${it.flagEmoji()}" }
    }
}
