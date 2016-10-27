package soInlineAnonymousFunctionArgumentDex

fun main(args: Array<String>) {
    //Breakpoint!
    val b = 1                              // 1

    foo(                                   // 2
            fun (){ test(1) }              // 3
    )

    foo(fun (){ test(1) })                 // 4 5
}                                          // 6

inline fun foo(f: () -> Unit) {
    f()
}

fun test(i: Int) = 1

// STEP_OVER: 10