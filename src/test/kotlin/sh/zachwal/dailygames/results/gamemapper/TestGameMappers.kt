package sh.zachwal.dailygames.results.gamemapper

import sh.zachwal.dailygames.leaderboard.PointCalculator
import sh.zachwal.dailygames.users.UserPreferencesService
import java.time.Clock

fun allGameMappers(
    pointCalculator: PointCalculator = PointCalculator(),
    clock: Clock,
    userPreferencesService: UserPreferencesService,
): Set<GameMapper> = setOf(
    WorldleMapper(pointCalculator),
    TradleMapper(pointCalculator),
    TravleMapper(),
    Top5Mapper(),
    FlagleMapper(pointCalculator),
    PinpointMapper(pointCalculator),
    GeocirclesMapper(),
    FramedMapper(pointCalculator),
    GeoGridMapper(),
    BandleMapper(pointCalculator),
    BracketCityMapper(),
    SizeItUpMapper(clock, userPreferencesService),
    CardleMapper(clock, userPreferencesService),
)
