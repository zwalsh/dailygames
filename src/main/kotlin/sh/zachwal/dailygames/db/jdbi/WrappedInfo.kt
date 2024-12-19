package sh.zachwal.dailygames.db.jdbi

import org.jdbi.v3.core.mapper.reflect.ColumnName

/**
 * TODO Will generate and store the wrapped info in the database so we don't have to repeatedly query all games.
 */
data class WrappedInfo(
    val id: Long,
    @ColumnName("user_id")
    val userId: Long,
)
