package app.elivestock.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import app.elivestock.Config;
import app.elivestock.R;
import app.elivestock.activities.ActivityHistory;
import app.elivestock.activities.LoginActivity;
import app.elivestock.models.UsersData;
import app.elivestock.utilities.SharedPref;

public class FragmentProfile extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    UsersData usersData;
    TextView txt_user_name;
    TextView txt_user_email;
    TextView txt_user_phone;
    TextView txt_user_address;
    MaterialRippleLayout btnLogout;
    MaterialRippleLayout btn_order_history, btnSell;
    LinearLayout lyt_root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        txt_user_name = view.findViewById(R.id.fullName);
        txt_user_email = view.findViewById(R.id.email);
        txt_user_phone = view.findViewById(R.id.contactNum);
        txt_user_address = view.findViewById(R.id.address);

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
                txt_user_name.setText(Objects.requireNonNull(usersData).getFullname());
                txt_user_email.setText(usersData.getEmail());
                txt_user_phone.setText(usersData.getContactNumber());
                txt_user_address.setText(usersData.getAddress());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        SharedPref sharedPref = new SharedPref(getActivity());

        lyt_root = view.findViewById(R.id.lyt_root);
        if (Config.ENABLE_RTL_MODE) {
            lyt_root.setRotationY(180);
        }

        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(getContext(), "Successfully Logout!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        btn_order_history = view.findViewById(R.id.btn_order_history);
        btn_order_history.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), ActivityHistory.class);
            startActivity(intent);
        });

        btnSell = view.findViewById(R.id.btnSell);
        btnSell.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), FragmentSell.class);
            startActivity(intent);
        });

        return view;
    }
}
