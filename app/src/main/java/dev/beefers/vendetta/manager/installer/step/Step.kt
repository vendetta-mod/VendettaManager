package dev.beefers.vendetta.manager.installer.step

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.koin.core.component.KoinComponent
import kotlin.time.measureTimedValue

@Stable
abstract class Step: KoinComponent {

    /**
     * Group this step belongs to
     */
    abstract val group: StepGroup

    /**
     * Label used in the installer ui
     */
    @get:StringRes
    abstract val nameRes: Int

    var status by mutableStateOf(StepStatus.QUEUED)
        protected set

    var progress by mutableStateOf<Float?>(null)
        protected set

    var durationMs by mutableIntStateOf(0)
        private set

    protected abstract suspend fun run(runner: StepRunner)

    suspend fun runCatching(runner: StepRunner): Throwable? {
        if (status != StepStatus.QUEUED)
            throw IllegalStateException("Cannot execute a step that has already started")

        status = StepStatus.ONGOING

        val (error, time) = measureTimedValue {
            try {
                run(runner)
                status = StepStatus.SUCCESSFUL
                null
            } catch (t: Throwable) {
                status = StepStatus.UNSUCCESSFUL
                t
            }
        }

        durationMs = time.inWholeMilliseconds.toInt()
        return error
    }

}