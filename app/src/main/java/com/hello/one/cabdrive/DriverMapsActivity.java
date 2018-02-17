package com.hello.one.cabdrive;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.transition.Visibility;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener ,RoutingListener{

    private GoogleMap mMap;
    private GoogleApiClient client;
    Location lastLocation;
    private Switch mSwitch;
    private float rideDistance;
    LocationRequest locationRequest;
    private  SupportMapFragment mapFragment;
    private String customerId = "",customerKey,customerThere;
    Boolean isLoggingOut = false;
    private int status = 0;
    private LinearLayout mCustomerInfo;
    private ValueEventListener value;
    private DatabaseReference puttingDriver;
    private ImageView mCustomerProfileImage,mDriverNav,mDriverProfHead;
    private TextView mCustomerNumber, mCustomerName,mCustomerDestination,mstartRide,mDriverNameHead;
    private FrameLayout mdriverLayout;
    private Button mCancelCustomerRide;
    private TabLayout tabLayout;
    private LatLng destinationLatLng;
    private ViewPager viewPager;
    private String driverId,destination,mCustomerCoordinates;
    private LatLng driverLatlng;
    public static int Reject =0;
    private CoordinatorLayout driverSwipeLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String DetailDriverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference Drivers = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DetailDriverId);

        if(lastLocation == null)
        {
       //     lastLocation.setLatitude(0);
         //   lastLocation.setLongitude(0);
           // Toast.makeText(this, "current locstion is null", Toast.LENGTH_SHORT).show();
        }
       // Toast.makeText(this, "Driver id "+DetailDriverId, Toast.LENGTH_SHORT).show();
        Drivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Services") == false || dataSnapshot.hasChild("car") == false || dataSnapshot.hasChild("name") == false || dataSnapshot.hasChild("number") == false || dataSnapshot.hasChild("ProfileImageUrl") == false) {


                    Intent drive = new Intent(DriverMapsActivity.this, driverProfile.class);
             //       Toast.makeText(DriverMapsActivity.this, "Insert details of driver first", Toast.LENGTH_SHORT).show();
                    startActivity(drive);
                    finish();
                    return;

                }
                if (dataSnapshot.hasChild("Services")  && dataSnapshot.hasChild("car")  && dataSnapshot.hasChild("name")  && dataSnapshot.hasChild("number")  && dataSnapshot.hasChild("ProfileImageUrl") ) {


                    if (dataSnapshot.child("Services").getValue().toString().trim().matches("") || dataSnapshot.child("name").getValue().toString().trim().matches("") || dataSnapshot.child("number").getValue().toString().trim().matches("") || dataSnapshot.child("ProfileImageUrl").getValue().toString().trim().matches("")) {
                        Intent drive = new Intent(DriverMapsActivity.this, driverProfile.class);
                     //   Toast.makeText(DriverMapsActivity.this, "Insert details of driver first", Toast.LENGTH_SHORT).show();
                        startActivity(drive);
                        finish();
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });
        setContentView(R.layout.activity_driver_maps);
        polylines = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);
        }
        final DrawerLayout mdriverLayout = (DrawerLayout) findViewById(R.id.driverLayout);
        mDriverNav = (ImageView) findViewById(R.id.mDriveNav);
        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);
        mCustomerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);
        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerNumber = (TextView) findViewById(R.id.customerNumber);
        mstartRide = (TextView) findViewById(R.id.startRide);
        mCancelCustomerRide = (Button) findViewById(R.id.cancelCustomerRide);
        mSwitch = (Switch) findViewById(R.id.switcher);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    connectDriver();
                    Toast.makeText(DriverMapsActivity.this, "Driver Working", Toast.LENGTH_SHORT).show();
                }
                else{
                    disconnectDriver();
                }
            }
        });

        mCustomerDestination = (TextView) findViewById(R.id.customerDestination);
        driverSwipeLayout = (CoordinatorLayout) findViewById(R.id.driverSwipeLayout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.driverNavView);

        View v = navigationView.getHeaderView(0);

        mDriverNameHead= (TextView)v.findViewById(R.id.driverNameHead);
        mDriverProfHead = (ImageView)v.findViewById(R.id.DriverProfHead);


        String driveId1=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driveId1);
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String,Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mDriverNameHead.setText(map.get("name").toString());
                    }

                    if(map.get("ProfileImageUrl")!=null){

                           Glide.with(getApplication()).load(map.get("ProfileImageUrl").toString()).into(mDriverProfHead);
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
                    case R.id.Profile :
                        Intent profile = new Intent(DriverMapsActivity.this,driverProfile.class);
                        startActivity(profile);
                        break;
                    case R.id.Logout:
                        isLoggingOut = true;
                        disconnectDriver();
                        FirebaseAuth.getInstance().signOut();

                        Intent Go = new Intent(DriverMapsActivity.this, driverLoginActivity.class);
                        startActivity(Go);
                        finish();
                        break;
                    case R.id.driverHistory:
                        Intent history = new Intent(DriverMapsActivity.this,historyActivity.class);
                        history.putExtra("customerOrDriver","Drivers");
                        startActivity(history);
                        //Toast.makeText(DriverMapsActivity.this, "No history", Toast.LENGTH_SHORT).show();
                        break;

                }
                DrawerLayout mdriverLayout = (DrawerLayout) findViewById(R.id.driverLayout);
                mdriverLayout.closeDrawer(Gravity.START);
                return true;
            }
        });
        //mdriverLayout = (FrameLayout) findViewById(R.id.driverLayout);


        ///ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawer,mdriverLayout,"Open","Close");

        mDriverNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mdriverLayout.openDrawer(Gravity.START);

            }
        });

        getAssignCustomer();



