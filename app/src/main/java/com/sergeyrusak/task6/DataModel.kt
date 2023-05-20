package com.sergeyrusak.task6

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sergeyrusak.task6.Weather

open class DataModel : ViewModel() {
    val data: MutableLiveData<MutableList<Weather>> by lazy {
        MutableLiveData (mutableListOf())
    }
}