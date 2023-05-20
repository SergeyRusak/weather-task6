package com.sergeyrusak.task6

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import androidx.databinding.DataBindingUtil
import com.sergeyrusak.task6.fragments.WeatherFragmentDetail
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sergeyrusak.task6.databinding.ActivityMainBinding
import com.sergeyrusak.task6.fragments.WeatherFragmentShort
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var API_KEY: String

    private var _cities = mutableListOf<String>()
    private var _weather = mutableListOf<Weather>()
    private val _weatherViewModel: DataModel by viewModels()

    var isShortFragment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myLog", "onCreate")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_layout, WeatherFragmentDetail.newInstance()).commit()
        this.API_KEY = R.string.API_KEY.toString()
        binding.btnAddCity.setOnClickListener {
            if (addCity(binding.inputCityName.text.toString())) {
                this.getWeatherForCity(binding.inputCityName.text.toString())
                binding.inputCityName.setText("")
                this._weatherViewModel.data.value = _weather
            }
        }
        binding.btnUpdateTemp.setOnClickListener {
            this.updateWeatherForAllCities()
            this._weatherViewModel.data.value = _weather
        }
        binding.btnChangeFragment.setOnClickListener {
            if (isShortFragment) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_layout, WeatherFragmentDetail.newInstance())
                    .commit()
                binding.btnChangeFragment.text = "Кратко"
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment_layout, WeatherFragmentShort.newInstance())
                    .commit()
                binding.btnChangeFragment.text = "Подробнее"
            }
            isShortFragment = !isShortFragment
        }
    }

    private fun addCity(text: String): Boolean {
        return if (text == "") {
            Toast.makeText(this, "Имя города не может быть пустым", Toast.LENGTH_SHORT).show()
            false
        } else {
            this._cities.add(text)
            true
        }
    }

    private fun requestWeather(nameCity: String): Weather {
        val weatherURL = "https://api.openweathermap.org/data/2.5/weather?q=${nameCity}&appid=${this.API_KEY}&units=metric";
        var data       = ""
        val weather    = Weather()
        weather.city   = nameCity

        lateinit var stream: InputStream

        try {
            stream = URL(weatherURL).getContent() as InputStream
        } catch (e: IOException) {
            weather.city = "Ошибка интернет подключения!"
            Log.d("myLog", e.message.toString())
            return weather
        }

        try {
            data = Scanner(stream).next()
            val jsonObj      = JSONObject(data)
            weather.temp     = jsonObj.getJSONObject("main").getString("temp").let { "$it°" }
            weather.humidity = jsonObj.getJSONObject("main").getString("humidity").let { "$it%" }
            val iconName     = JSONObject(jsonObj.getJSONArray("weather").getString(0)).getString("icon").let { "_${it.subSequence(1, it.length)}" }
            weather.iconId   = resources.getIdentifier(iconName, "drawable", packageName)

        } catch (e: Exception) {
            weather.city     = "Ошибка город не найден"
            Log.d("myLog", e.message.toString())
            return weather
        }

        return weather
    }
    private fun updateWeatherForAllCities() {
        GlobalScope.launch(Dispatchers.IO) {
            _weatherViewModel.data.value?.clear()
            _cities.forEach { it -> getWeatherForCity(it) }
        }
    }
    private fun getWeatherForCity(nameCity: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val weatherreq = requestWeather(nameCity)
            _weather.add(weatherreq)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("myLog", "onPause")

        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)

        val dataCities = Gson().toJson(this._cities)
        sharedPreferences.edit().putString("citiesData", dataCities).apply()

        val dataWeatherInfo = Gson().toJson(this._weatherViewModel.data.value)
        sharedPreferences.edit().putString("weatherInfoData", dataWeatherInfo).apply()
    }

    override fun onResume() {
        super.onResume()
        Log.d("myLog", "onResume")
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val dataCitiesJson = sharedPreferences.getString("citiesData", "")
        try {
            val type: Type = object : TypeToken<MutableList<String>>() {}.type
            this._cities = Gson().fromJson<MutableList<String>>(dataCitiesJson, type)
        } catch (e: Exception) {
            Log.d("myLog", "Ошибка загрузки данных о городах!")
        }
        val dataWeatherInfoJson = sharedPreferences.getString("weatherInfoData", "")
        try {
            val type: Type = object : TypeToken<MutableList<Weather>>() {}.type
            val dataFromStorage = Gson().fromJson<MutableList<Weather>>(dataWeatherInfoJson, type)
            if (dataFromStorage != null) {
                this._weatherViewModel.data.value = dataFromStorage
                _weather = dataFromStorage
            }
        } catch (e: Exception) {
            Log.d("myLog", "Error Load Data about weather!")
        }
    }
}