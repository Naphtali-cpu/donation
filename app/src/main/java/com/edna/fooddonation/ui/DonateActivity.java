package com.edna.fooddonation.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.edna.fooddonation.MainActivity;
import com.edna.fooddonation.R;
import com.edna.fooddonation.model.Donation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DonateActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText etFullName, etFoodItem, etPhone, etDescription;
    private GoogleMap mMap;

    private Button btDonate;
    private DatabaseReference databaseReference;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        databaseReference = FirebaseDatabase.getInstance().getReference("donations");

        etFullName = findViewById(R.id.fullname);
        etFoodItem = findViewById(R.id.foodItem);
        etPhone = findViewById(R.id.phone);
        etDescription = findViewById(R.id.description);
        btDonate = findViewById(R.id.buttonDonate);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.donor_map);
        mapFragment.getMapAsync(this);

        btDonate.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                requestLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (checkLocationPermission()) {
            // Get last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            });
        } else {
            // If location permission is not granted, Nairobi's coordinates are used as a default
            LatLng nairobi = new LatLng(-1.286389, 36.817223);
            mMap.addMarker(new MarkerOptions().position(nairobi).title("Nairobi"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nairobi, 10));
        }
    }

    private void requestLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    saveDonation(location);
                } else {
                    startLocationUpdates();
                }
            });
        }
    }

    private void startLocationUpdates() {
        if (checkLocationPermission()) {
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)
                    .setFastestInterval(5000);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            saveDonation(location);
                            stopLocationUpdates();
                            break;
                        }
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }
        }
    }

    private void saveDonation(Location location) {
        String fullname = etFullName.getText().toString().trim();
        String foodItem = etFoodItem.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (fullname.isEmpty()) {
            Toast.makeText(this, "Please fill in Fullname", Toast.LENGTH_SHORT).show();
        } else if (foodItem.isEmpty()) {
            Toast.makeText(this, "Please fill in the food item", Toast.LENGTH_SHORT).show();
        } else if (phone.isEmpty()) {
            Toast.makeText(this, "Please fill in your phone number", Toast.LENGTH_SHORT).show();
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Please fill in your description", Toast.LENGTH_SHORT).show();
        } else {
            boolean status = false;

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String donationId = databaseReference.push().getKey();

            Donation donation = new Donation(userId, fullname, foodItem, phone, description,
                    location.getLongitude(), location.getLatitude(), status);
            databaseReference.child(donationId).setValue(donation);

            Toast.makeText(this, "Donated " + foodItem + " Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DonateActivity.this, DonationList.class));
            finish();
        }
    }
}
