package com.polware.touristplacescolombia

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.polware.touristplacescolombia.MainActivity.Companion.touristPlaceDAO
import com.polware.touristplacescolombia.databinding.ActivityTouristPlaceBinding
import com.polware.touristplacescolombia.roomdb.TouristPlaceDAO
import com.polware.touristplacescolombia.roomdb.TouristPlaceEntity
import com.polware.touristplacescolombia.utils.AddressFromLatLng
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class TouristPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var bindingAddPlace: ActivityTouristPlaceBinding
    private var activityResultLauncherImageSelected: ActivityResultLauncher<Intent>? = null
    private var activityResultLauncherCameraPhoto: ActivityResultLauncher<Intent>? = null
    private var activityResultLauncherGooglePlaces: ActivityResultLauncher<Intent>? = null
    // Se crean dos variables para seleccionar fecha del calendario
    private var calendar = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    // Variable para URI de imagen guardada
    private var savedImagePath: Uri? = null
    private var latitude: Double = 0.0
    private var longitude: Double = -0.0
    private var placeDataToEdit: TouristPlaceEntity? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val IMAGES_FOLDER = "TouristPlacesImages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingAddPlace = ActivityTouristPlaceBinding.inflate(layoutInflater)
        setContentView(bindingAddPlace.root)

        setSupportActionBar(bindingAddPlace.toolbarAddPlace)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        bindingAddPlace.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //registerActivityForGooglePlaces()
        registerActivityForImageSelected()
        registerActivityForCameraPhoto()

        // Inicializamos Google Places (es necesario tener una cuenta de facturación para usarlo)
        if (!Places.isInitialized()) {
            Places.initialize(this, resources.getString(R.string.google_maps_places_key))
        }

        // Obtenemos extras del Intent para cargar detalles del lugar
        if (intent.hasExtra(MainActivity.PLACE_DETAILS)){
            placeDataToEdit = intent.getParcelableExtra(MainActivity.PLACE_DETAILS) as TouristPlaceEntity?
            // Cargamos los detalles del lugar en el View
            supportActionBar!!.title = "Edit Tourist Place"
            bindingAddPlace.editTextTitle.setText(placeDataToEdit!!.title)
            bindingAddPlace.editTextDescription.setText(placeDataToEdit!!.description)
            bindingAddPlace.editTextDate.setText(placeDataToEdit!!.date)
            bindingAddPlace.editTextLocation.setText(placeDataToEdit!!.location)
            latitude = placeDataToEdit!!.latitude
            longitude = placeDataToEdit!!.longitude
            savedImagePath = Uri.parse(placeDataToEdit!!.image)
            bindingAddPlace.imageViewPlace.setImageURI(savedImagePath)
            bindingAddPlace.buttonSave.text = "Update"
            //bindingAddPlace.buttonSave.background = ColorDrawable(ContextCompat.getColor(this, R.color.blue_grey))
            bindingAddPlace.buttonSave.background = getDrawable(R.drawable.button_edit_rounded) as Drawable
        }

        dateSetListener = DatePickerDialog.OnDateSetListener {
                datePicker, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            loadSelectedDate()
        }
        // Carga automáticamente la fecha actual
        loadSelectedDate()

        bindingAddPlace.editTextDate.setOnClickListener(this)
        bindingAddPlace.textViewAddImage.setOnClickListener(this)
        bindingAddPlace.buttonSave.setOnClickListener(this)
        //bindingAddPlace.editTextLocation.setOnClickListener(this)
        bindingAddPlace.buttonCurrentLocation.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id){
            R.id.editTextDate -> {
                DatePickerDialog(this@TouristPlaceActivity, dateSetListener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
            R.id.textViewAddImage -> {
                val addImageDialog = AlertDialog.Builder(this)
                addImageDialog.setTitle("Select Option")
                val addImageOptions = arrayOf("1. Select photo from gallery", "2. Capture photo from camera")
                addImageDialog.setItems(addImageOptions) {
                    _, which ->
                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                addImageDialog.show()
            }
            R.id.buttonSave -> {
                when {
                    bindingAddPlace.editTextTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    }
                    bindingAddPlace.editTextDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                    }
                    bindingAddPlace.editTextLocation.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
                    }
                    savedImagePath == null -> {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        if (placeDataToEdit == null){
                            addTouristPlace(touristPlaceDAO)
                        }
                        else {
                            val placeId = placeDataToEdit!!.id
                            updateTouristPlace(placeId, touristPlaceDAO)
                        }
                    }
                }
            }
            // No funciona porque no hay vinculada una cuenta de facturación de Places
            R.id.editTextLocation -> {
                try {
                    // List of fields which has to be passed
                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS)
                    val intentPlaces = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this)
                    activityResultLauncherGooglePlaces?.launch(intentPlaces)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.buttonCurrentLocation -> {
                if (!isLocationEnabled()) {
                    Toast.makeText(applicationContext, "Your location provider is turned off",
                        Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                else {
                    // Ask multiple permissions using Dexter
                    Dexter.withContext(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        .withListener(object: MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                if (report.areAllPermissionsGranted()){
                                    requestNewLocation()
                                    //Toast.makeText(this@TouristPlaceActivity, "Location permission is granted", Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,
                                                                            token: PermissionToken) {
                                showRationalDialogForPermissions()
                            }
                        }).onSameThread().check()
                }
            }
        }
    }

    private fun requestNewLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 10_000 // Equivale a 10 segundos
        locationRequest.numUpdates = 1
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper())
    }

    private val locationCallBack = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation!!
            latitude = lastLocation.latitude
            Log.i("Current Latitude: ", "$latitude")
            longitude = lastLocation.longitude
            Log.i("Current Longitude: ", "$longitude")
            val addressTask = AddressFromLatLng(this@TouristPlaceActivity, latitude, longitude)
            addressTask.setAddressListener(object: AddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?){
                    bindingAddPlace.editTextLocation.setText(address)
                }
                override fun onError() {
                    Log.e("Get Address: ", "Something went wrong")
                }
            })
            addressTask.getAddress()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun addTouristPlace(touristPlaceDAO: TouristPlaceDAO) {
        val title = bindingAddPlace.editTextTitle.text.toString()
        val image = savedImagePath.toString()
        val description = bindingAddPlace.editTextDescription.text.toString()
        val date = bindingAddPlace.editTextDate.text.toString()
        val location = bindingAddPlace.editTextLocation.text.toString()

        lifecycleScope.launch {
            touristPlaceDAO.insert(TouristPlaceEntity(title = title, image = image,
                description = description, date = date, location = location,
                latitude = latitude, longitude = longitude))
            Toast.makeText(applicationContext, "Tourist place saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateTouristPlace(id: Int, touristPlaceDAO: TouristPlaceDAO) {
        val title = bindingAddPlace.editTextTitle.text.toString()
        val image = savedImagePath.toString()
        val description = bindingAddPlace.editTextDescription.text.toString()
        val date = bindingAddPlace.editTextDate.text.toString()
        val location = bindingAddPlace.editTextLocation.text.toString()
        val placeLatitude = latitude
        val placeLongitude = longitude

        lifecycleScope.launch {
            touristPlaceDAO.update(TouristPlaceEntity(id, title, image, description, date,
                location, placeLatitude, placeLongitude))
            //Toast.makeText(applicationContext, "Tourist place updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun choosePhotoFromGallery(){
        // Ask multiple permissions using Dexter
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()){
                        //Toast.makeText(this@AddPlaceActivity, "Storage READ/WRITE permissions granted", Toast.LENGTH_SHORT).show()
                        val galleryIntent = Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        // ActivityResultLauncher -> replace startActivityForResult
                        activityResultLauncherImageSelected?.launch(galleryIntent)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,
                    token: PermissionToken) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun takePhotoFromCamera() {
        // Ask multiple permissions using Dexter
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .withListener(object: MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()){
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        // ActivityResultLauncher -> replace startActivityForResult
                        activityResultLauncherCameraPhoto?.launch(cameraIntent);
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>,
                                                                token: PermissionToken) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("You have turned off permission required for" +
                " this feature. It can be enabled under the Applications Settings")
            .setPositiveButton("Go To Settings") {
                _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") {
                dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadSelectedDate(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        bindingAddPlace.editTextDate.setText(sdf.format(calendar.time).toString())
    }

    private fun registerActivityForImageSelected() {
        activityResultLauncherImageSelected = registerForActivityResult(ActivityResultContracts
            .StartActivityForResult()) {
                result ->
                val resultCode = result.resultCode
                val data = result.data
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        val selectedImage = MediaStore.Images.Media
                            .getBitmap(contentResolver, data.data)
                        savedImagePath = saveImageToMemory(selectedImage)
                        Log.i("Saved image: ", "Path: $savedImagePath")
                        bindingAddPlace.imageViewPlace.setImageBitmap(selectedImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@TouristPlaceActivity, "Error loading image",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun registerActivityForCameraPhoto() {
        activityResultLauncherCameraPhoto = registerForActivityResult(ActivityResultContracts
            .StartActivityForResult()) {
                result ->
                val resultCode = result.resultCode
                val data = result.data
                if (resultCode == RESULT_OK) {
                    val photo: Bitmap = data!!.extras!!.get("data") as Bitmap
                    savedImagePath = saveImageToMemory(photo)
                    Log.i("Saved image: ", "Path: $savedImagePath")
                    bindingAddPlace.imageViewPlace.setImageBitmap(photo)
                }
                else {
                    Toast.makeText(this@TouristPlaceActivity, "Failed to capture photo",
                            Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun registerActivityForGooglePlaces() {
        activityResultLauncherGooglePlaces = registerForActivityResult(ActivityResultContracts
            .StartActivityForResult()) {
                result ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == RESULT_OK && data != null) {
                try {
                    val place: Place = Autocomplete.getPlaceFromIntent(data)
                    bindingAddPlace.editTextLocation.setText(place.address)
                    latitude = place.latLng!!.latitude
                    longitude = place.latLng!!.longitude
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@TouristPlaceActivity, "Error loading place",
                        Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Log.e("Google Places: ", "Cancelled")
            }
        }
    }

    private fun saveImageToMemory(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGES_FOLDER, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

}