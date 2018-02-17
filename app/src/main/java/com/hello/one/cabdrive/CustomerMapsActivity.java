package com.hello.one.cabdrive;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,RoutingListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    Location lastLocation;
    LocationRequest locationRequest;
    private Button mlogout,mCall,mProfile;
    private SupportMapFragment mapFragment;
    private Polyline line;
    private RatingBar mRatingBar;
    private LatLng pickUpLocation,source;
    private Boolean Cancel = false,isLoggingout=false,isDriverNotAvailable =false;
    private GeoQuery geoQuery;
    private String destination,requestService,activeCustomerid;
    private LinearLayout mDriverInfo;
    private ImageView mCustomerProfileImage,mCustomerProfHead,mCustomerNav;
    private DataSnapshot ds;
    private TextView mDriverNumber, mDriverName,mDriverCar,mDriverCancel,mDriverCall,mCustomerNameHead;
    final int LOCATION_REQUEST_CODE = 1;
    DatabaseReference driverLocationRef;
    ValueEventListener EventListener;
    Boolean lastPlace = false;
    Marker pickUpMarker,mDestinationMarker;
    private RadioGroup mRadioGroup;


    LatLng driverLatlng,destinationLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        String DetailId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cust = FirebaseDatabase.getInstance().getReference().child("Users").child("Custmores").child(DetailId);

        cust.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("name")==false || dataSnapshot.hasChild("number")==false || dataSnapshot.hasChild("ProfileImageUrl")==false) {
                    Intent cust = new Intent(CustomerMapsActivity.this, Profile.class);
                    startActivity(cust);
                    finish();
                    return;
                }
                if ( dataSnapshot.hasChild("name")  && dataSnapshot.hasChild("number")  && dataSnapshot.hasChild("ProfileImageUrl") ) {
                    if (dataSnapshot.child("name").getValue().toString().trim().matches("") || dataSnapshot.child("number").getValue().toString().trim().matches("") || dataSnapshot.child("ProfileImageUrl").getValue().toString().trim().matches("")) {
                        Intent cust = new Intent(CustomerMapsActivity.this, Profile.class);
                        startActivity(cust);
                        finish();
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });


        polylines = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }
        final DrawerLayout mcustomerLayout = (DrawerLayout) findViewById(R.id.customerLayout);

        mCustomerNav = (ImageView) findViewById(R.id.mCustomerNav);
        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        mCustomerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);
        mDriverName = (TextView) findViewById(R.id.driverNameView);
        mRatingBar = (RatingBar) findViewById(R.id.ratingbar1);
        mDriverNumber = (TextView) findViewById(R.id.driveerNumberView);
        mDriverCar = (TextView) findViewById(R.id.driveerCarView);
        mDriverCall= (TextView) findViewById(R.id.callDriver);
        mDriverCancel = (TextView) findViewById(R.id.cancelRide);
        mCall = (Button) findViewById(R.id.call);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.DPX);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.customerNavView);

         View v = navigationView.getHeaderView(0);

        mCustomerNameHead= (TextView)v.findViewById(R.id.CustomerNameHead);
        mCustomerProfHead = (ImageView)v.findViewById(R.id.CustomerProfHead);


        String userId1 = FirebaseAuth.getInstance().getCurrentUser().getUid();
       DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Custmores").child(userId1);
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                                            if (map.get("name") != null) {

                                                                mCustomerNameHead.setText(map.get("name").toString());
                                                            }
                                                            if (map.get("ProfileImageUrl") != null) {

                                                                Glide.with(getApplication()).load(map.get("ProfileImageUrl").toString()).into(mCustomerProfHead);
                                                            }

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.CustomerProfile :
                        Intent prof = new Intent(CustomerMapsActivity.this, Profile.class);
                        startActivity(prof);
                        break;
                    case R.id.CustomerLogout:
                        isLoggingout = true;
                        removeCustomerRequest();
                        FirebaseAuth.getInstance().signOut();
                        Intent Go = new Intent(CustomerMapsActivity.this, customerLoginActivity.class);
                        startActivity(Go);
                        finish();
                        break;
                    case R.id.customerHistory:
                        Intent history = new Intent(CustomerMapsActivity.this,historyActivity.class);
                        history.putExtra("customerOrDriver","Custmores");
                        startActivity(history);
                        break;

                }
                DrawerLayout mcustomerLayout1 = (DrawerLayout) findViewById(R.id.customerLayout);
                mcustomerLayout1.closeDrawer(Gravity.START);
                return true;
            }
        });
        //mdriverLayout = (FrameLayout) findViewById(R.id.driverLayout);


        ///ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawer,mdriverLayout,"Open","Close");

        mCustomerNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mcustomerLayout.openDrawer(Gravity.START);

            }
        });

        mDriverCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DriverId!=null) {
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverId);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                if (map.get("number") != null) {
                                    String callDriver = map.get("number").toString();
                                    Intent call = new Intent(Intent.ACTION_CALL);
                                    {
                                        if(callDriver.trim().isEmpty()){
                                            Toast.makeText(CustomerMapsActivity.this, "There is no number", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            call.setData(Uri.parse("tel:"+callDriver));
                                        }
                                        if(ActivityCompat.checkSelfPermission(CustomerMapsActivity.this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
                                            Toast.makeText(CustomerMapsActivity.this, "Please grant the permission to call", Toast.LENGTH_SHORT).show();
                                            requestPermission();
                                        }
                                        else{
                                            startActivity(call);
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
            }
        });
        mDriverCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                erasePolylines();
                mCall.setEnabled(true);
                endRide();


            }
        });
        try {
            mCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                if(Cancel)
//                {
//                    Cancel = false;
//                    geoQuery.removeAllListeners();
//                    driverLocationRef.removeEventListener(EventListener);
                    //}
//                else {
                    if (mDestinationMarker != null) {
                        int selectId = mRadioGroup.getCheckedRadioButtonId();
                        final RadioButton radioButton = (RadioButton) findViewById(selectId);

                        if (radioButton.getText() == null) {
                            return;
                        }
                        try {
                            requestService = radioButton.getText().toString();
                        }
                        catch(Exception e)
                        {
                            Toast.makeText(CustomerMapsActivity.this, "request service error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //Toast.makeText(CustomerMapsActivity.this, "Service needed"+requestService, Toast.LENGTH_SHORT).show();
                        Cancel = true;
                        mCall.setEnabled(false);
                         activeCustomerid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                       // Toast.makeText(CustomerMapsActivity.this, "activeCustomerId = "+activeCustomerid, Toast.LENGTH_SHORT).show();
                        DatabaseReference drivernotAvailable = FirebaseDatabase.getInstance().getReference().child("DriverNotAvailable").child(activeCustomerid);
                        drivernotAvailable.child("DriverUnavailable").child("").setValue(true);

                       // activeCustomerid = FirebaseAuth.getInstance().getCurrentUser().get
                        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                        GeoFire GF = new GeoFire(ref);
                        GF.setLocation(activeCustomerid, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));
                        pickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick up from here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        getRouterMarker(pickUpLocation,destinationLatLng);
                        //mMap.addCircle(new CircleOptions().center(pickUpLocation).radius(500).strokeWidth(15).strokeColor(Color.GREEN).fillColor(Color.argb(128, 255, 0, 0)));
                        mCall.setText("Finding Driver...");

                        final DatabaseReference checkAvailbility = FirebaseDatabase.getInstance().getReference();
                         checkAvailbility.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild("driverAvaliable")){
                                    Toast.makeText(CustomerMapsActivity.this, "finding driver from avaliable driver", Toast.LENGTH_SHORT).show();
                                    getCloestDistance();
                                }
                                else
                                {
                                    Toast.makeText(CustomerMapsActivity.this, "No driver exist", Toast.LENGTH_SHORT).show();
                                   // ref.removeValue();
                                    mCall.setEnabled(true);
                                    mCall.setText("Call Cab");
                                    Cancel = false;


                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else {
                        AlertDialog.Builder setDestinationFirst = new AlertDialog.Builder(CustomerMapsActivity.this);
                        setDestinationFirst.setTitle("Set Your destination first.");
                        setDestinationFirst.setCancelable(true);
                        setDestinationFirst.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog create = setDestinationFirst.create();
                        create.show();
                    }
                    //}
                }
            });
        }catch(Exception e){
            Toast.makeText(this, "Call again", Toast.LENGTH_SHORT).show();
        }

        try {
            PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {

                    destination = place.getAddress().toString();
                   // DatabaseReference desti = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverId).child("customerRequest").child("Destination");
                    //desti.setValue(destination);
                    Toast.makeText(CustomerMapsActivity.this, "destination" + destination, Toast.LENGTH_SHORT).show();
                    destinationLatLng = place.getLatLng();
                    Toast.makeText(CustomerMapsActivity.this, "destination Latlng" + destinationLatLng, Toast.LENGTH_SHORT).show();
//
                    if (destinationLatLng != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationLatLng));
                   mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                        mDestinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Your Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }

