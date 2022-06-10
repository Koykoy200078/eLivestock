package app.elivestock.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import app.elivestock.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView resetState;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> startActivity(new Intent(getApplicationContext(), LoginActivity.class)));

        mAuth = FirebaseAuth.getInstance();
        EditText emailAddress = findViewById(R.id.email);
        resetState = findViewById(R.id.resetText);
        Button resetBtn = findViewById(R.id.resetPasswordBtn);
        progressBar = findViewById(R.id.progressBar);
        resetBtn.setOnClickListener(v ->
        {
            progressBar.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(emailAddress.getText())) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Please input your email address to reset your password", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.fetchSignInMethodsForEmail(Objects.requireNonNull(emailAddress.getText()).toString()).addOnCompleteListener(task ->
                {
                    if (Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getSignInMethods()).isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        resetState.setText("The email you provided is not yet registered to our database.");
                    } else {
                        mAuth.sendPasswordResetEmail(emailAddress.getText().toString()).addOnCompleteListener(task1 ->
                        {
                            progressBar.setVisibility(View.GONE);
                            if (task1.isSuccessful()) {
                                resetState.setText("Successfully send reset password to your email address.");
                            } else {
                                resetState.setText(Objects.requireNonNull(task1.getException()).getMessage());
                            }
                        });
                    }
                });
            }
        });
    }
}