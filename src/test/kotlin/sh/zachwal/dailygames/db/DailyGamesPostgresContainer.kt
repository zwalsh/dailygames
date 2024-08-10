package sh.zachwal.dailygames.db

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class DailyGamesPostgresContainer : PostgreSQLContainer<DailyGamesPostgresContainer>(DockerImageName.parse("postgres:12.18"))
