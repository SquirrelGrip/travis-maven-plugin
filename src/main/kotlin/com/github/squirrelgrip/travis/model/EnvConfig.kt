package com.github.squirrelgrip.travis.model

class EnvConfig(
        private val _jobs: Map<String, String> = emptyMap()
) {
    val jobs = _jobs.map { (key, value) ->
        "$key=$value"
    }
}
