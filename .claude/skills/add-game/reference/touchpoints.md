# Every place a game touches

Line numbers are from the commit that added Bracket City and drift as files grow —
`rg -n "BRACKET_CITY"` to find the current ones. All Kotlin paths are relative to
`src/main/kotlin/sh/zachwal/dailygames/`.

## The submission path

```
POST /                                     home/HomeController.kt:46   reads the shareText form param
  └─ ResultService.createResult            results/ResultService.kt:44
       ├─ ShareTextParser.parse            results/ShareTextParser.kt:14   finds the matching GameMapper, extracts
       ├─ PuzzleDAO.getOrCreate                                            row in `puzzle` (game, number, date)
       └─ PuzzleResultDAO.insertResult                                     row in `result`, result_info as jsonb
```

Everything downstream — the home feed, leaderboards, streaks, wrapped, nav — reads
`PuzzleResult` and `Game` generically.

Per-game text detection, extraction, and share-line rendering all live in one
`GameMapper` implementation per game, in `results/gamemapper/`. `ShareTextParser` and
`home/ShareLineMapper` are thin facades that pick the right mapper out of an injected
`Set<GameMapper>` — see `results/gamemapper/GameMapper.kt`.

## Checklist

| Where | What to add | Compiler catches it? |
|---|---|---|
| `db/jdbi/puzzle/Game.kt:5` | The enum constant | — this is what forces the rest |
| `db/jdbi/puzzle/Game.kt:19` `displayName()` | Override, only if sentence case is wrong | **No** — has `else -> toSentenceCase()` |
| `db/jdbi/puzzle/Game.kt:28` `emoji()` | The everyday emoji | Yes |
| `db/jdbi/puzzle/Game.kt:44` `perfectEmoji()` | The perfect-score emoji | Yes |
| `db/jdbi/puzzle/Game.kt:60` `href()` | Link to the game | Yes |
| `db/NN_add_<game>.json` (new) | Liquibase changeset inserting one row into `game` | **No** |
| `db/changelog.json` | `include` entry for that file | **No** |
| `results/gamemapper/<Game>Mapper.kt` (new) | `class <Game>Mapper : GameMapper` — `matches()`, `extract()`, `shareLine()` | **No** |
| `results/resultinfo/<Game>Info.kt` (new) | `data class ... : ResultInfo()`, or an `object` with `equals` if stateless | **No** |
| `results/resultinfo/ResultInfo.kt:11` | `JsonSubTypes.Type(value = <Game>Info::class, name = "<game>")` | **No** — throws at (de)serialization |
| `guice/ApplicationModule.kt` | One `mapperBinder.addBinding().to(<Game>Mapper::class.java)` line | **No** — silently unrecognized without it |
| `leaderboard/PointCalculator.kt:14` `calculatePoints` | Join a group, or a new branch | Yes |
| `leaderboard/PointCalculator.kt:36` `maxPoints` | Join a group, or a new branch | Yes |
| `leaderboard/LeaderboardService.kt:45` | `BasicScoreHintView("Scoring: ...")` | Yes |
| `leaderboard/views/ScoreHintView.kt:11` | A subclass, only if the hint needs markup | — rarely needed |
| `home/HomeService.kt:27` `hiddenGames` | Only if the game should be hidden | **No** — visible by default |
| `answers/AnswerService.kt:16` | Branch + a `GameAnswerService`, only if you want answer reveals | **No** — `else -> null` at line 20 |

Nothing to do in `chat/`, `wrapped/`, `nav/`, `home/StreakService.kt`,
`home/ShareTextService.kt`, or any DAO — they all iterate `Game.values()`.

**There is no frontend work.** Nothing under `src/main/resources/static/` names any game;
all of it is server-rendered Kotlin HTML DSL.

## Tests

| File | What to add |
|---|---|
| `results/gamemapper/<Game>Fixtures.kt` (new) | The sample share texts as top-level `const val`s collected into `ALL` — the single source of truth other test files import from |
| `results/gamemapper/<Game>MapperTest.kt` (new) | Extends `GameMapperContractTest`; add specific `extract()`/`shareLine()` value assertions per sample |
| `results/gamemapper/TestGameMappers.kt` | Add the new mapper to `allGameMappers()` |
| `results/gamemapper/GameMapperMatchingTest.kt` | Add `<Game>Fixtures.ALL` to `allFixtures` |
| `results/ResultServiceTest.kt` | One end-to-end `can create a <Game> result` case |
| `leaderboard/PointCalculatorTest.kt` | A fixture plus assertions at the score boundaries |
| `results/resultinfo/SerializePuzzleResultInfoTest.kt` | Add the new info type to `resultInfoList()` |
| `results/resultinfo/DeserializeStoredPuzzleResultInfoTest.kt` | Add a literal stored-JSON case to `arguments()` |

Declare the sample share texts once, as top-level `const val`s in `<Game>Fixtures.kt`,
and import `<Game>Fixtures.ALL` by name into the other test files. That's how the
existing games do it.

