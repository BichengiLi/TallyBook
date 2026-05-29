package com.example.tallybook.data

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(typeString: String): TransactionType {
        return TransactionType.valueOf(typeString)
    }
}