package cz.cvut.fit.vwm.collaborativefiltering

import kotlinext.js.jsObject
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import kotlin.coroutines.*
import kotlin.js.Promise

internal val Event.inputValue: String
    get() = (target as? HTMLInputElement)?.value ?: (target as? HTMLTextAreaElement)?.value ?: ""


suspend fun <T> Promise<T>.await() = suspendCoroutine<T> { cont ->
    then({ value -> cont.resume(value) },
            { exception -> cont.resumeWithException(exception) })
}

fun <T> async(block: suspend () -> T): Promise<T> = Promise<T> { resolve, reject ->
    block.startCoroutine(object : Continuation<T> {
        override fun resumeWith(result: Result<T>) {
            if (result.exceptionOrNull() != null) {
                reject(result.exceptionOrNull()!!)
            } else if (result.getOrNull() != null) {
                resolve(result.getOrNull()!!)
            }
        }

        override val context: CoroutineContext get() = EmptyCoroutineContext
    })
}

fun launch(block: suspend () -> Unit) {
    async(block).catch { exception -> console.log("Failed with $exception") }
}

inline fun js(builder: dynamic.() -> Unit): dynamic = jsObject(builder)

fun jsstyle(builder: dynamic.() -> Unit): String = js(builder)
