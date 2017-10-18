package com.hello.one.cabdrive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.LocationManager;
import android.media.MediaCodec;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class customerLoginActivity extends AppCompatActivity {

    private TextInputEditText memail1,mpass1;
    private Button mlogin1,mregister1;
    private Button mDriver, mCust;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressBar;
    private FirebaseAuth.AuthStateListener FBAL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        mProgressBar = new ProgressDialog(this);
        memail1= (TextInputEditText) findViewById(R.id.email1);
        mpass1= (TextInputEditText) findViewById(R.id.pass1);
        mlogin1= (Button) findViewById(R.id.login1);
        mregister1= (Button) findViewById(R.id.register1);



        mAuth = FirebaseAuth.getInstance();
        FBAL = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user1=FirebaseAuth.getInstance().getCurrentUser();
                if(user1!=null)
                {

                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        turnGPSOn();


                 //       Toast.makeText(customerLoginActivity.this, "Enabling your GPS for your current location", Toast.LENGTH_SHORT).show();
                    }

                                Intent profile = new Intent(customerLoginActivity.this, CustomerMapsActivity.class);
                                startActivity(profile);
                                finish();
                                return;





                }
            }
        };
        mregister1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           registerLogin();
            }
        });
        mlogin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Login();
            }
        });
    }
    private void signIn(){
        final String email = memail1.getText().toString();
        final String pass = mpass1.getText().toString();
        mProgressBar.setMessage("Registering User...");
        mProgressBar.show();
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(customerLoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(customerLoginActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                }
                else {
                    turnGPSOn();


                   // Toast.makeText(customerLoginActivity.this, "Enabling your GPS for your current location in else", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void registerLogin(){
        if(TextUtils.isEmpty(memail1.getText().toString().trim())||TextUtils.isEmpty(mpass1.getText().toString().trim())){
            memail1.setError("Fields can't be Empty");
            mpass1.setError("Fields can't be Empty");
        }
        else if(!emailValidator(memail1.getText().toString())){
            memail1.setError("Please EnterValidEmailAddress");
        }
        else if(!passwordValidator(mpass1.getText().toString())){
            mpass1.setError("Password - Minimum One digit(0-9),No white space,Minimum 8-char length ");
        }
        else{
            Register();
        }
    }
    private void Register(){
        final String email = memail1.getText().toString();
        final String pass = mpass1.getText().toString();
        mProgressBar.setMessage("Registering User...");
        mProgressBar.show();
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(customerLoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(customerLoginActivity.this,"sign up error",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String user_id = mAuth.getCurrentUser().getUid();
                    DatabaseReference current_user_dp = FirebaseDatabase.getInstance().getReference().child("Users").child("Custmores").child(user_id);
                    current_user_dp.setValue(true);
                }
            }
        });
    }
    private void Login(){
        if(TextUtils.isEmpty(memail1.getText().toString().trim())||TextUtils.isEmpty(mpass1.getText().toString().trim())){
            memail1.setError("Fields can't be Empty");
            mpass1.setError("Fields can't be Empty");
        }
        else if(!emailValidator(memail1.getText().toString())){
            memail1.setError("Please Enter Valid Email Address");
        }
        else{
     signIn();
        }
    }
    private Boolean emailValidator(String email){
        Pattern pattern;
        Matcher matcher;
        final String Email_Pattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(Email_Pattern);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    private Boolean passwordValidator(String email){
        Pattern pattern;
        Matcher matcher;
        final String Password_Pattern = "^(?=.*[0-9])(?=\\S+$).{8,}$";
        pattern = Pattern.compile(Password_Pattern);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(FBAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(FBAL);
    }
}
