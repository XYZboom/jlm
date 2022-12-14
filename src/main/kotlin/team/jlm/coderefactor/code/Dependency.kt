package team.jlm.coderefactor.code

class Dependency(val type: DependencyType, val dependencyText: String, val dependClassName:String?) {

    override fun toString(): String {
        return "Dependency(type=$type, dependencyText='$dependencyText', dependClassName='$dependClassName')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dependency

        if (type != other.type) return false
        if (dependencyText != other.dependencyText) return false
        if (dependClassName != other.dependClassName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + dependencyText.hashCode()
        result = 31 * result + dependClassName.hashCode()
        return result
    }
}