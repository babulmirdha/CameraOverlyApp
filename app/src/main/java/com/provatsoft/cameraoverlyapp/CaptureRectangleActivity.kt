package com.provatsoft.cameraoverlyapp

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.cameraview.CameraView
import kotlinx.android.synthetic.main.activity_capture_photo_inside_rectangle.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class CaptureRectangleActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    private var mCurrentFlash = 0
    private var mBackgroundHandler: Handler? = null
    private val mOnClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.takePictureButton -> if (ContextCompat.checkSelfPermission(
                        this@CaptureRectangleActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    if (cameraView != null) {
                        cameraView!!.takePicture()
                    }
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@CaptureRectangleActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    ConfirmationDialogFragment.newInstance(
                        R.string.storage_permission_confirmation,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_STORAGE_PERMISSION,
                        R.string.storage_permission_not_granted
                    )
                        .show(
                            supportFragmentManager,
                            FRAGMENT_DIALOG
                        )
                } else {
                    ActivityCompat.requestPermissions(
                        this@CaptureRectangleActivity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_STORAGE_PERMISSION
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_photo_inside_rectangle)
        if (cameraView != null) {
            cameraView!!.addCallback(mCallback)
        }
        takePictureButton?.setOnClickListener(mOnClickListener)
        //        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(false);
//        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            cameraView!!.start()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ConfirmationDialogFragment.newInstance(
                R.string.camera_permission_confirmation,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION,
                R.string.camera_permission_not_granted
            )
                .show(supportFragmentManager, FRAGMENT_DIALOG)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onPause() {
        cameraView!!.stop()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler!!.looper.quitSafely()
            } else {
                mBackgroundHandler!!.looper.quit()
            }
            mBackgroundHandler = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (permissions.size != 1 || grantResults.size != 1) {
                    throw RuntimeException("Error on requesting camera permission.")
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this, R.string.camera_permission_not_granted,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (permissions.size != 1 || grantResults.size != 1) {
                    throw RuntimeException("Error on requesting storage permission.")
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this, R.string.storage_permission_not_granted,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.switch_flash -> if (cameraView != null) {
                mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.size
                item.setTitle(FLASH_TITLES[mCurrentFlash])
                item.setIcon(FLASH_ICONS[mCurrentFlash])
                cameraView!!.flash = FLASH_OPTIONS[mCurrentFlash]
            }
            R.id.switch_camera -> if (cameraView != null) {
                val facing = cameraView!!.facing
                cameraView!!.facing =
                    if (facing == CameraView.FACING_FRONT) CameraView.FACING_BACK else CameraView.FACING_FRONT
            }
        }
        return false
    }

    private val backgroundHandler: Handler
        private get() {
            if (mBackgroundHandler == null) {
                val thread = HandlerThread("background")
                thread.start()
                mBackgroundHandler = Handler(thread.looper)
            }
            return mBackgroundHandler!!
        }

    private val mCallback: CameraView.Callback =
        object : CameraView.Callback() {
            override fun onCameraOpened(cameraView: CameraView) {
                Log.d(TAG, "onCameraOpened")
            }

            override fun onCameraClosed(cameraView: CameraView) {
                Log.d(TAG, "onCameraClosed")
            }

            override fun onPictureTaken(
                cameraView: CameraView,
                data: ByteArray
            ) {
                Log.d(TAG, "onPictureTaken " + data.size)
                Toast.makeText(cameraView.context, R.string.picture_taken, Toast.LENGTH_SHORT)
                    .show()
                backgroundHandler.post {
                    // This demo app saves the taken picture to a constant file.
// $ adb pull /sdcard/Android/data/com.google.android.cameraview.demo/files/Pictures/picture.jpg
                    val file = File(
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "picture-rect.jpg"
                    )
                    var os: OutputStream? = null
                    try {
                        os = FileOutputStream(file)
                        os.write(data)
                        os.close()
                    } catch (e: IOException) {
                        Log.w(
                            TAG,
                            "Cannot write to $file",
                            e
                        )
                    } finally {
                        if (os != null) {
                            try {
                                os.close()
                            } catch (e: IOException) { // Ignore
                            }
                        }
                    }
                }
            }
        }

    class ConfirmationDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val args = arguments
            return AlertDialog.Builder(activity!!)
                .setMessage(args!!.getInt(ARG_MESSAGE))
                .setPositiveButton(
                    android.R.string.ok
                ) { dialog, which ->
                    val permissions =
                        args.getStringArray(ARG_PERMISSIONS)
                            ?: throw IllegalArgumentException()
                    ActivityCompat.requestPermissions(
                        activity!!,
                        permissions,
                        args.getInt(ARG_REQUEST_CODE)
                    )
                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { dialog, which ->
                    Toast.makeText(
                        activity,
                        args.getInt(ARG_NOT_GRANTED_MESSAGE),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .create()
        }

        companion object {
            private const val ARG_MESSAGE = "message"
            private const val ARG_PERMISSIONS = "permissions"
            private const val ARG_REQUEST_CODE = "request_code"
            private const val ARG_NOT_GRANTED_MESSAGE = "not_granted_message"
            fun newInstance(
                @StringRes message: Int,
                permissions: Array<String?>?, requestCode: Int,
                @StringRes notGrantedMessage: Int
            ): ConfirmationDialogFragment {
                val fragment = ConfirmationDialogFragment()
                val args = Bundle()
                args.putInt(ARG_MESSAGE, message)
                args.putStringArray(
                    ARG_PERMISSIONS,
                    permissions
                )
                args.putInt(ARG_REQUEST_CODE, requestCode)
                args.putInt(
                    ARG_NOT_GRANTED_MESSAGE,
                    notGrantedMessage
                )
                fragment.arguments = args
                return fragment
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CAMERA_PERMISSION = 1
        private const val REQUEST_STORAGE_PERMISSION = 2
        private const val FRAGMENT_DIALOG = "dialog"
        private val FLASH_OPTIONS = intArrayOf(
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON
        )
        private val FLASH_ICONS = intArrayOf(
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on
        )
        private val FLASH_TITLES = intArrayOf(
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on
        )
    }
}