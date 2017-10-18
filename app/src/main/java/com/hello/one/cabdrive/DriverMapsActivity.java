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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    LocationRequest locationRequest;
    private  SupportMapFragment mapFragment;
    private String customerId = "",customerKey,customerThere;
    Boolean isLoggingOut = false;
    private LinearLayout mCustomerInfo;
    private ValueEventListener value;
    private DatabaseReference puttingDriver;
    private ImageView mCustomerProfileImage,mDriverNav,mDriverProfHead;
    private TextView mCustomerNumber, mCustomerName,mCustomerDestination,mstartRide,mDriverNameHead;
    private FrameLayout mdriverLayout;
    private TabLayout tabLayout;
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

        Toast.makeText(this, "Driver id "+DetailDriverId, Toast.LENGTH_SHORT).show();
        Drivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Services") == false || dataSnapshot.hasChild("car") == false || dataSnapshot.hasChild("name") == false || dataSnapshot.hasChild("number") == false || dataSnapshot.hasChild("ProfileImageUrl") == false) {


                    Intent drive = new Intent(DriverMapsActivity.this, driverProfile.class);
                    Toast.makeText(DriverMapsActivity.this, "Insert details of driver first", Toast.LENGTH_SHORT).show();
                    startActivity(drive);
                    finish();
                    return;

                }
                if (dataSnapshot.hasChild("Services")  && dataSnapshot.hasChild("car")  && dataSnapshot.hasChild("name")  && dataSnapshot.hasChild("number")  && dataSnapshot.hasChild("ProfileImageUrl") ) {


                    if (dataSnapshot.child("Services").getValue().toString().trim().matches("") || dataSnapshot.child("name").getValue().toString().trim().matches("") || dataSnapshot.child("number").getValue().toString().trim().matches("") || dataSnapshot.child("ProfileImageUrl").getValue().toString().trim().matches("")) {
                        Intent drive = new Intent(DriverMapsActivity.this, driverProfile.class);
                        Toast.makeText(DriverMapsActivity.this, "Insert details of driver first", Toast.LENGTH_SHORT).show();
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


        mCustomerDestination = (TextView) findViewById(R.id.customerDestination);
        //LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View vi = inflator.inflate(R.layout.drivenavswipe,null);
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
                        Toast.makeText(DriverMapsActivity.this, "History not available now", Toast.LENGTH_SHORT).show();
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
        Uri navigationUri = Uri.parse("http://maps.google.com/maps?saddr="+driverLatlng.latitude+","+driverLatlng.longitude+"&daddr="+destination);
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
    }



            private void getAssignCustomer() {
         driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
      //previous  final DatabaseReference assignCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRiderId");//
                 puttingDriver = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("CustomerAppointed");
                 puttingDriver.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Toast.makeText(DriverMapsActivity.this, "Customer there = "+dataSnapshot.getValue().toString(), Toast.LENGTH_SHORT).show();
                            customerThere = dataSnapshot.getValue().toString();
                            getCustomerDestination();
                            getAssignedCustomerInfo();
                            LayoutInflater customerInfoDialouge = LayoutInflater.from(DriverMapsActivity.this);
                            final View dialogView = customerInfoDialouge.inflate(R.layout.customer_info,null);

                            final DatabaseReference assignCustomerRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerThere);
                            assignCustomerRef.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                                    if (dataSnapshot.exists()){ //&& customerId.equals(""))||(dataSnapshot.exists() && customerId.equals("")==false)) {
                                        Toast.makeText(DriverMapsActivity.this, "Customer request came", Toast.LENGTH_SHORT).show();
                                        if (dataSnapshot.exists()) {

                                            Snackbar requestComing = Snackbar.make(driverSwipeLayout, "A Customer request is coming", Snackbar.LENGTH_LONG).setAction("See", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    AlertDialog.Builder seeRequest = new AlertDialog.Builder(DriverMapsActivity.this);
                                                    seeRequest.setView(dialogView);
                                                    seeRequest.setCancelable(false);
                                                    seeRequest.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            customerId = dataSnapshot.getValue().toString();
                                                            customerKey = dataSnapshot.getKey().toString();
                                                            Toast.makeText(DriverMapsActivity.this, "Customer avaliable key = " + customerKey, Toast.LENGTH_SHORT).show();
                                                            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerThere).child("DriverAppointed");
                                                            HashMap mapAnswer = new HashMap();
                                                            //mapAnswer.put("Driver_Alloted", driverId);
                                                            mapAnswer.put("Answer", "Accept");
                                                            driverRef.updateChildren(mapAnswer);
                                                            getAssignCustomerPickUpLocation();

                                                        }
                                                    });
                                                    seeRequest.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerThere).child("DriverAppointed");
                                                            HashMap mapAnswer = new HashMap();
                                                            //mapAnswer.put("Driver_Alloted", driverId);
                                                            mapAnswer.put("Answer", "Rejected");
                                                            driverRef.updateChildren(mapAnswer);

                                                            DatabaseReference driverReject = FirebaseDatabase.getInstance().getReference().child("DriverNotAvailable").child(customerThere).child("DriverUnavailable").child(driverId);
                                                            driverReject.setValue(true);

                                                            DatabaseReference removeCustReq = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
                                                            removeCustReq.removeValue();
