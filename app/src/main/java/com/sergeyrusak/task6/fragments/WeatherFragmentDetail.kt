package com.sergeyrusak.task6.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sergeyrusak.task6.recycleCustomAdapters.WeatherRecycleAdapterDetail
import com.sergeyrusak.task6.DataModel
import com.sergeyrusak.task6.R

class WeatherFragmentDetail : Fragment() {
    private lateinit var weatherAdapter: WeatherRecycleAdapterDetail
    private val dataModel: DataModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_weather_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        weatherAdapter = WeatherRecycleAdapterDetail(ArrayList(dataModel.data.value?: mutableListOf()))

        view.findViewById<RecyclerView>(R.id.r_view).apply{
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = weatherAdapter
        }

        dataModel.data.observe(activity as LifecycleOwner) {
            weatherAdapter.updateData(ArrayList(it))
        }
    }

    companion object {
        fun newInstance() = WeatherFragmentDetail()
    }
}