# Per-game mapper refactor

## Problem

Game-specific logic is currently split across two parallel `when (game)`
dispatchers that must be kept in sync by hand:

- `ShareTextParser.identifyGame()` (regex/`contains` sniffing of raw share
  text) + one `extractXInfo()` method per game, used by
  `ResultService.createResult()`.
- `ShareLineMapper.mapToShareLine()`, a `when (result.game)` dispatch to one
  `toXShareLine()` method per game, used by `ShareTextService`.

Every new game (see the `add-game` skill) requires editing both files in
lockstep, and nothing enforces that a share text matches exactly one game —
`identifyGame` just returns the first regex that happens to match, so
ordering bugs are silent.

## Goal

One class per `Game`, each owning all three per-game concerns:

1. `matches(shareText): Boolean` — replaces the branch in `identifyGame()`.
2. `extract(shareText, user): ParsedResult` — replaces `extractXInfo()`.
3. `shareLine(result): String` — replaces `toXShareLine()`.

`ShareTextParser` and `ShareLineMapper` become thin facades that hold an
injected list of these and delegate. `ShareTextService`'s dependency on
`ShareLineMapper` is unchanged. `ShareTextParser`'s public surface does
shrink — it collapses `identifyGame()` + twelve `extractXInfo()` methods
into one `parse()` method — which lets `ResultService.createResult()` lose
its `when`-block entirely (see Facades below). `ResultService`'s own public
API (`createResult`, etc.) is unchanged; this is still an internal refactor
from the caller's perspective.

A new test enumerates every known sample share text against every mapper and
asserts exactly one `matches()` — so list order can never matter again.

## Design

### Interface

```kotlin
// sh.zachwal.dailygames.results.gamemapper.GameMapper
interface GameMapper {
    val game: Game
    fun matches(shareText: String): Boolean
    fun extract(shareText: String, user: User): ParsedResult
    fun shareLine(result: PuzzleResult): String
}
```

`user` is threaded through `extract` (not injected) because only
`SizeItUpMapper` needs it (to resolve "today" in the user's timezone via
`Clock` + `UserPreferencesService`, both of which *are* injected into that
one implementation) — every other implementation ignores the parameter.

### Per-game implementations

One class per current `Game` enum value, in a new
`sh.zachwal.dailygames.results.gamemapper` package — one per entry in
`Game.kt` at the time this lands (a `CARDLE` mapper is being added in
parallel on this branch, so treat "the twelve games" language below as
illustrative, not a hard count):

`WorldleMapper`, `TradleMapper`, `TravleMapper`, `Top5Mapper`,
`FlagleMapper`, `PinpointMapper`, `GeocirclesMapper`, `FramedMapper`,
`GeoGridMapper`, `BandleMapper`, `BracketCityMapper`, `SizeItUpMapper`,
plus whatever `Game` enum values exist by the time this is implemented.

Each is a straight cut-and-paste merge of its existing
`identifyGame` branch + `extractXInfo` body (from `ShareTextParser`) and its
`toXShareLine` body (from `ShareLineMapper`). `FLAGLE`/`TRADLE`/`FRAMED`/
`PINPOINT`/`BANDLE` currently share one `toStandardShareLine()` — keep that
as a small shared helper (extension fun or abstract base) so it isn't
duplicated five times.

```kotlin
@Singleton
class WorldleMapper @Inject constructor() : GameMapper {
    override val game = Game.WORLDLE

    private val regex = Regex("""...""")

    override fun matches(shareText: String) = regex.matches(shareText)

    override fun extract(shareText: String, user: User): ParsedResult {
        // body of ShareTextParser.extractWorldleInfo, unchanged
    }

    override fun shareLine(result: PuzzleResult): String {
        // body of ShareLineMapper.toWorldleShareLine, unchanged
    }
}
```

