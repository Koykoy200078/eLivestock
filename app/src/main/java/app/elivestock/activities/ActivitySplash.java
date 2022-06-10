package app.elivestock.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import app.elivestock.Config;
import app.elivestock.R;

public class ActivitySplash extends AppCompatActivity {

    Boolean isCancelled = false;
    long id = 0;
    String url = "";
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        progressBar = findViewById(R.id.progressBar);

        if (getIntent().hasExtra("nid")) {
            id = getIntent().getLongExtra("nid", 0);
            url = getIntent().getStringExtra("external_link");
        }

        new Handler().postDelayed(() -> {

            progressBar.setVisibility(View.GONE);

            if (!isCancelled) {
                if (id == 0) {
                    if (url.equals("") || url.equals("no_url")) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent a = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(a);

                        Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(b);

                        finish();
                    }
                } else if (id == 1010101010) {

                    Intent a = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(a);

                    Intent b = new Intent(getApplicationContext(), ActivityHistory.class);
                    startActivity(b);

                    finish();

                } else {

                    Intent a = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(a);

                    Intent b = new Intent(getApplicationContext(), ActivityNotificationDetail.class);
                    b.putExtra("product_id", id);
                    startActivity(b);

                    finish();

                }
            }
        }, Config.SPLASH_TIME);

    }

}