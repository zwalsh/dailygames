package sh.zachwal.dailygames.nav

import kotlinx.html.HEADER
import sh.zachwal.dailygames.chat.ChatService
import sh.zachwal.dailygames.shared_html.HTMLView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavViewFactory @Inject constructor(
    private val chatService: ChatService
) {

    fun navView(
        username: String,
        currentActiveNavItem: NavItem,
        insideNavItem: HTMLView<HEADER>? = null
    ): NavView {
        val currentChatCounts = chatService.currentChatCounts()

        return NavView(
            currentActiveNavItem = currentActiveNavItem,
            insideNavItem = insideNavItem,
            chatCounts = currentChatCounts,
        )
    }
}
