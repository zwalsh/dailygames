package sh.zachwal.dailygames.home

import kotlinx.html.HTML
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.home.views.HomeView
import sh.zachwal.dailygames.shared_html.HTMLView
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HomeService @Inject constructor() {

    fun homeView(user: User): HTMLView<HTML> {
        return HomeView(name = user.username)
    }
}