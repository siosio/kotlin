
object Host {
    operator fun plusAssign(s: String) {
        println(s)
    }
}

fun foo() = Host

fun test() {
    foo() += "123"
}