package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.home.views.HomeView
import sh.zachwal.dailygames.home.views.ShareTextModalView
import sh.zachwal.dailygames.results.ResultService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeService @Inject constructor(
    private val resultService: ResultService,
    private val shareLineMapper: ShareLineMapper,
) {

    fun homeView(user: User): HomeView {
        val shareTextLines = resultService.resultsForUserToday(user)
            .map(shareLineMapper::mapToShareLine)
        val modalView = ShareTextModalView(shareTextLines)
        return HomeView(
            username = user.username,
            resultFeed = resultService.resultFeed(user.id),
            shareTextModalView = modalView,
        )
    }
}
