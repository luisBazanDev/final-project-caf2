package pe.bazan.luis.android.loratester

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class GpsProvider {
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    lateinit var mainActivity: MainActivity

    constructor(mainActivity: MainActivity) {
        this.mainActivity = mainActivity

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity)
    }

    companion object {
        private const val PERMISSION_REQUEST_GPS_REQUEST=100
    }

    fun getCurrentLocation(callback: (location: Location) -> Unit) {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        mainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        mainActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestGpsPermission()
                }
                mFusedLocationProviderClient.lastLocation.addOnCompleteListener(mainActivity) { task ->
                    val location: Location?=task.result
                    if (location == null) {
                        Toast.makeText(mainActivity.applicationContext, "LOCATION IS NULL", Toast.LENGTH_SHORT).show()
                    } else {
                        callback.invoke(location)
                    }
                }
            } else {
                Toast.makeText(mainActivity.applicationContext, "GPS NOT ENABLED", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestGpsPermission()
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == PERMISSION_REQUEST_GPS_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mainActivity.applicationContext, "Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(mainActivity.applicationContext, "Denied permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) return true
        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = mainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun requestGpsPermission() {
        ActivityCompat.requestPermissions(
            mainActivity,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_GPS_REQUEST
        )
    }
}