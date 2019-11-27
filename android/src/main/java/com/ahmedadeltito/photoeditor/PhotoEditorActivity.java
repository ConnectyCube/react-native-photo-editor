package com.ahmedadeltito.photoeditor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.MediaRouteButton;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.content.res.ResourcesCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ahmedadeltito.photoeditor.widget.SlidingUpPanelLayout;
import com.ahmedadeltito.photoeditor.widget.VerticalSlideColorPicker;
import com.ahmedadeltito.photoeditor.photoeditorsdk.BrushDrawingView;
import com.ahmedadeltito.photoeditor.photoeditorsdk.OnPhotoEditorSDKListener;
import com.ahmedadeltito.photoeditor.photoeditorsdk.PhotoEditorSDK;
import com.ahmedadeltito.photoeditor.photoeditorsdk.ViewType;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.viewpagerindicator.PageIndicator;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;

import ui.photoeditor.R;

import static android.view.inputmethod.InputMethodManager.SHOW_FORCED;
import static android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT;

public class PhotoEditorActivity extends AppCompatActivity implements View.OnClickListener, OnPhotoEditorSDKListener {
    public static Typeface emojiFont = null;

    private ArrayList<Typeface> typeFaces = null;
    private int currentTypeFaceIndex = 0;

    protected static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY = 0x1;
    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    private final String TAG = "PhotoEditorActivity";
    private RelativeLayout parentImageRelativeLayout;
    private VerticalSlideColorPicker mainColorPicker;
    private TextView undoTextView, undoTextTextView, changeBackgroundTextView, clearAllTextView, clearAllTextTextView, messageTextInput;
    private SlidingUpPanelLayout mLayout;
    private View topShadow;
    private RelativeLayout topShadowRelativeLayout;
    private View undoLayout;
    private View bottomShadowRelativeLayout;
    private ArrayList<Integer> colorPickerColors;
    private int colorCodeTextView = -259; // white color   ;
    private PhotoEditorSDK photoEditorSDK;
    private int imageOrientation;
    private ImageView backgroundImageView,  eraseDrawingImageView, brushDrawingImageView;
    private int currentBgColorIndex = 0;
    private FloatingActionButton doneDrawingFloatingAB;
    private Bitmap backgroundBitMap;
    private int currentBackgroundColor = 0;
    private int colorPrimary = Color.parseColor("#017525");
    private String defaultBackgroundColor = null;
    private boolean brushWasAdded = false;
    private boolean focusOnText = false;

    // CROP OPTION
    private boolean cropperCircleOverlay = false;
    private boolean freeStyleCropEnabled = false;
    private boolean showCropGuidelines = true;
    private boolean hideBottomControls = false;
    private boolean hideTextInput = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        loadFonts();
        initPrimaryColor();

        String selectedImagePath = getIntent().getExtras().getString("selectedImagePath");
        focusOnText = getIntent().getExtras().getBoolean("focusOnText", false);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        if (TextUtils.isEmpty(selectedImagePath)) {

            ArrayList<Integer> intentColors = (ArrayList<Integer>) getIntent().getExtras().getSerializable("colorPickerColors");

            colorPickerColors = new ArrayList<>();
            if (intentColors != null) {
                colorPickerColors = intentColors;
            } else {
                colorPickerColors.add(getResources().getColor(R.color.black));
                colorPickerColors.add(getResources().getColor(R.color.brown_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.red_orange_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.orange_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.yellow_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.yellow_green_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.green_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.sky_blue_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.blue_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.violet_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.red_color_picker));
                colorPickerColors.add(getResources().getColor(R.color.white));
            }

            Log.d(TAG, "colors = " + colorPickerColors);

            defaultBackgroundColor = getIntent().getExtras().getString("defaultBackgroundColor");
            if (!TextUtils.isEmpty(defaultBackgroundColor)) {
                currentBackgroundColor = Color.parseColor(defaultBackgroundColor);
            } else {
                Random randomColor = new Random();
                currentBgColorIndex = randomColor.nextInt(colorPickerColors.size());
                currentBackgroundColor = colorPickerColors.get(currentBgColorIndex);
            }

            backgroundBitMap = Bitmap.createBitmap(DeviceUtils.getDeviceWidth(this), DeviceUtils.getDeviceHeight(this), Bitmap.Config.RGB_565);
            Log.d(TAG, "bg color = " + currentBackgroundColor);
            imageOrientation = ExifInterface.ORIENTATION_NORMAL;
            backgroundBitMap.eraseColor(currentBackgroundColor);
        } else {
            if (selectedImagePath.contains("content://")) {
                selectedImagePath = getPath(Uri.parse(selectedImagePath));
            }

            backgroundBitMap = BitmapFactory.decodeFile(selectedImagePath, options);

            try {
                ExifInterface exif = new ExifInterface(selectedImagePath);
                imageOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                backgroundBitMap = rotateBitmap(backgroundBitMap, imageOrientation, false);
            } catch (IOException e) {
                imageOrientation = ExifInterface.ORIENTATION_NORMAL;
                e.printStackTrace();
            }

            Log.w(TAG, "Income to Editor image size: width = " + options.outWidth + " height = " + options.outHeight);
        }

