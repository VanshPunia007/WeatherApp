package com.vanshpunia.weatherapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.vanshpunia.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Weather API key: a781b0f59ce3888d0802fac2ee68cada

class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        setContentView(binding.root)
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy") // Customizethe format as needed
        val formattedDate = today.format(formatter)
        val dayOfWeek: DayOfWeek = today.dayOfWeek
        binding.date.text = formattedDate
        binding.day.text = dayOfWeek.toString()
        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        val response = retrofit.getWeatherData("kurukshetra", "a781b0f59ce3888d0802fac2ee68cada", "metric")
        response.enqueue(object : Callback<WeatherApp>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (responseBody != null && response.isSuccessful){
                    val temperature = responseBody.main.temp.toString()
                    binding.temp.text = "$temperature"
                    binding.weather.text = responseBody.weather.first().main
                    binding.condition.text = responseBody.weather.first().main
                    binding.city.text = responseBody.name
                    binding.humidity.text = responseBody.main.humidity.toString()
                    binding.windSpeed.text = responseBody.wind.speed.toString()
                    binding.maxTemp.text = "MAX: ${responseBody.main.temp_max.toString()}"
                    binding.minTemp.text = "MIN: ${responseBody.main.temp_min.toString()}"
                    binding.sea.text = "${ responseBody.main.sea_level.toString() } hPa"
                    val sunrise = responseBody.sys.sunrise
                    val sunset = responseBody.sys.sunset
                    val instant = Instant.ofEpochSecond(sunrise.toLong())
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                        .withZone(ZoneId.systemDefault())
                    val sunriseTime = formatter.format(instant)
                    val instant1 = Instant.ofEpochSecond(sunset.toLong())
                    val formatter1 = DateTimeFormatter.ofPattern("hh:mm a")
                        .withZone(ZoneId.systemDefault())
                    val sunsetTime = formatter1.format(instant1)
                    binding.sunrise.text = sunriseTime
                    binding.sunset.text = sunsetTime
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }
}