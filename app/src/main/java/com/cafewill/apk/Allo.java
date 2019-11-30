package com.cafewill.apk;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Allo
{
    public static final boolean DEBUG_ECHO = true;

    public static final int DEF_MONO = 1200;
    public static final int DEF_BACKPRESSED = 3000;

    public static final String ECHO = "cube";
    public static void i (final String message) { if (DEBUG_ECHO) Log.i (ECHO, message); }
    public static void t (final Context context, String message) { if (DEBUG_ECHO) Toast.makeText (context, message, Toast.LENGTH_SHORT).show (); }
}
