package com.edna.fooddonation.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.africastalking.AfricasTalking;
import com.africastalking.SmsService;
import com.africastalking.sms.Recipient;
import com.edna.fooddonation.R;
import com.edna.fooddonation.auth.LoginActivity;
import com.edna.fooddonation.auth.RegistrationActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DonationDetail extends AppCompatActivity implements OnMapReadyCallback {

    TextView txtDonorName, txtDonationItem, txtDonationDesc;
    Button btnDonationRequest;
    private FusedLocationProviderClient fusedLocationClient;

    private GoogleMap mMap;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_detail);
        AfricasTalking.initialize("naphtalimakori", "5f74609d80137158f9908b58b63bf6cceb7881f54bb97375c37aa388557bbb01");


        txtDonorName = findViewById(R.id.donorName);
        txtDonationItem = findViewById(R.id.donationItem);
        txtDonationDesc = findViewById(R.id.donationDesc);
        btnDonationRequest = findViewById(R.id.requestButton);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.donationLocation);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String donorName = extras.getString("donorname");
            String donationItem = extras.getString("donation");
            String donationDesc = extras.getString("description");


            if (donorName != null)
                txtDonorName.setText(donorName);
            if (donationItem != null)
                txtDonationItem.setText(donationItem);
            if (donationDesc != null)
                txtDonationDesc.setText(donationDesc);
        }

        btnDonationRequest.setOnClickListener(v -> {
            sendSms();
        });
    }


    private void sendSms() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String phone = extras.getString("phone");
            String donor = extras.getString("donorname");
            String[] recipients = new String[] {"+"+phone};
            String message = "Hello, " + donor + " hope you are having a great day, \n" + "I have just requested for your donation from Food Donor App. " +
                    "Get in touch with this number as you will be communication more here on the way forward. \n Cheers!";

            String donationRefKey = getIntent().getStringExtra("donationRefKey");
            DatabaseReference donationRef = FirebaseDatabase.getInstance().getReference().child("donations").child(donationRefKey);

            new SendSmsTask(donationRef).execute(message, recipients);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Double latitude = extras.getDouble("lat");
            Double longitude = extras.getDouble("long");

            LatLng donationLocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(donationLocation).title("Food Donation Location"));

            float zoomLevel = 15.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(donationLocation, zoomLevel));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SendSmsTask extends AsyncTask<Object, Void, Boolean> {
        private DatabaseReference donationRef;

        public SendSmsTask(DatabaseReference donationRef) {
            this.donationRef = donationRef;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            String message = (String) params[0];
            String[] recipients = (String[]) params[1];

            SmsService sms = AfricasTalking.getService(AfricasTalking.SERVICE_SMS);

            try {
                sms.send(message, recipients, true);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                String donationRefKey = getIntent().getStringExtra("donationRefKey");
                if (donationRefKey != null) {
                    DatabaseReference donationRef = FirebaseDatabase.getInstance().getReference("donations").child(donationRefKey);
                    donationRef.child("status").setValue(true);
                }
                startActivity(new Intent(DonationDetail.this, DonationList.class));
                Toast.makeText(DonationDetail.this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DonationDetail.this, "Failed to send SMS / The recipient is blackmailed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

