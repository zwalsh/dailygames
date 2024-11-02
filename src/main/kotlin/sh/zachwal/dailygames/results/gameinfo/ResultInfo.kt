package sh.zachwal.dailygames.results.gameinfo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = FlagleInfo::class, name = "flagle"),
    JsonSubTypes.Type(value = GeocirclesInfo::class, name = "geocircles"),
    JsonSubTypes.Type(value = PinpointInfo::class, name = "pinpoint"),
    JsonSubTypes.Type(value = TradleInfo::class, name = "tradle"),
    JsonSubTypes.Type(value = Top5Info::class, name = "top5"),
    JsonSubTypes.Type(value = TravleInfo::class, name = "travle"),
    JsonSubTypes.Type(value = WorldleInfo::class, name = "worldle"),
)
sealed class ResultInfo

// TODO refactor ShareTextParser to return a ResultInfo object with a game-specific GameInfo object
// that is serializable. Then pull up common fields (puzzleNumber, score, shareTextNoLink) & drop serialization of
// common fields (score, date, etc.)
