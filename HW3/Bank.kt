import java.lang.Exception

class Bank {
    private var accounts: ArrayList<BankAccount> = arrayListOf()
    private var accountCounter = 1

    fun Load_Accounts() {
        if (accounts.isEmpty()) throw Exception("Ошибка! Нет открытых счетов")
        println("Список открытых счетов: ")
        for (account in accounts) println(account.toString())
    }

    fun Make_Account(name: String) { accounts.add(BankAccount(name, 0.0f, accountCounter++)) }
    fun Refill_Account(account: BankAccount, sum: Float) { account.balance += sum }
    fun Import_Money(src: BankAccount, dest: BankAccount, money: Float) {
        if (money > src.balance) throw Exception("Ошибка! Недостаточно средств для списания")
        dest.balance += money
        src.balance -= money
    }

    fun Get_Correct_Acount(isBrowseAccounts: Boolean): BankAccount {
        if (accounts.isEmpty()) throw Exception("Ошибка! Нет открытых счетов")
        if (isBrowseAccounts) Load_Accounts()
        println("Введите номер счета: ")
        var index: Int
        while (true) {
            try {
                index = readlnOrNull()!!.toInt()
                while (index !in 1..accounts.size) {
                    println("Ошибка! Введите верный номер счёта")
                    index = readlnOrNull()!!.toInt()
                }
                break
            } catch (e: Exception) {
                println("Ошибка! Введите верный номер счёта")
            }
        }

        return accounts[index - 1]
    }

    fun getValidPairOfAccounts(): Pair<BankAccount, BankAccount> {
        if (accounts.size == 1) throw Exception("Ошибка! Открыт всего 1 счёт")
        if (!containsAtLeastOneValidAccount()) throw Exception("Ошибка! Должен быть хотя бы 1 счёт с положительным балансом")
        Load_Accounts()

        println("Счёт списания. ")
        var src = Get_Correct_Acount(false)
        while (src.balance == 0.0f) {
            println("Ошибка! Счёт списания должен иметь положительный баланс")
            print("Счёт списания: ")
            src = Get_Correct_Acount(false)
        }

        println("Счёт зачисления: ")
        var dest = Get_Correct_Acount(false)
        while (src == dest) {
            println("Ошибка! Счёт зачисления не должен совпадать со счетом списания")
            println("Счёт зачисления: ")
            dest = Get_Correct_Acount(false)
        }

        return Pair(src, dest)
    }

    private fun containsAtLeastOneValidAccount(): Boolean {
        for (account in accounts) if (account.balance > 0) return true
        return false
    }
}
