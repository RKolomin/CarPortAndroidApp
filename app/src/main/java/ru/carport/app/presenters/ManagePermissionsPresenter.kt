package ru.carport.app.presenters

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.carport.app.MainActivity
import ru.carport.app.R

class ManagePermissionsPresenter(private val activity: MainActivity, private val list: List<String>, private val code: Int) {
    fun checkPermissions() {
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert()
        }
    }

    private fun isPermissionsGranted(): Int {
        var counter = 0;
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission) != 0) {
                Log.i("check permission", permission)
                counter += 1
            }
            //counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        Log.i("check permission", "counter: " + counter)
        return counter
    }

    private fun deniedPermission(): List<String> {
        return list.filter { ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.permission_header)
        builder.setMessage(R.string.permission_text)
        builder.setPositiveButton("OK") { _, _ -> requestPermissions() }
        builder.setNeutralButton(R.string.cancel, null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun requestPermissions() {
        val deniedPermission = deniedPermission()
        ActivityCompat.requestPermissions(activity, deniedPermission.toTypedArray(), code)
    }
}