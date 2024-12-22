package sh.zachwal.dailygames.home.views

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.script
import kotlinx.html.title
import sh.zachwal.dailygames.home.views.gamelinks.GameListView
import sh.zachwal.dailygames.nav.NavView
import sh.zachwal.dailygames.shared_html.HTMLView
import sh.zachwal.dailygames.shared_html.darkMode
import sh.zachwal.dailygames.shared_html.headSetup
import sh.zachwal.dailygames.shared_html.jquery

const val SHARE_TEXT_ID = "shareTextId"

data class HomeView(
    val resultFeed: List<ResultFeedItemView>,
    val shareTextModalView: ShareTextModalView?,
    val wrappedLinkView: WrappedLinkView?,
    val gameListView: GameListView,
    val nav: NavView,
) : HTMLView<HTML>() {

    val gameSubmitFormView = GameSubmitFormView(
        includeShareButton = shareTextModalView != null
    )

    override fun HTML.render() {
        head {
            title("Daily Games")
            headSetup()
            jquery()
        }
        body {
            darkMode()
            nav.renderIn(this)
            shareTextModalView?.renderIn(this)
            div(classes = "container") {
                wrappedLinkView?.renderIn(this@div)

                div(classes = "row") {
                    div(classes = "col") {
                        div(classes = "card mx-3") {
                            div(classes = "card-body bg-secondary-subtle") {
                                h1(classes = "card-title text-center") {
                                    +"Submit Game"
                                }
                                gameSubmitFormView.renderIn(this)
                            }
                        }
                    }
                }

                gameListView.renderIn(this)

                div(classes = "row border-top") {
                    div(classes = "col") {
                        h1(classes = "text-center mt-2") {
                            +"Feed"
                        }
                    }
                }
                div(classes = "row") {
                    resultFeed.forEach {
                        it.renderIn(this)
                    }
                }
            }
            script {
                src = "/static/src/js/home.js"
            }
        }
    }
}
