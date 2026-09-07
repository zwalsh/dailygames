---
name: add-game
description: Add a new daily puzzle game (Wordle-like) to the Daily Games app end to end — interview the user for the game's rules, emojis and sample share texts, then land the enum, migration, parser, result info, scoring and share line across nine small commits. Use when the user wants to add, support, or track a new game.
---

# Adding a new game

A "game" here is a daily web puzzle that produces a share text the user pastes into
Daily Games. Supporting one means teaching the app to recognise that share text, pull
numbers out of it, score it, and render it on the home feed and leaderboard.

Everything a game needs lives in ten files. `reference/touchpoints.md` lists all of them
with current line numbers, which ones the compiler will force you to update, and which
ones fail silently. Read it before you start.

## 1. Interview the user first

Do not start writing code with an incomplete picture. Ask all of this up front, in one
message, and do not guess at answers the user has not given:

**Identity**
- What is the game called, and how should the name be displayed? (`displayName()` falls
  back to sentence case of the enum name, so `GEOGRID` would render as "Geogrid" — ask
  whether that's right, or whether it needs an override like "GeoGrid" or "Top 5".)
- What is the URL people play it at?
- What emoji represents the game in a share line? What emoji marks a perfect score?
  Suggest a couple of options each and let the user pick — they are the one who has to
  look at these every day.

**Scoring**
- What does the score mean? What is the best possible result, and what is the worst?
- What counts as "perfect"?
- How should it map onto leaderboard points? Every existing game either scores
  `maxPoints + 1 - score` (guess-based games), or scores raw points, or needs its own
  formula. Ask which shape this game is.
- What one-line explanation should appear on the leaderboard page?

**Share text — this is where the work actually is**
- Ask for **as many real share texts as they can give you, and specifically ask for the
  weird ones**: a perfect result, a total failure, a middling result, and every unusual
  mode, rank, difficulty or bonus the game can produce.
- Does the share text contain a puzzle number? A date? Neither?
- Which lines are optional — do they disappear on some results?

The last two bullets matter more than anything else on this page. Bracket City shipped
with a bug because nobody had seen a "Puppetmaster" result, whose grid uses a purple
square the regex didn't include; it took a follow-up commit to fix. Bandle needed a
fourth sample before anyone noticed that a *skipped* guess (⬛) is a different character
from a *wrong* guess (🟥). If the user gives you three share texts, ask for more.

Paste each sample back to the user labelled with what you think it represents
("this is the failure case, right?") before you write a regex against it.

## 2. Work in nine commits

The three most recent games were all built the same way, and the pre-squash commits are
still in history — `git log --oneline --all --grep "GeoGrid"` shows the shape. Follow it.
Each step compiles and each step is one commit.

1. **Enum, emojis, link.** Add the constant to `Game.kt` and fill in `emoji()`,
   `perfectEmoji()`, `href()`, and `displayName()` if it needs an override.
2. **Migration.** `db/NN_add_<game>.json` inserting one row into the `game` table, plus
   an `include` entry in `db/changelog.json`.
3. **`TODO()` branches.** Add the enum constant to every exhaustive `when` the compiler
   complains about, with `TODO()` as the body. This is the trick that makes the next six
   steps safe: the compiler enumerates the work for you. Commit once it builds.
4. **Identify.** Add the detection branch to `ShareTextParser.identifyGame`.
5. **Extract, and test the perfect case.** Write `extract<Game>Info`, the `<Game>Info`
   class, and the `@JsonSubTypes` registration. Test against the perfect sample.
6. **Test the other samples.** One test per sample share text the user gave you.
7. **Insert results.** Replace the `TODO()` in `ResultService.parseResult`, and add an
   end-to-end `ResultServiceTest` case.
8. **Points.** Replace the `TODO()`s in `PointCalculator` and `LeaderboardService`, with
   tests covering the score boundaries.
9. **Share line.** Replace the `TODO()` in `ShareLineMapper`, with `ShareLineMapperTest`
   cases for at least the perfect and failure results.

Run `./gradlew ktlintFormat && ./gradlew build` before each commit. Do not leave a
`TODO()` in a commit you consider finished — `TODO()` compiles fine and throws
`NotImplementedError` in production. Step 3 is the only commit allowed to contain one.

## 3. Delegate the investigation

Send Sonnet sub-agents for the reading; do the writing yourself. Useful splits:

- **Regex design.** Give one agent the full set of sample share texts and ask it to
  report which fields are present in every sample, which are optional, which characters
  vary, and what a regex would have to tolerate. Ask for the union of every emoji that
  appears in any grid position — that is exactly the check Bracket City missed.
- **Precedent hunt.** Ask an agent which existing game is closest in structure to this
  one, and to report that game's parser, `PointCalculator` branch, `ScoreHintView` and
  `ShareLineMapper` function verbatim. Reuse beats invention here (see §4).
- **Post-implementation sweep.** After step 9, send an agent to `rg -i` your new game's
  name across the repo and compare the hits against `reference/touchpoints.md`, reporting
  anything on the list with no corresponding hit.

Give each agent the sample share texts inline — they cannot see your conversation.

## 4. Prefer joining a group over writing a new function

Most games do not need any new code outside the parser. Bandle added a game with zero
new functions in `PointCalculator`, `LeaderboardService` or `ShareLineMapper` — it just
joined the existing `WORLDLE, TRADLE, FLAGLE, FRAMED ->` arms in each. Check whether
yours can do the same before writing a bespoke branch:

- **Points.** If the game is "N guesses, fewer is better, X means failed", join the
  `maxPoints(result) + 1 - score` group. If it's "one point per correct answer", join the
  `-> score` group.
- **Share line.** If the line should read `<emoji> <Name> #123 4/6`, join
  `toStandardShareLine()`. It already handles the `X/6` failure form and appends the
  perfect emoji at `score == 1`.
- **Scoring hint.** Use `BasicScoreHintView("...")`. Only subclass `ScoreHintView` if the
  text needs a hyperlink, which is the sole reason `TravleScoreHintView` exists.

Write a custom branch when the game genuinely differs — Travle's par is per-puzzle and
has to be reverse-engineered from the result, so its point logic lives in private
extension functions on `TravleInfo` inside `PointCalculator`. That's the escape hatch,
not the default.

## 5. Conventions that are easy to get wrong

**The `@JsonSubTypes` name is permanent.** It is written into the `type` field of every
`result_info` JSON blob in the database. Pick it when you create the class — lowercase,
snake_case if multi-word (`bracket_city`) — and never change it afterwards. `FramedInfo`
is registered under the PascalCase name `FramedInfo` because it was renamed after rows
already existed and had to be reverted; don't add a second exception.

**Result info shape.** Use a `data class` if the game has fields worth storing. If it has
none, use a Kotlin `object` with a manual `equals` override, like `FlagleInfo`. Store the
raw facts you parsed, not derived values.

**`ParsedResult.score` is the scoring score.** It is what `PointCalculator` consumes, and
it is not always the number printed in the share text. GeoGrid stores `numCorrect` (0–9)
in `ParsedResult.score` and keeps the game's own `Score: 123.3` metric in a separate
`GeoGridInfo.score` field for display only. If your game has two numbers, be explicit
about which is which.

**Failure sentinel.** Guess-based games encode "did not solve" as `maxPoints + 1` — an
`x/6` share becomes score 7. Follow that if your game fits the shape.

**No puzzle number?** Bracket City synthesises one from the date as `YYYYMMDD`
(`year * 10000 + month * 100 + day`). Do the same.

**No date?** Pass `date = null`. Most games do.

**`shareTextNoLink`.** Check which side of the URL the content is on. Most games put the
link last, so `substringBefore("https://")` is right — Bracket City puts it in the middle
and needs `substringAfter(...)`, which was a shipped bug.

**Migration numbering.** The changeset `id` inside the JSON and the number in the
filename have drifted apart historically. Match the filename to the next file number and
give the changeset an unused id; don't assume they agree.

**Identification order.** `identifyGame` is a first-match-wins guard chain with no
ambiguity detection. Prefer an anchored `Regex(...).matches(shareText)` over a loose
`contains`, and check that your pattern doesn't swallow an existing game's share text —
add an assertion to the existing parser tests if there's any doubt.

## 6. Before you open the PR

- `./gradlew ktlintFormat && ./gradlew build` is clean.
- No `TODO()` remains for the new game.
- Add the new info type to `SerializePuzzleResultInfoTest.resultInfoList()` and
  `DeserializeStoredPuzzleResultInfoTest.arguments()`. These are hand-maintained lists,
  nothing enforces them, and several recent games are missing from both. The
  deserialization test is the only thing that would catch a broken `@JsonSubTypes` name.
- Check whether adding the game broke a sibling test. Once GeoGrid's share line gained a
  trailing perfect emoji, an existing assertion had to loosen from `endsWith("(200.6)")`
  to `contains("(200.6)")` — anything asserting on the end of a share line is fragile.
- Ask the user whether the game should start in `HomeService.hiddenGames`.
- Ask whether it needs an answer reveal. `AnswerService` has an `else -> null`, so a new
  game silently gets none; only Worldle, Tradle and Flagle have implementations.
- Walk the checklist in `reference/touchpoints.md` one row at a time.

Then open the PR. Title it after the game, matching the existing style: `Bandle game!`,
`Bracket city!`.
