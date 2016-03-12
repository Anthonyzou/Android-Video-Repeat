package com.contextswitch.videorepeat

import android.app.Activity
import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
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
class FullscreenActivity : Activity() {

    private var position = 0
    private var progressDialog: ProgressDialog? = null
    private var mediaControls: MediaController? = null

    fun delayedHide(time : Long = 1){
        Observable.just(true).delay(time, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            Log.d("hiding", "hiding")
            hide()
        })
    }

    var showing : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        container.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            if(showing){
                delayedHide()
            }
            else{
                show()
            }
            false
        })

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
            delayedHide()
            progressDialog?.dismiss()
            if (position == 0) {
                fullscreen_content.start()
            } else {
                fullscreen_content.pause()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        progressDialog?.dismiss();
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

    private fun hide() {
        showing = false
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_LOW_PROFILE
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//        )
    }

    private fun show() {
        showing = true
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        )
    }
}
