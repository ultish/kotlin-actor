package custom

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.system.measureTimeMillis

sealed interface CounterMessage
object IncrementCounter : CounterMessage // one-way message to increment counter
object DecrementCounter : CounterMessage // one-way message to increment counter
class GetCounter(val response: CompletableDeferred<Int>) : CounterMessage // a request with reply


fun CoroutineScope.customCounterStateMachine(channel: ReceiveChannel<CounterMessage>) = launch {
    var counter = 0 // actor state
    channel.consumeEach { message ->
        ensureActive()
        when (message) {
            is IncrementCounter -> counter++
            is GetCounter -> message.response.complete(counter)
            DecrementCounter -> TODO()
        }
    }
}

fun main() = runBlocking<Unit> {
    val channel = Channel<CounterMessage>()
    val counterStateMachine =
        customCounterStateMachine(channel) // create the counterStateMachine actor
    withContext(Dispatchers.Default) {
        massiveRun {
            channel.send(IncrementCounter)
        }
    }
    // send a message to get the counter value from the counterStateMachine actor
    val response = CompletableDeferred<Int>()
    channel.send(GetCounter(response))
    val count = response.await()
    println("Counter = ${count}")
    channel.close() // shutdown the counterStateMachine actor
}

//Helper function to simulate a massive concurrent input of messages
suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100  // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        coroutineScope { // scope for coroutines
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("Completed ${n * k} actions in $time ms")
}