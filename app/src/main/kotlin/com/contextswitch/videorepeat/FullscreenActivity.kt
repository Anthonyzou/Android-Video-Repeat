package com.contextswitch.videorepeat

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_fullscreen.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        Log.d("hiding", "showing")
        Observable.just(true).delay(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("hiding", "hiding")
                    hide()
                })
        false
    }

    private var position = 0
    private var progressDialog: ProgressDialog? = null
    private var mediaControls: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        container.setOnTouchListener(mDelayHideTouchListener)
        //BEGIN VIDEO STUFF
        if (mediaControls == null) {
            mediaControls = MediaController(this@FullscreenActivity)
        }

        // Create a progressbar
        progressDialog = ProgressDialog(this@FullscreenActivity)
        // Set progressbar title
        progressDialog?.setTitle("Loading video")
        // Set progressbar message
        progressDialog?.setMessage("Loading...")

        progressDialog?.setCancelable(false)
        // Show progressbar
        progressDialog?.show()

        fullscreen_content.setOnCompletionListener { mp:MediaPlayer ->
            mp.start()
        }
        fullscreen_content.setMediaController(mediaControls)

        fullscreen_content.setVideoURI(intent.data) // getIntent().data

        fullscreen_content.requestFocus()
        fullscreen_content.setOnPreparedListener {
            progressDialog?.dismiss()
            if (position == 0) {
                fullscreen_content.start()
            } else {
                fullscreen_content.pause()
            }
        }
        hide()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("Position", fullscreen_content.currentPosition)
        fullscreen_content.pause()
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        position = savedInstanceState.getInt("Position")
        fullscreen_content.seekTo(position)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }

    private fun hide() {
        // Hide UI first
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE
        )
        supportActionBar?.hide()
    }

    @SuppressLint("InlinedApi")
    private fun show() {
        // Show the system bar
        supportActionBar?.show()
    }


    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [.AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }
}