## Templates

### Migration — `db/26_add_<game>.json`

```json
{
  "databaseChangeLog": [
    {
      "changeSet": {
        "id": "26",
        "author": "zach",
        "changes": [
          {
            "insert": {
              "tableName": "game",
              "columns": [
                { "column": { "name": "name", "value": "YOUR_GAME" } },
                { "column": { "name": "instant_created", "valueComputed": "now()" } }
              ]
            }
          }
        ]
      }
    }
  ]
}
```

The `value` must match the enum constant name exactly — `utils/KtorUtils.kt` turns URL
path segments back into games with `Game.valueOf(it.uppercase())`.

No schema change is needed. Results live in one generic `result` table with a
`result_info jsonb` column. The per-game tables you'll see in migrations 6–9
(`travle_result` and friends) were dropped in migration 19; don't copy them.

### A parser that uses one regex with named groups

Best when the share text has a stable single-line header.

```kotlin
val worldleRegex = Regex(
    """\s*#Worldle\s+#(?<puzzleNumber>\d+)\s+\((?<day>\d{2})\.(?<month>\d{2})\.(?<year>\d{4})\)\s+(?<score>\S)/6\s+\((?<percentage>\d+)%\)[\s\S]*""",
)

fun extractWorldleInfo(shareText: String): ParsedResult {
    val match = worldleRegex.find(shareText) ?: throw IllegalArgumentException("Share text is not a Worldle share")
    val (puzzleNumber, day, month, year, score, percentage) = match.destructured
    return ParsedResult(
        puzzleNumber = puzzleNumber.toInt(),
        game = Game.WORLDLE,
        date = LocalDate.of(year.toInt(), month.toInt(), day.toInt()),
        score = score.toIntOrNull() ?: 7, // X / 6 scored as 7 points
        shareTextNoLink = shareText.substringBefore("https://").trim(),
        resultInfo = WorldleInfo(percentage = percentage.toInt()),
    )
}
```

Note the `[\s\S]*` tail: `identifyGame` calls `.matches()`, which must match the *whole*
string, so an anchored regex needs to swallow the rest of the share text.

### A parser that slices strings and tallies emoji

Best when the interesting data is spread over several lines, or lines are optional.

```kotlin
private val skipRegex = Regex("⬛")
private val correctBandRegex = Regex("🟨")
private val incorrectRegex = Regex("🟥")

fun extractBandleInfo(shareText: String): ParsedResult {
    val puzzleNumber = shareText.substringAfter("Bandle #").substringBefore(" ").trim().toInt()
    val scoreText = shareText.trim().lines().first().substringBefore("/").substringAfterLast(" ")
    val score = if (scoreText == "x") 7 else scoreText.toInt()
    return ParsedResult(
        puzzleNumber = puzzleNumber,
        game = Game.BANDLE,
        date = null,
        score = score,
        shareTextNoLink = shareText.substringBefore("Found").trim(),
        resultInfo = BandleInfo(
            numSkips = skipRegex.findAll(shareText).count(),
            numCorrectBand = correctBandRegex.findAll(shareText).count(),
            numIncorrect = incorrectRegex.findAll(shareText).count(),
        ),
    )
}
```

For optional lines, use `regex.find(shareText)?...?: default` rather than throwing —
Bracket City's "Peeks" and "Answers Revealed" lines are absent on some ranks.

### The standard share line

`results/gamemapper/StandardShareLine.kt` has a shared `PuzzleResult.toStandardShareLine(pointCalculator)`
extension used by `WorldleMapper`, `TradleMapper`, `FlagleMapper`, `PinpointMapper`,
`FramedMapper` and `BandleMapper`'s `shareLine()` implementations:

```kotlin
fun PuzzleResult.toStandardShareLine(pointCalculator: PointCalculator): String {
    val gameAndPuzzle = "${game.emoji()} ${game.displayName()} #$puzzleNumber"
    val maxPoints = pointCalculator.maxPoints(this)
    if (score == maxPoints + 1) {
        return "$gameAndPuzzle X/$maxPoints"
    }
    val line = "$gameAndPuzzle $score/$maxPoints"
    return if (score == 1) "$line ${game.perfectEmoji()}" else line
}
```

If your mapper's `shareLine()` doesn't join this group, remember the perfect emoji
yourself — GeoGrid shipped without it and needed a follow-up commit.

## Worked examples in history

| Game | Commit | Worth reading for |
|---|---|---|
| Bracket City | `6d8d315` | No puzzle number (synthesised `YYYYMMDD`), optional lines, custom share line |
| Bandle | `91305d1` | The minimal case — joined every existing group, only the parser was new |
| GeoGrid | `fedb3b9` | Two different "scores", string-slicing parser |
| Travle | — | The hard case: per-puzzle par computed in `PointCalculator`, negative scores for did-not-finish, custom `ScoreHintView` |

`git log --oneline --all --grep GeoGrid` still shows PR #117's pre-squash commits, which
are the nine-step sequence in `SKILL.md` as it was actually worked.
