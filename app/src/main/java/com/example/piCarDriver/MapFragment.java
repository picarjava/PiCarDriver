package com.example.piCarDriver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
    private boolean isExecuting;
    private boolean isEnd;
    private GoogleMap map;
    private FusedLocationProviderClient locationProviderClient;
    private SettingsClient settingsClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private Location location;
    private MainActivity activity;
    private Button online;
    private ToggleButton toggleButton;
    private LocationWebSocket locationWebSocket;
    private Driver driver;
    private OrderAdapterType orderAdapterType;
    private AsyncTask locationUpdateTask, directionTask, animateTask, arriveLocTask;
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
        toggleButton = view.findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener((v, b)-> {
            if (isOnline) {
                toggleButton.setClickable(false);
            }
        });
        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        settingsClient = LocationServices.getSettingsClient(activity);
        createLocationCallback();
        createLocationRequest();
        buildSettingLocationRequest();
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                                 .replace(R.id.map, mapFragment, "map")
                                 .commit();
        mapFragment.getMapAsync(this);
        online.setOnClickListener(v -> setOnlineButton());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "pause");
        stopLocationUpdates();
        if (directionTask != null)
            directionTask.cancel(true);
        if (animateTask != null)
            animateTask.cancel(true);
        if (arriveLocTask != null)
            arriveLocTask.cancel(true);
        if (locationUpdateTask != null)
            locationUpdateTask.cancel(true);
        if (locationWebSocket != null)
            locationWebSocket.close();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, Thread.currentThread().getName());
        map = googleMap;
        if (location == null)
            locationProviderClient.getLastLocation().addOnSuccessListener(activity, loc -> {
               location = loc;
               initMap();
            });
        else
            initMap();
    }

    private void initMap() {
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
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                  .target(latLng)
                                                                  .zoom(15)
                                                                  .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                location = locationResult.getLastLocation();
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
        isEnd = false;
        isExecuting = true;
        this.orderAdapterType = orderAdapterType;
        Order order = orderAdapterType.getOrder();
        Log.d(TAG, order.getStartLat() + " " + order.getStartLng());
        if (toggleButton.isChecked())
            stopLocationUpdates();
        // ensure is online
        if (!isOnline) {
            setOnlineButton();
        }

        online.setVisibility(View.INVISIBLE);
        directionTask = new DirectionTask(this, new LatLng(location.getLatitude(), location.getLongitude()),
                                          new LatLng(order.getStartLat(), order.getStartLng())).execute(getString(R.string.direction_key));
    }

    @Override
    public void getInSuccessCallBack() {
        isEnd = true;
        Order order = orderAdapterType.getOrder();
        directionTask = new DirectionTask(this, new LatLng(location.getLatitude(), location.getLongitude()),
                                          new LatLng(order.getEndLat(), order.getEndLng())).execute(getString(R.string.direction_key));
        bottomSheetDialogFragment.dismiss();

    }

    @SuppressWarnings("unchecked")
    private void latLngsCallBack(List<LatLng> latLngs) {
        Log.d(TAG, "latLngsCallBack");
        Order order = orderAdapterType.getOrder();
        Location endLocation = new Location("");
        endLocation.setLatitude(order.getEndLat());
        endLocation.setLongitude(order.getEndLng());

        if (toggleButton.isChecked()) {
            arriveLocTask = new ArriveLocTask(this, latLngs).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, endLocation);
            animateTask = new AnimateTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLngs);
        } else {
            if (location.distanceTo(endLocation) <= 5 || latLngs.isEmpty())
                onArrive();
            else {
                sendOnExecuting(latLngs);
                map.addPolyline(new PolylineOptions().color(Color.DKGRAY).width(10).addAll(latLngs));
                LatLng latLngNow = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                  .target(latLngNow)
                                                                  .zoom(17)
                                                                  .build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                directionTask = new DirectionTask(this, latLngNow, latLngs.get(latLngs.size() - 1)).execute(getString(R.string.direction_key));
            }
        }
    }

    private void sendOnExecuting(List<LatLng> latLngs) {
        if (locationWebSocket != null && locationWebSocket.isOpen()) {
            Gson gson = new Gson();
            Order order = orderAdapterType.getOrder();
            OutputInfo outputInfo = new OutputInfo(driver.getDriverID(), new OutputInfo.LatLng(location.getLatitude(), location.getLongitude()), false);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("getIn", isEnd);
            jsonObject.add("latLngs", gson.toJsonTree(latLngs));
            jsonObject.add("outputInfo", gson.toJsonTree(outputInfo));
            jsonObject.addProperty("memID", order.getMemId());
            locationWebSocket.send(jsonObject.toString());
        }
    }

    private static class DirectionTask extends AsyncTask<String, Void, String> {
        private final static String TAG = "DirectionTask";
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
            StringBuilder url = new StringBuilder().append(Constants.GOOGLE_DIRECTION_URL)
                                                   .append(origin)
                                                   .append(destination).append(key);
            Log.d(TAG, url.toString());
            try {
                if (!mapFragment.toggleButton.isChecked())
                    Thread.sleep(1000);

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
            } catch (InterruptedException e) {
                Log.d(TAG, "thread interrupted");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String jsonIn) {
            Log.i(TAG, Thread.currentThread().getName());
            JsonObject jsonObject = new Gson().fromJson(jsonIn, JsonObject.class);
            String encodeLine = jsonObject.get("routes")
                                          .getAsJsonArray()
                                          .get(0)
                                          .getAsJsonObject()
                                          .get("overview_polyline")
                                          .getAsJsonObject()
                                          .get("points")
                                          .getAsString();
            List<LatLng> latLngs = PolyUtil.decode(encodeLine);
            mapFragment.latLngsCallBack(latLngs);
        }
    }

    @SuppressWarnings("unchecked")
    private static class AnimateTask extends AsyncTask<List<LatLng>, Queue<LatLng>, Void> {
        private final static String TAG = "AnimateTask";
        private GoogleMap map;
        private MapFragment mapFragment;
        private Queue<LatLng> lngs;

        AnimateTask(MapFragment mapFragment) {
            this.mapFragment = mapFragment;
            this.map = mapFragment.map;
        }

        @Override
        protected Void doInBackground(List<LatLng>... lists) {
            lngs = new LinkedList<>(lists[0]);
            try {
                while (!lngs.isEmpty()) {
                    Log.d(TAG, "driving");
                    Thread.sleep(500);
                    LatLng latLng = lngs.element();
                    Location location = new Location("");
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    mapFragment.location = location;
                    mapFragment.sendOnExecuting((List<LatLng>) lngs);
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
            LatLng latLng = new LatLng(mapFragment.location.getLatitude(), mapFragment.location.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                                                              .target(latLng)
                                                              .zoom(17)
                                                              .build();
            map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(((BitmapDrawable) mapFragment.activity.getPhoto()).getBitmap())));
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
    }

    private static class ArriveLocTask extends AsyncTask<Location, Void, Void> {
        private final static String TAG = "ArriveLocTask";
        private MapFragment mapFragment;
        private List<LatLng> list;

        ArriveLocTask(MapFragment mapFragment, List<LatLng> list) {
            this.mapFragment = mapFragment;
            this.list = list;
        }

        @Override
        protected Void doInBackground(Location... locations) {
            try {
                AsyncTask animateTask = mapFragment.animateTask;
                while (locations[0].distanceTo(mapFragment.location) >= 5 || list.isEmpty()) {
                    if (animateTask != null && animateTask.getStatus() == Status.FINISHED)
                        break;

                    Log.d(TAG, String.valueOf(locations[0].distanceTo(mapFragment.location)));
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "thread interrupted");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mapFragment.onArrive();
        }
    }

    private void onArrive() {
        Log.d(TAG, "arrive");
        Bundle bundle = new Bundle();
        Order order = orderAdapterType.getOrder();
        int viewType = orderAdapterType.getViewType();
        bundle.putInt("viewType", viewType);
        bundle.putString("memID", order.getMemId());
        switch (viewType) {
            case OrderAdapterType.SINGLE_ORDER:
                bundle.putString("orderID", ((SingleOrder) order).getOrderID());
                break;
            case OrderAdapterType.LONG_TERM_ORDER:
                bundle.putString("orderID", ((LongTermOrder) order).getOrderIDs().get(0));
                break;
            case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                bundle.putLong("startTime", order.getStartTime().getTime());
            case OrderAdapterType.GROUP_ORDER:
                bundle.putString("groupID", ((GroupOrder) order).getGroupID());
                break;
        }

        String tag;
        if (!isEnd) {
            bottomSheetDialogFragment = new GetInBottomSheetFragment();
            isEnd = true;
            tag = "getIn";
        } else {
            bottomSheetDialogFragment = new GetOffBottomSheetFragment();
            tag = "getOff";
        }

        bottomSheetDialogFragment.setCancelable(false);
        bottomSheetDialogFragment.setArguments(bundle);
        bottomSheetDialogFragment.show(getChildFragmentManager(), tag);
    }

    private void setOnlineButton() {
        Log.d(TAG, String.valueOf(isOnline));
        if (!isOnline) {
            toggleButton.setClickable(false);
            if (online.getVisibility() == View.INVISIBLE)
                online.setVisibility(View.VISIBLE);

            if (driver.getBanned() == 1) {
                Toast.makeText(activity, "Banned!!!", Toast.LENGTH_SHORT).show();
                return;
            }

            isEnd = false;
            isOnline = true;
            isExecuting = false;
            if (locationWebSocket == null || locationWebSocket.isClosed())
                getNewLocationWebSocket();

            if (!toggleButton.isChecked())
                startLocationUpdate();

            locationUpdateTask = new LocationUpdateTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            online.setBackgroundResource(R.drawable.round_offline);
            online.setText("下線");
        } else {
            stopLocationUpdates();
            locationWebSocket.close();
            online.setText("上線");
            online.setBackgroundResource(R.drawable.round);
            isOnline = false;
            toggleButton.setClickable(true);
            locationUpdateTask.cancel(true);
        }
    }

    public void getNewLocationWebSocket() {
        try {
            URI uri = new URI(Constants.WEB_SOCKET_URL + "/locationWebSocket/" + driver.getDriverID());
            locationWebSocket = new LocationWebSocket(webSocketHandler, uri);
            locationWebSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static class LocationUpdateTask extends AsyncTask<Void, Void, Void> {
        private MapFragment mapFragment;

        private LocationUpdateTask(MapFragment mapFragment) {
            this.mapFragment = mapFragment;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            LocationWebSocket webSocket = mapFragment.locationWebSocket;
            Location location = mapFragment.location;
            Driver driver = mapFragment.driver;
            try {
                while (!mapFragment.isExecuting) {
                    if (webSocket != null && webSocket.isOpen()) {
                        OutputInfo outputInfo = new OutputInfo(driver.getDriverID(), new OutputInfo.LatLng(location.getLatitude(), location.getLongitude()), false);
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.add("outputInfo", new Gson().toJsonTree(outputInfo));
                        webSocket.send(jsonObject.toString());
                    }

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "thread interrupted");
            }

            return null;
        }
    }

    public void setOnlineButtonVisible() {
        isExecuting = false;
        online.setVisibility(View.VISIBLE);
    }
}
