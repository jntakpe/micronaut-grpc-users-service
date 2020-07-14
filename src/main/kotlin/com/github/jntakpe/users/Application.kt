package com.github.jntakpe.users

import io.micronaut.runtime.Micronaut.build

fun main(args: Array<String>) {
    build()
        .args(*args)
        .packages("com.github.jntakpe")
        .start()
}

