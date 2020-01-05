package br.com.weatherapp.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

data class FindResult(
    val list : List<City>
)

data class City(
    val id: Int,
    val name: String,
    val main : Main,
    val weather: List<Weather>,
    val wind: Wind,
    val clouds: Clouds,
    val sys : Sys
)

data class Sys(
    val country: String

)

data class Main(
    val temp: Float,
    val pressure: Float
)

data class Weather(
    val icon: String,
    val description: String
)

data class Wind(
    val speed: Float
)

data class Clouds(
    val all: Int
)

@Entity(tableName = "TB_FAVORITE")
data class Favorite(
    @PrimaryKey
    val id: Int,
    val name: String
)