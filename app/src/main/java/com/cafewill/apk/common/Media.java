package com.cafewill.apk.common;

import android.graphics.drawable.Drawable;

public class Media
{
    private String name;
    private String bundle;
    private String source;
    private String version;
    private String category;
    private Integer size;
    private Drawable icon;

    public Drawable getIcon ()
    {
        return icon;
    }

    public void setIcon (Drawable icon)
    {
        this.icon = icon;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public Integer getSize ()
    {
        return size;
    }

    public void setSize (Integer size)
    {
        this.size = size;
    }

    public String getBundle ()
    {
        return bundle;
    }

    public void setBundle (String bundle)
    {
        this.bundle = bundle;
    }

    public String getSource ()
    {
        return source;
    }

    public void setSource (String source)
    {
        this.source = source;
    }

    public String getVersion ()
    {
        return version;
    }

    public void setVersion (String version)
    {
        this.version = version;
    }

    public String getCategory ()
    {
        return category;
    }

    public void setCategory (String category)
    {
        this.category = category;
    }
}

