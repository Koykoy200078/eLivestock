package app.elivestock.activities;

import static app.elivestock.utilities.Constant.GET_TAX_CURRENCY;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request.Method;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.io.IOException;

import app.elivestock.Config;
import app.elivestock.R;
import app.elivestock.fragments.FragmentCategory;
import app.elivestock.fragments.FragmentHelp;
import app.elivestock.fragments.FragmentProfile;
import app.elivestock.fragments.FragmentRecent;
import app.elivestock.utilities.AppBarLayoutBehavior;
import app.elivestock.utilities.DBHelper;

public class MainActivity extends AppCompatActivity {

    public ViewPager viewPager;
    View view;
    MenuItem prevMenuItem;
    int pager_number = 4;
    DBHelper dbhelper;
    private BottomNavigationView navigation;
    private Toolbar toolbar;
    private long exitTime = 0;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(android.R.id.content);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pager_number);

        navigation = findViewById(R.id.navigation);
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_recent:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.nav_category:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.nav_info:
                    viewPager.setCurrentItem(2);
                    return true;
                case R.id.nav_profile:
                    viewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 1) {
                    toolbar.setTitle(R.string.title_nav_category);
                } else if (viewPager.getCurrentItem() == 2) {
                    toolbar.setTitle(R.string.title_nav_help);
                } else if (viewPager.getCurrentItem() == 3) {
                    toolbar.setTitle(R.string.title_nav_profile);
                } else {
                    toolbar.setTitle(R.string.app_name);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        dbhelper = new DBHelper(this);
        try {
            dbhelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        dbhelper.openDataBase();

//        if (dbhelper.isPreviousDataExist()) {
//            showAlertDialog();
//        }

        makeJsonObjectRequest();

        if (Config.ENABLE_RTL_MODE) {
            viewPager.setRotationY(180);
        }

    }

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(getString(R.string.db_exist_alert));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.dialog_option_yes), (dialog, which) -> {
            dbhelper.deleteAllData();
            dbhelper.close();
        });

        builder.setNegativeButton(getString(R.string.dialog_option_no), (dialog, which) -> {
            dbhelper.close();
            dialog.cancel();
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem((0), true);
        } else {
            exitApp();
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, getString(R.string.msg_exit), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    private void makeJsonObjectRequest() {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, GET_TAX_CURRENCY, null, response -> {
            Log.d("INFO", response.toString());
            try {
                final Double tax = response.getDouble("tax");
                final String currency_code = response.getString("currency_code");

                ImageButton btn_cart = findViewById(R.id.btn_add_cart);
                btn_cart.setOnClickListener(v -> {
                    Intent intent = new Intent(getApplicationContext(), ActivityCart.class);
                    intent.putExtra("tax", tax);
                    intent.putExtra("currency_code", currency_code);
                    startActivity(intent);
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            VolleyLog.d("INFO", "Error: " + error.getMessage());
            Toast.makeText(getApplicationContext(),
                    error.getMessage(), Toast.LENGTH_SHORT).show();
        });
        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    public class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentRecent();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentHelp();
                case 3:
                    return new FragmentProfile();
                default:
                    throw new IllegalArgumentException("Invalid View type: " + position);
            }
        }

        @Override
        public int getCount() {
            return pager_number;
        }

    }
}
