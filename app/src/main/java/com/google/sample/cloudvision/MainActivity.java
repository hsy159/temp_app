/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cloudvision;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Handler;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogRecord;

import static android.graphics.ImageFormat.NV21;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    Camera camera = null;
    SurfaceHolder holder = null;
    //private VideoView videoView; // private 안하면 왜 안되는걸까
    private TextView textView;

    public static final double intercept = 0.467190807864;
    public static String arrayOfKey[] = {"family car", "automotive exterior", "driving", "area", "controlled access highway", "sky", "traffic sign", "sport utility vehicle", "skyscraper", "vehicle",
            "residential area", "highway", "metropolis", "tower block", "thoroughfare", "track", "guard rail", "suburb", "skyway", "traffic", "walkway", "mode of transport", "intersection",
            "structure", "building", "mixed use", "girder bridge", "road trip", "plaza", "tree", "plant", "skyline", "horizon", "minivan", "public transport", "sidewalk", "road",
            "bridge", "compact car", "asphalt", "fixed link", "overpass", "shoulder", "pedestrian", "urban design", "street", "transport", "traffic congestion", "real estate",
            "urban area", "neighbourhood", "road surface"};
    public static double arrayOfWeight[] = {0.001959506, -0.000343122, 0.001275561, 0.001599445, 0.000541903, 0.00268464, 0.000818015, -0.003733233, 0.000110012, -0.002045991, 0.000873594,
            0.000193858, -0.000141353, 0.004337384, -0.000696148, 3.61E-05, 0.001959947, 0.000340459, 0.000160082, 0.001247439, -0.002186815, 0.000794712, -0.000493615, -0.00198564, -0.000546126,
            -0.000261767, -0.003233404, 0.002249064, 0.001689095, -0.000727519, -0.001039317, -0.000664931, -0.000457226, 0.000439203, -0.003442012, -0.001587461, 0.001189973, 0.000109912, -0.000714525,
            -5.31E-05, -0.000120956, -0.001965535, 0.000462156, -0.001215803, 0.001701634, -0.002908464, -0.000100532, -0.00119247, 0.000177161, 0.00090039, 0.000377653, -5.68E-05};


    private static final String CLOUD_VISION_API_KEY = "AIzaSyAtc1naSTJMToiz137wrOwpWzpeFIeeYwc";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static int Time_CNT = 0;
    public static String message = "";
    private static boolean Button_State = false;

    private static final String VID1 = "VID_20170714_151823_2";
    private static final String VID2 = "VID_20170714_152054_2";
    private static final String VID3 = "VID_20170714_152406_2";
    private static final String VID4 = "VID_20170714_152906_2";
    private static String[] files1;
    private static String[] files2;
    private static String[] files3;
    private static String[] files4;
    private static InputStream is;
    private static Bitmap bmp;

    private Handler mHandler;
    private Runnable mRunnable;
    private TimerTask mTask;
    private Timer mTimer;

    private TextView mImageDetails;
    private ImageView mMainImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        final AssetManager assetManager = getAssets();
        try {
            files1 = assetManager.list(VID1);
            files2 = assetManager.list(VID2);
            files3 = assetManager.list(VID3);
            files4 = assetManager.list(VID4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable(){
                    public void run(){
                        if(Time_CNT < files3.length){
                            Time_CNT++;
                            try {
                                is = assetManager.open(VID3 + "/" + files3[Time_CNT]);
                                bmp = BitmapFactory.decodeStream(is);
                                mMainImage.setImageBitmap(bmp);
                                if(Time_CNT % 15 == 0){
                                    callCloudVision(bmp,true);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else{Time_CNT++;}
                    }
                });
            }
        };

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        //.setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
            }
        });


        mImageDetails = (TextView) findViewById(R.id.image_details);
        mMainImage = (ImageView) findViewById(R.id.main_image);
        // camera
//        videoView = (VideoView) findViewById(R.id.videoView);
        textView = (TextView) findViewById(R.id.textView);

