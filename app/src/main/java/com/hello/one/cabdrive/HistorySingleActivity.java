package com.hello.one.cabdrive;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback ,RoutingListener{
    private GoogleMap mMap;
    private DatabaseReference historyRideInfoDb;
    private RatingBar mRatingBar;
    private LatLng destinationLatLng,pickUpLatLng;
    private String rideId,currentUserId,customerId,driverId,userDriverOrCustomer;
    private TextView mRideLocation,mRideDistance,mPhoneUser,mNameUser,mRideDate;
    private ImageView mUserImage;
    private SupportMapFragment mMapFragment;
    private String distance;
    private Double ridePrice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        polylines = new ArrayList<>();
        rideId = getIntent().getExtras().getString("rideId");
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.historyMap);
        mMapFragment.getMapAsync(this);

        mRideLocation = (TextView) findViewById(R.id.rideLocation);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mRideDistance = (TextView) findViewById(R.id.rideDistance);
        mRideDate = (TextView) findViewById(R.id.rideDate);
        mPhoneUser = (TextView) findViewById(R.id.userNumberHistory);
        mNameUser = (TextView) findViewById(R.id.userNameHistory);

        mUserImage = (ImageView) findViewById(R.id.userImage);

        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInformation();


    }

    private void getRideInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        if(child.getKey().equals("customer")){
                            customerId = child.getValue().toString();
                            if (!customerId.equals(currentUserId)){
                                userDriverOrCustomer = "Drivers";
                                getUserInformation("Custmores",customerId);
                            }
                        }

                            if(child.getKey().equals("driver")){
                                driverId = child.getValue().toString();
                                if (!driverId.equals(currentUserId)){
                                    userDriverOrCustomer = "Customer";
                                    getUserInformation("Drivers",driverId);
                                    displayCustomerRelatedObjects();
                                }
                            }
                        if(child.getKey().equals("timestamp")) {
                            mRideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("rating")){
                            mRatingBar.setRating(Float.valueOf(child.getValue().toString()));

                        }
                        if(child.getKey().equals("destination")) {
                            mRideLocation.setText(child.getValue().toString());

                        }
                        if(child.getKey().equals("distance")) {
                            distance = child.getValue().toString();
                            mRideDistance.setText(distance.substring(0,Math.min(distance.length(),5))+" km");
                            ridePrice = Double.valueOf(distance)*0.5;
                        }
                        if(child.getKey().equals("location")) {
                            pickUpLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("lon").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()),Double.valueOf(child.child("to").child("lon").getValue().toString()));
                            if(destinationLatLng!=new LatLng(0,0)){
                                getRouterMarker();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayCustomerRelatedObjects() {
        mRatingBar.setVisibility(View.VISIBLE);
      //  mPay.setVisibility(View.VISIBLE);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyRideInfoDb.child("rating").setValue(rating);
                DatabaseReference mDriverRatingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("rating");
                mDriverRatingDb.child(rideId).setValue(rating);
            }
        });
//        if(customerPaid){
//            mPay.setEnabled(false);
//        }else{
//            mPay.setEnabled(true);
//        }
//        mPay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                payPalPayment();
//            }
//        });
    }

    private void getUserInformation(String otherDriverOrCustomer, String otherUserId) {
        DatabaseReference mOtherUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(otherDriverOrCustomer).child(otherUserId);
        mOtherUserDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mNameUser.setText(map.get("name").toString());
                    }
                    if (map.get("number") != null) {
                        mPhoneUser.setText(map.get("number").toString());
                    }
                    if (map.get("ProfileImageUrl") != null) {

                        Glide.with(getApplication()).load(map.get("ProfileImageUrl").toString()).into(mUserImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void getRouterMarker() {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickUpLatLng,destinationLatLng)
                .build();
        routing.execute();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
        return date;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
mMap = googleMap;
    }

    private List<Polyline> polylines;
    private  static final int[] COLORS = {R.color.colorPrimaryDark};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e!= null){
            Toast.makeText(this, "Error"+  e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Something went wrong try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(pickUpLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.animateCamera(cameraUpdate);
        mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("Pick-Up Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.car)));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destinatiion Location"));
        if(polylines.size()>0){

            for (Polyline poly : polylines ){
                poly.remove();
            }
        }
        polylines = new ArrayList<>();
        //add routes.
        for(int i= 0;i<route.size();i++){
            //In case of more than 5 alternative routes.

            int colorIndex = i % COLORS.length;
            PolylineOptions polyoption = new PolylineOptions();
            polyoption.color(getResources().getColor(COLORS[colorIndex]));
            polyoption.width(5);
            polyoption.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyoption);
            polylines.add(polyline);


        }

    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for(Polyline poly : polylines){
            poly.remove();

        }
        polylines.clear();
    }
}
