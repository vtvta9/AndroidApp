package dev.edmt.android_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dev.edmt.android_app.Common.Common;
import dev.edmt.android_app.model.User;

public class Signin extends AppCompatActivity {
    EditText edtPhone, edtPassword;
    Button btnSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        edtPhone = (MaterialEditText)findViewById(R.id.edtPhone);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);

        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInterner(getBaseContext())) {

                    final ProgressDialog mDialog = new ProgressDialog(Signin.this);
                    mDialog.setMessage("Vui lòng chờ...");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //check if user not exiat in database
                            if (dataSnapshot.child(edtPhone.getText().toString()).exists()) {
                                //get user if
                                mDialog.dismiss();
                                User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                user.setPhone(edtPhone.getText().toString()); //set phone

                                if (user.getPassword().equals(edtPassword.getText().toString())) {
                                    {
                                        Intent homeIntent = new Intent(Signin.this, Home.class);
                                        Common.currentUser = user;
                                        startActivity(homeIntent);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(Signin.this, "Đăng Nhập Thất Bại", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(Signin.this, "Không có!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(Signin.this, "Check connettion!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }





}
