package com.example.cloudstore.filepickernew.decoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;

import com.google.firebase.database.collection.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * The constant bookinRatePatern.
     */
    public static final Pattern bookinRatePatern = Pattern.compile("^\\d{0,3}(([.]\\d{1,2})|([.]))?$");
    /**
     * The constant bookinDepositePatern.
     */
    public static final Pattern bookinDepositePatern = Pattern.compile("^\\d{0,5}(([.]\\d{1,2})|([.]))?$");
    private static final String TAG = Utils.class.getSimpleName();
    /**
     * The constant BLOCK_CHARACTER_SET.
     */
    public static String BLOCK_CHARACTER_SET = "~#^|$%*!@/()-'\":;,?{}=!&$^';,?×÷<>...{}€£¥₩%~`¤♡♥_|《》¡¿°•○●□■◇◆♧♣▲▼▶◀↑↓←→☆★▪:-);-):-(:'(1234567890.";
    /**
     * The constant specialCharFilter. to filter special character in textField
     */
    public static InputFilter specialCharFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source != null && BLOCK_CHARACTER_SET.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };
    private static String imageEncoded;


    /*
    ^                 # start-of-string
    (?=.*[0-9])       # a digit must occur at least once
    (?=.*[a-z])       # a lower case letter must occur at least once
    (?=.*[A-Z])       # an upper case letter must occur at least once
    (?=.*[@\$%&#_()=+?»«<>£§€{}.])  # a special character must occur at least once you can replace with your special characters
    (?=\\S+$)          # no whitespace allowed in the entire string
    .{4,}             # anything, at least six places though
    (?<=.{4,})$")
    $
*/
    private static String mOutputFilePath = "";

    /**
     * Is empty string boolean.
     * String validation method
     *
     * @param object the object
     * @return the boolean
     */
    public static boolean isEmptyString(String object) {
        return !(object != null && !object.isEmpty() && !object.equalsIgnoreCase("null") && object.trim().length() > 0 && !object.equalsIgnoreCase("(null)"));
    }

    /**
     * Is empty boolean.
     *
     * @param text the text
     * @return the boolean
     */
    public static boolean isEmpty(String text) {

        return !(text != null && !text.equalsIgnoreCase("null") && !text.trim().equals(""));
    }

    /**
     * Is valid email boolean.
     *
     * @param email the email
     * @return the boolean
     */
    public static boolean isValidEmail(String email) {

        try {
            if (email != null && email.length() > 0) {

//                String EMAIL_PATTERN = "^[\\p{L}_A-Za-z0-9-\\+]+(\\.[\\p{L}_A-Za-z0-9-]+)*@" +
//                        "[\\p{L}A-Za-z0-9-]+(\\.[\\p{L}A-Za-z0-9]+)*(\\.[\\p{L}A-Za-z]{2,})$";

                String EMAIL_PATTERN = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\#]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+";
//[a-zA-Z0-9\+\.\_\%\-\#]{1,256}\@[a-zA-Z0-9][a-zA-Z0-9\-]{0,64}(\.[a-zA-Z0-9][a-zA-Z0-9\-]{0,25})
                Pattern pattern = Pattern.compile(EMAIL_PATTERN);
                Matcher matcher = pattern.matcher(email);

                return matcher.matches();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN =
                "^" +
                        "(?=.*\\d)" +         //# a digit must occur at least once
                        "(?=.*[a-z])" +       //# a lower case letter must occur at least once
                        "(?=.*[A-Z])" +       //# an upper case letter must occur at least once
                        "(?=.*[@\\$%&#_()=+?»«<>£§€{}.])" +    //# a special character must occur at least once you can replace with your special characters
                        "(?=\\S+$)" +         //# no whitespace allowed in the entire string
                        "(?<=.{8,20})" +        //# anything, at least 8 char & at most 20
                        "$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    public static boolean isComplexPassword(String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^((?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])|(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[^a-zA-Z0-9])|(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[^a-zA-Z0-9])|(?=.*?[a-z])(?=.*?[0-9])(?=.*?[^a-zA-Z0-9])).{12,64}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    /**
     * Gets bitmap from the selected image
     * file which is going to be upload
     *
     * @param file             the selected image file
     * @param requiredRotation if the rotation is required pass 1
     * @return the new bitmap of provided file (you can store it to memory)
     */
    public static Bitmap getBitmapFromFile(File file, int requiredRotation) {

        Bitmap bitmap = null;
        int imageHeight;
        int imageWidth;
        FileInputStream fis = null;
        int maxSize = 1280;
        try {
            if (file != null && file.exists()) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                fis = new FileInputStream(file);
                BitmapFactory.decodeStream(fis, null, options);
                imageHeight = options.outHeight;
                imageWidth = options.outWidth;

                int scaleFactor = 1;
                int newWidth = imageWidth;
                int newHeight = imageHeight;

                if (imageWidth > maxSize || imageHeight > maxSize) {

                    if (imageWidth > imageHeight) {
                        scaleFactor = imageWidth / maxSize;
                        newWidth = 1280;
                        newHeight = imageHeight * 1280 / imageWidth;
                    } else {
                        scaleFactor = imageHeight / maxSize;
                        newHeight = 1280;
                        newWidth = imageWidth * 1280 / imageHeight;
                    }
                }

                options.inJustDecodeBounds = false;
                options.inSampleSize = scaleFactor;

                fis = new FileInputStream(file);

                bitmap = BitmapFactory.decodeStream(fis, null, options);
                if (imageWidth != newWidth || imageHeight != newHeight) {
                    try {
                        float scaleWidth = ((float) newWidth) / bitmap.getWidth();
                        float scaleHeight = ((float) newHeight) / bitmap.getHeight();
                        Matrix matrix = new Matrix();
                        matrix.preRotate(requiredRotation);
                        matrix.postScale(scaleWidth, scaleHeight);
                        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        bitmap.recycle();
                        return resizedBitmap;
                    } catch (OutOfMemoryError | Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * Gets software version.
     *
     * @param mContext the m context
     * @return the software version
     */
    public static PackageInfo getSoftwareVersion(Context mContext) {
        PackageInfo pi;
        try {
            pi = mContext.getPackageManager().getPackageInfo(mContext.getApplicationContext().getPackageName(), 0);
            return pi;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Is valid name boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public static boolean isValidName(String name) {


        try {
            String USERNAME_PATTERN = "^[a-zA-Z]*$";

            Pattern pattern = Pattern.compile(USERNAME_PATTERN);
            Matcher matcher = pattern.matcher(name);

            return matcher.matches();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets output media file uri.
     *
     * @param mContext the context
     * @return the output media file uri
     */
    public static Uri getOutputMediaFileUri(Context mContext) {
        File mediaFile;
        String tempFileName = "img_temp_" + System.currentTimeMillis() + ".png";
        File dir = null;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                dir = new File(mContext.getExternalFilesDir(null) , "/iPlusImage");
            } else {
                dir = new File(mContext.getFilesDir() + "/iPlusImage");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dir == null) {
            dir = new File(Environment.getDataDirectory() + "/iPlusImage");
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
        mediaFile = new File(dir, tempFileName);
        return Uri.fromFile(mediaFile);
    }

    /**
     * Gets output media file uri for VstarCam.
     *
     * @param mContext the context
     * @return the output media file uri
     */
    public static Uri vstarGetOutputMediaFileUri(Context mContext) {
        File mediaFile;
        String tempFileName = "img_temp_" + System.currentTimeMillis() + ".png";
        File dir = null;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                dir = new File(Environment.getExternalStorageDirectory() + "/IPCam");
            } else {
                dir = new File(mContext.getFilesDir() + "/IPCam");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dir == null) {
            dir = new File(Environment.getDataDirectory() + "/IPCam");
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }
        mediaFile = new File(dir, tempFileName);
        return Uri.fromFile(mediaFile);
    }

    public static void addLog(String msg) {
        try {
            Date dt = new Date();
            File root = Environment.getExternalStorageDirectory();
            if (root.canWrite()) {
                File file = new File(root, "FermaxLog.txt");
                FileWriter writer = new FileWriter(file, true);
                BufferedWriter out = new BufferedWriter(writer);
                out.append("\n [" + dt.toString() + "]   " + msg + "\n");
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add jpg signature file to gallery.
     *
     * @param mContext  the context of an application
     * @param signature the signature bitmap taken from signature pad.
     * @return the string file path.
     */
    public static String addJpgSignatureToGallery(Context mContext, Bitmap signature) {
        boolean result = false;
        File photo = null;
        try {
            photo = createImageFile(mContext);
            saveBitmapToJPG(signature, photo);
            scanMediaFile(mContext, photo);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result && photo != null)
            return photo.getAbsolutePath();
        else
            return "";
    }

    private static void scanMediaFile(Context mContext, File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }

    /**
     * Save bitmap to jpg file in specific location.
     *
     * @param bitmap the bitmap
     * @param photo  the photo
     * @throws IOException the io exception
     */
    public static void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public static File createVideoFile(Context context) throws IOException {
        // Generate a unique file name for the video
        String videoFileName = "video_temp_" + System.currentTimeMillis();

        // Define the directory for storing the video
        File customStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);

        // Create a temporary file in the custom directory
        File videoFile = File.createTempFile(videoFileName, ".mp4", customStorageDir);

        // Ensure the file is not null; fallback creation if needed
        if (videoFile == null) {
            videoFile = new File(customStorageDir, videoFileName + ".mp4");
        }

        // Return the created video file
        return videoFile;
    }

    // create function for creating a PDF file
    public static File createPdfFile(Context context) throws IOException {
        String pdfFileName = "file_" + System.currentTimeMillis() + ".pdf";

        File customStorageDir = context.getCacheDir();

        File pdfFile = new File(customStorageDir, pdfFileName);

        if (!pdfFile.exists()) {
            pdfFile = new File(customStorageDir,pdfFileName+".pdf");
        }
        return pdfFile;
    }

    // Method to get MIME type based on file extension
    public static String getExtention(String mimeType) {
        String ext = "";

        // Check for common document types and map them to MIME types
        switch (mimeType) {
            case "application/msword":
                ext = "doc";
                break;
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                ext = "docx";
                break;
            case "application/pdf":
                ext = "pdf";
                break;
            case "text/plain":
                ext = "txt";
                break;
            case "application/vnd.ms-excel":
                ext = "xls";
                break;
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                ext = "xlsx";
                break;
            case "application/vnd.ms-powerpoint":
                ext = "ppt";
                break;
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                ext = "pptx";
                break;
        }

        return ext;
    }


    public static File createImageFile(Context context) throws IOException {
        String imageFileName = "img_temp_" + System.currentTimeMillis();
        File customStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(imageFileName, ".png", customStorageDir);
        if (image == null) {
            image = new File(customStorageDir, imageFileName);
        }
        mOutputFilePath = "file:" + image.getAbsolutePath();
        return image;
    }
}