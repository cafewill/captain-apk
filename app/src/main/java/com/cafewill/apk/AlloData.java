package com.cafewill.apk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.cafewill.apk.common.Media;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class AlloData
{
    private Listener listener;

    public void setListener (Listener listener) { this.listener = listener; }
    public interface Listener { public void onLoaded (HashMap <String, Media> medias); public void onFailed (); }

    public void glanceMedia (final Context context)
    {
        Allo.i ("glanceMedia () @" + getClass());

        try
        {
            new Thread ()
            {
                public void run ()
                {
                    glanceMediaHandler (context);
                }
            }.start ();

        } catch (Exception e) { e.printStackTrace (); }
    }

    private void glanceMediaHandler (final Context context)
    {
        Allo.i ("glanceMediaHandler () @" + getClass());

        try
        {
            final HashMap <String, Media> medias = new HashMap ();
            List <PackageInfo> infos = context.getPackageManager ().getInstalledPackages (0);
            for (int i = 0; i < infos.size (); i++)
            {
                PackageInfo info = infos.get (i);

                String category = "";
                if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT)
                {
                    int categoryNo = info.applicationInfo.category;
                    if (null != ApplicationInfo.getCategoryTitle (context, categoryNo))
                    {
                        CharSequence categoryChars = ApplicationInfo.getCategoryTitle (context, categoryNo);
                        category = categoryChars.toString ();
                    }
                }

                Drawable icon = info.applicationInfo.loadIcon (context.getPackageManager ());
                String name = info.applicationInfo.loadLabel (context.getPackageManager ()).toString ();
                String bundle = info.applicationInfo.packageName;
                String source = info.applicationInfo.sourceDir;
                File file = new File (source); int size = (int) file.length ();
                String version = context.getPackageManager ().getPackageInfo (bundle, 0).versionName;

                Allo.i ("----------");
                Allo.i ("check package name : " + name);
                Allo.i ("check package bundle : " + bundle);
                Allo.i ("check package version : " + version);
                Allo.i ("check package source : " + source);
                Allo.i ("check package system : " + isSystemPackage (info));
                Allo.i ("----------");

                Media media = new Media ();
                media.setIcon (icon);
                media.setName (name);
                media.setSize (size);
                media.setBundle (bundle);
                media.setSource (source);
                media.setVersion (version);
                media.setCategory (category);
                medias.put (name.toLowerCase (), media);
            }

            (new Handler (Looper.getMainLooper ())).postDelayed (new Runnable ()
            {
                @Override
                public void run ()
                {
                    if (null != listener) listener.onLoaded (medias);
                }
            }, 10);
        } catch (Exception e) { e.printStackTrace (); }
    }

    private boolean isSystemPackage (PackageInfo info)
    {
        Allo.i ("isSystemPackage () @" + getClass ());

        return ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
    }

}
