import kotlin.serialization.KInput
import kotlin.serialization.KOutput
import kotlin.serialization.KSerializable
import kotlin.serialization.KSerialClassDesc

class Out() : KOutput {
    var step = 0
    var fail: String? = null

    override fun writeBegin(desc: KSerialClassDesc) {
        if (step == 0) step++ else if (fail == null) fail = "writeBegin"
    }

    override fun writeElement(desc: KSerialClassDesc, index: Int) {
        when (index) {
            0 -> if (step == 1) { step++; return }
            1 -> if (step == 3) { step++; return }
        }
        if (fail == null) fail = "writeElement($index)"
    }

    override fun writeValue(value: Any?) {
        when (step) {
            2 -> if (value == "s1") { step++; return }
            4 -> if (value == 42) { step++; return }
        }
        if (fail == null) fail = "writeValue($value)"
    }

    override fun writeEnd(desc: KSerialClassDesc) {
        if (step == 5) step++ else if (fail == null) fail = "writeEnd()"
    }

    fun done() = if (fail == null && step == 6) null else "OUT FAIL AT $step WITH $fail"
}

class Inp() : KInput {
    var step = 0
    var fail: String? = null

    override fun readBegin(desc: KSerialClassDesc) {
        if (step == 0) step++ else if (fail == null) fail = "readBegin"
    }

    override fun readElement(desc: KSerialClassDesc): Int {
        when (step) {
            1 -> { step++; return 0 }
            3 -> { step++; return 1 }
            5 -> { step++; return -1 }
        }
        if (fail == null) fail = "readElement"
        return -1
    }

    override fun readValue(): Any? {
        when (step) {
            2 -> { step++; return "s1" }
            4 -> { step++; return 42 }
        }
        if (fail == null) fail = "readValue"
        return null
    }

    override fun readEnd(desc: KSerialClassDesc) {
        if (step == 6) step++ else if (fail == null) fail = "readEnd"
    }

    fun done() = if (fail == null && step == 7) null else "INP FAIL AT $step WITH $fail"
}

@KSerializable
class Box(p1: String, p2: Int) {
    var value1 : String = p1
    var value2 : Int = p2
}

fun box() : String {
    val out = Out()
    out.write(Box("s1", 42), Box)
    var done = out.done()
    if (done != null) return done

    val inp = Inp()
    val box = inp.read(Box)
    done = inp.done()
    if (done != null) return done

    return "OK"
}
