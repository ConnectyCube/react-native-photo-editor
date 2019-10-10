
package ui.photoeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.ahmedadeltito.photoeditor.PhotoEditorActivity;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.HashMap;

public class RNPhotoEditorModule extends ReactContextBaseJavaModule {

  private class ResultCallbacks {
      public Callback onDoneCallback;
      public Callback onCancelCallback;

      ResultCallbacks(Callback onDoneCallback,Callback onCancelCallback) {
          this.onDoneCallback = onDoneCallback;
          this.onCancelCallback = onCancelCallback;
      }
  }

  private int PHOTO_EDITOR_REQUEST = 1;
  private static final String E_PHOTO_EDITOR_CANCELLED = "E_PHOTO_EDITOR_CANCELLED";

  private HashMap<String, ResultCallbacks> requestCodeCallbacksMap = new HashMap<>();

  private void addCallbacks(int requestCode, Callback onDoneCallback, Callback onCancelCallback) {
      ResultCallbacks callbacks = new ResultCallbacks(onDoneCallback, onCancelCallback);
      String key = String.valueOf(requestCode);
      requestCodeCallbacksMap.put(key, callbacks);
  }

  private void removeCallbacks(String requestCodeKey) {
      requestCodeCallbacksMap.remove(requestCodeKey);
  }

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        String requestCodeKey = String.valueOf(requestCode);
      if (requestCodeCallbacksMap.containsKey(requestCodeKey)) {
        Callback mDoneCallback = requestCodeCallbacksMap.get(requestCodeKey).onDoneCallback;
        Callback mCancelCallback = requestCodeCallbacksMap.get(requestCodeKey).onCancelCallback;
        if (mDoneCallback != null) {

          if (resultCode == Activity.RESULT_CANCELED) {
            mCancelCallback.invoke(resultCode);
          } else {
            Bundle resultIntentBundle = intent.getExtras();
            WritableMap result = Arguments.fromBundle(resultIntentBundle);
            Log.d("RNPhotoEditorModule", "edited file path = " + resultIntentBundle);
            mDoneCallback.invoke(result);
          }
        }

        removeCallbacks(requestCodeKey);
      }
    }
  };

  public RNPhotoEditorModule(ReactApplicationContext reactContext) {
    super(reactContext);

    reactContext.addActivityEventListener(mActivityEventListener);

  }



  @Override
  public String getName() {
    return "RNPhotoEditor";
  }

  @ReactMethod
  public void Edit(final ReadableMap props, final Callback onDone, final Callback onCancel) {
    Log.d("RNPhotoEditorModule", props.toString());
    String path = props.getString("path");
    String targetImageDirectoryName = props.getString("editedImageDirectory");
    String colorPrimary = props.getString("colorPrimary");

    //Process Stickers
    ReadableArray stickers = props.getArray("stickers");
    ArrayList<Integer> stickersIntent = new ArrayList<Integer>();

    for (int i = 0;i < stickers.size();i++) {
      int drawableId = getReactApplicationContext().getResources().getIdentifier(stickers.getString(i), "drawable", getReactApplicationContext().getPackageName());

      stickersIntent.add(drawableId);
    }

    //Process Hidden Controls
    ReadableArray hiddenControls = props.getArray("hiddenControls");
    ArrayList hiddenControlsIntent = new ArrayList<>();

    for (int i = 0;i < hiddenControls.size();i++) {
      hiddenControlsIntent.add(hiddenControls.getString(i));
    }

    //Process Colors
    ReadableArray colors = props.getArray("colors");
    ArrayList colorPickerColors = new ArrayList<>();

    for (int i = 0;i < colors.size();i++) {
      colorPickerColors.add(Color.parseColor(colors.getString(i)));
    }


    Intent intent = new Intent(getCurrentActivity(), PhotoEditorActivity.class);
    intent.putExtra("selectedImagePath", path);
    intent.putExtra("editedImageDirectory", targetImageDirectoryName);
    intent.putExtra("colorPrimary", colorPrimary);
    intent.putExtra("colorPickerColors", colorPickerColors);
    intent.putExtra("hiddenControls", hiddenControlsIntent);
    intent.putExtra("stickers", stickersIntent);

    PHOTO_EDITOR_REQUEST = (char)(Math.abs(path.hashCode()) / 65535);

    addCallbacks(PHOTO_EDITOR_REQUEST, onDone, onCancel);

    getCurrentActivity().startActivityForResult(intent, PHOTO_EDITOR_REQUEST);
  }
}
