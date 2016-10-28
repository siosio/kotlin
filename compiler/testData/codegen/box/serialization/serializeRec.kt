import kotlin.serialization.KInput
import kotlin.serialization.KOutput
import kotlin.serialization.KSerializable
import kotlin.serialization.KSerialClassDesc
import kotlin.serialization.KSerialSaver
import kotlin.serialization.KSerialLoader

// Serializable data class

@KSerializable
data class Container(
        val box: Box
)

@KSerializable
data class Box(
        val value1: String,
        val value2: Int
)

fun box(): String {
    val out = Out()
    out.write(Container, Container(Box("s1", 42)))
    out.done()

    val inp = Inp()
    val box = inp.read(Container)
    inp.done()

    return "OK"
}

class Failure(s: String) : Throwable(s)

open class State {
    var step = 0

    fun fail(msg: String): Nothing = throw Failure("@$step: $msg")

    fun checkContainerDesc(desc: KSerialClassDesc) {
        if (desc.name != "Container") fail("checkContainerDesc name $desc")
        if (desc.getElementName(0) != "box") fail("checkContainerDesc $desc")
    }

    fun checkBoxDesc(desc: KSerialClassDesc) {
        if (desc.name != "Box") fail("checkBoxDesc name $desc")
        if (desc.getElementName(0) != "value1") fail("checkBoxDesc0 $desc")
        if (desc.getElementName(1) != "value2") fail("checkBoxDesc1 $desc")
    }
}

class Out() : State(), KOutput {
    override fun writeBegin(desc: KSerialClassDesc) {
        when(step) {
            0 -> { checkContainerDesc(desc); step++; return }
            2 -> { checkBoxDesc(desc); step++; return }
        }
        fail("writeBegin($desc)")
    }

    override fun <T> writeSerializableValue(desc: KSerialClassDesc, index: Int, value: T, saver: KSerialSaver<T>) {
        when (step) {
            1 -> { checkContainerDesc(desc); if (index == 0) { step++; saver.save(this, value); return } }
        }
        fail("writeValue($desc, $index, $value)")
    }

    override fun writeValue(desc: KSerialClassDesc, index: Int, value: Any?) {
        when (step) {
            3 -> { checkBoxDesc(desc); if (index == 0 && value == "s1") { step++; return } }
            4 -> { checkBoxDesc(desc); if (index == 1 && value == 42) { step++; return } }
        }
        fail("writeValue($desc, $index, $value)")
    }

    override fun writeEnd(desc: KSerialClassDesc) {
        when(step) {
            5 -> { checkBoxDesc(desc); step++; return }
            6 -> { checkContainerDesc(desc); step++; return }
        }
        fail("writeEnd($desc)")
    }

    fun done() {
        if (step != 7) fail("OUT FAIL")
    }
}

class Inp() : State(), KInput {
    override fun readBegin(desc: KSerialClassDesc): Boolean {
        when(step) {
            0 -> { checkContainerDesc(desc); step++; return true }
            3 -> { checkBoxDesc(desc); step++; return true }
        }
        fail("readBegin($desc)")
    }

    override fun readElement(desc: KSerialClassDesc): Int {
        when (step) {
            1 -> { checkContainerDesc(desc); step++; return 0 }
            4 -> { checkBoxDesc(desc); step++; return 0 }
            6 -> { checkBoxDesc(desc); step++; return 1 }
            8 -> { checkBoxDesc(desc); step++; return -1 }
            10 -> { checkContainerDesc(desc); step++; return -1 }
        }
        fail("readElement($desc)")
        return -1
    }

    override fun <T> readSerializableValue(desc: KSerialClassDesc, index: Int, loader: KSerialLoader<T>): T {
        when (step) {
            2 -> { checkContainerDesc(desc); if (index == 0) { step++; return loader.load(this) } }
        }
        fail("readValue($desc, $index)")
    }

    override fun readValue(desc: KSerialClassDesc, index: Int): Any? {
        when (step) {
            5 -> { checkBoxDesc(desc); if (index == 0) { step++; return "s1" } }
            7 -> { checkBoxDesc(desc); if (index == 1) { step++; return 42 } }
        }
        fail("readValue($desc, $index)")
    }

    override fun readEnd(desc: KSerialClassDesc) {
        when(step) {
            9 -> { checkBoxDesc(desc); step++; return }
            11 -> { checkContainerDesc(desc); step++; return }
        }
        fail("readEnd($desc)")
    }

    fun done() {
        if (step != 12) fail("INP FAIL")
    }
}
