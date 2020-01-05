package br.com.weatherapp.ui.list

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.weatherapp.R
import br.com.weatherapp.data.RoomManager
import br.com.weatherapp.entity.City
import br.com.weatherapp.entity.Favorite
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.row_weather_layout.view.*

class ListAdapter() : RecyclerView.Adapter<ListAdapter.ViewHolder> (){

    private var list : List<City>? = null
    private var preferredUnit: String = "celsius"

    private var cityName: String = ""
    private var cityId: Int = 0



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list?.let {
            holder.bind(it[position], preferredUnit)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_weather_layout, parent, false)

        return ViewHolder(view, preferredUnit)

    }

    override fun getItemCount() = list?.size ?: 0


    fun updateData(list: List<City>?, preferredUnit: String){
        this.list = list
        this.preferredUnit = preferredUnit

        notifyDataSetChanged()
    }

    class ViewHolder(view: View, preferredUnit: String) : RecyclerView.ViewHolder(view){

        fun bind(city: City, preferredUnit: String){

            itemView.TxtCity.text = "${city.name}"
            itemView.txt_country.text = ", ${city.sys.country}"
            itemView.TxtValue.text = city.main.temp.toInt().toString()
            itemView.TxtClaoudsValue.text = city.clouds.all.toString() + "%"
            itemView.TxtWindValue.text = city.wind.speed.toString() + " m/s"
            itemView.TxtPressure.text = city.main.pressure.toString() + " hps"

            if( preferredUnit == "metric"){
                itemView.TxtUnit.text = "°C"
            }else {
                itemView.TxtUnit.text = "°F"
            }


            if(city.weather.isNotEmpty()){
                itemView.TxtDescription.text = city.weather[0].description

                Glide.with(itemView.context)
                    .load("http://openweathermap.org/img/w/${city.weather[0].icon}.png")
                    .into(itemView.imgWeatherIcon)
            }

            var favorite = FindOneFavoriteAsync(itemView.context, city.id).execute().get()
            if(favorite){
                itemView.ButtonFavorite.setBackgroundResource(android.R.drawable.presence_online)
            }else{
                itemView.ButtonFavorite.setBackgroundResource(android.R.drawable.presence_invisible)
            }



            itemView.ButtonFavorite.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    itemView.ButtonFavorite.setBackgroundResource(android.R.drawable.presence_invisible)
                    DeleteFavoriteAsync(itemView.context, city.id, city.name).execute()
                }else{
                    itemView.ButtonFavorite.setBackgroundResource(android.R.drawable.presence_online)
                    InsertFavoriteAsync(itemView.context, city.id, city.name).execute()

                }

            }

        }


    }


    class FindOneFavoriteAsync(context: Context, cityid : Int): AsyncTask<Void, Void, Boolean>(){

        var cityid = cityid

        val db = RoomManager.getInstance(context)

        override fun doInBackground(vararg params: Void?): Boolean {

            try{
                var city = db?.getCityDao()?.favoriteById(cityid)
                if(city != null){
                    return true
                }

            }catch (e: Exception){

            }


            return false
        }

    }

    class InsertFavoriteAsync(context: Context, cityid : Int, cityName: String): AsyncTask<Void, Void, Boolean>(){

        var cityid = cityid
        var cityName = cityName

        val db = RoomManager.getInstance(context)

        override fun doInBackground(vararg params: Void?): Boolean {

            try{
                val favorite = Favorite(cityid, cityName)
                db?.getCityDao()?.insertFavorite(favorite)

                db?.getCityDao()?.allFavorites()?.forEach {
                    Log.d("WELL", it.toString())
                }
            }catch (e: Exception){

            }
            return true
        }

    }

    class DeleteFavoriteAsync(context: Context, cityid : Int, cityName: String): AsyncTask<Void, Void, Boolean>(){

        var cityid = cityid
        var cityName = cityName

        val db = RoomManager.getInstance(context)

        override fun doInBackground(vararg params: Void?): Boolean {

            try{
                val favorite = Favorite(cityid, cityName)
                db?.getCityDao()?.deleteFavorite(favorite)
            }catch (e: Exception){

            }
            return true
        }


    }


}
