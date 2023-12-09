import kotlin.Exception
enum class Status {
    Browse, Open, Refill, Transfer, Exit
}

fun main() {
    var bank = Bank()
    var handler = CommandHandler(bank)
    var status: Status
    do {
        handler.Print_Comm()
        status = handler.Get_Command()
        try {
            when (status) {
                Status.Browse -> bank.Load_Accounts()
                Status.Open -> {
                    val name = handler.Get_Correct_Name()
                    bank.Make_Account(name)
                }

                Status.Refill -> {
                    val account = bank.Get_Correct_Acount(true)
                    val sum = handler.Get_Correct_Sum()
                    bank.Refill_Account(account, sum)
                }

                Status.Transfer -> {
                    var pair = bank.getValidPairOfAccounts()
                    var src = pair.first
                    var dest = pair.second
                    while (true) {
                        try {
                            val sum = handler.Get_Correct_Sum()
                            bank.Import_Money(src, dest, sum)
                            break
                        } catch (e: Exception) {
                            println(e.message)
                        }
                    }
                }
                else -> {}
            }
        } catch (e: Exception) {
            println(e.message)
        }
    } while (status != Status.Exit)
}