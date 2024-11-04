package sh.zachwal.dailygames.admin

import sh.zachwal.dailygames.admin.views.BackfillPageView
import sh.zachwal.dailygames.db.dao.game.ResultDAO
import sh.zachwal.dailygames.nav.NavItem
import sh.zachwal.dailygames.nav.NavViewFactory
import sh.zachwal.dailygames.results.ResultService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackfillService @Inject constructor(
    private val navViewFactory: NavViewFactory,
    private val resultService: ResultService,
    private val resultDAO: ResultDAO,
) {

    fun backfillView(): BackfillPageView {
        return BackfillPageView(
            navView = navViewFactory.navView("zach", NavItem.PROFILE),
            success = false,
            failure = false,
            resultsBackfilled = null,
            resultsExisting = null,
            resultsFailed = null,
        )
    }

    fun backfillAllResults(): BackfillPageView {
        val result = runBackfill()
        return BackfillPageView(
            navView = navViewFactory.navView("zach", NavItem.PROFILE),
            success = result.failed == 0,
            failure = result.failed > 0,
            resultsBackfilled = result.backfilled,
            resultsExisting = result.existing,
            resultsFailed = result.failed,
        )
    }

    data class BackfillResult(
        val backfilled: Int,
        val existing: Int,
        val failed: Int,
    )

    private fun runBackfill(): BackfillResult {
        TODO()
    }
}