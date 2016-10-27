// MODULE: lib
// FILE: lib.kt
package lib

@native fun bar()

val bar = 32

// FILE: lib.js
function bar() {
    return 23;
}

// MODULE: main(lib)
// FILE: main.kt
package main

import lib.*

fun box(): String {
    assertEquals(23, bar())
    assertEquals(32, bar)

    return "OK"
}