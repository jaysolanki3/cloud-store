package com.example.cloudstore.filepickernew.decoder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The type Path util.
 *
 * @author Vijay Desai
 */
public class PathUtil {

    private static final String TAG = PathUtil.class.getName();

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @return the path
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        Utils.addLog("Document File URI ::" + uri.toString() + "\n");
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            Utils.addLog("DocumentProvider && SDK > kitkat" + "\n");
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                Utils.addLog("isExternalStorageDocument :: true \n");
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    Utils.addLog("primary :: true " + type + " \n");
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                Utils.addLog("isDownloadsDocument :: true  \n");

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                Utils.addLog("contentUri :: " + id + " \n");
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                Utils.addLog("isMediaDocument :: true  \n");
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    Utils.addLog("image :: true  \n");
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    Utils.addLog("video :: true  \n");
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    Utils.addLog("audio :: true  \n");
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }

        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            Utils.addLog("MediaStore (and general)" + "\n");

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                Utils.addLog("isGooglePhotosUri :: true" + "\n");
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            Utils.addLog("file :: true " + uri.getScheme() + " \n");
            return uri.getPath();
        }

        return null;
    }

    /**
     * Gets image path.
     *
     * @param context    the context
     * @param contentUri the content uri
     * @return the image path
     */
    public static String getImagePath(Context context, Uri contentUri) {
        String res = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * Gets video path.
     *
     * @param context    the context
     * @param contentUri the content uri
     * @return the video path
     */
    public static String getVideoPath(Context context, Uri contentUri) {
        String res = null;
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                Utils.addLog("cursor found :: true  \n");
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Utils.addLog("cursor null :: true  \n");
        return null;
    }

    /**
     * Gets real path from uri.
     *
     * @param context    the context
     * @param contentUri the content uri
     * @return the real path from uri
     */
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    /**
     * Is external storage document boolean.
     *
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Is downloads document boolean.
     *
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Is media document boolean.
     *
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Is google photos uri boolean.
     *
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.     * @return Whether the Uri authority is Google Docs Photo.
     */
    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders
     *
     * @param context The activity context
     * @param uri     The Uri to query
     * @return path of image file
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(final Context context, final Uri uri) {

        try {

            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            Utils.addLog("Document File URI ::" + uri.toString() + "\n");

            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

                Utils.addLog("DocumentProvider && SDK > kitkat" + "\n");

                if (isExternalStorageDocument(uri)) {
                    Utils.addLog("isExternalStorageDocument is called" + "\n");
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        Utils.addLog("primary is called" + type + "\n");
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else {
                        Utils.addLog("primary not called" + type + "\n");
                        Pattern DIR_SEPARATOR = Pattern.compile("/");
                        Set<String> rv = new HashSet<>();
                        String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
                        String rawSecondaryStorageStr = System.getenv("SECONDARY_STORAGE");
                        String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
                        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
                            Utils.addLog("rawEmulatedStorageTarget :: true \n");
                            if (TextUtils.isEmpty(rawExternalStorage)) {
                                Utils.addLog("storage/sdcard0 :: true \n");
                                rv.add("/storage/sdcard0");
                            } else {
                                Utils.addLog("rawExternalStorage :: true \n");
                                rv.add(rawExternalStorage);
                            }
                        } else {
                            String rawUserId;
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                Utils.addLog("Below JELLY_BEAN_MR1 :: true \n");
                                rawUserId = "";
                            } else {
                                Utils.addLog("Not Below JELLY_BEAN_MR1 :: true \n");
                                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                                String[] folders = DIR_SEPARATOR.split(path);
                                String lastFolder = folders[folders.length - 1];
                                boolean isDigit = false;
                                try {
                                    Integer.valueOf(lastFolder);
                                    isDigit = true;
                                } catch (NumberFormatException ignored) {
                                }
                                rawUserId = isDigit ? lastFolder : "";
                            }
                            if (TextUtils.isEmpty(rawUserId)) {
                                Utils.addLog("rawUserId is Empty : true \n");
                                rv.add(rawEmulatedStorageTarget);
                            } else {
                                Utils.addLog("rawUserId is Empty : false \n");
                                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
                            }
                        }
                        if (!TextUtils.isEmpty(rawSecondaryStorageStr)) {
                            Utils.addLog("rawSecondaryStorageStr is Empty : false \n");
                            String[] rawSecondaryStorage = rawSecondaryStorageStr.split(File.pathSeparator);
                            Collections.addAll(rv, rawSecondaryStorage);
                        }
                        String[] temp = rv.toArray(new String[rv.size()]);
                        for (int i = 0; i < temp.length; i++) {
                            File tempf = new File(temp[i] + "/" + split[1]);
                            if (tempf.exists()) {
                                Utils.addLog(temp[i] + "/" + split[1] + "\n");
                                return temp[i] + "/" + split[1];
                            }
                        }
                    }
                } else if (isDownloadsDocument(uri)) {
                    Utils.addLog("isDownloadsDocument is called" + "\n");
                    String id = DocumentsContract.getDocumentId(uri);
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);

                } else if (isMediaDocument(uri)) {
                    Utils.addLog("isMediaDocument : true \n");
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] split = docId.split(":");
                    String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    String selection = "_id=?";
                    String[] selectionArgs = new String[]{split[1]};

                    Utils.addLog("isMediaDocument : " + contentUri + " " + selectionArgs + "\n");
                    return getDataColumn(context, contentUri, selection, selectionArgs);

                } else {
                    Utils.addLog("saveImageFromUri Save Image from Uri : \n");
                    return saveImageFromUri(context, uri, 100);
                }

            } else if ("content".equalsIgnoreCase(uri.getScheme())) {

                Utils.addLog("icontent .equalsIgnoreCase(uri.getScheme()) is called" + "\n");
                // Return the remote address
                if (isGooglePhotosUri(uri)) {
                    Utils.addLog("isGooglePhotosUri : true " + uri + "\n");
                    return uri.getLastPathSegment();
                } else if (isGoogleDriveUri(uri)) {
                    Utils.addLog("isGoogleDriveUri : true " + uri + "\n");
                    String name = getGDriveDataColumn(context, uri, null, null);
                    if (name != null) {
                        Utils.addLog("name  " + name + "\n");
                        int dot = name.lastIndexOf(".");
                        String ext = name.substring(dot + 1).toLowerCase();
                        if (ext.toLowerCase().equalsIgnoreCase("jpeg") || ext.toLowerCase().equalsIgnoreCase("jpg") || ext.toLowerCase().equalsIgnoreCase("png")) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
                                String fileName = "Img_" + (new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH)).format(new Date()) + ".png";
                                return saveBitmapToExternalStorage(context, bitmap, fileName, 100);//
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                return getDataColumn(context, uri, null, null);
                //return saveImageFromUri(context, uri, 100);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                Utils.addLog("getScheme has file : true " + uri.getScheme() + "\n");
                return uri.getPath();
            } else {
                Utils.addLog("Finally save image" + uri + "\n");
                return saveImageFromUri(context, uri, 100);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Utils.addLog("There is an excepton in above code. \n");
        }
        return null;
    }

    /**
     * Get bitmap from Uri and save it to sdcard
     *
     * @param context            activity context
     * @param uri                uri of the image file
     * @param compressPercentage the % of compression you want to apply while saving image
     * @return path of saved image
     */

    private static String saveImageFromUri(Context context, Uri uri, int compressPercentage) {

        int imageHeight;
        int imageWidth;

        try {

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            InputStream fis = context.getContentResolver().openInputStream(uri);
            try {
                BitmapFactory.decodeStream(fis, null, o);
                imageHeight = o.outHeight;
                imageWidth = o.outWidth;
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }

            int scaleFactor = 1;
            int newWidth = imageWidth;
            int newHeight = imageHeight;

            if (imageWidth > 1400 || imageHeight > 1400) {

                if (imageWidth > imageHeight) {
                    scaleFactor = imageWidth / 1400;
                    newWidth = 1280;
                    newHeight = imageHeight * 1280 / imageWidth;
                } else {
                    scaleFactor = imageHeight / 1400;
                    newHeight = 1280;
                    newWidth = imageWidth * 1280 / imageHeight;
                }
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scaleFactor;
            fis = context.getContentResolver().openInputStream(uri);

            try {
                Bitmap bitmap = BitmapFactory.decodeStream(fis, null, o2);

                if (imageWidth != newWidth || imageHeight != newHeight) {
                    try {
                        float scaleWidth = ((float) newWidth) / bitmap.getWidth();
                        float scaleHeight = ((float) newHeight) / bitmap.getHeight();
                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidth, scaleHeight);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                    } catch (OutOfMemoryError | Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    ExifInterface exif = new ExifInterface(uri.getPath());
                    if (exif != null) {
                        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        int rotationInDegrees = exifToDegrees(exifOrientation);
                        if (rotationInDegrees != 0) {
                            bitmap = rotateBitmap(bitmap, rotationInDegrees);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(new Date());
                String fileName = "Img_" + timeStamp + ".png";
                return saveBitmapToExternalStorage(context, bitmap, fileName, compressPercentage);

            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } catch (
                Exception e
        ) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getGDriveDataColumn(Context context, Uri uri, String selection,
                                             String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_display_name";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;

    }

    /**
     * It will give image rotation degree from exif parameter
     *
     * @param exifOrientation exif orientation value
     * @return degree to rotate the image
     */
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    /**
     * It will rotate bitmap to given degree
     *
     * @param bitmap  bitmap to rotate
     * @param degrees degree to apply rotation in bitmap
     * @return bitmap with rotation
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * It will save image on sdcard with given name
     *
     * @param context            activity context
     * @param bitmap             bitmap which needs to be save in sdcard
     * @param fileName           name of the file
     * @param compressPercentage the % of compression you want to apply while saving image
     * @return path of saved bitmap
     */
    public static String saveBitmapToExternalStorage(final Context context, final Bitmap bitmap, final String fileName, int compressPercentage) {
        try {

            File dir = new File(Utils.getOutputMediaFileUri(context).getPath());
            // File location to save image
            File mediaFile = new File(dir, fileName);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mediaFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, compressPercentage, fos);
                fos.flush();

                //Media scanner need to scan for the image saved
                Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri fileContentUri = Uri.fromFile(mediaFile);
                mediaScannerIntent.setData(fileContentUri);
                context.sendBroadcast(mediaScannerIntent);

                return mediaFile.getAbsolutePath();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception while trying to save file to internal storage: " + mediaFile + " " + e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception while trying to flush the output stream" + e);
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception wile trying to close file output stream." + e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * It will save image on sdcard with given name
     *
     * @param context            activity context
     * @param bitmap             bitmap which needs to be save in sdcard
     * @param fileName           name of the file
     * @param compressPercentage the % of compression you want to apply while saving image
     * @return path of saved bitmap
     */
    public static String saveBitmapToExternalStorage1(final Context context, final Bitmap bitmap, final String fileName, int compressPercentage) {
        try {

            File dir = new File(Utils.getOutputMediaFileUri(context).getPath());
            // File location to save image
            //File mediaFile = new File(dir, fileName);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(dir);
                bitmap.compress(Bitmap.CompressFormat.PNG, compressPercentage, fos);
                fos.flush();

                //Media scanner need to scan for the image saved
                Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri fileContentUri = Uri.fromFile(dir);
                mediaScannerIntent.setData(fileContentUri);
                context.sendBroadcast(mediaScannerIntent);

                return dir.getAbsolutePath();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception while trying to save file to internal storage: " + dir + " " + e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception while trying to flush the output stream" + e);
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception wile trying to close file output stream." + e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * It will save image on sdcard with given name
     *
     * @param context            activity context
     * @param bitmap             bitmap which needs to be save in sdcard
     * @param fileName           name of the file
     * @param compressPercentage the % of compression you want to apply while saving image
     * @return path of saved bitmap
     */
    public static String vstarSaveBitmapToExternalStorage(final Context context, final Bitmap bitmap, final String fileName, int compressPercentage) {
        try {

            File dir = new File(Utils.vstarGetOutputMediaFileUri(context).getPath());
            // File location to save image
            //File mediaFile = new File(dir, fileName);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(dir);
                bitmap.compress(Bitmap.CompressFormat.PNG, compressPercentage, fos);
                fos.flush();

                //Media scanner need to scan for the image saved
                Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri fileContentUri = Uri.fromFile(dir);
                mediaScannerIntent.setData(fileContentUri);
                context.sendBroadcast(mediaScannerIntent);

                return dir.getAbsolutePath();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception while trying to save file to internal storage: " + dir + " " + e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception while trying to flush the output stream" + e);
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception wile trying to close file output stream." + e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * It will return temporary directory path of the app
     *
     * @param context Activity context
     * @return Path of the temp directory
     */
    public static String getTempDirectoryPath(Context context) {
        File dirPath = null;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                dirPath = new File(Environment.getExternalStorageDirectory() + "/" + "cloud" + "/Images");
            } else {
                dirPath = new File(context.getFilesDir() + "/" + "cloud" + "/Images");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dirPath == null) {
            dirPath = new File(Environment.getDataDirectory() + "/" + "cloud" + "/Images");
        }

        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }

        return dirPath.getPath();
    }

    //Convert filepath to Uri
    public static String getPath(Uri _uri, Context context) {
        String filePath;
        Cursor returnCursor = context.getContentResolver().query(_uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        String size = Long.toString(returnCursor.getLong(sizeIndex));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(_uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream != null ? inputStream.available() : 0;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            outputStream.close();
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        filePath = file.getPath();
        if (filePath == null) {filePath = getPathFromUri(context, _uri);}
        if (filePath == null) {_uri.getPath();}
        return filePath;
    }

}