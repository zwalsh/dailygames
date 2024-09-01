package sh.zachwal.dailygames.db.dao

import org.jdbi.v3.sqlobject.statement.SqlQuery
import sh.zachwal.dailygames.db.jdbi.UserPreferences

interface UserPreferencesDAO {
    @SqlQuery(
        """
            INSERT INTO public.user_preferences (user_id)
            VALUES (:userId)
            RETURNING *
        """
    )
    fun createUserPreferencesWithDefaults(userId: Long): UserPreferences
}