package soInlineFun

fun main(args: Array<String>) {
    //Breakpoint!
    simple()                           // 1

//    withParam(1 + a)
//
//    withLambda { "hi" }
}

inline fun simple() {
    foo()
}                 // 3

inline fun withParam(i: Int) {
}                                      // 4

inline fun withLambda(a: () -> Unit) {
    a()                                // 5
}                                      // 6

fun foo() {}

// STEP_OVER: 6