package com.cafewill.apk.common;

import android.util.SparseArray;
import android.view.View;
import androidx.annotation.NonNull;

public class MediaHolder
{
	@SuppressWarnings("unchecked")
	public static <T extends View> T get (@NonNull View view, @NonNull int id)
	{
		SparseArray <View> viewHolder = (SparseArray <View>) view.getTag ();
		if (null == viewHolder)
		{
			viewHolder = new SparseArray <View> ();
			view.setTag (viewHolder);
		}
		View childView = viewHolder.get (id);
		if (null == childView)
		{
			childView = view.findViewById (id);
			viewHolder.put (id, childView);
		}
		return (T) childView;
	}
}
