package com.example.taximobil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomersMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener

{

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    Marker driverMarker,PicUpMarker;
    GeoQuery  geoQuery;

    private Button customerLogoutButton,settingsButton;
    private Button callTaxiButton;
    private  String customerID;
    private LatLng CustomerPosition;
    private int radius = 1;
    private Boolean driverFound = false, requestType;
    private String driverFoundId;


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference CustomerDataBaseRef;
    private DatabaseReference DriversAvailableRef;
    private DatabaseReference DriversRef;
    private DatabaseReference DriversLocationRef;

    private ValueEventListener DriverLoactionRefListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);

        customerLogoutButton = (Button) findViewById(R.id.customer_logout_button);
        settingsButton =(Button)findViewById(R.id.customer_settings_button);
        callTaxiButton = (Button) findViewById(R.id.customer_order_button);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Requests");
        DriversAvailableRef = FirebaseDatabase.getInstance().getReference().child("Driver Available");
        DriversLocationRef = FirebaseDatabase.getInstance().getReference().child("Driver Working");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomersMapActivity.this,SettingsActivity.class);
                intent.putExtra("type", "Customers");
                startActivity(intent);
            }
        });

        customerLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                LogoutCustimer();
            }
        });

        callTaxiButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (requestType)
                {
                    requestType = false;
                    geoQuery.removeAllListeners();
                    DriversLocationRef.removeEventListener(DriverLoactionRefListener);

                    if(driverFound !=null)
                    {
                        DriversRef = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Drivers").child(driverFoundId).child("CustomerRideID");


                        DriversRef.removeValue();
                        driverFoundId = null;
                    }

                    driverFound = false;
                    radius = 1;
                    GeoFire geofire = new GeoFire(CustomerDataBaseRef);
                    geofire.removeLocation(customerID);

                    if(PicUpMarker !=null)
                    {
                        PicUpMarker.remove();
                    }

                    if(driverMarker !=null)
                    {
                        driverMarker.remove();
                    }


                    callTaxiButton.setText("Вызвать такси");
                }
                else
                {

                    requestType = true;

                    GeoFire geofire = new GeoFire(CustomerDataBaseRef);
                    geofire.setLocation(customerID,new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()));

                    CustomerPosition = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                    PicUpMarker = mMap.addMarker(new MarkerOptions().position(CustomerPosition)
                            .title("Местоположение").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                    callTaxiButton.setText("Поиск свободного авто...");
                    getNearbyDrivers();
                }

            }
        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastlocation = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

      String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();


    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    private void LogoutCustimer() {
        Intent welcomeIntent = new Intent(CustomersMapActivity.this,WelcomeActivity.class);
        startActivity(welcomeIntent);
        finish();
    }

    private void getNearbyDrivers() {
        GeoFire geoFire = new GeoFire(DriversAvailableRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPosition.latitude,CustomerPosition.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType)
                {
                    driverFound = true;
                    driverFoundId = key;

                    DriversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID",customerID);
                    DriversRef.updateChildren(driverMap);

                    GetDriverLocation();
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

            }

            @Override
            public void onGeoQueryError(DatabaseError error)
            {
                if (!driverFound)
                {
                    radius = radius + 1;
                    getNearbyDrivers();
                }
            }
        });
    }

    private void GetDriverLocation() {
   DriverLoactionRefListener =  DriversLocationRef.child(driverFoundId).child("l").addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists() && requestType)
            {
                List<Object> driverLocationMap = (List<Object>) snapshot.getPriority();

                double LocationLat = 0;
                double LocationNg = 0;

                callTaxiButton.setText("Водитель найден");


                if(driverLocationMap.get(0) !=null)
                {
                    LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                }

                if(driverLocationMap.get(1) !=null)
                {
                    LocationNg = Double.parseDouble(driverLocationMap.get(1).toString());
                }
                LatLng DriverLatLng = new LatLng(LocationLat, LocationNg);
                if(driverMarker !=null)
                {
                    driverMarker.remove();
                }
                Location location1 = new Location("");
                location1.setLatitude(CustomerPosition.latitude);
                location1.setLongitude(CustomerPosition.longitude);


                Location location2 = new Location("");
                location1.setLatitude(DriverLatLng.latitude);
                location1.setLongitude(DriverLatLng.longitude);

                float Distance = location1.distanceTo(location2);
                if(Distance>100)
                {
                    callTaxiButton.setText("Ваше такси подъезжает" );
                }
                else {

                callTaxiButton.setText("Расстояние до такси" + String.valueOf(Distance));

                driverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Водитель рядом").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    }
}