        Typeface newFont = getFontFromRes(R.raw.eventtusicons);

        emojiFont = getFontFromRes(R.raw.emojioneandroid);

        BrushDrawingView brushDrawingView = findViewById(R.id.drawing_view);
        mainColorPicker = findViewById(R.id.main_color_picker);
        parentImageRelativeLayout = findViewById(R.id.parent_image_rl);
        TextView closeTextView = findViewById(R.id.close_tv);
        TextView addTextView = findViewById(R.id.add_text_tv);
        TextView addPencil = findViewById(R.id.add_pencil_tv);
        RelativeLayout deleteRelativeLayout = findViewById(R.id.delete_rl);
        TextView deleteTextView = findViewById(R.id.delete_tv);
        changeBackgroundTextView = findViewById(R.id.change_background_btn);
        TextView addImageEmojiTextView = findViewById(R.id.add_image_emoji_tv);
        TextView addCropTextView = findViewById(R.id.add_crop_tv);
//        TextView saveTextView = (TextView) findViewById(R.id.save_tv);
//        TextView saveTextTextView = (TextView) findViewById(R.id.save_text_tv);
        undoTextView = findViewById(R.id.undo_tv);
        undoTextTextView = findViewById(R.id.undo_text_tv);
        doneDrawingFloatingAB = findViewById(R.id.done_drawing_btn);
        eraseDrawingImageView = findViewById(R.id.erase_drawing_img);
        brushDrawingImageView = findViewById(R.id.brush_drawing_img);
        clearAllTextView = findViewById(R.id.clear_all_tv);
        clearAllTextTextView = findViewById(R.id.clear_all_text_tv);
        messageTextInput = findViewById(R.id.messageText);
        FloatingActionButton goToNextFAB = findViewById(R.id.go_to_next_screen_btn);
        backgroundImageView = findViewById(R.id.photo_edit_iv);
        mLayout = findViewById(R.id.sliding_layout);
        topShadow = findViewById(R.id.top_shadow);
        topShadowRelativeLayout = findViewById(R.id.top_parent_rl);
        undoLayout = findViewById(R.id.undo_layout);
        bottomShadowRelativeLayout = findViewById(R.id.bottom_parent_rl);

        messageTextInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageTextInput, InputMethodManager.SHOW_IMPLICIT);

        ViewPager pager = findViewById(R.id.image_emoji_view_pager);
        PageIndicator indicator = findViewById(R.id.image_emoji_indicator);

        backgroundImageView.setImageBitmap(backgroundBitMap);

        mainColorPicker.setColorPrimary(colorPrimary);
        mainColorPicker.setStartColor(Color.BLACK);

        closeTextView.setTypeface(newFont);
//        addTextView.setTypeface(newFont);
        addPencil.setTypeface(newFont);
        changeBackgroundTextView.setTypeface(newFont);
        changeBackgroundTextView.setVisibility(currentBackgroundColor == 0 ? View.GONE : View.VISIBLE);
        addImageEmojiTextView.setTypeface(newFont);
        addCropTextView.setTypeface(newFont);