//                                        rejectCustomer();

//                                    geoFireRemove.removeLocation(driverId);
//
//                                    if (pickMarker != null) {
//                                        pickMarker.remove();
//                                    }
//                                    if (assignCustomerPickUpLocationListener != null) {
//                                        assignCustomerPickUpLocationRef.removeEventListener(assignCustomerPickUpLocationListener);
//                                    }
                                                            //mCustomerInfo.setVisibility(View.GONE);
                                                            mCustomerName.setText("");
                                                            mCustomerNumber.setText("");
                                                            mCustomerDestination.setText("Destination...");
                                                            mCustomerProfileImage.setImageResource(R.mipmap.prof);
                                                        }
                                                    });
                                                    AlertDialog create = seeRequest.create();
                                                    create.show();
                                                }
                                            });
                                            requestComing.show();
                                        }
                                        else
                                        {
                                            Toast.makeText(DriverMapsActivity.this, "Driver not appointed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else {
                                        erasePolylines();
                                        customerId = "";
                                        if (pickMarker != null) {
                                            pickMarker.remove();
                                        }
                                        if (assignCustomerPickUpLocationListener != null) {
                                            assignCustomerPickUpLocationRef.removeEventListener(assignCustomerPickUpLocationListener);
                                        }
                                       // mCustomerInfo.setVisibility(View.GONE);
                                        mCustomerName.setText("");
                                        mCustomerNumber.setText("");
                                        mCustomerProfileImage.setImageResource(R.mipmap.prof);
                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            } );
