import com.google.gson.*
import java.io.File
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class DateDeserializer : JsonSerializer<Date>, JsonDeserializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    override fun deserialize(
        json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
    ): Date {
        val dateStr = json?.asString
        return dateFormat.parse(dateStr)
    }

    override fun serialize(
        src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(dateFormat.format(src))
    }
}

class Cinema {
    private val movies = mutableListOf<Movie>()
    private val showtimes = mutableListOf<Showtime>()
    private val availableTickets = mutableListOf<Ticket>()
    private val soldTickets = mutableListOf<Ticket>()

    init {
        // Загрузка данных из файлов JSON
        loadMovies()
        loadShowtimes()
        createAvailableAndSoldTickets()
    }

    fun buyTicket() {
        val showtimesForSelectedMovie: List<Showtime> = displayAllShowtimesForMovie()

        println()
        while (true) {
            print("Choose a showtime by entering a number from 1 to ${showtimesForSelectedMovie.size} or type '0' to return back: ")

            val selectedShowtimeNumber = readlnOrNull()?.toIntOrNull()

            if (selectedShowtimeNumber == 0) {
                println()
                processUserMenu()
                break
            }

            if (selectedShowtimeNumber != null && selectedShowtimeNumber in 1..showtimesForSelectedMovie.size) {
                val selectedShowtime = showtimesForSelectedMovie[selectedShowtimeNumber - 1]

                buyTicketForShowtime(selectedShowtime)
                processUserMenu()
                break
            } else {
                printlnColor("Invalid choice or showtime not found. Try one more time!", ConsoleColor.RED)
            }
        }
    }

    private fun buyTicketForShowtime(selectedShowtime: Showtime) {
        printlnColor("-----------------------------------------------------------------", ConsoleColor.PURPLE)
        if (selectedShowtime.availableSeats.isNotEmpty()) {
            println("${selectedShowtime.availableSeats.size}/30 seats are available for ${selectedShowtime.movie.title}")

            while (true) {
                println("Here are the numbers of available seats: ${selectedShowtime.availableSeats}")
                displaySeats(selectedShowtime)
                print("Please choose one of them to buy a ticket for this seat number or type '0' to return back: ")

                val selectedSeatNumber = readlnOrNull()?.toIntOrNull()

                if (selectedSeatNumber == 0) {
                    println()
                    buyTicket()
                    break
                }

                if (selectedSeatNumber != null && selectedShowtime.availableSeats.contains(selectedSeatNumber)) {
                    // The selected seat number is valid and available for booking
                    print("Enter the name of the ticket holder: ")
                    val ticketHolderName = readlnOrNull()

                    sellTicket(selectedShowtime, selectedSeatNumber, ticketHolderName)

                    break
                } else {
                    printlnColor("Invalid seat number or seat is not available. Try again", ConsoleColor.RED)
                }
            }
        } else {
            printlnColor("No available seats for this showtime. Press enter to return back", ConsoleColor.RED)
            readlnOrNull()
            buyTicket()
        }
    }

    private fun sellTicket(
        showtime: Showtime, seatNumber: Int, purchaserName: String? = "undefined"
    ): Pair<Boolean, String?> {
        // Реализация фиксации продажи билета
        if (showtime.availableSeats.contains(seatNumber)) {
            // Remove the seat from availableSeats
            showtime.availableSeats.remove(seatNumber)

            // Add the seat to purchasedSeats
            showtime.purchasedSeats.add(seatNumber)

            // Find the corresponding ticket in availableTickets
            val ticket = findAvailableTicket(showtime, seatNumber)
            if (ticket != null) {
                // Remove the ticket from availableTickets
                availableTickets.remove(ticket)

                ticket.isAvailable = false
                ticket.purchaserName = purchaserName

                // Add the ticket to soldTickets
                soldTickets.add(ticket)
                printlnColor(
                    "Ticket was successfully purchased!\nYour ticket Id -- ${ticket.id}\nMake sure you wrote it down, because you can on;y return ticket with ticket Id!",
                    ConsoleColor.GREEN
                )

                return true to ticket.id
            } else {
                println("Error: Ticket not found.")
                return false to null
            }
        } else {
            println("Seat $seatNumber is not available for purchase.")
            return false to null
        }
    }

