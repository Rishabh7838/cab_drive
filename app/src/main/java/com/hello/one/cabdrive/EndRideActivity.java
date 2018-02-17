package com.hello.one.cabdrive;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class EndRideActivity extends AppCompatActivity {
private TextView ridePrice;
   private String Price;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_ride);
        ridePrice = (TextView) findViewById(R.id.endPrice);

        Price = getIntent().getExtras().getString("ridePrice");
        ridePrice.setText(Price);
    }
}
