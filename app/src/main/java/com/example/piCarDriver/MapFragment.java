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
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.piCarDriver.bottomSheet.GetInBottomSheetFragment;
import com.example.piCarDriver.bottomSheet.GetOffBottomSheetFragment;
import com.example.piCarDriver.model.GroupOrder;
import com.example.piCarDriver.model.LongTermOrder;
import com.example.piCarDriver.model.Order;
import com.example.piCarDriver.model.OrderAdapterType;
import com.example.piCarDriver.model.SingleOrder;
import com.example.piCarDriver.webSocket.LocationWebSocket;
import com.example.piCarDriver.webSocket.WebSocketHandler;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MapFragment extends Fragment implements OnMapReadyCallback, WebSocketHandler.WebSocketCallBack {
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
    private OrderAdapterType orderAdapterType;
    private DirectionTask directionTask;
    private AnimateTask animateTask;
    private ArriveLocTask arriveLocTask;
    private WebSocketHandler webSocketHandler;
    private BottomSheetDialogFragment bottomSheetDialogFragment;

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
        webSocketHandler = new WebSocketHandler(this);
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
                getNewLocationWebSocket();
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
        Log.d(TAG, "pause");
        if (directionTask != null)
            directionTask.cancel(true);
        if (animateTask != null)
            animateTask.cancel(true);
        if (arriveLocTask != null)
            arriveLocTask.cancel(true);
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

    @SuppressLint("MissingPermission")
    public void startLocationUpdate() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                      .addOnSuccessListener(activity, locationSettingsResponse -> locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()))
                      .addOnFailureListener(activity, e -> {
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
                                 String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
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
    public void drawDirectionCallBack(OrderAdapterType orderAdapterType) {
        this.orderAdapterType = orderAdapterType;
        Order order = orderAdapterType.getOrder();
        Log.d(TAG, order.getStartLat() + " " + order.getStartLng());
        stopLocationUpdates();
        if (locationWebSocket != null)
            locationWebSocket.close();
        online.setVisibility(View.INVISIBLE);
        online.setText("上線");
        isOnline = false;
        directionTask = new DirectionTask(this, new LatLng(location.getLatitude(), location.getLongitude()),
                                          new LatLng(order.getStartLat(), order.getStartLng()));
        directionTask.execute(getString(R.string.direction_key));
        arriveLocTask = new ArriveLocTask(this, false);
        Location startLocation = new Location("");
        startLocation.setLatitude(order.getStartLat());
        startLocation.setLongitude(order.getStartLng());
        arriveLocTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, startLocation);
    }

    @Override
    public void getInSuccessCallBack() {
        Order order = orderAdapterType.getOrder();
        directionTask = new DirectionTask(this, new LatLng(location.getLatitude(), location.getLongitude()),
                                          new LatLng(order.getEndLat(), order.getEndLng()));
        directionTask.execute(getString(R.string.direction_key));
        bottomSheetDialogFragment.dismiss();
        arriveLocTask = new ArriveLocTask(this, true);
        Location endLocation = new Location("");
        endLocation.setLatitude(order.getEndLat());
        endLocation.setLongitude(order.getEndLng());
        arriveLocTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, endLocation);
    }

    @SuppressWarnings("unchecked")
    private void latLngsCallBack(List<LatLng> latLngs) {
        Log.d(TAG, "latLngsCallBack");
        animateTask = new AnimateTask(this, map);
        animateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLngs);
    }

    private static class DirectionTask extends AsyncTask<String, Void, String> {
        private final static String TAG = "CommonTask";
        private MapFragment mapFragment;
        private LatLng startLatLng;
        private LatLng endLatLng;

        DirectionTask(MapFragment mapFragment, LatLng startLatLng, LatLng endLatLng) {
            this.mapFragment = mapFragment;
            this.startLatLng = startLatLng;
            this.endLatLng = endLatLng;
        }

        @Override
        protected String doInBackground(String... strings) {
            String origin = "origin=" + startLatLng.latitude + "," + startLatLng.longitude;
            String destination = "&destination=" + endLatLng.latitude + "," + endLatLng.longitude;
            String key = "&key=" + strings[0];
            StringBuilder url = new StringBuilder().append(Constants.GOOGLE_DIRECTION_URL).append(origin).append(destination).append(key);
            Log.d(TAG, url.toString());
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url.toString()).openConnection();
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
            Log.i(TAG, Thread.currentThread().getName());
            JsonObject jsonObject = new Gson().fromJson(jsonIn, JsonObject.class);
            String encodeLine = jsonObject.get("routes").getAsJsonArray().get(0).getAsJsonObject().get("overview_polyline").getAsJsonObject().get("points").getAsString();
            List<LatLng> latLngs = PolyUtil.decode(encodeLine);
            mapFragment.map.addPolyline(new PolylineOptions().color(Color.DKGRAY).width(10).addAll(latLngs));
            LatLngBounds latLngBounds = LatLngBounds.builder()
                                                    .include(startLatLng)
                                                    .include(endLatLng)
                                                    .build();
            LatLng center = latLngBounds.getCenter();
            center = new LatLng(center.latitude - Math.abs(startLatLng.latitude - endLatLng.latitude), center.longitude);
            latLngBounds = latLngBounds.including(center);
            mapFragment.map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100));
            mapFragment.latLngsCallBack(latLngs);
        }
    }

    @SuppressWarnings("unchecked")
    private static class AnimateTask extends AsyncTask<List<LatLng>, Queue<LatLng>, Void> {
        private final static String TAG = "AnimateTask";
        private GoogleMap map;
        private MapFragment mapFragment;

        AnimateTask(MapFragment mapFragment,GoogleMap map) {
            this.map = map;
            this.mapFragment = mapFragment;
        }

        @Override
        protected Void doInBackground(List<LatLng>... lists) {
            Queue<LatLng> lngs = new LinkedList<>(lists[0]);
            try {
                while (!lngs.isEmpty()) {
                    Log.d(TAG, "driving");
                    Thread.sleep(500);
                    LatLng latLng = lngs.element();
                    Location location = new Location("");
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    mapFragment.location = location;
                    publishProgress(lngs);
                    lngs.poll();
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "thread interrupted");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Queue<LatLng>... values) {
            map.clear();
            map.addPolyline(new PolylineOptions().color(Color.DKGRAY).width(10).addAll(values[0]));
        }
    }

    private static class ArriveLocTask extends AsyncTask<Location, Void, Void> {
        private final static String TAG = "ArriveLocTask";
        private MapFragment mapFragment;
        private boolean isEnd;

        ArriveLocTask(MapFragment mapFragment, boolean isEnd) {
            this.mapFragment = mapFragment;
            this.isEnd = isEnd;
        }

        @Override
        protected Void doInBackground(Location... locations) {
            try {
                while (locations[0].distanceTo(mapFragment.location) >= 5) {
                    Log.d(TAG, String.valueOf(locations[0].distanceTo(mapFragment.location)));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "thread interrupted");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "arrive");
            Bundle bundle = new Bundle();
            Order order = mapFragment.orderAdapterType.getOrder();
            int viewType = mapFragment.orderAdapterType.getViewType();
            bundle.putInt("viewType", viewType);
            bundle.putString("driverID", order.getDriverID());
            switch (viewType) {
                case OrderAdapterType.SINGLE_ORDER:
                    bundle.putString("orderID", ((SingleOrder) order).getOrderID());
                    break;
                case OrderAdapterType.LONG_TERM_ORDER:
                    bundle.putString("orderID", ((LongTermOrder) order).getOrderIDs().get(0));
                    break;
                case OrderAdapterType.GROUP_ORDER:
                case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                    bundle.putString("groupID", ((GroupOrder) order).getGroupID());
                    break;
            }
            String tag;
            if (!isEnd) {
                mapFragment.bottomSheetDialogFragment = new GetInBottomSheetFragment();
                tag = "getIn";
            } else if (viewType == OrderAdapterType.SINGLE_ORDER || viewType == OrderAdapterType.LONG_TERM_ORDER) {
                if (viewType == OrderAdapterType.SINGLE_ORDER) {
                    SingleOrder singleOrder = (SingleOrder) order;
                    if (singleOrder.getOrderType() > 1) {
                        mapFragment.getNewLocationWebSocket();
                        return;
                    }
                }

                mapFragment.bottomSheetDialogFragment = new GetOffBottomSheetFragment();
                tag = "getOff";
            } else {
                mapFragment.getNewLocationWebSocket();
                return;
            }

            mapFragment.bottomSheetDialogFragment.setArguments(bundle);
            mapFragment.bottomSheetDialogFragment.setCancelable(false);
            mapFragment.bottomSheetDialogFragment.show(mapFragment.getChildFragmentManager(), tag);
            mapFragment.getNewLocationWebSocket();
        }
    }

    private void getNewLocationWebSocket() {
        try {
            URI uri = new URI(Constants.WEB_SOCKET_URL + "/locationWebSocket/" + driver.getDriverID());
            locationWebSocket = new LocationWebSocket(webSocketHandler, uri);
            locationWebSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
