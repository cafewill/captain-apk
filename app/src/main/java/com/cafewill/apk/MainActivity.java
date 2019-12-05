package com.cafewill.apk;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.cafewill.apk.common.*;
import org.apache.commons.io.FileUtils;

public class MainActivity extends AppCompatActivity
{
    private ListView mediaListView;
    private MediaAdapter mediaAdapter;
    private SwipeRefreshLayout swipeLayout;

    private long backPressedInterval = 0;
    private boolean permissionStatus = false;

    private List <Media> mediaList = new ArrayList <> ();
    private HashMap <String, Media> mediaHash = new HashMap ();

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
            setContentView (R.layout.activity_main);

            swipeLayout = (SwipeRefreshLayout) findViewById(R.id.list_layout);
            swipeLayout.setOnRefreshListener (new SwipeRefreshLayout.OnRefreshListener ()
            {
                @Override
                public void onRefresh ()
                {
                    final EditText searchText = (EditText) findViewById (R.id.search_text);
                    String keyword = searchText.getText ().toString ().trim ();
                    if (!keyword.isEmpty ())
                    {
                        searchMedia (keyword);
                    }
                    else
                    {
                        (new Handler ()).postDelayed (new Runnable() {
                            @Override
                            public void run () {
                                glanceMedia ();
                            }
                        }, 10);
                    }
                }
            });

            mediaListView = (ListView) findViewById (R.id.list_view);
            mediaAdapter = new MediaAdapter(getApplicationContext (), R.layout.media_list, mediaList, mediaListView);
            mediaListView.setAdapter (mediaAdapter);
            mediaListView.setOnItemClickListener (new OnItemClickListener ()
            {
                @Override
                public void onItemClick (AdapterView<?> parent, View view, int index, long id)
                {
                    Allo.i ("onItemClick () [" + index + "] @" + getClass ());

                    mediaAdapter.touchInfo (index);
                }
            });

            initActionBar ();
            clearNotification ();
            enableWritePermission ();