    private fun findAvailableTicket(showtime: Showtime, seatNumber: Int): Ticket? {
        val ticketId = createTicketId(showtime, seatNumber)

        return availableTickets.find { it.id == ticketId }
    }

    private fun findSoldTicket(showtime: Showtime, seatNumber: Int): Ticket? {
        val ticketId = createTicketId(showtime, seatNumber)

        return soldTickets.find { it.id == ticketId }
    }

    private fun createTicketId(showtime: Showtime, seatNumber: Int): String {
        val movieTitle = showtime.movie.title

        val zonedDateTime = ZonedDateTime.ofInstant(showtime.startTime.toInstant(), ZoneId.of("Europe/Moscow"))
        val dateFormat = SimpleDateFormat("yyyy.MM.dd_HH:mm_z", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone(zonedDateTime.zone)
        val formattedDate = dateFormat.format(showtime.startTime)

        val ticketId = "${movieTitle}_${formattedDate}_seat_№$seatNumber"

        return ticketId
    }


    fun returnTicket() {
        // Реализация возврата билета
        printlnColor("-----------------------------------------------------------------", ConsoleColor.PURPLE)
        println("We are ready to return in case showtime hasn't started yet!\nPlease be ready to provide your ticket id!")

        while (true) {
            print("Enter the id of the ticket you are willing to return or type 'exit' if you want to return back: ")
            val ticketId = readlnOrNull() ?: ""
            if (ticketId.trim().lowercase(Locale.getDefault()) == "exit") {
                println()
                processStartingMenu()
                break
            }

            val soldTicket = soldTickets.find { it.id == ticketId }

            if (soldTicket != null && !soldTicket.isAvailable) {
                if (soldTicket.showtime.startTime.after(Date()) && !soldTicket.checkedByController) {
                    // Process the return of a sold ticket
                    // Remove the ticket from soldTickets
                    soldTickets.remove(soldTicket)
                    soldTicket.showtime.purchasedSeats.remove(soldTicket.seatNumber)

                    soldTicket.isAvailable = true
                    soldTicket.purchaserName = null

                    soldTicket.showtime.availableSeats.add(soldTicket.seatNumber)
                    availableTickets.add(soldTicket)

                    println("Ticket with ID $ticketId returned successfully.")
                    processUserMenu()
                    break
                } else {
                    printlnColor(
                        "Cannot return the ticket. Showtime has started or the ticket has been already checked.",
                        ConsoleColor.RED
                    )
                    processUserMenu()
                    break
                }
            } else if (soldTicket != null) {
                println("This ticket is not purchased by anyone, try to enter another ticket id.")
            } else {
                println("This ticket is not found, try to enter another ticket id.")
            }
        }
    }

    private fun displaySeats(showtime: Showtime) {
        // Отображение свободных и проданных мест для выбранного сеанса
        val rows = 5
        val seatsPerRow = 6

        println()
        printlnColor("            Screen            ", ConsoleColor.PURPLE)
        printlnColor("------------------------------", ConsoleColor.PURPLE)

        for (row in 1..rows) {
            printColor("  |", ConsoleColor.PURPLE)
            for (seat in 1..seatsPerRow) {
                val seatNumber = (row - 1) * seatsPerRow + seat
                if (!showtime.availableSeats.contains(seatNumber)) {
                    printColor(" X ", ConsoleColor.RED)
                    printColor("|", ConsoleColor.PURPLE)
                } else {
                    printColor(" ${seatNumber.toString()} ", ConsoleColor.GREEN)
                    printColor("|", ConsoleColor.PURPLE)
                }
            }
            printlnColor("\n  ----------------------------", ConsoleColor.PURPLE)
        }
        println()
    }

    fun displayAllMovies() {
        // Отображение всех фильмов в этом кинотеатре
        printlnColor("-----------------------------------------------------------------", ConsoleColor.PURPLE)
        println("All movies in the cinema:")
        movies.forEachIndexed { index, movie ->
            println(
                "${index + 1}. Movie name: ${movie.title} | Duration: ${movie.durationMinutes} mins | Genre: ${movie.genre} | " + "Country: ${movie.countryProduced} | Director: ${movie.directorName}"
            )
        }
    }

    fun displayAllShowtimes() {
        // Отображение всех сеансов в этом кинотеатре
        printlnColor("-----------------------------------------------------------------", ConsoleColor.PURPLE)
        println("Showtimes in the cinema:")
        showtimes.forEachIndexed { index, showtime ->
            println(
                "${index + 1}. Movie name: ${showtime.movie.title} | Start time: ${showtime.startTime} | Available seats: ${showtime.availableSeats} | Movie duration: ${showtime.movie.durationMinutes} |"
            )
        }
    }

    fun displayAllShowtimesForMovie(): List<Showtime> {
        // Отображение всех сеансов в этом кинотеатре на определенный фильм
        displayAllMovies()

        println()
        while (true) {
            print("Choose a movie by entering a number from 1 to ${movies.size} or type '0' to return back: ")

            val selectedMovieNumber = readlnOrNull()?.toIntOrNull()

            if (selectedMovieNumber == 0) {
                println()
                processUserMenu()
                break
            }

            if (selectedMovieNumber != null && selectedMovieNumber in 1..movies.size) {
                val selectedMovie = movies[selectedMovieNumber - 1]

                val showtimesForSelectedMovie = showtimes.filter { it.movie == selectedMovie }
                printlnColor("-----------------------------------------------------------------", ConsoleColor.PURPLE)
                if (showtimesForSelectedMovie.isNotEmpty()) {
                    println("Showtimes for ${selectedMovie.title}:")
                    showtimesForSelectedMovie.forEachIndexed { index, showtime ->
                        println("${index + 1}. Start Time: ${showtime.startTime} | Available Seats: ${showtime.availableSeats.size}/30")
                    }
                } else {
                    println("No showtimes available for ${selectedMovie.title}.")
                }
                return showtimesForSelectedMovie
            } else {
                printlnColor("Invalid choice or movie not found. Try one more time!", ConsoleColor.RED)
            }
        }
        return emptyList()
    }

    fun displayAllTickets() {
        displayAllSoldTickets()
        displayAllAvailableTickets()
    }

    private fun displayAllAvailableTickets() {
        // Отображение всех доступных билетов в этом кинотеатре
        println("Available tickets:")
        availableTickets.forEach {
            println(
                "Ticket ID: ${it.id} | Movie: ${it.showtime.movie.title} | Seat: № ${it.seatNumber} | Available: ${it.isAvailable}"
            )
        }
    }

    fun displayAllSoldTickets() {
        // Отображение всех проданных билетов в этом кинотеатре
        println("Sold tickets:")
        soldTickets.forEach {
            println(
                "Ticket ID: ${it.id} | Purchaser name: ${it.purchaserName} | Movie: ${it.showtime.movie.title} | Seat: № ${it.seatNumber} | Available: ${it.isAvailable} | Checked: ${it.checkedByController}"
            )
        }
    }

    fun editMovie() {
        // Редактирование данных о фильме
        // Отображение всех фильмов
        displayAllMovies()

        println()
        while (true) {
            print("Choose a movie you want to edit by entering a number from 1 to ${movies.size} or type '0' to return back: ")

            val selectedMovieNumber = readlnOrNull()?.toIntOrNull()

            if (selectedMovieNumber == 0) {
                println()
                processUserMenu()
                break
            }

            if (selectedMovieNumber != null && selectedMovieNumber in 1..movies.size) {
                val selectedMovie = movies[selectedMovieNumber - 1]
                print("Which data you want to edit?\n1.Title\n2.Duration\n3.Country\n4.Director\n5.Genre\nEnter a number from 1 to 5 or 0 to exit: ")
                while (true) {
                    val choice = readlnOrNull()?.toIntOrNull()
                    println()

                    when (choice) {
                        0 -> {
                            break
                        }

                        1 -> {
                            println("Enter new title")
                            val title = readlnOrNull() ?: ""

                            selectedMovie.title = title
                            recreateIdForAvailableTickets()
                            println("Title changed")
                            break
                        }

                        2 -> {
                            println("Enter new duration")
                            val duration = readlnOrNull()?.toIntOrNull()
                            if (duration != null) {
                                selectedMovie.durationMinutes = duration
                                println("Duration changed")
                                break
                            } else {
                                println("Failed:(")
                            }
                        }

                        3 -> {
                            println("Enter new country")
                            val country = readlnOrNull() ?: ""
                            selectedMovie.countryProduced = country
                            println("Country changed")
                            break
                        }

                        4 -> {
                            println("Enter new director")
                            val director = readlnOrNull() ?: ""
                            selectedMovie.directorName = director
                            println("Director changed")
                            break
                        }

                        5 -> {
                            println("Enter new genre")
                            val genre = readlnOrNull() ?: ""
                            selectedMovie.genre = genre
                            println("Genre changed")
                            break
                        }

                        else -> {
                            printlnColor("Invalid choice!", ConsoleColor.RED)
                            printColor(
                                "Please enter a valid number from 1 to 5 depending on the desired action: ",
                                ConsoleColor.YELLOW
                            )
                        }
                    }
                }
                processUserMenu()
                break
            } else {
                printlnColor("Invalid choice or movie not found. Try one more time!", ConsoleColor.RED)
            }
        }
    }

    fun editShowtime() {
        // Редактирование расписания сеансов
        // Отображение всех сеансов в этом кинотеатре
        displayAllShowtimes()

        println()
        while (true) {
            print("Choose a showtime you want to edit by entering a number from 1 to ${showtimes.size} or type '0' to return back: ")

            val selectedShowtimeNumber = readlnOrNull()?.toIntOrNull()

            if (selectedShowtimeNumber == 0) {
                println()
                processUserMenu()
                break
            }

            if (selectedShowtimeNumber != null && selectedShowtimeNumber in 1..showtimes.size) {
                val selectedShowtime = showtimes[selectedShowtimeNumber - 1]
                while (true) {
                    print("Which data you want to edit?\n1.StartTime\n2.AvailableSeats\n3.PurchasedSeats\n4.CheckedSeats\nEnter a number from 1 to 4 or 0 to exit: ")

                    val choice = readlnOrNull()?.toIntOrNull()
                    println()

                    when (choice) {
                        0 -> {
                            break
                        }

                        1 -> {
                            println("Please enter new date in the following format: yyyy-MM-dd HH:mm:ss")
                            val dateString = readlnOrNull() ?: ""

                            try {
                                // Define the expected date format
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                                // Parse the entered date string
                                val date = dateFormat.parse(dateString)

                                selectedShowtime.startTime = date
                                recreateIdForAvailableTickets()
                                println("Date changed")
                                break
                            } catch (e: Exception) {
                                println("Error parsing date. Please use the correct format.")
                            }
                        }

                        2 -> {
                            println("Please enter the list of available seats separated by commas (e.g., 1,2,3,4). Please ensure each seat is an integer between 1 and 30.")
                            val seatsString = readlnOrNull() ?: ""
                            val availableSeats = seatsString.split(",").mapNotNull {
                                val seatNumber = it.trim().toIntOrNull()
                                if (seatNumber in 1..30) seatNumber else null
                            }
                            if (availableSeats.size == seatsString.split(",").size) {
                                selectedShowtime.availableSeats = availableSeats.toMutableList()
                                for (seat in 1..30) {
                                    if (seat !in selectedShowtime.availableSeats && seat !in selectedShowtime.purchasedSeats) {
                                        selectedShowtime.purchasedSeats.add(seat)
                                    }
                                    if (seat in selectedShowtime.availableSeats && seat in selectedShowtime.purchasedSeats) {
                                        selectedShowtime.purchasedSeats.remove(seat)
                                    }
                                    val foundAvailableTicket = findAvailableTicket(selectedShowtime, seat)
                                    val foundSoldTicket = findSoldTicket(selectedShowtime, seat)
                                    if (foundAvailableTicket != null && seat !in selectedShowtime.availableSeats) {
                                        availableTickets.remove(foundAvailableTicket)
                                        foundAvailableTicket.isAvailable = false
                                        foundAvailableTicket.purchaserName = null
                                        soldTickets.add(foundAvailableTicket)
                                    }
                                    if (foundSoldTicket != null && seat in selectedShowtime.availableSeats) {
                                        soldTickets.remove(foundSoldTicket)
                                        foundSoldTicket.isAvailable = true
                                        foundSoldTicket.purchaserName = null
                                        availableTickets.add(foundSoldTicket)
                                    }
                                }
                                println("Success")
                                break
                            } else {
                                println("Invalid seat numbers. Please ensure each seat is an integer between 1 and 30.")
                            }
                        }

                        3 -> {
                            println("Please enter the list of purchased seats separated by commas (e.g., 1,2,3,4). Please ensure each seat is an integer between 1 and 30.")
                            val seatsString = readlnOrNull() ?: ""
                            val purchasedSeats = seatsString.split(",").mapNotNull {
                                val seatNumber = it.trim().toIntOrNull()
                                if (seatNumber in 1..30) seatNumber else null
                            }
                            if (purchasedSeats.size == seatsString.split(",").size) {
                                selectedShowtime.purchasedSeats = purchasedSeats.toMutableList()
                                for (seat in 1..30) {
                                    if (seat !in selectedShowtime.availableSeats && seat !in selectedShowtime.purchasedSeats) {
                                        selectedShowtime.availableSeats.add(seat)
                                    }
                                    if (seat in selectedShowtime.availableSeats && seat in selectedShowtime.purchasedSeats) {
                                        selectedShowtime.availableSeats.remove(seat)
                                    }
                                    val foundAvailableTicket = findAvailableTicket(selectedShowtime, seat)
                                    val foundSoldTicket = findSoldTicket(selectedShowtime, seat)
                                    if (foundAvailableTicket != null && seat !in selectedShowtime.availableSeats) {
                                        availableTickets.remove(foundAvailableTicket)
                                        foundAvailableTicket.isAvailable = false
                                        foundAvailableTicket.purchaserName = null
                                        soldTickets.add(foundAvailableTicket)
                                    }
                                    if (foundSoldTicket != null && seat in selectedShowtime.availableSeats) {
                                        soldTickets.remove(foundSoldTicket)
                                        foundSoldTicket.isAvailable = true
                                        foundSoldTicket.purchaserName = null
                                        availableTickets.add(foundSoldTicket)
                                    }
                                }
                                for (seat in selectedShowtime.purchasedSeats) {
                                    val foundTicket = findAvailableTicket(selectedShowtime, seat)
                                    if (foundTicket != null) {
                                        availableTickets.remove(foundTicket)
                                        selectedShowtime.availableSeats.remove(foundTicket.seatNumber)
                                        foundTicket.isAvailable = false
                                        soldTickets.add(foundTicket)
                                    }

                                }
                                println("Success")
                                break
                            } else {
                                println("Invalid seat numbers. Please ensure each seat is an integer between 1 and 30.")
                            }
                        }

                        4 -> {
                            println("Please enter the list of checked seats separated by commas (e.g., 1,2,3,4). Please ensure each seat is an integer between 1 and 30.")
                            val seatsString = readlnOrNull() ?: ""
                            val checkedSeats = seatsString.split(",").mapNotNull {
                                val seatNumber = it.trim().toIntOrNull()
                                if (seatNumber in 1..30) seatNumber else null
                            }
                            if (checkedSeats.size == seatsString.split(",").size) {
                                val invalidSeats = checkedSeats.filter { it in selectedShowtime.availableSeats }
                                if (invalidSeats.isEmpty()) {
                                    selectedShowtime.checkedSeats = checkedSeats.toMutableList()
                                    for (seat in 1..30) {
                                        val foundAvailableTicket = findAvailableTicket(selectedShowtime, seat)
                                        val foundSoldTicket = findSoldTicket(selectedShowtime, seat)
                                        if (seat in selectedShowtime.checkedSeats && foundSoldTicket != null) {
                                            foundSoldTicket.checkedByController = true
                                        }
                                        if (seat !in selectedShowtime.checkedSeats && foundSoldTicket != null) {
                                            foundSoldTicket.checkedByController = false
                                        }
                                    }
                                    println("Success")
                                    break
                                } else {
                                    println("Invalid seat numbers. Some tickets are not sold!")
                                }
                            } else {
                                println("Invalid seat numbers. Please ensure each seat is an integer between 1 and 30 and ticket is sold.")
                            }
                        }

                        else -> {
                            printlnColor("Invalid choice!", ConsoleColor.RED)
                            printColor(
                                "Please enter a valid number from 1 to 5 depending on the desired action: ",
                                ConsoleColor.YELLOW
                            )
                        }
                    }
                }
                processUserMenu()
                break
            } else {
                printlnColor("Invalid choice or movie not found. Try one more time!", ConsoleColor.RED)
            }
        }
    }

    fun checkTicket() {
        // Отметка занятых мест
        println("Send me the ticket id to check as checked by controller or type 'exit' to return.")
        while (true) {
            print("Ticket id: ")
            val ticketId = readlnOrNull() ?: ""
            if (ticketId.trim().lowercase(Locale.getDefault()) == "exit") {
                println()
                processUserMenu()
                break
            }
            val foundTicket = soldTickets.find { it.id == ticketId }
            if (foundTicket != null) {
                foundTicket.checkedByController = true
                println("Ticket was successfully checked")
                processUserMenu()
                break
            } else {
                println("Ticket not found or nor sold yet. Try one more time.")
            }
        }
    }

    private fun loadMovies() {
        // Загрузка данных о фильмах из файла JSON
        val gson = Gson()
        val moviesJson = File("src/movies.json").readText()

        val listType = object : TypeToken<List<Movie>>() {}.type
        val moviesList: List<Movie> = gson.fromJson(moviesJson, listType)

        movies.addAll(moviesList)
    }

    private fun loadShowtimes() {
        // Загрузка расписания сеансов из файла JSON
        val gson = GsonBuilder().registerTypeAdapter(Date::class.java, DateDeserializer()).create()

        val showtimesJson = File("src/showtimes.json").readText()
        val showtimeList = gson.fromJson<List<Showtime>>(showtimesJson, object : TypeToken<List<Showtime>>() {}.type)

        showtimeList.forEach { showtime ->
            val matchingMovie = movies.find { it.title == showtime.movie.title }
            showtime.movie = matchingMovie ?: showtime.movie
        }

        showtimes.addAll(showtimeList)
    }

    private fun createAvailableAndSoldTickets() {
        // Создаем список всех доступных билетов с учетом тех, что уже выкуплены
        for (showtime in showtimes) {
            val movieTitle = showtime.movie.title

            // Convert Date to ZonedDateTime
            val zonedDateTime = ZonedDateTime.ofInstant(showtime.startTime.toInstant(), ZoneId.of("Europe/Moscow"))

            // Format date with time and time zone abbreviation
            val dateFormat = SimpleDateFormat("yyyy.MM.dd_HH:mm_z", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone(zonedDateTime.zone)
            val formattedDate = dateFormat.format(showtime.startTime)

            for (seatNumber in showtime.availableSeats) {
                val ticketId = "${movieTitle}_${formattedDate}_seat_№$seatNumber"

                val ticket = Ticket(
                    id = ticketId, showtime = showtime, seatNumber = seatNumber, isAvailable = true
                )

                availableTickets.add(ticket)
            }

            for (seatNumber in showtime.purchasedSeats) {
                val ticketId = "${movieTitle}_${formattedDate}_seat_№$seatNumber"

                val ticket = Ticket(
                    id = ticketId, showtime = showtime, seatNumber = seatNumber, isAvailable = false
                )

                soldTickets.add(ticket)
            }
        }

        processSoldTickets()
    }

    private fun recreateIdForAvailableTickets() {
        for (ticket in availableTickets) {
            val movieTitle = ticket.showtime.movie.title

            // Convert Date to ZonedDateTime
            val zonedDateTime =
                ZonedDateTime.ofInstant(ticket.showtime.startTime.toInstant(), ZoneId.of("Europe/Moscow"))

            // Format date with time and time zone abbreviation
            val dateFormat = SimpleDateFormat("yyyy.MM.dd_HH:mm_z", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone(zonedDateTime.zone)
            val formattedDate = dateFormat.format(ticket.showtime.startTime)

            ticket.id = "${movieTitle}_${formattedDate}_seat_№${ticket.seatNumber}"
        }
    }

    // Загрузка проданных билетов из файла JSON и их последующий учет в списках доступных и проданных
    private fun processSoldTickets() {
        val soldTicketsIdsJson = File("src/soldTicketsIds.json").readText()
        val soldTicketsMap = JsonParser.parseString(soldTicketsIdsJson).asJsonObject

        for ((ticketId, ticketInfo) in soldTicketsMap.entrySet()) {
            findAndMakeTicketUnavailable(ticketId)
        }

        val gson = GsonBuilder().registerTypeAdapter(Date::class.java, DateDeserializer()).create()

        val soldTicketsJson = File("src/soldTickets.json").readText()
        val soldTicketsList = gson.fromJson<List<Ticket>>(soldTicketsJson, object : TypeToken<List<Ticket>>() {}.type)
        soldTicketsList.forEach { soldTicket ->
            val foundSoldInAvailableTicket = availableTickets.find { it.id == soldTicket.id }
            if (foundSoldInAvailableTicket != null) {
                availableTickets.remove(foundSoldInAvailableTicket)
            }

            val matchingShowtime =
                showtimes.find { it.movie == soldTicket.showtime.movie && it.startTime == soldTicket.showtime.startTime }

            soldTicket.showtime = matchingShowtime ?: soldTicket.showtime
            soldTicket.isAvailable = false
            soldTicket.showtime.availableSeats.remove(soldTicket.seatNumber)
            if (soldTicket.seatNumber !in soldTicket.showtime.purchasedSeats) {
                soldTicket.showtime.purchasedSeats.add(soldTicket.seatNumber)
            }

            if (soldTicket.checkedByController && soldTicket.seatNumber !in soldTicket.showtime.checkedSeats) {
                soldTicket.showtime.checkedSeats.add(soldTicket.seatNumber)
            }
            if (!soldTicket.checkedByController && soldTicket.seatNumber in soldTicket.showtime.checkedSeats) {
                soldTicket.showtime.checkedSeats.remove(soldTicket.seatNumber)
            }

            val index = soldTickets.indexOfFirst { it.id == soldTicket.id }
            if (index != -1) {
                soldTickets[index] = soldTicket
            } else {
                soldTickets.add(soldTicket)
            }
        }
    }

    private fun findAndMakeTicketUnavailable(ticketId: String) {
        val ticket = availableTickets.find { it.id == ticketId }

        if (ticket != null) {
            availableTickets.remove(ticket)
            ticket.isAvailable = false
            ticket.showtime.availableSeats.remove(ticket.seatNumber)
            ticket.showtime.purchasedSeats.add(ticket.seatNumber)
            soldTickets.add(ticket)
        }
    }

    fun saveData() {
        // Сохранение данных в файлы JSON
        val gson = GsonBuilder().registerTypeAdapter(Date::class.java, DateDeserializer()).setPrettyPrinting().create()

        println()
        printlnColor("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-", ConsoleColor.GREEN)
        val moviesJson = gson.toJson(movies)
        File("src/movies.json").writeText(moviesJson)
        printlnColor("Movies data saved successfully.", ConsoleColor.GREEN)

        val showtimesJson = gson.toJson(showtimes)
        File("src/showtimes.json").writeText(showtimesJson)
        printlnColor("Showtimes data saved successfully.", ConsoleColor.GREEN)

        val soldTicketsMap = soldTickets.associate { it.id to it }
        val soldTicketsJsonIdKeys = gson.toJson(soldTicketsMap)
        File("src/soldTicketsIds.json").writeText(soldTicketsJsonIdKeys)
        printlnColor("Sold tickets with Ids as keys data saved successfully.", ConsoleColor.GREEN)

        val soldTicketsJson = gson.toJson(soldTickets)
        File("src/soldTickets.json").writeText(soldTicketsJson)
        printlnColor("Sold tickets data saved successfully.", ConsoleColor.GREEN)

        val availableTicketsJson = gson.toJson(availableTickets)
        File("src/availableTickets.json").writeText(availableTicketsJson)
        printlnColor("Available tickets data saved successfully.", ConsoleColor.GREEN)
    }
}
