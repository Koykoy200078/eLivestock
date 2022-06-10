package app.elivestock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

import app.elivestock.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText email, password, fullName, birthdate, address, contactNum;
    private RadioGroup radioGroup;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        firebaseAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        fullName = findViewById(R.id.fullName);
        birthdate = findViewById(R.id.birthdate);
        address = findViewById(R.id.address);
        contactNum = findViewById(R.id.contactNum);
        radioGroup = findViewById(R.id.radioButton);
        Button registerBtn = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);
        registerBtn.setOnClickListener(v -> {
            final String txt_email = Objects.requireNonNull(email.getText()).toString();
            final String txt_password = Objects.requireNonNull(password.getText()).toString();
            final String txt_fullname = Objects.requireNonNull(fullName.getText()).toString();
            final String txt_birthdate = Objects.requireNonNull(birthdate.getText()).toString();
            final String txt_address = Objects.requireNonNull(address.getText()).toString();
            final String txt_contactNumber = Objects.requireNonNull(contactNum.getText()).toString();
            int checkedId = radioGroup.getCheckedRadioButtonId();
            RadioButton selected_gender = radioGroup.findViewById(checkedId);
            if (selected_gender == null) {
                Toast.makeText(RegisterActivity.this, "Select your gender", Toast.LENGTH_SHORT).show();
            } else {
                final String gender = selected_gender.getText().toString();
                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password) || TextUtils.isEmpty(txt_fullname) || TextUtils.isEmpty(txt_birthdate) || TextUtils.isEmpty(txt_address) || TextUtils.isEmpty(txt_contactNumber)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    register(txt_email, txt_password, txt_fullname, txt_birthdate, txt_address, txt_contactNumber, gender);
                }
            }
        });
    }

    private void register(String txt_email, String txt_password, String txt_fullname, String txt_birthdate, String txt_address, String txt_contactNumber, String gender) {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(txt_email, txt_password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser rUser = firebaseAuth.getCurrentUser();
                assert rUser != null;
                String userId = rUser.getUid();
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("userId", userId);
                hashMap.put("email", txt_email);
                hashMap.put("fullname", txt_fullname);
                hashMap.put("birthdate", txt_birthdate);
                hashMap.put("address", txt_address);
                hashMap.put("contactNumber", txt_contactNumber);
                hashMap.put("gender", gender);
                //hashMap.put("imageUrl", "default");
                databaseReference.setValue(hashMap).addOnCompleteListener(task1 -> {
                    progressBar.setVisibility(View.GONE);
                    if (task1.isSuccessful()) {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}