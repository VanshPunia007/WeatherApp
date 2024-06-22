package com.vanshpunia.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.vanshpunia.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

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
        /** To get current date and day of the week
        val today = LocalDate.now()
        val formatter =
            DateTimeFormatter.ofPattern("dd MMMM, yyyy") // Customizethe format as needed
        val formattedDate = today.format(formatter)
        val dayOfWeek: DayOfWeek = today.dayOfWeek
        binding.date.text = formattedDate
        binding.day.text = dayOfWeek.toString()
        */
        fetchWeatherData("Kurukshetra")
        searchCity()
    }

    private fun searchCity() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                var city = query?.trim() ?: ""
                // Hide the keyboard
                inputMethodManager.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
                fetchWeatherData(city)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }


    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName, "a781b0f59ce3888d0802fac2ee68cada", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            @SuppressLint("SetTextI18n")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (responseBody != null && response.isSuccessful) {
                    val temperature = responseBody.main.temp.toString()
                    binding.temp.text = temperature
                    binding.maxTemp.text = "MAX: ${responseBody.main.temp_max}"
                    binding.minTemp.text = "MIN: ${responseBody.main.temp_min}"

                    binding.city.text = responseBody.name

                    binding.weather.text = responseBody.weather.firstOrNull()?.main?: "unknown"

                    binding.humidity.text = responseBody.main.humidity.toString()
                    binding.windSpeed.text = responseBody.wind.speed.toString()
                    binding.condition.text = responseBody.weather.firstOrNull()?.main?: "unknown"
//                    binding.conditionIcon.setImageResource(responseBody.weather.firstOrNull()?.icon?: "unknown")
                    binding.sea.text = "${responseBody.main.sea_level}" + " hpa"

                    // binding.day.text = LocalDate.now().dayOfWeek.toString()
                    // binding.date.text = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM, yyyy"))

                    binding.day.text = dayName()
                    binding.date.text = date()

                    val sunrise = responseBody.sys.sunrise
                    val sunset = responseBody.sys.sunset
                    binding.sunrise.text = setTime(sunrise.toLong())
                    binding.sunset.text = setTime(sunset.toLong())

                    changeBackground(binding.weather.text.toString(), binding.sunset.text.toString(), binding.sunrise.text.toString())
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeBackground(weather: String, sunset : String, sunrise : String) {
            val currentTimeMillis = System.currentTimeMillis()
            val currTime = setTime(currentTimeMillis)
            when(weather){
                "Rain", "Drizzle"->{
                    binding.root.setBackgroundResource(R.drawable.rain_dark2)
                    binding.lottieAnimationView.setAnimation(R.raw.rain)
                }
                "Mist", "Smoke", "Haze", "Dust", "Fog", "Sand"->{
                    binding.root.setBackgroundResource(R.drawable.mist)
                    binding.lottieAnimationView.setAnimation(R.raw.cloud)
                }
                "Thunderstorm"->{
                    binding.root.setBackgroundResource(R.drawable.thunderstorm)
                    binding.lottieAnimationView.setAnimation(R.raw.rain)
                }
                "Clear", "Sunny", "Clear Sky" ->{
                    if (isDaytime(sunrise, sunset)) {
                        binding.root.setBackgroundResource(R.drawable.sunny_background)
                        binding.lottieAnimationView.setAnimation(R.raw.sun)
                    } else {
                        binding.root.setBackgroundResource(R.drawable.night_sky)
                        binding.city.setTextColor(Color.WHITE)
                        binding.temp.setTextColor(Color.WHITE)
                        binding.weather.setTextColor(Color.WHITE)
                        binding.maxTemp.setTextColor(Color.WHITE)
                        binding.minTemp.setTextColor(Color.WHITE)
                        binding.day.setTextColor(Color.WHITE)
                        binding.date.setTextColor(Color.WHITE)
                        binding.textView4.setTextColor(Color.WHITE)
                        binding.textView12.setTextColor(Color.WHITE)
                        binding.textView14.setTextColor(Color.WHITE)
                        binding.textView13.setTextColor(Color.WHITE)
                        try{
                            binding.lottieAnimationView.setAnimation(R.raw.moon)
                        }catch (e : Exception){
                            binding.lottieAnimationView.setAnimation(R.raw.sun)
                        }
                    }
                }
                "Clouds", "Partly Clouds", "Overcast" ->{
                    binding.root.setBackgroundResource(R.drawable.clouds)
                    binding.lottieAnimationView.setAnimation(R.raw.cloud)
                }
                "Snow", "Light Snow", "Heavy Snow", "Blizzard" ->{
                    binding.root.setBackgroundResource(R.drawable.snowfall3)
                    binding.lottieAnimationView.setAnimation(R.raw.snow)
                }
            }
        binding.lottieAnimationView.playAnimation()
    }

    private fun isDaytime(sunrise: String, sunset: String): Boolean {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Get current time in "hh:mm a" format
        val currentTimeString = timeFormat.format(Date())

        val sunsetDate = timeFormat.parse(sunset)
        val sunriseDate = timeFormat.parse(sunrise)
        val currentDate = timeFormat.parse(currentTimeString)
        return currentDate.after(sunriseDate) && currentDate.before(sunsetDate)
    }

    fun dayName(): String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }
    fun date(): String{
        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun setTime(time: Long): String {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            .withZone(ZoneId.systemDefault())
        val instant = Instant.ofEpochSecond(time)
        val timeStamp = formatter.format(instant)
        return timeStamp
    }
}