package sh.zachwal.dailygames.home

import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.home.views.HomeView
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HomeService @Inject constructor() {

    fun homeView(user: User): HomeView {
        return HomeView(username = user.username)
    }
}