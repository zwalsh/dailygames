package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.home.views.HomeView
import sh.zachwal.dailygames.home.views.ShareTextModalView
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeService @Inject constructor(
    private val resultService: ResultService,
    private val shareLineMapper: ShareLineMapper,
    private val navViewFactory: NavViewFactory
) {

    fun homeView(user: User): HomeView {
        val shareTextLines = resultService.resultsForUserToday(user)
            .map(shareLineMapper::mapToShareLine)
        val modalView = ShareTextModalView(shareTextLines)
        val navView = navViewFactory.navView(
            username = user.username,
            currentActiveNavItem = NavItem.HOME,
        )

        return HomeView(
            resultFeed = resultService.resultFeed(user.id),
            shareTextModalView = modalView,
            nav = navView,
        )
    }
}
