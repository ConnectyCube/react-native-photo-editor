package com.ahmedadeltito.photoeditor;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoEncoderUtils {
    private static final String TAG = "VideoEncoderUtils";

    public static void combineVideoWithImage(Context context,
                                             final String resultDirectoryName,
                                             final String sourceVideoPath,
                                             final String imgPath,
                                             final VideoEncoderCallback callback)
    {
        try {
            final FFmpeg ffmpeg = FFmpeg.getInstance(context);
            ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    callback.onError("Error loading ffmpeg libraries");
                }

                @Override
                public void onSuccess() {
                    try {
                        final String resultVideoPath = getEditedFilePath(resultDirectoryName);
                        String[] cmd = prepareCommandForEncoder(sourceVideoPath, imgPath, resultVideoPath);

                        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                            @Override
                            public void onSuccess(String message) {
                                Log.v(TAG, "SUCCESS: " + message);
                                callback.onSuccess(resultVideoPath);
                            }

                            @Override
                            public void onProgress(String message) {
                                Log.v(TAG, "PROGRESS: " + message);
                            }

                            @Override
                            public void onFailure(String message) {
                                Log.v(TAG, "FAILURE: " + message);
                                callback.onError(message);
                            }

                            @Override
                            public void onStart() {
                                Log.v(TAG, "START");
                            }

                            @Override
                            public void onFinish() {
                                Log.v(TAG, "FINISH");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError(e.getMessage());
                    }
                }

                @Override public void onStart() {}
                @Override public void onFinish() {}
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e.getMessage());
        }
    }

    private static String[] prepareCommandForEncoder(String sourceVideoPath, String imgPath, String resultVideoPath) {
        return new String[]{
                "-y",
                "-i", sourceVideoPath,
                "-i", imgPath,
                "-filter_complex", "[0:v]crop=iw:ih [v1]; [1:v][v1]scale2ref[wm][v1];[v1][wm]overlay=0:0", //scale image to video size
                "-b:v", "8M", // bitrate video
                "-b:a", "128k", // bitrate audio
                "-c:v", "libx264",
                "-crf", "28",
                "-preset", "ultrafast",
                "-c:a", "copy",
                "-tune", "fastdecode",
                resultVideoPath
        };
    }

    private static String getEditedFilePath(String editedMediaDirectory) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoName = "VID_" + timeStamp + ".mp4";

        String folderName = editedMediaDirectory;
        if (TextUtils.isEmpty(folderName)) {
            folderName = "VideoEditor";
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), folderName);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d("VideoEditor", "Failed to create directory");
        }

        String selectedOutputPath = mediaStorageDir.getPath() + File.separator + videoName;
        Log.d("VideoEditor", "selectedOutputPath = " + selectedOutputPath);

        return selectedOutputPath;
    }

    public interface VideoEncoderCallback {
        void onSuccess(String resultVideoPath);

        void onError(String errorMessage);
    }
}