//        camera = Camera.open();
//        Camera.Parameters params = camera.getParameters();
//        params.setPreviewFrameRate(10); // 초당 1번
//        params.setPreviewFpsRange(10000, 20000); // FPS min, max
//        params.setPreviewSize(640, 480); // 창 사이즈
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        params.setPreviewFormat(NV21);
//        camera.setDisplayOrientation(90); // 각도
//        camera.setParameters(params);
//
//        holder = videoView.getHolder();
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//        holder.addCallback(this);

    }
    @Override
    protected void onDestroy() {
        Log.i("test", "onDestory()");
        mTimer.cancel();
        super.onDestroy();
    }

    public void onClick(View v){
        if (!Button_State){
            Button_State = !Button_State;
            mTimer = new Timer();
            mTimer.schedule(mTask, 0,200);
        }
        else{
            Button_State = !Button_State;
            onDestroy();
        }
    }
    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData(), false);
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri, boolean Camera) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                /*Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);*/
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if (Camera == true) {
                    callCloudVision(bitmap, true);
                } else {
                    callCloudVision(bitmap, false);
                }
                mMainImage.setImageBitmap(bitmap); // 이미지 보여주는 화면 나중엔 이거 없애야 함

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap, final boolean Camera) throws IOException {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null); // null은 http request initializer
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        if (Camera == true) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        } else {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
                        }

                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                        //ImageIO.write(imageBytes, "JPEG" , new File("filename.jpg"));

                        String text = new String(imageBytes, StandardCharsets.US_ASCII);


                        // Base64 encode the JPEG
                        String base64_test = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                        //Log.e("encoding test",base64_test);
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);
                        //base64EncodedImage.get
                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(40);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                mImageDetails.setText(result);
            }
        }.execute();
    }


    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        boolean wordExists = false;
        boolean roadExists;
        boolean roadExists2;
        double sum = 0;

        Hashtable<String, Double> weight_table = new Hashtable();
        for (int i = 0; i < arrayOfKey.length; i++) {
            weight_table.put(arrayOfKey[i], arrayOfWeight[i]);
        }

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
//        if (labels != null) {
//            for (EntityAnnotation label : labels) {
//                message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
//                message += "\n";
//            }
//        } else {
//            message += "nothing";
//        }
        roadExists = weight_table.containsKey("road");
        roadExists2 = weight_table.containsKey("Road");
        if ((roadExists | roadExists2) == false) {
            message += "도로가 아닙니다";
            return message;
        }

        for (EntityAnnotation label : labels) {
            wordExists = weight_table.containsKey(label.getDescription());
            if (wordExists) {
                sum += weight_table.get(label.getDescription()) * label.getScore() * 100;
            }
        }
        sum += intercept;

        message += "Cloud Test 값: " + sum + "\n";
        Log.d("Cloud CNT", Time_CNT/2 + "  " + sum);
        if (sum < 0.4) {
            message += "고가도로 밑입니다.\n";
            Log.d("Cloud Test", "고가 밑  " + sum);
        } else {
            message += "고가도로 위입니다.\n";
            Log.d("Cloud Test", "고가 위  " + sum);
        }
        return message;
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        if(Time_CNT/2 < files3.length) {
//            try {
//                //Log.d("CNT test", "" + Time_CNT/2);
//                is = getBaseContext().getAssets().open(VID3 + "/" + files2[Time_CNT/3]);
//                bmp = BitmapFactory.decodeStream(is);
//                //mMainImage.setImageBitmap(bmp);
//                if (Time_CNT % 30 == 0) {
//                    //Log.d("cloud image", "" + files2[Time_CNT/3]);
//                    //message += files2[Time_CNT/2].toString() + '\n';
//                    //callCloudVision(bmp, true);
//                }
//                Time_CNT++;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        else{
//            Time_CNT++;
//        }
    }


    //Log.e("test", "format:" + camera.getParameters().getSupportedPreviewFormats());

//        YuvImage yuv = new YuvImage(data, NV21, camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, null);
//        Rect rect = new Rect (0, 0, camera.getParameters ().getPreviewSize().width, camera.getParameters ().getPreviewSize().height);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
//        yuv.compressToJpeg (rect, 100, baos);
//        Bitmap bmp = BitmapFactory.decodeByteArray (baos.toByteArray (), 0, baos.size ());
//
//        textView.setText("DATA " + data.toString());
//        textView.setHighlightColor(Color.BLACK);
//        textView.setTextColor(Color.WHITE);
//        textView.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);
//        Time_CNT++;
//        if(Time_CNT % 200 == 0){
//            try {
//                callCloudVision(bmp,true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.unlock();
            camera.reconnect();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //camera.setPreviewCallback(this); // 얘가 onPreviewFrame()함수를 호출
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}

