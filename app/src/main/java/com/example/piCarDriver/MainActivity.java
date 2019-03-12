package com.example.piCarDriver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.piCarDriver.task.CommonTask;
import com.example.piCarDriver.task.ImageTask;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                               DriverCallBack, LocationNowCallBack {
    private final static String TAG = "MainActivity";
    private final static int SEQ_LOGIN = 0;
    private final static int PERMISSION_REQUEST = 0;
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private Driver driver;
    private String driverName;
    private boolean isLogin;
    private NavigationView navigationView;
    private FusedLocationProviderClient locationProviderClient;
    private SettingsClient settingsClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private Location location;
    private LocationWebSocket locationWebSocket;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView hamburger = findViewById(R.id.hamburger);
        hamburger.setOnClickListener(v -> drawer.openDrawer(Gravity.START));
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        askPermissions();
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        createLocationCallback();
        createLocationRequest();
        buildSettingLocationRequest();
        SharedPreferences preferences = getSharedPreferences(Util.preference, MODE_PRIVATE);
        String account = preferences.getString("account", "");
        String password = preferences.getString("password", "");
        if (preferences.getBoolean("login", false)) {
            if (isInvalidLogin(account, password))
                startActivityForResult(new Intent(this, LoginActivity.class), SEQ_LOGIN);
            else
                storeDriverInfo();
        } else
            startActivityForResult(new Intent(this, LoginActivity.class), SEQ_LOGIN);

    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        locationProviderClient.getLastLocation()
                              .addOnSuccessListener(location -> {
                                  this.location = location;
                                  getSupportFragmentManager().beginTransaction()
                                                             .replace(R.id.frameLayout, new MapFragment(), "Map")
                                                             .commit();
                              });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLogin) {
            URI uri = null;
            try {
                uri = new URI(Util.URL + "/locationWebSocket/" + driver.getDriverID());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if (locationWebSocket == null)
                locationWebSocket = new LocationWebSocket(uri);
            if(!locationWebSocket.isOpen())
                locationWebSocket.connect();

            startLocationUpdate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        if (locationWebSocket != null)
            locationWebSocket.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportFragmentManager().popBackStack();
            else
                super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager manager = getSupportFragmentManager();
        if (id == R.id.nav_order) {
            String order = "Order";
            manager.popBackStack(order, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            manager.beginTransaction()
                   .replace(R.id.frameLayout, new OrderFragment(), order)
                   .addToBackStack(order)
                   .commit();
        } else if (id == R.id.nav_favor_setting) {
            String preference = "Preference";
            manager.popBackStack(preference, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            manager.beginTransaction()
                   .replace(R.id.frameLayout, new PreferenceFragment(), preference)
                   .addToBackStack(preference)
                   .commit();
        } else if (id == R.id.nav_logout) {
            SharedPreferences preferences = getSharedPreferences(Util.preference, MODE_PRIVATE);
            preferences.edit()
                       .putBoolean("login", false)
                       .putString("account", "")
                       .putString("password", "")
                       .apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, SEQ_LOGIN);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SEQ_LOGIN) {
                String account = data.getStringExtra("account");
                String password = data.getStringExtra("password");
                if(isInvalidLogin(account, password))
                    startActivityForResult(new Intent(this, LoginActivity.class), SEQ_LOGIN);
                else
                    storeDriverInfo();
            }
        }
    }

    private void storeDriverInfo() {
        View view = navigationView.getHeaderView(0);
        TextView name = view.findViewById(R.id.driverName);
        name.setText(driverName);
    }

    private boolean isInvalidLogin(String account, String password) {
        SharedPreferences preferences = getSharedPreferences(Util.preference, MODE_PRIVATE);
        if (isNetworkConnected()) {
            String jsonIn = null;
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "login");
                jsonObject.addProperty("account", account);
                jsonObject.addProperty("password", password);
                jsonIn = new CommonTask().execute("/driverApi", jsonObject.toString()).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (jsonIn != null) {
                Log.d(TAG, jsonIn);
                JsonObject jsonObject = new Gson().fromJson(jsonIn, JsonObject.class);
                if (jsonObject.has("auth") && "OK".equals(jsonObject.get("auth").getAsString())) {
                    driverName = jsonObject.get("driverName").getAsString();
                    preferences.edit()
                               .putBoolean("login", true)
                               .putString("account", account)
                               .putString("password", password)
                               .apply();
                    driver = new GsonBuilder().setDateFormat("yyyy-MM-dd")
                                              .create()
                                              .fromJson(jsonObject.get("driver").getAsString(), Driver.class);
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("action", "getPicture");
                    jsonObject.addProperty("memID", driver.getMemID());
                    try {
                        new ImageTask(this).execute("/memberApi", jsonObject.toString()).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isLogin = true;
                    return false;
                }
            }
        }

        isLogin = false;
        return true;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return  info != null && info.isConnected();
    }

    private void askPermissions() {
        String[] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION,
                                 Manifest.permission.ACCESS_FINE_LOCATION
                               };
        Set<String> permissionRequest = new HashSet<>();
        for (String permission: permissions) {
            int result = checkSelfPermission(permission);
            if (result != PackageManager.PERMISSION_GRANTED)
                permissionRequest.add(permission);
        }

        if (!permissionRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionRequest.toArray(new String[permissionRequest.size()]), PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST:
                for (int result: grantResults)
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission needed", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    break;
        }
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                location = locationResult.getLastLocation();
                if (isLogin) {
                    OutputInfo outputInfo = new OutputInfo(driver.getDriverID(), new OutputInfo.LatLng(location.getLatitude(), location.getLongitude()));
                    locationWebSocket.send(new Gson().toJson(outputInfo));
                }
            }
        };
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildSettingLocationRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void startLocationUpdate() {
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }}).addOnFailureListener(this, e -> {
            int state = ((ApiException) e).getStatusCode();
            switch (state) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.e(TAG, "Location settings are not satisfied. Attempting to upgrade location settings ");
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the
                        // result in onActivityResult().
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sie) {
                        Log.e(TAG, "PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    String errorMessage = "Location settings are inadequate, and cannot be " +
                            "fixed here. Fix in Settings.";
                    Log.e(TAG, errorMessage);
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        locationProviderClient.removeLocationUpdates(locationCallback)
                              .addOnCompleteListener(this, task -> Log.e(TAG, "Cancel location updates requested"));
    }

    @Override
    public Driver driverCallBack() {
        return driver;
    }

    @Override
    public Location locationCallBack() {
        return location;
    }
}
