package sh.zachwal.dailygames.db.jdbi

import sh.zachwal.dailygames.roles.Role

data class UserRole(
    val userId: Long,
    val role: Role
)
