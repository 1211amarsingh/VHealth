package com.vhealth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PermissionsRationaleActivity : AppCompatActivity() {

    val ALL_PERMISSIONS = setOf(
//        Permission.createWritePermission(StepsRecord::class),
        Permission.createReadPermission(StepsRecord::class),
//        Permission.createWritePermission(TotalCaloriesBurnedRecord::class),
//        Permission.createReadPermission(TotalCaloriesBurnedRecord::class),
    )
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(this) }

    lateinit var permissionController: PermissionController
    lateinit var requestPermissionsResult: ActivityResultLauncher<Set<Permission>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions_rationale)

        permissionController = healthConnectClient.permissionController

        requestPermissionsResult =
            registerForActivityResult(permissionController.createRequestPermissionActivityContract()) { granted ->
                onPermissionResult(granted)
            }

        checkConnected(permissionController)
    }

    private fun checkConnected(permissionController: PermissionController) {
        lifecycleScope.launch {

            val grantedPermissions = permissionController.getGrantedPermissions(ALL_PERMISSIONS)
            if (grantedPermissions.size == 0) { //no permission granted
                findViewById<LinearLayout>(R.id.llError).visibility = View.VISIBLE
            } else { //Permissions allowed do your job
                startMainActivity()
            }
        }
    }

    fun onClickAllow(view: View) {
        requestPermissionsResult.launch(ALL_PERMISSIONS)
    }

    private fun onPermissionResult(granted: Set<Permission>?) {
        if (granted?.containsAll(ALL_PERMISSIONS) == true) { // Permissions successfully granted
            startMainActivity()
        } else {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}