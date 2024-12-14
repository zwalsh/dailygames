package sh.zachwal.dailygames.wrapped

import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.wrapped.views.WrappedSection
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
                WrappedSection("f"),
                WrappedSection("f"),
                WrappedSection("f"),
                WrappedSection("f"),
                WrappedSection("f"),
            )
        )
    }
}