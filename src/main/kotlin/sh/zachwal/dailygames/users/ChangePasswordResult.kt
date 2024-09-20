package sh.zachwal.dailygames.users

sealed class ChangePasswordResult

object ChangePasswordSuccess : ChangePasswordResult()

class ChangePasswordFailure(val errorMessage: String) : ChangePasswordResult()