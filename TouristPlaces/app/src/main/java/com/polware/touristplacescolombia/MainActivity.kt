package com.polware.touristplacescolombia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.polware.touristplacescolombia.adapters.MainAdapter
import com.polware.touristplacescolombia.databinding.ActivityMainBinding
import com.polware.touristplacescolombia.roomdb.TouristPlaceDAO
import com.polware.touristplacescolombia.roomdb.TouristPlaceEntity
import com.polware.touristplacescolombia.utils.SwipeToDeleteCallback
import com.polware.touristplacescolombia.utils.SwipeToEditCallback
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    companion object {
        lateinit var touristPlaceDAO: TouristPlaceDAO
        var PLACE_DETAILS = "intent_extra_place_details"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        touristPlaceDAO = (application as TouristPlaceApplication).database.touristPlaceDAO()
        lifecycleScope.launch {
            touristPlaceDAO.getAllTouristPlaces().collect {
                val listTouristPlaces = ArrayList(it)
                setupMainRecyclerView(listTouristPlaces, touristPlaceDAO)
            }
        }

        binding.fabAddPlace.setOnClickListener {
            val intent = Intent(this, TouristPlaceActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setupMainRecyclerView(touristPlacesList: ArrayList<TouristPlaceEntity>, touristPlaceDAO: TouristPlaceDAO) {
        if (touristPlacesList.isNotEmpty()) {
            val mainAdapter = MainAdapter(this, touristPlacesList,
                { itemPlace -> onPlaceClickListener(itemPlace) })
            binding.recyclerViewMain.layoutManager = LinearLayoutManager(this)
            binding.recyclerViewMain.adapter = mainAdapter
            binding.recyclerViewMain.visibility = View.VISIBLE
            binding.textViewMainView.visibility = View.GONE

            // Invoke swipe RIGHT to Edit a place
            ItemTouchHelper(object: SwipeToEditCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val editAdapter = binding.recyclerViewMain.adapter as MainAdapter
                    val editPlace = editAdapter.getDataPlaceSwiped(viewHolder.bindingAdapterPosition)
                    editTouristPlaceDetails(editPlace)
                    editAdapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
                }
            }).attachToRecyclerView(binding.recyclerViewMain)

            // Invoke swipe LEFT to Delete a place
            ItemTouchHelper(object: SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val deleteAdapter = binding.recyclerViewMain.adapter as MainAdapter
                    val deletePlace = deleteAdapter.getDataPlaceSwiped(viewHolder.bindingAdapterPosition)
                    deleteTouristPlace(deletePlace)
                    deleteAdapter.notifyItemChanged(viewHolder.bindingAdapterPosition)
                }
            }).attachToRecyclerView(binding.recyclerViewMain)
        }
        else {
            binding.recyclerViewMain.visibility = View.GONE
            binding.textViewMainView.visibility = View.VISIBLE
        }
    }

    private fun onPlaceClickListener(itemPlace: TouristPlaceEntity) {
        //Toast.makeText(this, "Place: ${itemPlace.title}", Toast.LENGTH_SHORT).show()
        val intentDetails = Intent(this, PlaceDetailsActivity::class.java)
        // La clase TouristPlaceEntity se hace Parcelable para enviar objeto a otra actividad
        intentDetails.putExtra(PLACE_DETAILS, itemPlace)
        startActivity(intentDetails)
    }

    private fun editTouristPlaceDetails(itemPlace: TouristPlaceEntity) {
        val intentEdit = Intent(this, TouristPlaceActivity::class.java)
        intentEdit.putExtra(MainActivity.PLACE_DETAILS, itemPlace)
        startActivity(intentEdit)
    }

    private fun deleteTouristPlace(itemPlace: TouristPlaceEntity) {
        val id = itemPlace.id
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete: ${itemPlace.title}")
        builder.setMessage("Are you sure you want to delete it?")
        builder.setIcon(R.drawable.warning)
        builder.setPositiveButton("Yes") { _, _ ->
            lifecycleScope.launch {
                touristPlaceDAO.delete(TouristPlaceEntity(id))
                Snackbar.make(binding.root, "Tourist place deleted", Snackbar.LENGTH_LONG)
                    .withColor(R.color.dark_blue)
                    .show()
            }
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar{
        this.view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, colorInt))
        return this
    }

}