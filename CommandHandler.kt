class CommandHandler(bank: Bank) {
    var commands: Array<String> = arrayOf("Просмотр списка счетов.", "Открытие счета.", "Пополнение счёта.", "Перевод денег между счетами.", "Выход.")
    var bank: Bank

    init { this.bank = bank }

    fun Print_Comm() {
        var i = 1
        for (value in commands)
        {
            println("${i}) $value")
            i++
        }
    }

    fun Get_Command(): Status {
        var number: Int
        while (true) {
            try {
                number = readlnOrNull()?.toInt() ?: 0
                if (number !in 1..5) throw java.lang.NumberFormatException()
                break
            } catch (e: NumberFormatException) { println("Неверный ввод! Введите целое число в диапазоне от 1 до 5.") }
        }
        if(number == 1) return Status.Browse
        else if(number == 2) return Status.Open
        else if(number == 3) return Status.Refill
        else if(number == 4) return Status.Transfer
        else return Status.Exit
    }


    fun Get_Correct_Name(): String {
        println("Введите название счёта: ")
        var name = readlnOrNull()
        while (name!!.isEmpty()) {
            println("Имя не может быть пустым!")
            name = readlnOrNull()
        }
        return name
    }

    fun Get_Correct_Sum(): Float {
        println("Введите сумму пополнения: ")
        var result: Float
        while (true) {
            try {
                result = readlnOrNull()!!.toFloat()
                while (result <= 0) {
                    println("Некорректный ввод данных! Введите положительную сумму денег: ")
                    result = readlnOrNull()!!.toFloat()
                }
                break
            } catch (e: Exception) {  println("Ошибка! Введите число: ") }

        }
        return result
    }
}