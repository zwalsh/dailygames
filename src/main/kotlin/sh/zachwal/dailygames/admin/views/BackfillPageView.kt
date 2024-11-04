package sh.zachwal.dailygames.admin.views

import kotlinx.html.DIV
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.li
import kotlinx.html.title
import kotlinx.html.ul
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup

data class BackfillPageView(
    val navView: NavView,
    val success: Boolean,
    val failure: Boolean,
    val resultsBackfilled: Int?,
    val resultsExisting: Int?,
    val resultsFailed: Int?,
) : HTMLView<HTML>() {
    override fun HTML.render() {
        head {
            title {
                +"Backfill"
            }
            headSetup()
        }
        body {
            darkMode()
            navView.renderIn(this)
            div(classes = "container") {
                h1 {
                    +"Backfill Result Data"
                }
                if (success) {
                    div(classes = "alert alert-success") {
                        +"Success"
                    }
                }
                if (failure) {
                    div(classes = "alert alert-danger") {
                        +"Failure"
                    }
                }
                resultList()
                button(classes = "btn btn-primary") {
                    +"Backfill"
                }
            }
        }
    }

    private fun DIV.resultList() {
        if (resultsBackfilled == null && resultsExisting == null && resultsFailed == null) {
            return
        }
        ul {
            resultsBackfilled?.let {
                li {
                    +"Migrated: $it results"
                }
            }
            resultsExisting?.let {
                li {
                    +"Existing: $it results"
                }
            }
            resultsFailed?.let {
                li {
                    +"Failed: $it results"
                }
            }
        }
    }
}
