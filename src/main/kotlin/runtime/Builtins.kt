package com.chingis.runtime

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

object Builtins {
    fun register(env: Environment) {
        fun native(arity: Int, body: (List<Value>, Int, Int) -> Value) = Value.Native(arity, body)

        env.defineLocal("print", native(1) { args, _, _ ->
            println(args[0])
            args[0]
        })
        env.defineLocal("abs", native(1) { args, line, col ->
            Value.Num(abs(args[0].toNum(line, col)))
        })
        env.defineLocal("floor", native(1) { args, line, col ->
            Value.Num(floor(args[0].toNum(line, col)))
        })
        env.defineLocal("sqrt", native(1) { args, line, col ->
            Value.Num(sqrt(args[0].toNum(line, col)))
        })
    }
}
