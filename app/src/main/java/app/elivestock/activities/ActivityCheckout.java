package app.elivestock.activities;

import static app.elivestock.utilities.Constant.GET_SHIPPING;
import static app.elivestock.utilities.Constant.POST_ORDER;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import app.elivestock.Config;
import app.elivestock.R;
import app.elivestock.models.UsersData;
import app.elivestock.utilities.DBHelper;
import app.elivestock.utilities.SharedPref;

public class ActivityCheckout extends AppCompatActivity {

    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    RequestQueue requestQueue;
    Button btn_submit_order;
    EditText edt_name, edt_email, edt_phone, edt_address, edt_shipping, edt_order_list, edt_order_total, edt_comment;
    String str_name, str_email, str_phone, str_address, str_shipping, str_order_list, str_order_total, str_comment;
    String data_order_list = "";
    double str_tax;
    String str_currency_code;
    ProgressDialog progressDialog;
    DBHelper dbhelper;
    ArrayList<ArrayList<Object>> data;
    View view;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    String date = dateFormat.format(Calendar.getInstance().getTime());
    SharedPref sharedPref;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    UsersData usersData;
    private final String rand = getRandomString();
    private Spinner spinner;
    private ArrayList<String> arrayList;
    private JSONArray result;

    private static String getRandomString() {
        final Random random = new Random();
        final StringBuilder stringBuilder = new StringBuilder(9);
        for (int i = 0; i < 9; ++i)
            stringBuilder.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return stringBuilder.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        view = findViewById(android.R.id.content);

        edt_name = findViewById(R.id.edt_name);
        edt_email = findViewById(R.id.edt_email);
        edt_phone = findViewById(R.id.edt_phone);
        edt_address = findViewById(R.id.edt_address);
        edt_shipping = findViewById(R.id.edt_shipping);
        edt_order_list = findViewById(R.id.edt_order_list);
        edt_order_total = findViewById(R.id.edt_order_total);
        edt_comment = findViewById(R.id.edt_comment);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        assert firebaseUser != null;
        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(firebaseUser.getUid());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersData = dataSnapshot.getValue(UsersData.class);
                edt_name.setText(Objects.requireNonNull(usersData).getFullname());
                edt_email.setText(usersData.getEmail());
                edt_phone.setText(usersData.getContactNumber());
                edt_address.setText(usersData.getAddress());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        sharedPref = new SharedPref(this);

        setupToolbar();
        getSpinnerData();
        getTaxCurrency();

        dbhelper = new DBHelper(this);
        dbhelper.openDataBase();

        // Creating Volley newRequestQueue
        requestQueue = Volley.newRequestQueue(ActivityCheckout.this);
        progressDialog = new ProgressDialog(ActivityCheckout.this);

        btn_submit_order = findViewById(R.id.btn_submit_order);

        edt_order_list.setEnabled(false);

        getDataFromDatabase();
        submitOrder();

    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_checkout);
        }
    }

    private void getSpinnerData() {

        arrayList = new ArrayList<>();
        spinner = findViewById(R.id.spinner);

        StringRequest stringRequest = new StringRequest(GET_SHIPPING, response -> {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                result = jsonObject.getJSONArray("result");
                getShipping(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    edt_shipping.setText(setShipping(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        },
                error -> {

                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void getShipping(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject json = jsonArray.getJSONObject(i);
                arrayList.add(json.getString("shipping_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(ActivityCheckout.this, R.layout.spinner_item, arrayList);
        myAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
    }

    private String setShipping(int position) {
        String name = "";
        try {
            JSONObject json = result.getJSONObject(position);
            name = json.getString("shipping_name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    public void submitOrder() {
        btn_submit_order.setOnClickListener(v -> getValueFromEditText());
    }

    public void getValueFromEditText() {

        str_name = edt_name.getText().toString();
        str_email = edt_email.getText().toString();
        str_phone = edt_phone.getText().toString();
        str_address = edt_address.getText().toString();
        str_shipping = edt_shipping.getText().toString();
        str_order_list = edt_order_list.getText().toString();
        str_order_total = edt_order_total.getText().toString();
        str_comment = edt_comment.getText().toString();

        if (str_name.equalsIgnoreCase("") ||
                str_email.equalsIgnoreCase("") ||
                str_phone.equalsIgnoreCase("") ||
                str_address.equalsIgnoreCase("") ||
                str_shipping.equalsIgnoreCase("") ||
                str_order_list.equalsIgnoreCase("")) {
            Snackbar.make(view, R.string.checkout_fill_form, Snackbar.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.checkout_dialog_title);
            builder.setMessage(R.string.checkout_dialog_msg);
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.dialog_option_yes), (dialog, which) -> requestAction());
            builder.setNegativeButton(getResources().getString(R.string.dialog_option_no), null);
            builder.setCancelable(false);
            builder.show();
        }
    }

    public void requestAction() {

        progressDialog.setTitle(getString(R.string.checkout_submit_title));
        progressDialog.setMessage(getString(R.string.checkout_submit_msg));
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, POST_ORDER, ServerResponse -> {

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                progressDialog.dismiss();
                dialogSuccessOrder();
            }, 2000);

        },
                volleyError -> {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {

                if (OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId() == null) {

                    Map<String, String> params = new HashMap<>();
                    params.put("code", rand);
                    params.put("name", str_name);
                    params.put("email", str_email);
                    params.put("phone", str_phone);
                    params.put("address", str_address);
                    params.put("shipping", str_shipping);
                    params.put("order_list", str_order_list);
                    params.put("order_total", str_order_total);
                    params.put("comment", str_comment);
                    params.put("player_id", "0");
                    params.put("date", date);
                    params.put("server_url", Config.ADMIN_PANEL_URL);

                    return params;

                } else {

                    Map<String, String> params = new HashMap<>();
                    params.put("code", rand);
                    params.put("name", str_name);
                    params.put("email", str_email);
                    params.put("phone", str_phone);
                    params.put("address", str_address);
                    params.put("shipping", str_shipping);
                    params.put("order_list", str_order_list);
                    params.put("order_total", str_order_total);
                    params.put("comment", str_comment);
                    params.put("player_id", OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId());
                    params.put("date", date);
                    params.put("server_url", Config.ADMIN_PANEL_URL);

                    return params;

                }
            }

        };

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        RequestQueue requestQueue = Volley.newRequestQueue(ActivityCheckout.this);
        requestQueue.add(stringRequest);
    }

    public void getTaxCurrency() {
        Intent intent = getIntent();
        str_tax = intent.getDoubleExtra("tax", 0);
        str_currency_code = intent.getStringExtra("currency_code");
    }

    @SuppressLint("SetTextI18n")
    public void getDataFromDatabase() {

        data = dbhelper.getAllData();

        double Order_price = 0;
        double Total_price;
        double tax;

        for (int i = 0; i < data.size(); i++) {
            ArrayList<Object> row = data.get(i);

            String Menu_name = row.get(1).toString();
            String Quantity = row.get(2).toString();


            double Sub_total_price = Double.parseDouble(row.get(3).toString());

            String _Sub_total_price = String.format(Locale.ENGLISH, "%1$,.0f", Sub_total_price);

            Order_price += Sub_total_price;

            if (Config.ENABLE_DECIMAL_ROUNDING) {
                data_order_list += (Quantity + " " + Menu_name + " " + _Sub_total_price + " " + str_currency_code + ",\n");
            } else {
                data_order_list += (Quantity + " " + Menu_name + " " + Sub_total_price + " " + str_currency_code + ",\n");
            }
        }

        if (data_order_list.equalsIgnoreCase("")) {
            data_order_list += getString(R.string.no_order_menu);
        }

        tax = Order_price * (str_tax / 100);
        Total_price = Order_price + tax;

        String price_tax = String.format(Locale.ENGLISH, "%1$,.0f", str_tax);
        String _Order_price = String.format(Locale.ENGLISH, "%1$,.0f", Order_price);
        String _tax = String.format(Locale.ENGLISH, "%1$,.0f", tax);
        String _Total_price = String.format(Locale.ENGLISH, "%1$,.0f", Total_price);

        if (Config.ENABLE_DECIMAL_ROUNDING) {
            data_order_list += "\n" +
                    getResources().getString(R.string.txt_order) + " " + _Order_price + " " + str_currency_code + "\n" +
                    getResources().getString(R.string.txt_tax) + " " + price_tax + " % : " + _tax + " " + str_currency_code + "\n" +
                    getResources().getString(R.string.txt_total) + " " + _Total_price + " " + str_currency_code;


            edt_order_total.setText(_Total_price + " " + str_currency_code);

        } else {
            data_order_list += "\n" + getResources().getString(R.string.txt_order) + " " + Order_price + " " + str_currency_code +
                    "\n" + getResources().getString(R.string.txt_tax) + " " + str_tax + " % : " + tax + " " + str_currency_code +
                    "\n" + getResources().getString(R.string.txt_total) + " " + Total_price + " " + str_currency_code;

            edt_order_total.setText(Total_price + " " + str_currency_code);
        }

        edt_order_list.setText(data_order_list);

    }

    public void dialogSuccessOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.checkout_success_title);
        builder.setMessage(R.string.checkout_success_msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.checkout_option_ok, (dialog, which) -> {
            dbhelper.addDataHistory(rand, str_order_list, str_order_total, date);
            dbhelper.deleteAllData();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        AlertDialog alert = builder.create();
        alert.show();
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
    public void onResume() {
        edt_name.setText(sharedPref.getYourName());
        edt_email.setText(sharedPref.getYourEmail());
        edt_phone.setText(sharedPref.getYourPhone());
        edt_address.setText(sharedPref.getYourAddress());
        super.onResume();
    }

}
