package com.hello.one.cabdrive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.LocationManager;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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

public class driverLoginActivity extends AppCompatActivity {
private TextInputEditText memail,mpass;
    private Button mlogin,mregister;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener FBAL;
    private ProgressDialog mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        mProgressBar = new ProgressDialog(this);
        memail= (TextInputEditText) findViewById(R.id.email);
        mpass= (TextInputEditText) findViewById(R.id.pass);
        mlogin= (Button) findViewById(R.id.login);
        mregister= (Button) findViewById(R.id.register);
try {
    mAuth = FirebaseAuth.getInstance();
    FBAL = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    turnGPSOn();


                    Toast.makeText(driverLoginActivity.this, "Enabling your GPS for your current location", Toast.LENGTH_SHORT).show();
                }


                            Intent profile = new Intent(driverLoginActivity.this, DriverMapsActivity.class);
                            startActivity(profile);
                             finish();
                            return;

                    }



        }
    };
}
catch(Exception e)
{
    Toast.makeText(this, "Error = "+e.getMessage(), Toast.LENGTH_SHORT).show();
}
        mregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerLogin();
            }
        });
        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
    }
    private void Register(){
        final String email = memail.getText().toString();
        final String pass = mpass.getText().toString();
        mProgressBar.setMessage("Registering User...");
        mProgressBar.setCanceledOnTouchOutside(false);
        mProgressBar.show();

        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(driverLoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    mProgressBar.dismiss();
                    Toast.makeText(driverLoginActivity.this,"sign up error",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    String user_id = mAuth.getCurrentUser().getUid();

                }
            }
        });
    }
    private void signIn(){
        final String email = memail.getText().toString();
        final String pass = mpass.getText().toString();
        mProgressBar.setMessage("Loging in");
        mProgressBar.setCanceledOnTouchOutside(false);
        mProgressBar.show();

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(driverLoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    mProgressBar.dismiss();
                    Toast.makeText(driverLoginActivity.this, "sign up error", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void Login(){
        if(TextUtils.isEmpty(memail.getText().toString().trim())||TextUtils.isEmpty(mpass.getText().toString().trim())){
            memail.setError("Fields can't be Empty");
            mpass.setError("Fields can't be Empty");
        }
        else if(!emailValidator(memail.getText().toString())){
            memail.setError("" +
                    "Please EnterValidEmailAddress");
        }
        else{
            signIn();
        }
    }
    private void registerLogin(){
        if(TextUtils.isEmpty(memail.getText().toString().trim())||TextUtils.isEmpty(mpass.getText().toString().trim())){
            memail.setError("Fields can't be Empty");
            mpass.setError("Fields can't be Empty");
        }
        else if(!emailValidator(memail.getText().toString())){
            memail.setError("Please EnterValidEmailAddress");
        }
        else if(!passwordValidator(mpass.getText().toString())){
            mpass.setError("Password - Minimum One digit(0-9),No white space,Minimum 8-char length ");
        }
        else{
            Register();
        }
    }
    private Boolean passwordValidator(String email){
        Pattern pattern;
        Matcher matcher;
        final String Password_Pattern = "^(?=.*[0-9])(?=\\S+$).{8,}$";
        pattern = Pattern.compile(Password_Pattern);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private Boolean emailValidator(String email){
        Pattern pattern;
        Matcher matcher;
        final String Email_Pattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(Email_Pattern);
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
