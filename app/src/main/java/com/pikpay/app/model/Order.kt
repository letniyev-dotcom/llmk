package com.pikpay.app.model

enum class OrderStatus { PENDING, SUCCESS, ERROR }

data class Order(
    val id: Int,
    val amount: Int,
    val time: String,
    val status: OrderStatus,
    val auto: Boolean = false
)

enum class OrderTab(val label: String) {
    ALL("Все"),
    PENDING("Ожидают"),
    SUCCESS("Подтв."),
    ERROR("Ошибки")
}

fun sampleOrders() = listOf(
    Order(4021, 1489, "14:32", OrderStatus.SUCCESS, auto = true),
    Order(4022, 1890, "14:35", OrderStatus.PENDING),
    Order(4023, 345, "14:35", OrderStatus.PENDING),
    Order(4019, 2260, "13:58", OrderStatus.SUCCESS, auto = true),
    Order(4015, 960, "12:20", OrderStatus.ERROR)
)
