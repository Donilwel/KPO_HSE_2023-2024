import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.io.File
import java.security.MessageDigest
import java.util.*
import java.nio.charset.StandardCharsets

class UserAccount(val encryptedEmail: String = "", val encryptedPassword: String = "", var status: String)

fun encryptData(data: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val hashedBytes = md.digest(data.toByteArray(StandardCharsets.UTF_8))

    return Base64.getEncoder().encodeToString(hashedBytes)
}

open class Account {
    private val gson = Gson()
    open val status: String = "undefined status"
    fun saveAccount(account: UserAccount) {
        val accountsJson = File("src/userAccounts.json")
        val existingAccounts = if (accountsJson.exists()) {
            gson.fromJson(accountsJson.readText(), Map::class.java)
        } else {
            emptyMap<String, UserAccount>()
        }

        val updatedAccounts = existingAccounts + (account.encryptedEmail to account)
        val updatedJson = gson.toJson(updatedAccounts)


        accountsJson.writeText(updatedJson)
    }

    open fun createAccount(email: String, password: String) {
        val encryptedEmail = encryptData(email)
        val encryptedPassword = encryptData(password)

        println("Warning! Unspecified account created...")
        val account = UserAccount(encryptedEmail, encryptedPassword, "unspecified")

        // Save the account to userAccounts.json
        saveAccount(account)
    }
}

class EmployeeAccount : Account() {
    override val status: String = "employee"
    override fun createAccount(email: String, password: String) {
        val encryptedEmail = encryptData(email)
        val encryptedPassword = encryptData(password)

        val account = UserAccount(encryptedEmail, encryptedPassword, "employee")

        // Save the account to userAccounts.json
        saveAccount(account)
    }
}

    class CustomerAccount : Account() {
    override val status: String = "customer"
    override fun createAccount(email: String, password: String) {
        val encryptedEmail = encryptData(email)
        val encryptedPassword = encryptData(password)

        val account = UserAccount(encryptedEmail, encryptedPassword, "customer")

        // Save the account to userAccounts.json
        saveAccount(account)

        printlnColor("Your account has been successfully created!", ConsoleColor.GREEN)
    }
}

fun customerAuthorisation() {
    val customer = CustomerAccount()

    println("Let's create a new account for you or log in to an existing one!")
    printlnColor("If you want to return to main menu just type 'exit'.", ConsoleColor.BLACK)
    print("Enter your email: ")
    val email = readlnOrNull() ?: ""

    if (email.trim().lowercase(Locale.getDefault()) == "exit") {
        println()
        processStartingMenu()
        return
    } else {
        signUpOrSignIn(email, customer)
    }
    printlnColor("Now you can access all the functionality of our cinema!", ConsoleColor.BLUE)
    println()
    user.status = "customer"
    processUserMenu()
}

fun employeeAuthorisation() {
    val employee = EmployeeAccount()

    println("Let's create a new account for you or log in to an existing one!")
    println("As you want to sign in or sign up as an employee I need you to provide me a secret code or type 'exit' to return to main menu.")
    while (true) {
        printlnColor("Just type '1234':)", ConsoleColor.BLACK)
        printColor("Enter secret code: ", ConsoleColor.YELLOW)
        val secretCode = readlnOrNull() ?: ""
        if (secretCode.trim().lowercase(Locale.getDefault()) == "exit") {
            println()
            processStartingMenu()
            return
        }
        if (secretCode.trim().lowercase(Locale.getDefault()) == "1234") {
            printlnColor("Access granted!", ConsoleColor.GREEN)
            break
        } else {
            printlnColor("Wrong code!\nTry again or type 'exit' to return to main menu", ConsoleColor.RED)
        }
    }
    print("Enter your email: ")
    val email = readlnOrNull() ?: ""

    if (email.trim().lowercase(Locale.getDefault()) == "exit") {
        println()
        processStartingMenu()
        return
    } else {
        signUpOrSignIn(email, employee)
    }
    printlnColor("Now you can access all the functionality of our cinema!", ConsoleColor.BLUE)
    println()
    user.status = "employee"
    processUserMenu()
}

fun signUpOrSignIn(email: String, user: Account) {
    val accountsJson = File("src/userAccounts.json")
    val accounts = loadAccounts()
    val hashedEnteredEmail = encryptData(email)
    val gson = Gson()

    if (doesEmailExist(email)) {
        // The email already exists, prompt for password
        printlnColor("You already have an account!", ConsoleColor.BLUE)

        // Выставляем актуальный статус юзера в json с данными о юзерами. То есть если юзер когда-то создал аккаунт в качестве клиенте, а потом залогинился как сотрудник, то статус станет = employee, и наоборот.
        accounts[hashedEnteredEmail]?.status = user.status
        val updatedJson = gson.toJson(accounts)
        accountsJson.writeText(updatedJson)

        while (true) {
            printColor("Enter your password: ", ConsoleColor.YELLOW)
            val enteredPassword = readlnOrNull() ?: ""

            if (isPasswordCorrect(email, enteredPassword)) {
                // Password is correct, log in
                printlnColor("Logged in successfully!", ConsoleColor.GREEN)
                break
            } else {
                // Incorrect password, ask for a repeat
                printlnColor("Incorrect password. Please try again.", ConsoleColor.RED)
            }
        }
    } else {
        // The email doesn't exist, create a new account
        printColor("You don't have an account yet. Let's create one!\nCreate your password: ", ConsoleColor.BLUE)
        val password = readlnOrNull() ?: ""

        user.createAccount(email, password)
    }
}

fun doesEmailExist(email: String): Boolean {
    // Hash the entered email
    val hashedEnteredEmail = encryptData(email)

    // Load existing accounts from userAccounts.json
    val existingAccounts = loadAccounts()

    // Check if the email exists in the accounts
    return existingAccounts.keys.any { it == hashedEnteredEmail }
}

fun loadAccounts(): Map<String, UserAccount> {
    val gson = Gson()
    val accountsJson = File("src/userAccounts.json")

    val type: Type = object : TypeToken<Map<String, UserAccount>>() {}.type

    return if (accountsJson.exists()) {
        gson.fromJson(accountsJson.readText(), type)
    } else {
        emptyMap()
    }
}

fun isPasswordCorrect(email: String, enteredPassword: String): Boolean {
    // Load existing accounts from userAccounts.json
    val existingAccounts = loadAccounts()

    // Hash the entered email
    val hashedEnteredEmail = encryptData(email)

    // Get the UserAccount associated with the entered email
    val userAccount = existingAccounts[hashedEnteredEmail]

    // Compare the entered password with the stored password
    return userAccount?.encryptedPassword == encryptData(enteredPassword)
}
