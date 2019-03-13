package com.example.piCarDriver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.piCarDriver.model.SingleOrder;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, WebSocketHandler.DrawDirectionCallBack {
    private final static String TAG = "MapFragment";
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private boolean isOnline;
    private GoogleMap map;
    private FusedLocationProviderClient locationProviderClient;
    private SettingsClient settingsClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private Location location;
    private MainActivity activity;
    private Button online;
    private LocationWebSocket locationWebSocket;
    private Driver driver;
    private DirectionTask directionTask;
    private List<LatLng> latLngs;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        driver = activity.driverCallBack();
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        WebSocketHandler webSocketHandler = new WebSocketHandler(this);
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        online = view.findViewById(R.id.online);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show());
        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        settingsClient = LocationServices.getSettingsClient(activity);
        createLocationCallback();
        createLocationRequest();
        buildSettingLocationRequest();
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                                 .replace(R.id.map, mapFragment, "map")
                                 .commit();
        locationProviderClient.getLastLocation()
                              .addOnSuccessListener(location -> {
                                  this.location = location;
                                  mapFragment.getMapAsync(this);
                              });
        online.setOnClickListener(v -> {
            if (!isOnline) {
                isOnline = true;
                URI uri = null;
                try {
                    uri = new URI(Constants.WEB_SOCKET_URL + "/locationWebSocket/" + driver.getDriverID());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                locationWebSocket = new LocationWebSocket(webSocketHandler, uri);
                locationWebSocket.connect();
                startLocationUpdate();
                online.setBackgroundResource(R.drawable.round_offline);
                online.setText("下線");
            } else {
                stopLocationUpdates();
                locationWebSocket.close();
                online.setText("上線");
                online.setBackgroundResource(R.drawable.round);
                isOnline = false;
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        offline();
    }

    private void offline() {
        stopLocationUpdates();
        if (directionTask != null)
            directionTask.cancel(true);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, Thread.currentThread().getName());
        map = googleMap;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                                                          .target(latLng)
                                                          .zoom(15)
                                                          .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        map.getUiSettings().setAllGesturesEnabled(false);
    }



    private void createLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                location = locationResult.getLastLocation();
                if (locationWebSocket.isOpen()) {
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

    public void startLocationUpdate() {
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }}).addOnFailureListener(activity, e -> {
            int state = ((ApiException) e).getStatusCode();
            switch (state) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    Log.e(TAG, "Location settings are not satisfied. Attempting to upgrade location settings ");
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the
                        // result in onActivityResult().
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sie) {
                        Log.e(TAG, "PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    String errorMessage = "Location settings are inadequate, and cannot be " +
                            "fixed here. Fix in Settings.";
                    Log.e(TAG, errorMessage);
                    Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    public void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        locationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(activity, task -> Log.e(TAG, "Cancel location updates requested"));
    }

    @Override
    public void drawDirectionCallBack(String message) {
        Log.i(TAG, Thread.currentThread().getName());
        offline();
        online.setVisibility(View.INVISIBLE);
        online.setText("上線");
        isOnline = false;
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd mm:ss").create();
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        SingleOrder singleOrder = null;
        if (jsonObject.has("singleOrder"))
            singleOrder = gson.fromJson(jsonObject.get("singleOrder").getAsString(), SingleOrder.class);

        assert singleOrder != null;
        directionTask = new DirectionTask(this, map, new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(singleOrder.getEndLat(), singleOrder.getEndLng()));
        directionTask.execute(Constants.GOOGLE_DIRECTION_URL + "origin=" + location.getLatitude() + "," + location.getLongitude() +
                              "&destination=" + singleOrder.getEndLat() + "," + singleOrder.getEndLng() + "&key=" + getString(R.string.direction_key));
    }

    private void latLngsCallBack(List<LatLng> latLngs) {
        this.latLngs = latLngs;

    }

    private static class DirectionTask extends AsyncTask<String, Void, String> {
        private final static String TAG = "CommonTask";
        private MapFragment mapFragment;
        private GoogleMap map;
        private LatLng startLatLng;
        private LatLng endLatLng;

        DirectionTask(MapFragment mapFragment,GoogleMap map, LatLng startLatLng, LatLng endLatLng) {
            this.map = map;
            this.mapFragment = mapFragment;
            this.startLatLng = startLatLng;
            this.endLatLng = endLatLng;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, strings[0]);
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(strings[0]).openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("content-type", "charset=utf-8;");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonIn = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                    jsonIn.append(line);

                connection.disconnect();
                bufferedReader.close();
                return jsonIn.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String jsonIn) {
            JsonObject jsonObject = new Gson().fromJson(jsonIn, JsonObject.class);
            String encodeLine = jsonObject.get("routes").getAsJsonArray().get(0).getAsJsonObject().get("overview_polyline").getAsJsonObject().get("points").getAsString();
            List<LatLng> latLngs = PolyUtil.decode(encodeLine);
            map.addPolyline(new PolylineOptions().color(Color.DKGRAY).width(10).addAll(latLngs));
            LatLngBounds latLngBounds = LatLngBounds.builder()
                                                    .include(startLatLng)
                                                    .include(endLatLng)
                                                    .build();
            LatLng center = latLngBounds.getCenter();
            center = new LatLng(center.latitude - Math.abs(startLatLng.latitude - endLatLng.latitude), center.longitude);
            latLngBounds = latLngBounds.including(center);
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
            mapFragment.latLngsCallBack(latLngs);
        }
    }
}
