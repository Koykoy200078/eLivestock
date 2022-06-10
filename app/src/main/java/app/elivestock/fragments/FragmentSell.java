package app.elivestock.fragments;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import app.elivestock.Config;
import app.elivestock.R;

public class FragmentSell extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final String TAG = "FragmentSell";
    String url = Config.MOBILE_PANEL_URL;
    String camPath;
    ValueCallback<Uri[]> f_string;
    ActivityResultLauncher<Intent> myARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Uri[] results = null;
                    if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        f_string.onReceiveValue(null);
                        return;
                    }

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (null == f_string) {
                            return;
                        }
                        ClipData clipData;
                        String stringData;
                        try {
                            clipData = Objects.requireNonNull(result.getData()).getClipData();
                            stringData = result.getData().getDataString();
                        } catch (Exception e) {
                            clipData = null;
                            stringData = null;
                        }

                        if (clipData == null && stringData == null && camPath != null) {
                            results = new Uri[]{Uri.parse(camPath)};
                        } else {
                            if (null != clipData) {
                                Log.d(TAG, "clipData: " + clipData);
                                final int numSelectedFiles = clipData.getItemCount();
                                results = new Uri[numSelectedFiles];
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    results[i] = clipData.getItemAt(i).getUri();
                                }
                            } else {
                                try {
                                    assert result.getData() != null;
                                    Bitmap camPhoto = (Bitmap) result.getData().getExtras().get("data");
                                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                                    camPhoto.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                                    MediaStore.Images.Media.insertImage(getContentResolver(), camPhoto, null, null);
                                } catch (Exception ignore) {
                                    results = new Uri[]{Uri.parse(stringData)};
                                }
                            }
                        }
                        f_string.onReceiveValue(results);
                        f_string = null;
                    }
                }
            });

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup Theme
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sell);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            }
        }

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setUseWideViewPort(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                f_string = filePathCallback;
                Intent takePictureIntent;
                takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(FragmentSell.this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = create_image();
                        takePictureIntent.putExtra("PhotoPath", camPath);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed!", ex);
                    }

                    if (photoFile != null) {
                        camPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                intentArray = new Intent[]{takePictureIntent};
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                myARL.launch(chooserIntent);
                return true;
            }
        });
    }

    private File create_image() throws IOException {
        String fileName = new SimpleDateFormat("d MM yyyy", Locale.getDefault()).format(new Date());
        String newName = "file_" + fileName + "_";
        File sdDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(newName, ".jpg", sdDirectory);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
            }
        }
    }
}