mstartRide.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        //Toast.makeText(DriverMapsActivity.this, "Pickup ="+driverLatlng+" Destination= "+mCustomerCoordinates, Toast.LENGTH_SHORT).show();
        AlertDialog.Builder rideOrPickup = new AlertDialog.Builder(DriverMapsActivity.this);
        rideOrPickup.setTitle("Customer Picked ?");
        rideOrPickup.setCancelable(false);
        rideOrPickup.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mCancelCustomerRide.setText("End Ride");
                status = 3;
                //Uri navigationUri = Uri.parse("http://maps.google.com/maps?saddr="+driverLatlng.latitude+","+driverLatlng.longitude+"&daddr="+destination);
                Uri navigationUri = Uri.parse("google.navigation:q="+destination);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,navigationUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if(mapIntent.resolveActivity(getPackageManager())!=null){
                    startActivity(mapIntent);
                    return;
                }
                else{
                    Toast.makeText(DriverMapsActivity.this, "No Map related app found", Toast.LENGTH_SHORT).show();
                }

            }
        });
        rideOrPickup.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                status = 2;
                mCancelCustomerRide.setText("Start your ride with customer");
                //Uri navigationUri = Uri.parse("http://maps.google.com/maps?saddr="+lastLocation.getLatitude()+","+lastLocation.getLongitude()+"&daddr="+driverLatlng.latitude+","+driverLatlng.longitude);
                Uri navigationUri = Uri.parse("google.navigation:q="+driverLatlng.latitude+","+driverLatlng.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,navigationUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if(mapIntent.resolveActivity(getPackageManager())!=null){
                    startActivity(mapIntent);
                    return ;

                }
                else{
                    Toast.makeText(DriverMapsActivity.this, "No Map related app found", Toast.LENGTH_SHORT).show();
                }

            }
        });
        AlertDialog create = rideOrPickup.create();
        create.show();

    }
});


        mCancelCustomerRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){

                    case 1:
                        endRide();
                        mCancelCustomerRide.setVisibility(View.GONE);
//                        status=2;
//                        erasePolylines();
//                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
//                            getRouterMarker(destinationLatLng);
//                        }
//                        mCancelCustomerRide.setText("drive completed");
//                        mCancelCustomerRide.setVisibility(View.GONE);
                        break;
                    case 2:
                        Toast.makeText(DriverMapsActivity.this, "Click 'start ride' button in info on screen", Toast.LENGTH_LONG).show();
//                        recordRide();
//                        endRide();
                        break;
                    case 3:

                        Intent intent = new Intent(DriverMapsActivity.this, EndRideActivity.class);
                        Bundle b = new Bundle();
                        b.putString("ridePrice", rideDistance+"");
                        intent.putExtras(b);
                        startActivity(intent);
                        endRide();
                        break;
                }
            }
        });
    }



            private void getAssignCustomer() {
         driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();


                DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
                assignedCustomerRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            status = 1;

                            //Toast.makeText(DriverMapsActivity.this, "Got driver", Toast.LENGTH_SHORT).show();
                            customerId = dataSnapshot.getValue().toString();
                            getAssignCustomerPickUpLocation();
                            getCustomerDestination();
                            getAssignedCustomerInfo();
                            getHasRideCanceled();
                        }else{
                           // Toast.makeText(DriverMapsActivity.this, "Ending Driver Ride", Toast.LENGTH_SHORT).show();
                            //endRide();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


                        }



    private void getCustomerDestination() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
        assignCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && customerId != null) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("destination") != null) {
                        destination = map.get("destination").toString();
                        mCustomerDestination.setText("Destination: " + destination);

                    } else {
                        mCustomerDestination.setText("Destination...");
                    }
                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if (map.get("destinationLat") != null) {
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if (map.get("destinationLon") != null) {
                        destinationLng = Double.valueOf(map.get("destinationLon").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        assignCustomerCoordinates.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists() && customerId!=null) {
//
//                    mCustomerCoordinates = dataSnapshot.getValue().toString();
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    private void getAssignedCustomerInfo() {
        mCustomerInfo.setVisibility(View.VISIBLE);
        mCancelCustomerRide.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Inside Customer Info", Toast.LENGTH_SHORT).show();
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Custmores").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mCustomerName.setText(map.get("name").toString());
                    }
                    if (map.get("number") != null) {
                        mCustomerNumber.setText(map.get("number").toString());
                    }
                    if (map.get("ProfileImageUrl") != null) {

                        Glide.with(getApplication()).load(map.get("ProfileImageUrl").toString()).into(mCustomerProfileImage);
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    Marker pickMarker;
    DatabaseReference assignCustomerPickUpLocationRef;
    ValueEventListener assignCustomerPickUpLocationListener;
    private void getAssignCustomerPickUpLocation() {
//        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
         assignCustomerPickUpLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
       assignCustomerPickUpLocationListener = assignCustomerPickUpLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double LocationLat = 0;
                    double LocationLon = 0;
                    if(map.get(0)!=null)
                    {
                        LocationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null)
                    {
                        LocationLon = Double.parseDouble(map.get(1).toString());
                    }

                    driverLatlng = new LatLng(LocationLat,LocationLon);

                    //Location loc1 = new Location("");



                    pickMarker =  mMap.addMarker(new MarkerOptions().position(driverLatlng).title("PickUp Location.").icon(BitmapDescriptorFactory.fromResource(R.mipmap.customer)));

                    getRouterMarker(driverLatlng);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideCanceled(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Toast.makeText(DriverMapsActivity.this, "inside getHasRideCanceled", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(DriverMapsActivity.this, "Customer Cancelled the ride", Toast.LENGTH_LONG).show();
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void endRide(){
        Toast.makeText(DriverMapsActivity.this, "Ending ride from driver", Toast.LENGTH_SHORT).show();
      // mCancelCustomerRide.setText("picked customer");
        erasePolylines();
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId="";
        rideDistance=0;
        if(pickMarker != null){
            pickMarker.remove();
        }
        if (assignCustomerPickUpLocationListener != null){
            assignCustomerPickUpLocationRef.removeEventListener(assignCustomerPickUpLocationListener);
        }
        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerNumber.setText("");
        mCustomerDestination.setText("Destination: --");
        mCancelCustomerRide.setText("Cancel Ride");
        mCancelCustomerRide.setVisibility(View.GONE);
        mCustomerProfileImage.setImageResource(R.mipmap.prof);
    }

    private void recordRide(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Custmores").child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver", userId);
        map.put("customer", customerId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("destination", destination);
        map.put("location/from/lat",driverLatlng.latitude);
        map.put("location/from/lon",driverLatlng.longitude);
        map.put("location/to/lat",destinationLatLng.latitude);
        map.put("location/to/lon",destinationLatLng.longitude);
        map.put("distance",rideDistance);
        historyRef.child(requestId).updateChildren(map);
    }

    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }

    private void getRouterMarker(LatLng driverLatLng) {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()),driverLatLng)
                .build();
        routing.execute();
    }

public void connectDriver(){
    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(DriverMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQUEST_CODE);
    }
    LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQUEST_CODE);
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
        if(getApplicationContext()!=null) {

                if(!customerId.equals("")) {
                    rideDistance += lastLocation.distanceTo(location) / 1000;
                }
            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvaliable = FirebaseDatabase.getInstance().getReference("driverAvaliable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driverWorking");
            GeoFire geoFireAvaliable = new GeoFire(refAvaliable);
            GeoFire geoFireWorking = new GeoFire(refWorking);


            switch (customerId) {
                case "":
                    geoFireWorking.removeLocation(userid);
                    geoFireAvaliable.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvaliable.removeLocation(userid);
                    geoFireWorking.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
private void disconnectDriver()
{

    LocationServices.FusedLocationApi.removeLocationUpdates(client ,this);
//    puttingDriver.removeEventListener(value);
    String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    final DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
    driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                driverRef.removeValue();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
    DatabaseReference refAvaliable = FirebaseDatabase.getInstance().getReference("driverAvaliable");
    GeoFire geoFire = new GeoFire(refAvaliable);
    geoFire.removeLocation(userid);
    //rejectCustomer();
}
final int LOCATION_REQUEST_CODE = 1;
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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        finish();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(!isLoggingOut){
//            disconnectDriver();
//        }
//    }



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