`SizeItUpMapper` gets `Clock` and `UserPreferencesService` injected (moved
out of `ResultService`); `WorldleMapper`/standard mappers that need
`PointCalculator` for `shareLine` get it injected directly instead of it
living on the facade.

Per-game logic tests move with the code: each `XMapperTest.kt` replaces the
relevant slice of the current `ShareTextParser*Test.kt` /
`ShareLineMapperTest.kt` files (see Test suite below for how their fixtures
also feed the cross-mapper matching test).

### Wiring the list

Use Guice `Multibinder` — decided over a single growing `@Provides` method
so adding a mapper is one `addBinding()` line instead of editing a shared
method's parameter list (and signature order) every time:

```kotlin
// build.gradle.kts — needs adding, not currently a dependency
implementation("com.google.inject.extensions:guice-multibindings:4.2.3") // confirmed latest -- double-check compatibility
```

```kotlin
// ApplicationModule.kt
override fun configure() {
    val mapperBinder = Multibinder.newSetBinder(binder(), GameMapper::class.java)
    mapperBinder.addBinding().to(WorldleMapper::class.java)
    mapperBinder.addBinding().to(TradleMapper::class.java)
    // ... one addBinding() line per game
}
```

This still means touching `ApplicationModule` once per new game (Multibinder
doesn't do classpath auto-discovery) — we confirmed that's the intended
tradeoff over introducing a scanning/SPI mechanism this codebase has no
precedent for. `ShareTextParser`/`ShareLineMapper` then inject
`Set<GameMapper>` (Multibinder produces a `Set`, not a `List`) instead of a
`List<GameMapper>`.

### Facades

```kotlin
@Singleton
class ShareTextParser @Inject constructor(
    private val mappers: Set<GameMapper>,
) {
    fun parse(shareText: String, user: User): ParsedResult {
        val mapper = mappers.firstOrNull { it.matches(shareText) }
            ?: throw IllegalArgumentException("Share text could not be recognized as a valid game")
        return mapper.extract(shareText, user)
    }
}

@Singleton
class ShareLineMapper @Inject constructor(
    private val mappers: Set<GameMapper>,
) {
    fun mapToShareLine(result: PuzzleResult): String =
        mappers.first { it.game == result.game }.shareLine(result)
}
```

`identifyGame()` goes away rather than staying alongside `extract()` —
grepping the codebase, its only caller is `ResultService.createResult()`
(just to look the game up for an error-log line before immediately
re-deriving it via the `parseResult` `when`-block), so folding both steps
into one `parse()` call removes a redundant lookup, not just a `when`-block:

```kotlin
// ResultService.kt
fun createResult(user: User, shareText: String): PuzzleResult {
    val parsedResult = try {
        shareTextParser.parse(shareText, user)
    } catch (e: IllegalArgumentException) { // Note - throw a specific exception here and disambiguate between an unrecognized game and a specific IllegalArgumentException in one of the mappers.
        logger.error("Could not recognize $shareText as a valid game")
        throw e
    }

    val puzzle = getOrCreatePuzzle(Puzzle(parsedResult.game, parsedResult.puzzleNumber, parsedResult.date))
    // ... unchanged from here down
}
```

The private `parseResult(shareText, game, user)` when-block is deleted
entirely — `ParsedResult.game` (already on the data class) replaces the
separately-looked-up `game` local. `ResultService` also drops its `clock`
and `userPreferencesService` dependencies here, since those move into
`SizeItUpMapper` — keep them only if something else in the class still
needs them (worth a quick grep before deleting the constructor params).

`ShareTextService` is untouched — it already only calls
`shareLineMapper.mapToShareLine`.

### Test suite

Avoid discovering `XMapperTest` subclasses reflectively at runtime (fragile,
and ironically reintroduces an ordering/discovery problem in the test
suite itself). Instead make the fixture lists themselves the shared source
of truth: each game gets a plain (non-JUnit) fixtures object that both its
own test class and the cross-mapper test import directly.

```kotlin
// test fixtures, e.g. WorldleFixtures.kt
object WorldleFixtures {
    const val PERFECT = """..."""
    const val NON_PERFECT = """..."""
    val ALL = listOf(PERFECT, NON_PERFECT)
}
```

```kotlin
// shared base — asserts the contract every mapper must satisfy
abstract class GameMapperContractTest {
    abstract val mapper: GameMapper
    abstract val fixtures: List<String>
    private val testUser = /* a fixture User */

    @TestFactory
    fun `each fixture matches and extracts without throwing`() =
        fixtures.map { text ->
            DynamicTest.dynamicTest(text.lineSequence().first()) {
                assertThat(mapper.matches(text)).isTrue()
                mapper.extract(text, testUser) // must not throw (wrap in some assertDoesNotThrow utility)
            }
        }
}

// WorldleMapperTest.kt
class WorldleMapperTest : GameMapperContractTest() {
    override val mapper = WorldleMapper()
    override val fixtures = WorldleFixtures.ALL

    @Test
    fun `maps worldle perfect share line`() {
        // existing specific shareLine/extract-value assertions, unchanged
    }
}
```

```kotlin
// GameMapperMatchingTest.kt — the cross-product test
class GameMapperMatchingTest {
    private val mappers: List<GameMapper> = listOf(WorldleMapper(), TradleMapper(), /* ... */)
    private val allFixtures: List<String> =
        WorldleFixtures.ALL + TradleFixtures.ALL + /* ... one per game */

    @Test
    fun `every fixture matches exactly one mapper`() {
        allFixtures.forEach { text ->
            assertThat(mappers.filter { it.matches(text) }).hasSize(1)
        }
    }
}
```

This still means editing two lists (`mappers`, `allFixtures`) when a game is
added, same as the `GameMapper`/fixtures files themselves — acceptable
since `add-game` already touches a fixed set of files per new game; it's
the runtime `matches()`/`extract()` dispatch that no longer depends on
order, which was the actual bug class we're closing.

Existing `ShareTextParser*Test.kt` and `ShareLineMapperTest.kt` files are
deleted, replaced by the `XMapperTest.kt` files above plus
`GameMapperMatchingTest`.

## Migration steps

1. Add the `guice-multibindings` dependency and the `GameMapper` interface
   in a new `results/gamemapper` package.
2. Port one game at a time (start with a simple one, e.g. `Tradle`) to
   validate the pattern: new `XMapper` + `XFixtures` + `XMapperTest`,
   `addBinding()` in `ApplicationModule`; keep `ShareTextParser`/
   `ShareLineMapper` on the old code path meanwhile.
3. Once every `Game` enum value has a mapper, swap `ShareTextParser` and
   `ShareLineMapper` to the delegating `parse()`/`mapToShareLine()`
   implementations in the same commit, delete the old `when` bodies in both
   classes, and collapse `ResultService.createResult()`/`parseResult()` as
   shown above.
4. Add `GameMapperMatchingTest` and delete the old
   `ShareTextParser*Test.kt` / `ShareLineMapperTest.kt` files.
5. Update the `add-game` skill's file checklist: new steps are "add
   `XMapper` + `XFixtures` + `XMapperTest`", "add one `addBinding()` line in
   `ApplicationModule`", and "add `XFixtures.ALL` to `GameMapperMatchingTest`"
   — replacing the old checklist items for editing `ShareTextParser`,
   `ShareLineMapper`, and their respective test files.

## Open questions

- Should `matches()` for `ShareLineMapper`'s use case (dispatch by
  `result.game`, not by text) also go through `matches(shareText)`? No —
  keep the facades dispatching `shareLine` by `it.game == result.game`
  directly; only the text-identification path needs the regex-based
  `matches()`.
- `GeoGridMapper`/`BandleMapper`/`SizeItUpMapper` currently identify via
  `contains`/`startsWith` rather than a full regex — keep that; `matches()`
  doesn't have to be regex-based, just boolean.
