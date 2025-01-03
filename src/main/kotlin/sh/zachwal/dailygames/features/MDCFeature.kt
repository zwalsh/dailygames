package sh.zachwal.dailygames.features

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.MDC

class MDCFeature(private val mdcProvider: (ApplicationCall) -> Map<String, String>) {

    class Configuration {
        var mdcProvider: (ApplicationCall) -> Map<String, String> = { emptyMap() }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, MDCFeature> {
        override val key = AttributeKey<MDCFeature>("MDCFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): MDCFeature {
            val configuration = Configuration().apply(configure)
            val feature = MDCFeature(configuration.mdcProvider)

            val beforePhase = PipelinePhase("Before")
            val afterPhase = PipelinePhase("After")

            pipeline.insertPhaseBefore(ApplicationCallPipeline.Call, beforePhase)
            pipeline.insertPhaseAfter(ApplicationCallPipeline.Call, afterPhase)

            pipeline.intercept(beforePhase) {
                val mdcValues = feature.mdcProvider(call)
                mdcValues.forEach { (key, value) ->
                    MDC.put(key, value)
                }
            }

            pipeline.intercept(afterPhase) {
                MDC.clear()
            }

            return feature
        }
    }
}