//        saveTextView.setTypeface(newFont);
        undoTextView.setTypeface(newFont);
        clearAllTextView.setTypeface(newFont);
        deleteTextView.setTypeface(newFont);
        doneDrawingFloatingAB.setBackgroundTintList(ColorStateList.valueOf(colorPrimary));
        goToNextFAB.setBackgroundTintList(ColorStateList.valueOf(colorPrimary));

        final List<Fragment> fragmentsList = new ArrayList<>();

        ImageFragment imageFragment = new ImageFragment();
        ArrayList stickers = (ArrayList<Integer>) getIntent().getExtras().getSerializable("stickers");
        if (stickers != null && stickers.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("stickers", stickers);

            imageFragment.setArguments(bundle);
        }

        fragmentsList.add(imageFragment);

        EmojiFragment emojiFragment = new EmojiFragment();
        fragmentsList.add(emojiFragment);

        PreviewSlidePagerAdapter adapter = new PreviewSlidePagerAdapter(getSupportFragmentManager(), fragmentsList);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(5);
        indicator.setViewPager(pager);

        photoEditorSDK = new PhotoEditorSDK.PhotoEditorSDKBuilder(PhotoEditorActivity.this)
                .parentView(parentImageRelativeLayout) // add parent image view
                .childView(backgroundImageView) // add the desired image view
                .deleteView(deleteRelativeLayout) // add the deleted view that will appear during the movement of the views
                .brushDrawingView(brushDrawingView) // add the brush drawing view that is responsible for drawing on the image view
                .buildPhotoEditorSDK(); // build photo editor sdk
        photoEditorSDK.setOnPhotoEditorSDKListener(this);
        photoEditorSDK.setBrushEraserSize(50f);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    mLayout.setScrollableView(((ImageFragment) fragmentsList.get(position)).imageRecyclerView);
                else if (position == 1)
                    mLayout.setScrollableView(((EmojiFragment) fragmentsList.get(position)).emojiRecyclerView);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        closeTextView.setOnClickListener(this);
        addImageEmojiTextView.setOnClickListener(this);
        addCropTextView.setOnClickListener(this);
        changeBackgroundTextView.setOnClickListener(this);
        addTextView.setOnClickListener(this);
        addPencil.setOnClickListener(this);
