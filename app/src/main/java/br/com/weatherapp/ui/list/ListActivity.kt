package br.com.weatherapp.ui.list

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import br.com.weatherapp.R
import br.com.weatherapp.api.RetrofitManager
import br.com.weatherapp.common.Constants
import br.com.weatherapp.common.Constants.PREFS_LANG
import br.com.weatherapp.common.Constants.PREFS_TEMP
import br.com.weatherapp.data.RoomManager
import br.com.weatherapp.entity.Favorite
import br.com.weatherapp.entity.FindResult
import br.com.weatherapp.ui.setting.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListActivity : AppCompatActivity(), Callback<FindResult> {


    private var preferredUnit : String = "metric";
    private var preferredLang : String = "EN";

    private val adapter: ListAdapter by lazy {
        ListAdapter()
    }


    private val SP: SharedPreferences by lazy {
        getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPreferences()

        btnSearch.setOnClickListener {
            if(isdeviceConnected()){
                getCities()
            }else{
                Toast.makeText(this, "You are offline", Toast.LENGTH_LONG).show()
            }


        }


        edtCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if( edtCity.isClickable ){
                    getPreferredCities();

                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


            }

        })

        initRecyclerView()
        getPreferredCities();



    }

    private fun initRecyclerView(){
        rvWeather.adapter = adapter

    }


    private fun getCities(){

        progressBar.visibility = View.VISIBLE
        val call = RetrofitManager.getWeatherService()
            .find(edtCity.text.toString(), preferredLang,  preferredUnit, Constants.API_KEY)

        call.enqueue(this)
    }

    private fun getPreferredCities(){
        progressBar.visibility = View.VISIBLE

        var listCities = FindFavoriteAsync(applicationContext).execute().get() as String

        if(listCities.isNotEmpty()){
            val call = RetrofitManager.getWeatherService()
                .getPreferred(listCities, Constants.API_KEY)
            call.enqueue(this)
        }else{
            progressBar.visibility = View.GONE
        }

    }


    private fun isdeviceConnected(): Boolean{
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        checkPreferences()
    }

    private fun checkPreferences(){
        var unit = SP.getBoolean(Constants.PREFS_TEMP, true)
        var lang = SP.getBoolean(Constants.PREFS_LANG, true)

        if(unit == true){
            preferredUnit = "metric"
        }else{
            preferredUnit = "imperial"
        }

        if(lang == true) {
            preferredLang = "EN"
        }else{
            preferredLang = "PT"
        }

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.getItemId()) {
            R.id.setting_item -> {
                goToSettingsActivity()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    private fun goToSettingsActivity(){
        val intentEnterSettings = Intent(this, SettingsActivity::class.java)
        startActivity(intentEnterSettings)
    }

    override fun onFailure(call: Call<FindResult>, t: Throwable) {
        progressBar.visibility = View.GONE
    }

    override fun onResponse(call: Call<FindResult>, response: Response<FindResult>) {

        if (response.isSuccessful){
            adapter.updateData(response.body()?.list, preferredUnit)
        }else{

        }
        progressBar.visibility = View.GONE
    }

    class FindFavoriteAsync(context: Context): AsyncTask<Void, Void, String>(){

        var listFavorite : List<Favorite> = ArrayList()
        var listFavoriteCityNames : MutableList<String> = ArrayList()
        var resultStringOfCities = ""
        var context = context


        val db = RoomManager.getInstance(context)

        override fun doInBackground(vararg params: Void?): String {

            try{

                listFavorite =  db?.getCityDao()?.allFavorites() as List<Favorite>
                db?.getCityDao()?.allFavorites()?.forEach {
                    Log.d("WELL", it.toString())
                }
            }catch (e: Exception){

            }
            for (n in listFavorite){
                listFavoriteCityNames.add(n.id.toString())
            }
            resultStringOfCities = listFavoriteCityNames.joinToString(separator= ",")

            return resultStringOfCities
        }


    }


}