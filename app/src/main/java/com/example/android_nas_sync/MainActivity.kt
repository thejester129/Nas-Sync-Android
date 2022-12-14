package com.example.android_nas_sync

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.android_nas_sync.databinding.ActivityMainBinding
import com.example.android_nas_sync.service.SyncService
import com.example.android_nas_sync.viewmodels.MappingsViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MappingsViewModel by viewModels()
    private var FOLDER_PICK_ACTIVITY_CODE = 1
    private var PERMISSION_REQUEST_READ_CODE = 2
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
            } else {
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        requestPermissionsIfNeeded()

        if(!isServiceRunningInForeground(SyncService::class.java)){
            try{
                applicationContext.startForegroundService(Intent(baseContext, SyncService::class.java))
            }
            catch(e:Exception){
                viewModel.addSnackMessage("Service failed to start")
            }
        }
    }


    private fun requestPermissionsIfNeeded(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_CODE)
        }

        // Requesting manage all storage for android >= 11
        // TODO this could probably be done in a more scoped way
        if(SDK_INT >= 30){
            if (!Environment.isExternalStorageManager() ){
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        uri
                    )
                )
            }
        }

//        if(SDK_INT >= 33){
//           if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
//               != PackageManager.PERMISSION_GRANTED){
//               val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
//
//               startActivity(
//                   Intent(
//                       Settings.ACTION_APP_NOTIFICATION_SETTINGS,
//                       uri
//                   )
//               )
//           }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == FOLDER_PICK_ACTIVITY_CODE
            && resultData?.data != null) {
            resultData.data?.also { uri ->
                val mapping = viewModel.currentlyEditedMapping.value
                mapping?.sourceFolder = uri.toString()
                viewModel.currentlyEditedMapping.value = mapping
                // Persist uri read permission
                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_refresh -> handleRefresh()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleRefresh():Boolean{
        viewModel.syncAllMappings()
        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun isServiceRunningInForeground(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }
}