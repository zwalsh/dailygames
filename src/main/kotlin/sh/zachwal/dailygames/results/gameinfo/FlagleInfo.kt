package sh.zachwal.dailygames.results.gameinfo

object FlagleInfo : ResultInfo() {
    // Must override this because, in deserialization, a new instance is created
    override fun equals(other: Any?): Boolean {
        return other is FlagleInfo
    }
}
