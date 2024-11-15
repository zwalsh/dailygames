package sh.zachwal.dailygames.results.resultinfo

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import sh.zachwal.dailygames.db.jdbi.puzzle.Game

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