//                                @Override
//                                public void onDataChange(final DataSnapshot dataSnapshot) {
//                                    if (dataSnapshot.exists()){ //&& customerId.equals(""))||(dataSnapshot.exists() && customerId.equals("")==false)) {
//                                        Toast.makeText(DriverMapsActivity.this, "Customer request came", Toast.LENGTH_SHORT).show();
//                                        if (dataSnapshot.exists()) {
//
//                                            Snackbar requestComing = Snackbar.make(mdriverLayout, "A Customer request is coming", Snackbar.LENGTH_LONG).setAction("See", new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View v) {
//                                                    AlertDialog.Builder seeRequest = new AlertDialog.Builder(DriverMapsActivity.this);
//                                                    seeRequest.setTitle("Do you want to accept customer request");
//                                                    seeRequest.setCancelable(false);
//                                                    seeRequest.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//
//                                                            customerId = dataSnapshot.getValue().toString();
//                                                            customerKey = dataSnapshot.getKey().toString();
//                                                            Toast.makeText(DriverMapsActivity.this, "Customer avaliable key = " + customerKey, Toast.LENGTH_SHORT).show();
//                                                            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerKey).child("DriverAppointed");
//                                                            HashMap mapAnswer = new HashMap();
//                                                            //mapAnswer.put("Driver_Alloted", driverId);
//                                                            mapAnswer.put("Answer", "Accept");
//                                                            driverRef.updateChildren(mapAnswer);
//                                                            getAssignCustomerPickUpLocation();
//                                                            getCustomerDestination();
//                                                            getAssignedCustomerInfo();
//                                                        }
//                                                    });
//                                                    seeRequest.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
//                                                        @Override
//                                                        public void onClick(DialogInterface dialog, int which) {
//
//                                                            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("DriverAppointed");
//                                                            HashMap mapAnswer = new HashMap();
//                                                            //mapAnswer.put("Driver_Alloted", driverId);
//                                                            mapAnswer.put("Answer", "Rejected");
//                                                            driverRef.updateChildren(mapAnswer);
//
//                                                            DatabaseReference driverReject = FirebaseDatabase.getInstance().getReference().child("DriverNotAvailable").child(customerId);
//                                                            HashMap driverNotAvailable = new HashMap();
//
//                                                            driverNotAvailable.put("DriverUnavailable",driverId);
//                                                            driverReject.updateChildren(mapAnswer);
////                                        rejectCustomer();
//
////                                    geoFireRemove.removeLocation(driverId);
////
////                                    if (pickMarker != null) {
////                                        pickMarker.remove();
////                                    }
////                                    if (assignCustomerPickUpLocationListener != null) {
////                                        assignCustomerPickUpLocationRef.removeEventListener(assignCustomerPickUpLocationListener);
////                                    }
//                                                            mCustomerInfo.setVisibility(View.GONE);
//                                                            mCustomerName.setText("");
//                                                            mCustomerNumber.setText("");
//                                                            mCustomerProfileImage.setImageResource(R.mipmap.prof);
//                                                        }
//                                                    });
//                                                    AlertDialog create = seeRequest.create();
//                                                    create.show();
//                                                }
//                                            });
//                                            requestComing.show();
//                                        }
//                                        else
//                                        {
//                                            Toast.makeText(DriverMapsActivity.this, "Driver not appointed", Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                    else {
//                                        erasePolylines();
//                                        customerId = "";
//                                        if (pickMarker != null) {
//                                            pickMarker.remove();
//                                        }
//                                        if (assignCustomerPickUpLocationListener != null) {
//                                            assignCustomerPickUpLocationRef.removeEventListener(assignCustomerPickUpLocationListener);
//                                        }
//                                        mCustomerInfo.setVisibility(View.GONE);
//                                        mCustomerName.setText("");
//                                        mCustomerNumber.setText("");
//                                        mCustomerProfileImage.setImageResource(R.mipmap.prof);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });


                        }
                        else
                            Toast.makeText(DriverMapsActivity.this, "forget about customer", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




    }

    private void rejectCustomer() {
        //String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
       // String driverRemoveid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    private void getCustomerDestination() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("Destination");
       // final DatabaseReference assignCustomerCoordinates = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("Destination");//
        assignCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && customerId!=null) {

                    destination = dataSnapshot.getValue().toString();

                    mCustomerDestination.setText("Destination = "+destination);
                } else {
                    mCustomerDestination.setText("Destination...");
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

        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Custmores").child(customerThere);
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
                    //mCustomerInfo.setVisibility(View.VISIBLE);

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
         assignCustomerPickUpLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerThere).child("l");
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

    private void getRouterMarker(LatLng driverLatLng) {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()),driverLatLng)
                .build();
        routing.execute();
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
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

    @Override
    protected void onStop() {
        super.onStop();
        if(!isLoggingOut){
            disconnectDriver();
        }
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
