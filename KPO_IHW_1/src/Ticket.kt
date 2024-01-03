data class Ticket(
    var id: String,
    var showtime: Showtime,
    val seatNumber: Int,
    var isAvailable: Boolean = true,
    var purchaserName: String? = null,
    var checkedByController: Boolean = false
)
