package sh.zachwal.dailygames.wrapped

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.wrapped.views.RanksTableSection
import sh.zachwal.dailygames.wrapped.views.StatSection
import sh.zachwal.dailygames.wrapped.views.SummaryTableSection
import sh.zachwal.dailygames.wrapped.views.TextSection
import sh.zachwal.dailygames.wrapped.views.WelcomeSection
import sh.zachwal.dailygames.wrapped.views.WrappedView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WrappedService @Inject constructor() {

    fun wrappedView(year: Int, wrappedId: String): WrappedView {
        return WrappedView(
            name = "zach",
            year = year,
            sections = listOf(
                WelcomeSection(year, "zach"),
                StatSection(
                    topText = "You played...",
                    stat = 123,
                    bottomText = "...games this year.",
                ),
                StatSection(
                    topText = "That ranks...",
                    stat = 15,
                    bottomText = "...across all players!",
                ),
                // points scored
                StatSection(
                    topText = "You scored...",
                    stat = 1234,
                    bottomText = "...points this year.",
                ),
                StatSection(
                    topText = "That ranks...",
                    stat = 10,
                    bottomText = "...overall!",
                ),
                // Favorite game
                TextSection(
                    topText = "Your favorite game was...",
                    middleText = "${Game.WORLDLE.emoji()}Worldle${Game.WORLDLE.emoji()}",
                    bottomText = "...you played it 123 times!",
                ),
                TextSection(
                    topText = "Your best day was...",
                    middleText = "September 14th",
                    bottomText = "...when you scored 39 points!",
                ),
                StatSection(
                    topText = "You played Daily Games for...",
                    stat = 1212,
                    bottomText = "...minutes this year.",
                ),
                StatSection(
                    topText = "That's number...",
                    stat = 3,
                    bottomText = "... of all players!",
                ),
                TextSection(
                    topText = "Your best game was...",
                    middleText = "${Game.WORLDLE.emoji()}Worldle${Game.WORLDLE.emoji()}",
                    bottomText = "",
                ),
                TextSection(
                    topText = "Your average Worldle score was...",
                    middleText = "5.5",
                    bottomText = "...which ranks #2!",
                    fontSizeOverride = "35vw;"
                ),
                SummaryTableSection(
                    f = "f"
                ),
                RanksTableSection(
                    title = "Totals",
                ),
                RanksTableSection(
                    title = "Averages",
                ),
            )
        )
    }
}
