class BankAccount(name: String, balance: Float, number: Int) {
    var number: Int
    var name: String
    var balance: Float
    init {
        this.name = name
        this.balance = balance
        this.number = number
    }
    override fun toString(): String = "Номер счёта: $number, имя счёта: $name, баланс на счёте: $balance"
    override operator fun equals(other: Any?): Boolean {
        var flag: Boolean
        flag = false
        if (other is BankAccount) return this.balance == other.balance && this.name == other.name && this.number == other.number
        return flag
    }
}