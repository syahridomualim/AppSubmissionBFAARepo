package com.theboss.wibi.submiss2appgithubuserwibi.ui.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.theboss.wibi.submiss2appgithubuserwibi.R
import com.theboss.wibi.submiss2appgithubuserwibi.data.database.DatabaseContract.UserFavoriteColumns.Companion.CONTENT_URI
import com.theboss.wibi.submiss2appgithubuserwibi.data.model.Favorite
import com.theboss.wibi.submiss2appgithubuserwibi.ui.adapter.FavoriteAdapter
import com.theboss.wibi.submiss2appgithubuserwibi.util.helper.MappingHelper
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.user_items.view.*
import kotlinx.coroutines.*

class FavoriteActivity : AppCompatActivity() {
    //inisialisasi
    private lateinit var adapter: FavoriteAdapter
    private lateinit var uriWithId: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        supportActionBar?.title = "User Favorite"

        showRecyclerListViewFavoriteUser()
        //proses ambil data menggunakan background thread
        loadUserFavoritesAsync()
    }

    //setup recyclerview
    private fun showRecyclerListViewFavoriteUser(){
        adapter = FavoriteAdapter()

        rv_favorite.layoutManager = LinearLayoutManager(this)
        rv_favorite.setHasFixedSize(true)
        rv_favorite.adapter = adapter

        adapter.setOnItemClickCallback(object : FavoriteAdapter.OnItemClickCallback{

            override fun onItemClicked(data: Favorite) {
                selectedUser(data)
            }

            override fun btnFavoriteClicked(view: View, data: Favorite) {
                if(data.favorite == 1){
                    //data dihapus dr db
                    deleteUserById(view, data.id.toString())
                    Toast.makeText(this@FavoriteActivity, "${data.login} ${getString(R.string.delete_form_favorite)}", Toast.LENGTH_SHORT).show()
                    data.favorite = 0
                    val iconFavorite = R.drawable.outline_favorite_border_black_24dp
                    view.btn_favorite.setImageResource(iconFavorite)
                }
            }
        })
    }

    //proses ambil data menggunakan background thread
    private fun loadUserFavoritesAsync() {
        GlobalScope.launch ( Dispatchers.Main ){
            progress_bar.visibility = View.VISIBLE
            val deferredUserFavorites = async (Dispatchers.IO){
                //meangambil data dengan contentResolver (provider)
                val cursor = contentResolver.query(CONTENT_URI, null, null, null,null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            progress_bar.visibility = View.INVISIBLE
            val userFavorites =deferredUserFavorites.await()
            if (userFavorites.size > 0){
                adapter.listFavorites = userFavorites
            }else{
                adapter.listFavorites = ArrayList()
                delay(300)
                Toast.makeText(this@FavoriteActivity, getString(R.string.no_data_now), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //intent ke halaman detail sesuai user yg dipilih
    private fun selectedUser(userFav: Favorite){
        val username = userFav.login

        val intentToDetail = Intent(this@FavoriteActivity, DetailActivity::class.java)
        intentToDetail.putExtra(DetailActivity.EXTRA_USERNAME, username)
        startActivity(intentToDetail)
    }

    //delete data in db by id
    private fun deleteUserById (view: View, id: String){
        //mwnghapus dengan id
        uriWithId = Uri.parse(CONTENT_URI.toString() + "/" + id)

        //delete with contentResolver (Provider)
        view.context.contentResolver.delete(uriWithId, null, null)

    }

    override fun onRestart() {
        showRecyclerListViewFavoriteUser()
        //proses ambil data menggunakan background thread
        loadUserFavoritesAsync()
        super.onRestart()
    }
}
