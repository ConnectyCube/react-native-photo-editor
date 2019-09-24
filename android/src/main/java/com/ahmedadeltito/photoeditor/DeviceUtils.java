package com.ahmedadeltito.photoeditor;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

public class DeviceUtils {

    public static int getDeviceWidth(AppCompatActivity compatActivity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        compatActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getDeviceHeight(AppCompatActivity compatActivity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        compatActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
