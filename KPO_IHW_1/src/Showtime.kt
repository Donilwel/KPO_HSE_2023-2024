import java.util.*

data class Showtime(
    var movie: Movie,
    var startTime: Date,
    var availableSeats: MutableList<Int>,
    var purchasedSeats: MutableList<Int> = mutableListOf(),
    var checkedSeats: MutableList<Int> = mutableListOf()
)
