package sh.zachwal.dailygames.db.jdbi

import org.jdbi.v3.core.mapper.reflect.ColumnName

data class UserPreferences constructor(
    val id: Long,
    @ColumnName("user_id")
    val userId: Long,
    @ColumnName("time_zone")
    val timeZone: String,
)