            (new Handler ()).postDelayed (new Runnable () {
                @Override
                public void run () {
                    glanceMedia ();
                }
            }, 10);
        } catch (Exception e) { e.printStackTrace (); }
    }

    @Override
    protected void onResume ()
    {
        super.onResume ();

        Allo.i ("onResume () @" + getClass ());
    }

    @Override
    public void onPause ()
    {
        super.onPause ();

        Allo.i ("onPause () @" + getClass ());
    }

    @Override
    public void onStop ()
    {
        super.onStop ();

        Allo.i ("onStop () @" + this.getClass ());
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy ();

        Allo.i ("onDestroy () @" + getClass ());
    }

    @Override
    public void onBackPressed ()
    {
        Allo.i ("onBackPressed () @" + getClass ());

        try
        {
            if (System.currentTimeMillis () > backPressedInterval)
            {
                backPressedInterval = System.currentTimeMillis() + Allo.DEF_BACKPRESSED;
                Toast.makeText (this, R.string.message_quitback, Toast.LENGTH_SHORT).show ();
            }
            else if (System.currentTimeMillis() <= backPressedInterval)
            {
                super.onBackPressed ();
            }
        } catch (Exception e) { e.printStackTrace (); }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        Allo.i ("onRequestPermissionsResult () @" + getClass ());

        try
        {
            switch (requestCode)
            {
                case Const.CODE_WRITE_STORAGE:
                    if (grantResults.length > 0 && PackageManager.PERMISSION_GRANTED == grantResults [0])
                    {
                        permissionStatus = true;
                        Toast.makeText (this, getString (R.string.message_permission_allowed), Toast.LENGTH_SHORT).show ();
                    }
                    else
                    {
                        permissionStatus = false;
                        Toast.makeText (this, getString (R.string.message_permission_disallowed), Toast.LENGTH_SHORT).show ();
                    }
            }
        } catch (Exception e) { e.printStackTrace (); }
    }

    ///////////////////////
    // @CUSTOMIZE
    ///////////////////////

    private void initActionBar ()
    {
        Allo.i ("initActionBar () @" + getClass ());

        try
        {
            Toolbar toolbar = (Toolbar) findViewById (R.id.controll_toolbar);
            toolbar.setPadding(0, 0, 0, 0);
            toolbar.setContentInsetsAbsolute(0, 0);
            setSupportActionBar (toolbar);
            getSupportActionBar ().setDisplayShowTitleEnabled (false);
            getSupportActionBar ().setDisplayShowCustomEnabled (true);
            getSupportActionBar ().setDisplayShowHomeEnabled (false);
            getSupportActionBar ().setCustomView (R.layout.toolbar);

            final EditText searchText = (EditText) findViewById (R.id.search_text);
            searchText.setOnEditorActionListener (new TextView.OnEditorActionListener ()
            {
                @Override
                public boolean onEditorAction (TextView v, int action, KeyEvent event)
                {
                    Allo.i ("onEditorAction [" + action + "] @" + getClass ());

                    boolean handled = false;
                    if (EditorInfo.IME_ACTION_SEND == action || EditorInfo.IME_ACTION_SEARCH == action)
                    {
                        hideKeyboard (v);
                        String keyword = v.getText ().toString ();
                        if (!keyword.isEmpty ())
                        {
                            searchMedia (keyword);
                        }
                        else
                        {
                            (new Handler ()).postDelayed (new Runnable() {
                                @Override
                                public void run () {
                                    glanceMedia ();
                                }
                            }, 10);
                        }

                        return true;
                    }
                    return handled;
                }
            });
        } catch (Exception e) { e.printStackTrace (); }
    }

    private void clearNotification ()
    {
        Allo.i ("clearNotification () @" + getClass ());

        try
        {
            ((NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE)).cancelAll ();
        } catch (Exception e) { e.printStackTrace (); }
    }

    private void glanceMedia ()
    {
        Allo.i ("glanceMedia () @" + getClass ());

        mediaHash.clear ();
        mediaList.clear ();
        if (null != mediaAdapter) mediaAdapter.notifyDataSetChanged ();
        if (null != swipeLayout) swipeLayout.setRefreshing (true);

        AlloData data = new AlloData ();
        data.setListener (new AlloData.Listener ()
        {
            public void onLoaded (HashMap <String, Media> medias)
            {
                Allo.i ("onLoaded () @" + getClass ());

                glanceMediaDone (medias);
            }

            public void onFailed ()
            {
                Allo.i ("onFailed () @" + getClass ());
            }
        });

        data.glanceMedia (getApplicationContext ());
    }

    private void glanceMediaDone (final HashMap <String, Media> medias)
    {
        Allo.i ("glanceMediaDone () @" + getClass ());

        try
        {
            mediaHash.clear ();
            mediaHash = medias;

            int count = 0;
            mediaList.clear ();
            List <String> keys = new ArrayList <String> (mediaHash.keySet ());
            Collections.sort (keys);
            for (String key : keys)
            {
                count++;
                mediaList.add (mediaHash.get (key));
            }
            if (null != swipeLayout) swipeLayout.setRefreshing (false);
            if (null != mediaAdapter) mediaAdapter.notifyDataSetChanged ();

            String message = getString (R.string.message_search_installed);
            message = message.replaceAll ("#NN#", "" + count);
            Toast.makeText (getApplicationContext (), message, Toast.LENGTH_SHORT).show ();
        } catch (Exception e) { e.printStackTrace (); }
    }

    private void searchMedia (final String keyword)
    {
        Allo.i ("searchMedia () @" + getClass ());

        try
        {
            mediaList.clear ();
            mediaListView.requestFocus ();
            if (null != swipeLayout) swipeLayout.setRefreshing (true);
            if (null != mediaAdapter) mediaAdapter.notifyDataSetChanged ();

            int count = 0;
            mediaList.clear ();
            List <String> keys = new ArrayList <String> (mediaHash.keySet ());
            Collections.sort (keys);
            for (String key : keys)
            {
                Media media = mediaHash.get (key);
                if (media.getName ().toLowerCase ().contains (keyword.toLowerCase ()) || media.getBundle ().toLowerCase ().contains (keyword.toLowerCase ()))
                {
                    count++;
                    mediaList.add (media);
                }
            }
            if (null != swipeLayout) swipeLayout.setRefreshing (false);
            if (null != mediaAdapter) mediaAdapter.notifyDataSetChanged ();

            String message = getString (R.string.message_search_installed);
            message = message.replaceAll ("#NN#", "" + count);
            Toast.makeText (getApplicationContext (), message, Toast.LENGTH_SHORT).show ();
        } catch (Exception e) { e.printStackTrace (); }
    }

    private void hideKeyboard (View v)
    {
        Allo.i ("hideKeyboard () @" + getClass ());

        try
        {
            InputMethodManager inputKeyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (null != inputKeyboard) inputKeyboard.hideSoftInputFromWindow (v.getWindowToken(), 0);
        } catch (Exception e) { e.printStackTrace (); }
    }

    public void enableWritePermission ()
    {
        Allo.i ("enableWritePermission () @" + getClass ());

        try
        {
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT)
            {
                if (PackageManager.PERMISSION_GRANTED == checkSelfPermission (Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    permissionStatus = true;
                }
                else
                {
                    ActivityCompat.requestPermissions (this, new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, Const.CODE_WRITE_STORAGE);
                }
            }
        } catch (Exception e) { e.printStackTrace (); }
    }

    public class MediaAdapter extends ArrayAdapter <Media>
    {
        private Context context;
        private List <Media> medias;
        private int resource;

        public MediaAdapter (Context context, int resource, List <Media> objects, ListView listView)
        {
            super (context, resource, objects);

            Allo.i ("MediaAdapter () @" + getClass ());

            try
            {
                this.context = context;
                this.resource = resource;
                this.medias = objects;
                mediaListView = listView;
            } catch (Exception e) { e.printStackTrace (); }
        }

        @Override @NonNull
        public View getView (final int index, @Nullable View view, @NonNull ViewGroup parent)
        {
            Allo.i ("getView () [" + index + "] @" + getClass ());

            View currentView = view;

            try
            {
                if (null == currentView) currentView = LayoutInflater.from (context).inflate (resource, parent, false);

                final Media media = this.medias.get (index);
                final Button infoButton = MediaHolder.get (currentView, R.id.media_info);
                final Button saveButton = MediaHolder.get (currentView, R.id.media_save);
                final ImageView mediaIcon = MediaHolder.get (currentView, R.id.media_icon);
                final TextView mediaName = MediaHolder.get (currentView, R.id.media_name);
                final TextView mediaSize = MediaHolder.get (currentView, R.id.media_size);
                final TextView mediaBundle = MediaHolder.get (currentView, R.id.media_bundle);
                final TextView mediaCategory = MediaHolder.get (currentView, R.id.media_category);

                String displayName = media.getName ();
                String displayBundle = media.getBundle ();
                String displayCategory = media.getCategory ();

                DecimalFormat sizeFormat = new DecimalFormat ("#.##");
                String displaySize = sizeFormat.format ((double) media.getSize () / (1024 * 1024)) + " MB";

                mediaName.setText (displayName);
                mediaSize.setText (displaySize);
                mediaBundle.setText (displayBundle);
                mediaCategory.setText (displayCategory);
                mediaIcon.setImageDrawable (media.getIcon ());
                infoButton.setOnClickListener (new OnClickListener ()
                {
                    @Override
                    public void onClick (View v)
                    {
                        touchInfo (index);
                    }
                });
                saveButton.setOnClickListener (new OnClickListener ()
                {
                    @Override
                    public void onClick (View v)
                    {
                        enableWritePermission ();
                        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT && permissionStatus == false) {
                            Toast.makeText (getApplicationContext (), getString (R.string.message_permission_disallowed), Toast.LENGTH_SHORT).show ();
                            return;
                        }

                        touchSave (media);
                    }
                });
            } catch (Exception e) { e.printStackTrace (); }

            return currentView;
        }

        public boolean touchInfo (int index)
        {
            Allo.i ("touchInfo () @" + getClass ());

            try
            {
                final Media media = this.medias.get (index);
                Intent intent = new Intent (Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory (Intent.CATEGORY_DEFAULT);
                intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags (Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.setData (Uri.parse (Const.PACKAGE + media.getBundle ()));
                startActivity (intent);
            } catch (Exception e) { e.printStackTrace (); }

            return true;
        }

        public void touchSave (Media media)
        {
            Allo.i ("touchSave () @" + getClass ());

            try
            {
                String name = media.getBundle ();
                String bundle = media.getBundle ();
                String version = media.getVersion ();

                String saveAs = bundle + "_" + version + "_" + name;
                saveAs = saveAs.replaceAll ("[:\\\\/*?|<>]", " ").replaceAll ("\\s+", "_");

                Toast.makeText (getApplicationContext (), getString (R.string.message_download) + " " + saveAs , Toast.LENGTH_SHORT).show ();

                (new SaveAsyncTask ()).executeOnExecutor (AsyncTask.THREAD_POOL_EXECUTOR, media);
            } catch (Exception e) { e.printStackTrace (); }
        }

        private final class SaveAsyncTask extends AsyncTask <Media, Void, String>
        {
            private NotificationManager notificationManager;
            private NotificationCompat.Builder notificationBuilder;
            private final int notificationId = (int) System.currentTimeMillis ();

            @Override
            protected String doInBackground (Media... params)
            {
                Allo.i ("doInBackground () @" + getClass ());

                try
                {
                    Media media = (Media) params [0];
                    String name = media.getName ();
                    String bundle = media.getBundle ();
                    String source = media.getSource ();
                    String version = media.getVersion ();
                    String savePath = Environment.DIRECTORY_DOWNLOADS;
                    File saveRoot = Environment.getExternalStoragePublicDirectory (savePath);

                    try
                    {
                        File origin = new File (source);
                        ZipFile zipFile = new ZipFile (source);

                        String saveAs = bundle + "_" + version + "_" + name;
                        saveAs = saveAs.replaceAll ("[:\\\\/*?|<>]", " ").replaceAll ("\\s+", "_");

                        ///////////////////////
                        // copy source to target (apk)
                        ///////////////////////

                        File target = new File (saveRoot, saveAs + ".apk"); FileUtils.copyFile (origin , target);

                        ///////////////////////
                        // listing dex, manifest & res/* (in apk)
                        ///////////////////////

                        int total = 0;

                        try
                        {
                            int count = 0;

                            Enumeration <? extends ZipEntry> entries = zipFile.entries ();
                            while (entries.hasMoreElements ())
                            {
                                ZipEntry entry = entries.nextElement ();
                                String each = entry.getName (); each = each.toLowerCase ();
                                if (each.startsWith (Const.RESOURCE.toLowerCase ()) || each.equals (Const.CLASSDEX.toLowerCase ()) || each.equals (Const.MANIFEST.toLowerCase ())) { count++; }
                            }
                            total = count;
                        } catch (Exception e) { e.printStackTrace(); }

                        try
                        {
                            int count = 0;
                            if (0 < total)
                            {
                                initNotification (name);
                                sendNotification (name, total, count);

                                ///////////////////////
                                // save to text file (no zip, because lite version)
                                ///////////////////////

                                FileOutputStream fileOS = new FileOutputStream (saveRoot + System.getProperty ("file.separator") + saveAs + "_meta.txt");

                                Enumeration <? extends ZipEntry> entries = zipFile.entries ();

                                while (entries.hasMoreElements ())
                                {
                                    ZipEntry entry = entries.nextElement ();
                                    String each = entry.getName ().trim (); each = each.toLowerCase ();

                                    if (each.startsWith (Const.RESOURCE.toLowerCase ()) || each.equals (Const.CLASSDEX.toLowerCase ()) || each.equals (Const.MANIFEST.toLowerCase ()))
                                    {
                                        count++;
                                        int inter = 1; if (10 < total) inter = (int) (total / 10);
                                        if (10 < total && (0 == (int) (count % inter))) { sendNotification (name, total, count); }
                                        fileOS.write (("(" + count + ") " + entry.getName ().trim () + System.getProperty ("line.separator")).getBytes ());
                                    }
                                }

                                fileOS.flush ();
                                fileOS.close ();

                                sendNotification (name, total, total);
                            }
                        } catch (Exception e) { e.printStackTrace(); } finally { if (null != zipFile) { try { zipFile.close (); } catch (Exception ignored) { } } }
                    } catch (Exception e) { e.printStackTrace(); }
                } catch (Exception e) { e.printStackTrace (); }

                return null;
            }

            private void initNotification (String title)
            {
                Allo.i ("initNotification () @" + getClass ());

                try
                {
                    String channelId = getApplicationContext ().getPackageName ();
                    String channelName = getApplicationContext ().getPackageName ();

                    if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT)
                    {
                        NotificationChannel channel = new NotificationChannel (channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                        channel.setShowBadge (false);
                        notificationManager = ((NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE));
                        notificationManager.createNotificationChannel (channel);
                        notificationBuilder = new NotificationCompat.Builder (getBaseContext (), channelId)
                                .setSmallIcon (R.drawable.ic_launcher_round)
                                .setContentTitle (title)
                                .setContentIntent (PendingIntent.getActivity (getBaseContext (), 0, (new Intent ()).addFlags (Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT));
                    }
                    else
                    {
                        notificationManager = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);
                        notificationBuilder = new NotificationCompat.Builder (getBaseContext (), channelId)
                                .setSmallIcon (R.drawable.ic_launcher_round)
                                .setContentTitle (title)
                                .setContentIntent (PendingIntent.getActivity (getBaseContext (), 0, new Intent (), PendingIntent.FLAG_UPDATE_CURRENT));
                    }
                } catch (Exception e) { e.printStackTrace (); }
            }

            private void sendNotification (String text, int total, int count)
            {
                Allo.i ("sendNotification () @" + getClass ());

                try
                {
                    int progress = 1;
                    String message = getString (R.string.message_download);
                    if (0 < total) { if (progress < (count * 100 / total)) progress = (count * 100 / total); }
                    if (0 == count) { message += " (files " + total + ")"; } else { message += " (files " + count + "/" + total + ")"; }
                    notificationBuilder.setProgress (100, progress, false);
                    notificationBuilder.setContentText (message); notificationManager.notify (notificationId, notificationBuilder.build ());
                } catch (Exception e) { e.printStackTrace (); }
            }
        }
    }
}
