package com.glamvibe.utils

class Constants {
    object Role {
        const val CLIENT = "Клиент"
        const val ADMINISTRATOR = "Администратор"
    }

    object WeekDay {
        const val SUNDAY = "Вс"
        const val MONDAY = "Пн"
        const val TUESDAY = "Вт"
        const val WEDNESDAY = "Ср"
        const val THURSDAY = "Чт"
        const val FRIDAY = "Пт"
        const val SATURDAY = "Сб"
    }

    object Status {
        const val IN_PROCESSING = "В обработке"
        const val WAITING = "Ожидает выполнения"
        const val DONE = "Выполнено"
        const val CANCELLATION_BY_THE_CLIENT = "Отмена клиентом"
        const val CANCELLATION_BY_THE_ADMINISTRATOR = "Отмена администратором"
    }
}