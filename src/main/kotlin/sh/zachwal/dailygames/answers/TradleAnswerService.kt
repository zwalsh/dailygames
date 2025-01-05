package sh.zachwal.dailygames.answers

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradleAnswerService @Inject constructor() : GameAnswerService(Game.TRADLE) {

    private val tradleAnswers: Map<LocalDate, Locale> = javaClass.getResource("/tradle.csv")
        ?.readText()
        ?.split("\n")
        ?.filter {
            // This can't be parsed into a date because it's not a real date
            !it.contains("2025-02-29") && it.contains(",")
        }
        ?.associate { line ->
            // date is a String of format 2022-07-07
            val (date, country) = line.split(",")
            LocalDate.parse(date) to Locale("", country.uppercase().trim())
        }
        ?: throw IllegalStateException("Could not load tradle answers")

    // See https://github.com/alexandersimoes/tradle/blob/4505adf01f718956f81b8047f5b0480598915ca8/src/components/Share.tsx#L15
    // Used under MIT license
    /*
    MIT License

    Copyright (c) 2022 teuteuf

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
     */
    private val tradleStartDate = LocalDate.of(2022, 3, 6)

    override fun answer(puzzle: Puzzle): String? {
        val answerLocale = answerForDate(dateForPuzzleNumber(puzzle.number))
        val displayName = answerLocale?.displayCountry
        val emoji = answerLocale?.flagEmoji()
        return "$displayName $emoji"
    }

    private fun dateForPuzzleNumber(puzzleNumber: Int): LocalDate {
        return tradleStartDate.plusDays(puzzleNumber.toLong())
    }

    fun answerForDate(date: LocalDate): Locale? {
        return tradleAnswers[date]
    }
}
