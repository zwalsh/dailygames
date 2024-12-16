package sh.zachwal.dailygames.wrapped

import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.wrapped.views.StatSection
import sh.zachwal.dailygames.wrapped.views.TextSection
import sh.zachwal.dailygames.wrapped.views.WelcomeSection
import sh.zachwal.dailygames.wrapped.views.WrappedView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WrappedService @Inject constructor(
    private val navViewFactory: NavViewFactory,
) {

    fun wrappedView(year: Int, wrappedId: String): WrappedView {
        val navView = navViewFactory.navView("zach", NavItem.PROFILE)
        return WrappedView(
            name = "zach",
            year = year,
            navView = navView,
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
                TextSection(
                    topText = "That's in the top...",
                    middleText = "15%",
                    bottomText = "...overall!",
                    fontSizeOverride = "35vw",
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
                TextSection(
                    topText = "That's in the top...",
                    middleText = "10%",
                    bottomText = "...of all players!",
                    fontSizeOverride = "35vw",
                ),
                // Favorite game
                TextSection(
                    topText = "Your favorite game was...",
                    middleText = "${Game.WORLDLE.emoji()} Worldle ${Game.WORLDLE.emoji()}",
                    bottomText = "",
                ),
                StatSection(
                    topText = "You played ...",
                    stat = 123,
                    bottomText = "...games of Worldle.",
                ),
                TextSection(
                    topText = "Your best day was...",
                    middleText = "September 14th",
                    bottomText = "",
                ),
                StatSection(
                    topText = "You scored...",
                    stat = 39,
                    bottomText = "...points that day!",
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
                    middleText = "${Game.WORLDLE.emoji()} Worldle ${Game.WORLDLE.emoji()}",
                    bottomText = "",
                ),
                TextSection(
                    topText = "Your average Worldle score was...",
                    middleText = "5.5",
                    bottomText = "",
                ),
                StatSection(
                    topText = "Which ranks...",
                    stat = 2,
                    bottomText = "...overall!",
                ),
            )
        )
    }
}