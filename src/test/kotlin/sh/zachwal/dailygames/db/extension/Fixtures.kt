package sh.zachwal.dailygames.db.extension

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import sh.zachwal.dailygames.db.dao.UserDAO
import sh.zachwal.dailygames.db.dao.game.PuzzleDAO
import sh.zachwal.dailygames.db.jdbi.User
import sh.zachwal.dailygames.db.jdbi.puzzle.Game
import sh.zachwal.dailygames.db.jdbi.puzzle.Puzzle

class Fixtures(
    jdbi: Jdbi
) {
    private val userDAO: UserDAO = jdbi.onDemand()

    lateinit var zach: User
    lateinit var jackie: User


    private val puzzleDAO: PuzzleDAO = jdbi.onDemand()
    lateinit var worldle123Puzzle: Puzzle
    lateinit var flagle123Puzzle: Puzzle

    fun runFixtures() {
        zach = userDAO.createUser("zach", "hashedPassword")!!.also {
            println("Created user: $it")
        }
        jackie = userDAO.createUser("jackie", "hashedPassword")!!

        worldle123Puzzle = puzzleDAO.insertPuzzle(Puzzle(Game.WORLDLE, 123, null))
        flagle123Puzzle = puzzleDAO.insertPuzzle(Puzzle(Game.FLAGLE, 123, null))
    }
}
