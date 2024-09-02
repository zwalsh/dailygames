package sh.zachwal.dailygames.db.dao

import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import sh.zachwal.dailygames.db.jdbi.UserPreferences

interface UserPreferencesDAO {
    @SqlQuery(
        """
            INSERT INTO public.user_preferences (user_id)
            VALUES (:userId)
            RETURNING *
        """
    )
    fun createDefaultPreferences(userId: Long): UserPreferences

    @SqlQuery(
        """
            SELECT * FROM public.user_preferences
            WHERE user_id = :userId
        """
    )
    fun getByUserId(userId: Long): UserPreferences?

    @SqlUpdate(
        """
            UPDATE public.user_preferences
            SET time_zone = :timeZone
            WHERE user_id = :userId
        """
    )
    fun updateTimeZone(userId: Long, timeZone: String)
}
