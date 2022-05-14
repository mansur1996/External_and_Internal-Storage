package com.example.modul6lesson5internal_externalstorages.activity

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.modul6lesson5internal_externalstorages.R
import com.example.modul6lesson5internal_externalstorages.adapter.ExternalAdapter
import com.example.modul6lesson5internal_externalstorages.adapter.InternalAdapter
import com.example.modul6lesson5internal_externalstorages.databinding.ActivityMainBinding
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val APP_PERMISSION_CODE = 1001
    private val isPersistent = false

    /**
     * to save photo
     */
    private val isInternal = true

    private var readPermissionGranted = false
    var writePermissionGranted = false

    private lateinit var binding: ActivityMainBinding

    private lateinit var allowPermissionBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

    }

    private fun initViews(){

        requestPermissions()
//        checkStoragePaths()
//        createInternalFiles()
//        createExternalFile()

        binding.btnSaveInt.setOnClickListener {
            saveInternalFile("Internal")
        }

        binding.btnReadInt.setOnClickListener {
            readInternalFile()
        }

        binding.btnDeleteInt.setOnClickListener {
            deleteInternalFile()
        }

        binding.btnSaveEx.setOnClickListener {
            saveExternalFile("External")
        }

        binding.btnReadEx.setOnClickListener {
            readExternalFile()
        }

        binding.btnDeleteEx.setOnClickListener {
            deleteExternalFile()
        }

        binding.btnTakePhotoCamera.setOnClickListener {
            takePhoto.launch()
        }

        binding.btnLoadInter.setOnClickListener {

        }
        binding.recyclerView.adapter = InternalAdapter(this, loadPhotosFromInternalStorage())

        binding.btnLoadEx.setOnClickListener {
            binding.recyclerView.adapter = ExternalAdapter(this, loadPhotosFromExternalStorage())
        }

//        allowPermissionBtn.setOnClickListener {
//            openAppPermission()
//        }

    }



    private fun loadPhotosFromExternalStorage(): List<Uri> {

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
        )
        val photos = mutableListOf<Uri>()
        return contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                photos.add(contentUri)
            }
            photos.toList()
        } ?: listOf()
    }

    private fun loadPhotosFromInternalStorage() : List<Bitmap>{
        val files = filesDir.listFiles()
        return files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
            val bytes = it.readBytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bitmap
        } ?: listOf()
    }

    private fun deleteInternalFile() {
        val fileName = "pdp_internal.txt"
        val file: File
        file = if (isPersistent) {
            File(filesDir, fileName)
        } else {
            File(cacheDir, fileName)
        }
        if (file.exists()) {
            file.delete()
            Toast.makeText(this, String.format("File %s has been deleted", fileName), Toast.LENGTH_LONG).show()

        } else {
            Toast.makeText(this, String.format("File %s doesn't exist", fileName), Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteExternalFile() {
        val fileName = "pdp_external.txt"
        val file: File
        file = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }
        if (file.exists()) {
            file.delete()
            Toast.makeText(this, "File %s has been deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "File %s doesn't exist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri

        startActivityForResult(intent, APP_PERMISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == APP_PERMISSION_CODE) {
            // Here we check if the user granted the permission or not using
            //Manifest and PackageManager as usual
            checkPermissionIsGranted()
        }

    }

    private fun checkPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED){
            allowPermissionBtn.visibility = View.GONE
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
        }
        else{
            allowPermissionBtn.visibility = View.VISIBLE
            Toast.makeText(this, "Not granted", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Both
     */
    private fun checkStoragePaths() {
        val internal_m1 = getDir("custom", 0)
        val internal_m2 = filesDir

        val external_m1 = getExternalFilesDir(null)
        val external_m2 = externalCacheDir
        val external_m3 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)


        Log.d("StorageActivity", internal_m1.absolutePath)
        Log.d("StorageActivity", internal_m2.absolutePath)
        Log.d("StorageActivity", external_m1!!.absolutePath)
        Log.d("StorageActivity", external_m2!!.absolutePath)
        Log.d("StorageActivity", external_m3!!.absolutePath)
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){ bitmap ->
        val filename = UUID.randomUUID().toString()

        val isPhotoSaved = if(isInternal){
            savePhotoToInternalStorage(filename, bitmap!!)
        }else{
            if(writePermissionGranted){
                savePhotoToExternalStorage(filename, bitmap!!)
            }else{
                false
            }
        }
        if(isPhotoSaved){
            Toast.makeText(this,"Photo saved successfully", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"Failed to save photo", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Internal
     */
    private fun createInternalFiles(){
        val fileName = "pdp_internal.txt"
        val file : File

        file = if(isPersistent){
            File(filesDir, fileName)
        }else{
            File(cacheDir, fileName)
        }
        if(!file.exists()){
            try {
                file.createNewFile()
                Toast.makeText(this, String.format("File %s has been created", fileName), Toast.LENGTH_SHORT).show()

            }catch (e : IOException){
                Toast.makeText(this, String.format("File %s creation failed", fileName), Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, String.format("File %s already exists", fileName), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveInternalFile(data : String){
        val fileName = "pdp_internal.txt"
        try {
            val fileOutputStream : FileOutputStream
            fileOutputStream = if(isPersistent){
                openFileOutput(fileName, MODE_PRIVATE)
            }else{
                val file = File(cacheDir, fileName)
                FileOutputStream(file)
            }
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, String.format("Write to %s successful", fileName), Toast.LENGTH_SHORT).show()
        }catch (e : Exception){
            e.printStackTrace()
            Toast.makeText(this, String.format("Write to file %s failed", fileName), Toast.LENGTH_SHORT).show()
        }

    }

    private fun readInternalFile(){
        val fileName = "pdp_internal.txt"
        try {
            val fileInputStream : FileInputStream
            fileInputStream = if(isPersistent){
                openFileInput(fileName)
            }else{
                val file = File(cacheDir, fileName)
                FileInputStream(file)
            }
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines : MutableList<String?> = ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null){
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Log.d("@@@", readText.toString())
            Toast.makeText(this, String.format("Read from file %s successful", fileName), Toast.LENGTH_SHORT).show()
        } catch (e : java.lang.Exception){
            e.printStackTrace()
            Toast.makeText(this, String.format("Read from file %s failed", fileName), Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhotoToInternalStorage(filename: String, bitmap: Bitmap): Boolean {
        return try {
            openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if(!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)){
                    throw IOException("Couldn't save bitmap")
                }
            }
            true
        }catch (e : IOException){
            e.printStackTrace()
            false
        }
    }


    /**
     * External
     */
    private fun createExternalFile() {
        val fileName = "pdp_external.txt"
        val file: File
        file = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }
        Log.d("@@@", "absolutePath: " + file.absolutePath)
        if (!file.exists()) {
            try {
                file.createNewFile()
                Toast.makeText(this, String.format("File %s has been created", fileName), Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                Toast.makeText(this, String.format("File %s creation failed", fileName), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, String.format("File %s already exists", fileName), Toast.LENGTH_LONG).show()
        }
    }

    private fun requestPermissions(){
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if(!readPermissionGranted){
            Toast.makeText(this, "READ_EXTERNAL_STORAGE was not allowed", Toast.LENGTH_SHORT).show()
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if(!writePermissionGranted){
            Toast.makeText(this, "WRITE_EXTERNAL_STORAGE was not allowed", Toast.LENGTH_SHORT).show()
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(permissionsToRequest.isNotEmpty())
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
        readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
        writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

        if(readPermissionGranted) Toast.makeText(this, "READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show()
        else{
            allowPermissionBtn.visibility = View.VISIBLE
        }

        if(writePermissionGranted) Toast.makeText(this, "WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show()
        else{
            allowPermissionBtn.visibility = View.VISIBLE
        }
    }

    private fun saveExternalFile(data : String){
        val fileName = "pdp_external.txt"
        val file : File

        file = if(isPersistent){
            File(getExternalFilesDir(null), fileName)
        }else{
            File(externalCacheDir, fileName)
        }
        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, "Write to %s successful", Toast.LENGTH_SHORT).show()
        }catch (e : Exception){
            e.printStackTrace()
            Toast.makeText(this, "Write to file %s failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readExternalFile(){
        val fileName = "pdp_external.txt"
        val file : File
        file = if(isPersistent){
            File(getExternalFilesDir(null), fileName)
        }else{
            File(externalCacheDir, fileName)
        }
        try {
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines : MutableList<String?> = java.util.ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null){
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Log.d("StorageActivity", readText)
            Toast.makeText(this, "Read from file %s successful", Toast.LENGTH_SHORT).show()
        }catch (e : java.lang.Exception){
            e.printStackTrace()
            Toast.makeText(this, "Read from file %s failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhotoToExternalStorage(filename: String, bitmap: Bitmap): Boolean {
        val collection = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else{
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }
        return try {
            contentResolver.insert(collection, contentValues)?.also { uri->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if(!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)){
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("couldn't create MediaStore entry")
            true
        }catch (e : IOException){
            e.printStackTrace()
            false
        }
    }

}