//                }
//                else {
//                    mDestinationMarker.remove();
//                    line.remove();
//                    mDestinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Your Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//                }


                }

                @Override
                public void onError(Status status) {

                }
            });
        }catch(NullPointerException e){
            Toast.makeText(CustomerMapsActivity.this,""+e,Toast.LENGTH_SHORT).show();
        }
    }



            private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},1);

    }


    private int radius=1;
    private Boolean driverFound=false;
    private String DriverId;
    private CircleOptions driverCircleOption;
    private Circle driverCircle;
    private int i=0;

private void getCloestDistance() {
    try {


        DatabaseReference driverData = FirebaseDatabase.getInstance().getReference().child("driverAvaliable");
        final DatabaseReference driverNotAvailable = FirebaseDatabase.getInstance().getReference().child("DriverNotAvailable").child(activeCustomerid);
        driverNotAvailable.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    ds = dataSnapshot.child("DriverUnavailable");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        GeoFire gf = new GeoFire(driverData);
        geoQuery = gf.queryAtLocation(new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), radius);
        try {
            geoQuery.removeAllListeners();
        }
        catch(Exception e){
            Toast.makeText(CustomerMapsActivity.this, "Geoquery error = "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && Cancel && ds.hasChild(key)==false) {


                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                if (driverFound) {
                                    return;
                                }
                                if (driverMap.get("Services").toString().equals(requestService)) {
                                    driverFound = true;
                                    DriverId = dataSnapshot.getKey();
                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverId).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map1 = new HashMap();
                                    map1.put("customerRideId", customerId);
                                    map1.put("destinationLat", destinationLatLng.latitude);
                                    map1.put("destinationLon", destinationLatLng.longitude);
                                    map1.put("destination", destination);
                                    driverRef.updateChildren(map1);
                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();

                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


//                    mCall.setText("Waiting for driver response");
                }

                        }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius++;
                    getCloestDistance();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }catch(Exception e)
    {
        Toast.makeText(this, "getClosestDistance exception", Toast.LENGTH_SHORT).show();
    }
}


    private void getRouterMarker(LatLng pickUpLocation1 ,LatLng destinationLatLng1) {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickUpLocation1,destinationLatLng1)
                .build();
        routing.execute();
    }

        private void getDriverInfo () {
            try {
                mRadioGroup.setVisibility(View.GONE);
                mDriverInfo.setVisibility(View.VISIBLE);
                DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverId);
                mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            if (map.get("name") != null) {
                                mDriverName.setText(map.get("name").toString());
                            }
                            if (map.get("number") != null) {
                                mDriverNumber.setText(map.get("number").toString());
                            }
                            if (map.get("car") != null) {
                                mDriverCar.setText(map.get("car").toString());
                            }
                            if (map.get("ProfileImageUrl") != null) {

                                Glide.with(getApplication()).load(map.get("ProfileImageUrl").toString()).into(mCustomerProfileImage);
                            }
                            float ratingSum = 0;
                            float ratingsTotal = 0;
                            float ratingsAvg = 0;
                            for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                                ratingSum = ratingSum + Float.valueOf(child.getValue().toString());
                                ratingsTotal++;
                            }
                            if(ratingsTotal!= 0){
                                ratingsAvg = ratingSum/ratingsTotal;
                                mRatingBar.setRating(ratingsAvg);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } catch (NullPointerException e) {
                Toast.makeText(this, "driverinfo error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    private Marker mdriverMarker;

private void getDriverLocation()
{
    //Toast.makeText(CustomerMapsActivity.this,"Inside getdriverlocation",Toast.LENGTH_SHORT).show();

     driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driverWorking").child(DriverId).child("l");
    EventListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                List<Object> map = (List<Object>) dataSnapshot.getValue();
                double LocationLat = 0;
                double LocationLon = 0;
                mCall.setText("Driiver Found");
                if (map.get(0) != null) {
                    LocationLat = Double.parseDouble(map.get(0).toString());
                }
                if (map.get(1) != null) {
                    LocationLon = Double.parseDouble(map.get(1).toString());
                }

                driverLatlng = new LatLng(LocationLat,LocationLon);

                if (mdriverMarker != null) {
                    mdriverMarker.remove();
                }

                mdriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatlng).title("Your Driver.").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));


                Location loc1 = new Location("");
                loc1.setLatitude(pickUpLocation.latitude);
                loc1.setLongitude(pickUpLocation.longitude);

                Location loc2 = new Location("");
                loc2.setLatitude(driverLatlng.latitude);
                loc2.setLongitude(driverLatlng.longitude);
                float dist = loc1.distanceTo(loc2);
                if(dist<1000) {
                    mCall.setText("Driver Found at = " + dist + " meters");
                    if(dist<100)
                        mCall.setText("Driver has approached");

                }
                else
                    mCall.setText("Driver Found at = " + (dist/1000) + " Km");

            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
}
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverId).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    Toast.makeText(CustomerMapsActivity.this, "Ending customer ride", Toast.LENGTH_SHORT).show();

                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        Toast.makeText(CustomerMapsActivity.this, "Ending  ride from customer", Toast.LENGTH_SHORT).show();
        Cancel = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(EventListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
        if (DriverId != null) {
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverId).child("customerRequest");
            driverRef.setValue("true");
            DriverId = null;
        }
        driverFound = false;
        radius = 1;
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire GF = new GeoFire(ref);
        GF.removeLocation(userid);
        if (pickUpMarker != null) {
            pickUpMarker.remove();
        }
        mDriverInfo.setVisibility(View.GONE);
        mRadioGroup.setVisibility(View.VISIBLE);
        mDriverName.setText("");
        mDriverNumber.setText("");
        mDriverCar.setText("");
        mCustomerProfileImage.setImageResource(R.mipmap.prof);
        mCall.setText("Call Cab");
        mCall.setEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQUEST_CODE);
            }

        }
        buildApiClient();
        mMap.setMyLocationEnabled(true);
    }
    protected synchronized void buildApiClient()
    {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        lastLocation=location;
        lastPlace = true;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case LOCATION_REQUEST_CODE : {
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    mapFragment.getMapAsync(this);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(!isLoggingout){
//            removeCustomerRequest();
//        }
//
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!isLoggingout){
            removeCustomerRequest();
        }
    }

    private void removeCustomerRequest() {
        LocationServices.FusedLocationApi.removeLocationUpdates(client ,this);
        //geoQuery.removeAllListeners();
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refAvaliable = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(refAvaliable);
        geoFire.removeLocation(userid);
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
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRoutePath) {
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
