package com.cafewill.apk;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

public class MonoActivity extends AppCompatActivity
{
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Allo.i ("onCreate () @" + getClass ());

        try
        {
            requestWindowFeature (Window.FEATURE_NO_TITLE);
            requestWindowFeature (Window.FEATURE_ACTION_BAR_OVERLAY);
            getWindow ().requestFeature (Window.FEATURE_NO_TITLE);
            getWindow ().requestFeature (Window.FEATURE_ACTION_BAR_OVERLAY);
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } catch (Exception e) { e.printStackTrace (); }

        startMono ();
    }

    @Override
    public void finish ()
    {
        super.finish ();

        Allo.i ("finish () @" + getClass ());

        try
        {
            this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } catch (Exception e) { e.printStackTrace (); }
    }

    private void startMono ()
    {
        Allo.i ("startMono () @" + getClass ());

        try
        {
            (new Handler ()).postDelayed (new Runnable ()
            {
                @Override
                public void run ()
                {
                    Intent mono = new Intent (MonoActivity.this, MainActivity.class);
                    startActivity (mono); finish ();
                }
            }, Allo.DEF_MONO);
        } catch (Exception e) { e.printStackTrace (); }
    }
}
