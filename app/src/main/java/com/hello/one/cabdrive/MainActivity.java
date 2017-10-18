package com.hello.one.cabdrive;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private TextView mDriver, mCust;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDriver = (TextView) findViewById(R.id.Drive);
        mCust = (TextView) findViewById(R.id.Cust);
        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent drive = new Intent(MainActivity.this, driverLoginActivity.class);
                startActivity(drive);

                return;
            }
        });
        mCust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cust = new Intent(MainActivity.this, customerLoginActivity.class);
                startActivity(cust);

                return;
            }
        });
    }


}