package com.chingis.runtime

import com.chingis.error.RuntimeError

class Environment(private val parent: Environment? = null) {
    private val vars = linkedMapOf<String, Value>()

    fun get(name: String, line: Int): Value =
        vars[name] ?: parent?.get(name, line) ?: throw RuntimeError(line, "undefined variable '$name'")

    fun set(name: String, value: Value) {
        if (!assignExisting(name, value)) vars[name] = value
    }

    // walk up and update the first scope that owns 'name'; return false if not found
    private fun assignExisting(name: String, value: Value): Boolean {
        if (vars.containsKey(name)) { vars[name] = value; return true }
        return parent?.assignExisting(name, value) ?: false
    }

    fun defineLocal(name: String, value: Value) { vars[name] = value }

    // ordered entries of THIS scope only (for printing globals)
    fun locals(): List<Pair<String, Value>> = vars.entries.map { it.key to it.value }
}
