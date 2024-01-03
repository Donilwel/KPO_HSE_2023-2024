import kotlin.system.exitProcess

val cinema = Cinema()
val user = UserAccount(status = "undefined")

fun main() {
    printlnColor("Welcome to the Cinema!", ConsoleColor.CYAN)
    println()
    processStartingMenu()
}

fun processStartingMenu() {
    printlnColor("Please choose an action.", ConsoleColor.GREEN)
    println("1. Sign in or sign up as a cinema customer")
    println("2. Sign in or sign up as a cinema employee")
    println("3. Continue as a guest customer")
    println("4. Exit the Cinema")
    printColor("Enter a number from 1 to 4 depending on the desired action: ", ConsoleColor.YELLOW)

    while (true) {
        val choice = readlnOrNull()?.toIntOrNull()
        println()

        when (choice) {
            1 -> {
                customerAuthorisation()
                break
            }

            2 -> {
                employeeAuthorisation()
                break
            }

            3 -> {
                processUserMenu()
                break
            }

            4 -> {
                printlnColor("Exiting the Cinema. Goodbye!", ConsoleColor.BLUE)
                cinema.saveData()
                exitProcess(0)
            }

            else -> {
                printlnColor("Invalid choice!", ConsoleColor.RED)
                printColor(
                    "Please enter a valid number from 1 to 3 depending on the desired action: ",
                    ConsoleColor.YELLOW
                )
            }
        }
    }
}

fun processUserMenu() {
    if (user.status == "employee") {
        processMenuForEmployee()
    } else {
        processMenuForCustomer()
    }
}

fun processMenuForCustomer() {
    printlnColor("Please choose an action.", ConsoleColor.GREEN)
    println("1. See the list of all movies in our cinema")
    println("2. See all showtimes for all movies")
    println("3. See all showtimes for specific movie")
    println("4. Buy a ticket")
    println("5. Return a ticket")
    println("6. Log out")
    println("7. Exit the Cinema")
    printColor("Enter a number from 1 to 7 depending on the desired action: ", ConsoleColor.YELLOW)

    while (true) {
        val choice = readlnOrNull()?.toIntOrNull()
        println()

        when (choice) {
            1 -> {
                cinema.displayAllMovies()
                println("\nPress enter to return back")
                readlnOrNull()
                processUserMenu()
                break
            }

            2 -> {
                cinema.displayAllShowtimes()
                println("\nPress enter to return back")
                readlnOrNull()
                processUserMenu()
                break
            }

            3 -> {
                cinema.displayAllShowtimesForMovie()
                println("\nPress enter to return back")
                readlnOrNull()
                processUserMenu()
                break
            }

            4 -> {
                cinema.buyTicket()
                break
            }

            5 -> {
                cinema.returnTicket()
                break
            }

            6 -> {
                processStartingMenu()
                break
            }

            7 -> {
                printlnColor("Exiting the Cinema. Goodbye!", ConsoleColor.BLUE)
                cinema.saveData()
                exitProcess(0)
            }

            else -> {
                printlnColor("Invalid choice!", ConsoleColor.RED)
                printColor(
                    "Please enter a valid number from 1 to 3 depending on the desired action: ",
                    ConsoleColor.YELLOW
                )
            }
        }
    }
}

fun processMenuForEmployee() {
    printlnColor("Please choose an action.", ConsoleColor.GREEN)
    println("1. See the list of all movies in our cinema")
    println("2. See all showtimes for all movies")
    println("3. See all showtimes for specific movie")
    println("4. Buy a ticket")
    println("5. Return a ticket")
    println("6. Edit movies info")
    println("7. Edit showtimes info")
    println("8. Check a ticket by controller")
    println("9. Show all tickets")
    println("10. Show all sold tickets")
    println("11. Log out")
    println("12. Exit the Cinema")
    printColor("Enter a number from 1 to 10 depending on the desired action: ", ConsoleColor.YELLOW)

    while (true) {
        val choice = readlnOrNull()?.toIntOrNull()
        println()

        when (choice) {
            1 -> {
                cinema.displayAllMovies()
                println("\nPress enter to return back")
                readlnOrNull()
                processUserMenu()
                break
            }

            2 -> {
                cinema.displayAllShowtimes()
                println("\nPress enter to return back")
                readlnOrNull()
                processUserMenu()
                break
            }

            3 -> {
                cinema.displayAllShowtimesForMovie()
                println("\nPress enter to return back")
                readlnOrNull()
                processUserMenu()
                break
            }

            4 -> {
                cinema.buyTicket()
                break
            }

            5 -> {
                cinema.returnTicket()
                break
            }

            6 -> {
                cinema.editMovie()
                break
            }

            7 -> {
                cinema.editShowtime()
                break
            }

            8 -> {
                cinema.checkTicket()
                break
            }

            9 -> {
                cinema.displayAllTickets()
                println("\nPress enter to return back")
                readlnOrNull()
                processUserMenu()
                break
            }

            10 -> {
                cinema.displayAllSoldTickets()
                println("\nPress enter to return back")
                readlnOrNull()
                processUserMenu()
                break
            }

            11 -> {
                processStartingMenu()
                break
            }

            12 -> {
                printlnColor("Exiting the Cinema. Goodbye!", ConsoleColor.BLUE)
                cinema.saveData()
                exitProcess(0)
            }

            else -> {
                printlnColor("Invalid choice!", ConsoleColor.RED)
                printColor(
                    "Please enter a valid number from 1 to 3 depending on the desired action: ",
                    ConsoleColor.YELLOW
                )
            }
        }
    }
}

fun printlnColor(text: String, color: ConsoleColor) {
    println("${color.code}$text${ConsoleColor.RESET.code}")
}

fun printColor(text: String, color: ConsoleColor) {
    print("${color.code}$text${ConsoleColor.RESET.code}")
}

enum class ConsoleColor(val code: String) {
    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m")
}