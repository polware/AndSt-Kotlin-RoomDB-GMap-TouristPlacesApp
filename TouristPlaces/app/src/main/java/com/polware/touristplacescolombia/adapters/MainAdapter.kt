package com.polware.touristplacescolombia.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.polware.touristplacescolombia.databinding.ItemTouristPlaceBinding
import com.polware.touristplacescolombia.roomdb.TouristPlaceEntity

class MainAdapter(private val context: Context, private val list: ArrayList<TouristPlaceEntity>,
                  private val placeClickListener: (itemPlace: TouristPlaceEntity) -> Unit):
    RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    // Binding del layout "item_tourist_place"
    class ViewHolder(binding: ItemTouristPlaceBinding): RecyclerView.ViewHolder(binding.root) {
        val cardView = binding.cardViewPlace
        val imagePlace = binding.circleImageTouristPlace
        val textViewTitle = binding.textViewTitle
        val textViewDescription = binding.textViewDescription
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTouristPlaceBinding.inflate(LayoutInflater
            .from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = list[position]
        holder.imagePlace.setImageURI(Uri.parse(place.image))
        holder.textViewTitle.text = place.title
        holder.textViewDescription.text = place.description

        holder.cardView.setOnClickListener {
            placeClickListener.invoke(place)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getDataPlaceSwiped(position: Int): TouristPlaceEntity {
        return list[position]
    }

}