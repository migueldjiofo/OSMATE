package com.osmate.app.domain.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

object LocationPermissionUtils {
    fun currentState(context: Context): LocationPermissionState {
        val fineLocationGranted = context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = context.checkSelfPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return when {
            fineLocationGranted -> LocationPermissionState.GrantedFine
            coarseLocationGranted -> LocationPermissionState.GrantedCoarse
            else -> LocationPermissionState.Denied
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        return currentState(context) != LocationPermissionState.Denied
    }
}