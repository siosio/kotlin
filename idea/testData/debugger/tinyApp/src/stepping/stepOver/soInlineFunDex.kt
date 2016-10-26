package soInlineFunDex

fun main(args: Array<String>) {
    //Breakpoint!
    val a = 1                          // 1

    simple()                           // 2

    withParam(1 + a)

    withLambda { "hi" }
}

inline fun simple() {}                 // 3

inline fun withParam(i: Int) {
}                                      // 4

inline fun withLambda(a: () -> Unit) {
    a()                                // 5
}                                      // 6

// STEP_OVER: 6