//        saveTextView.setOnClickListener(this);
//        saveTextTextView.setOnClickListener(this);
        undoTextView.setOnClickListener(this);
        undoTextTextView.setOnClickListener(this);
        doneDrawingFloatingAB.setOnClickListener(this);
        eraseDrawingImageView.setOnClickListener(this);
        brushDrawingImageView.setOnClickListener(this);
        clearAllTextView.setOnClickListener(this);
        clearAllTextTextView.setOnClickListener(this);
        goToNextFAB.setOnClickListener(this);

        new CountDownTimer(500, 100) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                mLayout.setScrollableView(((ImageFragment) fragmentsList.get(0)).imageRecyclerView);
            }

        }.start();

        ArrayList hiddenControls = (ArrayList<Integer>) getIntent().getExtras().getSerializable("hiddenControls");
        for (int i = 0; i < hiddenControls.size(); i++) {
            if (hiddenControls.get(i).toString().equalsIgnoreCase("text")) {
                addTextView.setVisibility(View.INVISIBLE);
            }
            if (hiddenControls.get(i).toString().equalsIgnoreCase("clear")) {
                clearAllTextView.setVisibility(View.INVISIBLE);
                clearAllTextTextView.setVisibility(View.INVISIBLE);
            }
            if (hiddenControls.get(i).toString().equalsIgnoreCase("crop")) {
                addCropTextView.setVisibility(View.GONE);
            }
            if (hiddenControls.get(i).toString().equalsIgnoreCase("draw")) {
                addPencil.setVisibility(View.INVISIBLE);
            }
            if (hiddenControls.get(i).toString().equalsIgnoreCase("save")) {
//                saveTextTextView.setVisibility(View.INVISIBLE);
//                saveTextView.setVisibility(View.INVISIBLE);
            }
            if (hiddenControls.get(i).toString().equalsIgnoreCase("share")) {

            }
            if (hiddenControls.get(i).toString().equalsIgnoreCase("sticker")) {
                addImageEmojiTextView.setVisibility(View.INVISIBLE);
            }
            if (hiddenControls.get(i).toString().equalsIgnoreCase("input")) {
                messageTextInput.setVisibility(View.INVISIBLE);
                hideTextInput = true;
            }
        }

        Log.d(TAG, "focusOnText: = " + focusOnText);
        if (focusOnText) {
            goToNextFAB.post(new Runnable() {
                @Override
                public void run() {
                    openAddTextPopupWindow("", colorCodeTextView, typeFaces.get(currentTypeFaceIndex));
                }
            });
        }
    }

    private void loadFonts() {
        typeFaces = new ArrayList<>(Arrays.asList(
                ResourcesCompat.getFont(this, R.font.roboto_medium),
                ResourcesCompat.getFont(this, R.font.amiri),
                ResourcesCompat.getFont(this, R.font.finger_paint),
                ResourcesCompat.getFont(this, R.font.great_vibes),
                ResourcesCompat.getFont(this, R.font.passion_one)
        ));
    }

    private void initPrimaryColor() {
        String colorPrimary = getIntent().getExtras().getString("colorPrimary");
        if (!TextUtils.isEmpty(colorPrimary)) {
            try {
                int color = Color.parseColor(colorPrimary);
                if (color != 0) this.colorPrimary = color;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private boolean stringIsNotEmpty(String string) {
        if (string != null && !string.equals("null")) {
            if (!string.trim().equals("")) {
                return true;
            }
        }
        return false;
    }

    public void addEmoji(String emojiName) {
        photoEditorSDK.addEmoji(emojiName, emojiFont);
        if (mLayout != null)
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void addImage(Bitmap image) {
        photoEditorSDK.addImage(image);
        if (mLayout != null)
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void addText(String text, int colorCodeTextView, Typeface textTypeFace) {
        photoEditorSDK.addText(text, colorCodeTextView, textTypeFace);
    }

    private void changeBackgroundColor(int colorCode) {
        backgroundBitMap.eraseColor(colorCode);
        backgroundImageView.setImageBitmap(backgroundBitMap);
    }

    private void clearAllViews() {
        photoEditorSDK.clearAllViews();
        brushWasAdded = false;
        onRemoveViewListener(0);
    }

    private void undoViews() {
        photoEditorSDK.viewUndo();
    }

    private void eraseDrawing() {
        photoEditorSDK.brushEraser();
        toggleBrushModeIcons(false);
    }

    private void openAddTextPopupWindow(String text, final int colorCode, Typeface textTypeFace) {
        final int[] tempColor = new int[1];
        tempColor[0] = colorCode;

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(SHOW_FORCED, 0);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addTextPopupWindowRootView = inflater.inflate(R.layout.add_text_popup_window, null);

        final PopupWindow pop = new PopupWindow(PhotoEditorActivity.this);
        pop.setContentView(addTextPopupWindowRootView);
        pop.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setFocusable(true);
        pop.setBackgroundDrawable(null);
        pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        final TextView changeTextStyle = addTextPopupWindowRootView.findViewById(R.id.change_text_style);

        final EditText addTextEditText = addTextPopupWindowRootView.findViewById(R.id.add_text_edit_text);
        if (stringIsNotEmpty(text)) {
            addTextEditText.setTypeface(textTypeFace);
            addTextEditText.setText(text);
            addTextEditText.setTextColor(colorCode == 0 ? getResources().getColor(R.color.white) : colorCode);
        }

        addTextEditText.requestFocus();


        changeTextStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Typeface nextTextTypeFace = getNextTypeFace();
                addTextEditText.setTypeface(nextTextTypeFace);
            }
        });

        FloatingActionButton saveTextBtn = addTextPopupWindowRootView.findViewById(R.id.save_text_btn);
        saveTextBtn.setBackgroundTintList(ColorStateList.valueOf(colorPrimary));
        saveTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorCodeTextView = tempColor[0];
                addText(addTextEditText.getText().toString(), colorCodeTextView, addTextEditText.getTypeface());
                pop.dismiss();
            }
        });

        VerticalSlideColorPicker colorPicker = addTextPopupWindowRootView.findViewById(R.id.color_picker_vertical);
        colorPicker.setOnColorChangeListener(new VerticalSlideColorPicker.OnColorChangeListener() {
            @Override
            public void onColorChange(int selectedColor) {
                tempColor[0] = selectedColor;
                addTextEditText.setTextColor(selectedColor);
            }
        });
        colorPicker.setStartColor(colorCodeTextView == 0 ? getResources().getColor(R.color.white) : colorCodeTextView);
        colorPicker.setColorPrimary(colorPrimary);

        ImageView backBtn = addTextPopupWindowRootView.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
            }
        });

        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                imm.toggleSoftInput(SHOW_IMPLICIT, 0);
                updateView(View.VISIBLE);
            }
        });

        updateView(View.GONE);
        pop.showAtLocation(addTextPopupWindowRootView, Gravity.TOP, 0, 0);
    }

    private Typeface getNextTypeFace() {
        if (++currentTypeFaceIndex > (typeFaces.size() - 1)) {
            currentTypeFaceIndex = 0;
        }
        return typeFaces.get(currentTypeFaceIndex);
    }

    private int getNextBackgroundColor() {
        if (++currentBgColorIndex > (colorPickerColors.size() - 1)) {
            currentBgColorIndex = 0;
        }
        return colorPickerColors.get(currentBgColorIndex);
    }

    private void onChangeBackgroundColor() {
        currentBackgroundColor = getNextBackgroundColor();
        changeBackgroundColor(currentBackgroundColor);
    }

    private void updateView(int visibility) {
        topShadow.setVisibility(visibility);
        topShadowRelativeLayout.setVisibility(visibility);
        undoLayout.setVisibility(visibility);
        bottomShadowRelativeLayout.setVisibility(visibility);
        if (!hideTextInput) {
            messageTextInput.setVisibility(visibility);
        }
    }

    private void updateBrushDrawingView(boolean brushDrawingMode) {
        photoEditorSDK.setBrushDrawingMode(brushDrawingMode);
        if (brushDrawingMode) {
            updateView(View.GONE);
            mainColorPicker.setVisibility(View.VISIBLE);
            doneDrawingFloatingAB.show();
            eraseDrawingImageView.setVisibility(View.VISIBLE);
            brushDrawingImageView.setVisibility(View.VISIBLE);
            mainColorPicker.setOnColorChangeListener(new VerticalSlideColorPicker.OnColorChangeListener() {
                @Override
                public void onColorChange(int selectedColor) {
                    photoEditorSDK.setBrushColor(selectedColor);
                    toggleBrushModeIcons(true);
                }
            });
        } else {
            updateView(View.VISIBLE);
            mainColorPicker.setVisibility(View.INVISIBLE);
            doneDrawingFloatingAB.hide();
            eraseDrawingImageView.setVisibility(View.GONE);
            brushDrawingImageView.setVisibility(View.GONE);
            mainColorPicker.setOnColorChangeListener(null);
        }

        toggleBrushModeIcons(brushDrawingMode);
    }

    private void toggleBrushModeIcons(boolean isBrushActive) {
        if (isBrushActive){
            brushDrawingImageView.setBackground(getResources().getDrawable(R.drawable.rounded_border));
            eraseDrawingImageView.setBackground(null);
        } else {
            eraseDrawingImageView.setBackground(getResources().getDrawable(R.drawable.rounded_border));
            brushDrawingImageView.setBackground(null);
        }
    }

    private void returnBackWithSavedImage() {
        int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            updateView(View.GONE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            parentImageRelativeLayout.setLayoutParams(layoutParams);
            new CountDownTimer(1000, 500) {
                public void onTick(long millisUntilFinished) {

                }

                public void onFinish() {
                    Intent returnIntent = new Intent();

                    if (isSDCARDMounted()) {
                        String selectedOutputPath = getEditedFilePath();
                        String imageMessageText = messageTextInput.getText().toString();
                        returnIntent.putExtra("messageText", imageMessageText);
                        returnIntent.putExtra("imagePath", selectedOutputPath);

                        File file = new File(selectedOutputPath);

                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            if (parentImageRelativeLayout != null) {
                                parentImageRelativeLayout.setDrawingCacheEnabled(true);

                                Bitmap bitmap = parentImageRelativeLayout.getDrawingCache();
                                Bitmap rotatedBitmap = rotateBitmap(bitmap, imageOrientation, true);
                                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                            }

                            out.flush();
                            out.close();

                            try {
                                ExifInterface exifDest = new ExifInterface(file.getAbsolutePath());
                                exifDest.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(imageOrientation));
                                exifDest.saveAttributes();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception var7) {
                            var7.printStackTrace();
                        }
                    }

                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }.start();
            // Toast.makeText(this, getString(R.string.save_image_succeed), Toast.LENGTH_SHORT).show();
        } else {
            showPermissionRequest();
        }
    }


    private void returnBackWithUpdateImage() {
        updateView(View.GONE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        parentImageRelativeLayout.setLayoutParams(layoutParams);
        new CountDownTimer(1000, 500) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageName = "/IMG_" + timeStamp + ".jpg";

//                 String selectedImagePath = getIntent().getExtras().getString("selectedImagePath");
//                 File file = new File(selectedImagePath);
                String newPath = getCacheDir() + imageName;
                File file = new File(newPath);

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (parentImageRelativeLayout != null) {
                        parentImageRelativeLayout.setDrawingCacheEnabled(true);
                        Bitmap bitmap = parentImageRelativeLayout.getDrawingCache();
                        Bitmap rotatedBitmap = rotateBitmap(bitmap, imageOrientation, true);
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                    }

                    out.flush();
                    out.close();
                    try {
                        ExifInterface exifDest = new ExifInterface(file.getAbsolutePath());
                        exifDest.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(imageOrientation));
                        exifDest.saveAttributes();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception var7) {
                    var7.printStackTrace();
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra("imagePath", newPath);
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            }
        }.start();
    }

    private boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        return status.equals("mounted");
    }

    public void showPermissionRequest() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.access_media_permissions_msg));
        builder.setPositiveButton(getString(R.string.continue_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ActivityCompat.requestPermissions(PhotoEditorActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY);
            }
        });
        builder.setNegativeButton(getString(R.string.not_now), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(PhotoEditorActivity.this, getString(R.string.media_access_denied_msg), Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY) {
            // If request is cancelled, the result arrays are empty.
            int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                returnBackWithSavedImage();
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, getString(R.string.media_access_denied_msg), Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    @Override
    public void onBackPressed() {
        if (mainColorPicker.getVisibility() == View.VISIBLE) {
            updateBrushDrawingView(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_tv) {
            onBackPressed();
        } else if (v.getId() == R.id.add_image_emoji_tv) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        } else if(v.getId() == R.id.add_crop_tv) {
            startCropping(clearAllTextView.getVisibility() == View.VISIBLE || clearAllTextTextView.getVisibility() == View.VISIBLE);
        } else if (v.getId() == R.id.add_text_tv) {
            currentTypeFaceIndex = 0;
            openAddTextPopupWindow("", colorCodeTextView, typeFaces.get(currentTypeFaceIndex));
        } else if (v.getId() == R.id.add_pencil_tv) {
            updateBrushDrawingView(true);
        } else if (v.getId() == R.id.done_drawing_btn) {
            updateBrushDrawingView(false);
        } else if (v.getId() == R.id.save_tv || v.getId() == R.id.save_text_tv) {
            returnBackWithSavedImage();
        } else if (v.getId() == R.id.clear_all_tv || v.getId() == R.id.clear_all_text_tv) {
            clearAllViews();
        } else if (v.getId() == R.id.undo_text_tv || v.getId() == R.id.undo_tv) {
            undoViews();
        } else if (v.getId() == R.id.erase_drawing_img) {
            eraseDrawing();
        } else if (v.getId() == R.id.brush_drawing_img) {
            photoEditorSDK.setBrushDrawingMode(true);
            toggleBrushModeIcons(true);
        } else if (v.getId() == R.id.go_to_next_screen_btn) {
            returnBackWithSavedImage();
        } else if (v.getId() == R.id.change_background_btn) {
            onChangeBackgroundColor();
        }
    }

    @Override
    public void onEditTextChangeListener(String text, int colorCode, Typeface textTypeFace) {
        openAddTextPopupWindow(text, colorCode, textTypeFace);
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
        if (numberOfAddedViews > 0) {
            undoTextView.setVisibility(View.VISIBLE);
            undoTextTextView.setVisibility(View.VISIBLE);
        }

        clearAllTextView.setVisibility(brushWasAdded || numberOfAddedViews > 0 ? View.VISIBLE : View.GONE);
        clearAllTextTextView.setVisibility(brushWasAdded || numberOfAddedViews > 0 ? View.VISIBLE : View.GONE);

        switch (viewType) {
            case BRUSH_DRAWING:
                Log.i("BRUSH_DRAWING", "onAddViewListener");
                break;
            case EMOJI:
                Log.i("EMOJI", "onAddViewListener");
                break;
            case IMAGE:
                Log.i("IMAGE", "onAddViewListener");
                break;
            case TEXT:
                Log.i("TEXT", "onAddViewListener");
                break;
        }
    }

    @Override
    public void onRemoveViewListener(int numberOfAddedViews) {
        Log.i(TAG, "onRemoveViewListener");
        if (numberOfAddedViews == 0) {
            undoTextView.setVisibility(View.GONE);
            undoTextTextView.setVisibility(View.GONE);
        }

        clearAllTextView.setVisibility(!brushWasAdded && numberOfAddedViews == 0 ? View.GONE : View.VISIBLE);
        clearAllTextTextView.setVisibility(!brushWasAdded && numberOfAddedViews == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        switch (viewType) {
            case BRUSH_DRAWING:
                Log.i("BRUSH_DRAWING", "onStartViewChangeListener");
                break;
            case EMOJI:
                Log.i("EMOJI", "onStartViewChangeListener");
                break;
            case IMAGE:
                Log.i("IMAGE", "onStartViewChangeListener");
                break;
            case TEXT:
                Log.i("TEXT", "onStartViewChangeListener");
                break;
        }
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {
        switch (viewType) {
            case BRUSH_DRAWING:
                Log.i("BRUSH_DRAWING", "onStopViewChangeListener");
                brushWasAdded = true;
                clearAllTextView.setVisibility(View.VISIBLE);
                clearAllTextTextView.setVisibility(View.VISIBLE);
                break;
            case EMOJI:
                Log.i("EMOJI", "onStopViewChangeListener");
                break;
            case IMAGE:
                Log.i("IMAGE", "onStopViewChangeListener");
                break;
            case TEXT:
                Log.i("TEXT", "onStopViewChangeListener");
                break;
        }
    }

    private class PreviewSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;

        PreviewSlidePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments == null) {
                return (null);
            }
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private Typeface getFontFromRes(int resource) {
        Typeface tf = null;
        InputStream is = null;
        try {
            is = getResources().openRawResource(resource);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find font in resources!");
        }

        String outPath = getCacheDir() + "/tmp" + System.currentTimeMillis() + ".raw";

        try {
            byte[] buffer = new byte[is.available()];
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outPath));

            int l = 0;
            while ((l = is.read(buffer)) > 0)
                bos.write(buffer, 0, l);

            bos.close();

            tf = Typeface.createFromFile(outPath);

            // clean up
            new File(outPath).delete();
        } catch (IOException e) {
            Log.e(TAG, "Error reading in font!");
            return null;
        }

        Log.d(TAG, "Successfully loaded font.");

        return tf;
    }

    private void startCropping(boolean showApplyDialog) {
        if (showApplyDialog){
            showStartCropDialog();
        } else {
            startCropping();
        }
    }

    private void showStartCropDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startCropping();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setMessage(R.string.apply_changes_and_ctart_crop);

        builder.create().show();
    }

    private void startCropping() {
        final UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(100);
        options.setCircleDimmedLayer(cropperCircleOverlay);
        options.setFreeStyleCropEnabled(freeStyleCropEnabled);
        options.setShowCropGrid(showCropGuidelines);
        options.setHideBottomControls(hideBottomControls);
        options.setAllowedGestures(
                UCropActivity.ALL, // When 'scale'-tab active
                UCropActivity.ALL, // When 'rotate'-tab active
                UCropActivity.ALL  // When 'aspect ratio'-tab active
        );
        options.setActiveWidgetColor(colorPrimary);
        options.setToolbarColor(colorPrimary);
        options.setStatusBarColor(colorPrimary);

        if (parentImageRelativeLayout != null) {
            parentImageRelativeLayout.setDrawingCacheEnabled(true);

            Bitmap bitmap = parentImageRelativeLayout.getDrawingCache();

            Executors.newSingleThreadExecutor().submit(
                    new SaveBitmapToFileAction(
                            this,
                            bitmap,
                            imageOrientation,
                            null,
                            new ResultCallback<String>() {
                                @Override
                                public void onSuccess(String path) {
                                    System.out.println(path);
                                    UCrop uCrop = UCrop
                                            .of(Uri.fromFile(new File(path)),
                                                    Uri.fromFile(new File(getTmpImg(PhotoEditorActivity.this))))
                                            .withOptions(options);

                                    uCrop.start(PhotoEditorActivity.this);
                                }

                                @Override
                                public void onError(Exception e) {
                                    e.printStackTrace();
                                }
                            }));
        }
    }

    private static String getTmpImg(Context context) {
        context.getCacheDir().mkdir();

        return context.getCacheDir().getAbsolutePath() + File.separator + UUID.randomUUID().toString() + ".jpg";
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    try {
                        photoEditorSDK.clearAllViews();
                        brushWasAdded = false;
                        onRemoveViewListener(0);
                        parentImageRelativeLayout.destroyDrawingCache();
                        parentImageRelativeLayout.setDrawingCacheEnabled(false);
                        backgroundBitMap = MediaStore.Images.Media.getBitmap(this.getContentResolver() , resultUri);
                        backgroundImageView.setImageBitmap(backgroundBitMap);
                        changeBackgroundTextView.setVisibility(View.INVISIBLE);
                    } catch (Exception ex) {
                        System.out.println("NO IMAGE DATA FOUND");
                    }
                } else {
                    System.out.println("NO IMAGE DATA FOUND");
                }
            } else {
                System.out.println("NO RESULT");
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == UCrop.REQUEST_CROP){
            parentImageRelativeLayout.setDrawingCacheEnabled(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected String getPath(final Uri uri) {
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(this, uri)) {
            // ExternalStorageProvider
            if (GalleryUtils.isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (GalleryUtils.isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return GalleryUtils.getDataColumn(this, contentUri, null, null);
            }
            // MediaProvider
            else if (GalleryUtils.isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return GalleryUtils.getDataColumn(this, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return GalleryUtils.getDataColumn(this, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private String getEditedFilePath(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "IMG_" + timeStamp + ".jpg";

        String folderName = getIntent().getExtras().getString("editedImageDirectory");
        if (TextUtils.isEmpty(folderName)) {
            folderName = "PhotoEditorSDK";
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d("PhotoEditorSDK", "Failed to create directory");
        }

        String selectedOutputPath = mediaStorageDir.getPath() + File.separator + imageName;
        Log.d("PhotoEditorSDK", "selectedOutputPath = " + selectedOutputPath);

        return selectedOutputPath;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation, boolean reverse) {
        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);

                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);

                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);

                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                if (!reverse) {
                    matrix.setRotate(90);
                } else {
                    matrix.setRotate(-90);
                }

                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                if (!reverse) {
                    matrix.setRotate(90);
                } else {
                    matrix.setRotate(-90);
                }

                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                if (!reverse) {
                    matrix.setRotate(-90);
                } else {
                    matrix.setRotate(90);
                }

                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                if (!reverse) {
                    matrix.setRotate(-90);
                } else {
                    matrix.setRotate(90);
                }

                break;
            default:
                return bitmap;
        }

        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();

            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class SaveBitmapToFileAction implements Runnable {

        private final Bitmap bitmap;
        private final int imageOrientation;
        private String path;
        private final ResultCallback <String> callback;
        private final Context context;

        SaveBitmapToFileAction(Context context, Bitmap bitmap, int imageOrientation, String path, ResultCallback<String> callback) {
            this.context = context;
            this.bitmap = bitmap;
            this.imageOrientation = imageOrientation;
            this.path = path;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                if (TextUtils.isEmpty(path)){
                    path = getTmpImg(context);
                }

                File file = new File(path);

                FileOutputStream out = new FileOutputStream(file);
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                }

                out.flush();
                out.close();

                ExifInterface exifDest = new ExifInterface(file.getAbsolutePath());
                exifDest.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(imageOrientation));
                exifDest.saveAttributes();

                callback.onSuccess(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                callback.onError(e);
            }
        }
    }

    private interface ResultCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
