package com.example.piCarDriver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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

import com.example.piCarDriver.model.OrderAdapterType;
import com.example.piCarDriver.orderAdapter.OrderAdapter;
import com.example.piCarDriver.orderPageFragment.OrderPageFragment;
import com.example.piCarDriver.orderPageFragment.ScheduleOrderFragment;
import com.example.piCarDriver.task.CommonTask;
import com.example.piCarDriver.task.ImageTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                               DriverCallBack,
                                                               OrderFragment.OrderPagesCallBack,
                                                               OrderAdapter.ExecuteSchedule {
    private final static String TAG = "MainActivity";
    private final static int SEQ_LOGIN = 0;
    private final static int PERMISSION_REQUEST = 0;
    private Driver driver;
    private String driverName;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private List<OrderPage> orderPages;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer_layout);
        ImageView hamburger = findViewById(R.id.hamburger);
        hamburger.setOnClickListener(v -> drawer.openDrawer(Gravity.START));
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        askPermissions();
        SharedPreferences preferences = getSharedPreferences(Constants.preference, MODE_PRIVATE);
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

    @Override
    protected void onStart() {
        super.onStart();
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.frameLayout, new MapFragment(), "Map")
                                   .commit();
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
        if (id == R.id.nav_order) {
            orderPages = new OrderPagesBuilder().addFragment("/singleOrderApi","getNewSingleOrder", OrderAdapterType.SINGLE_ORDER, "單人訂單")
                                                .addFragment("/singleOrderApi", "getLongTermSingleOrder", OrderAdapterType.LONG_TERM_ORDER, "長期訂單")
                                                .addFragment("/groupOrderApi", "getGroupOrder", OrderAdapterType.GROUP_ORDER, "揪團訂單")
                                                .addFragment("/groupOrderApi", "getLongTermGroupOrder", OrderAdapterType.LONG_TERM_GROUP_ORDER, "長期揪團訂單")
                                                .build();
            setNavigationItemFragment("SingleOrder", new OrderFragment());
        } else if (id == R.id.nav_schedule) {
            setNavigationItemFragment("Schedule", new ScheduleOrderFragment());
        } else if (id == R.id.nav_favor_setting) {
            setNavigationItemFragment("Preference", new PreferenceFragment());
        } else if (id == R.id.nav_logout) {
            SharedPreferences preferences = getSharedPreferences(Constants.preference, MODE_PRIVATE);
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

    private void setNavigationItemFragment(String tag, Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        manager.beginTransaction()
               .replace(R.id.frameLayout, fragment, tag)
               .addToBackStack(tag)
               .commit();
    }

    @Override
    public void executeSchedule(OrderAdapterType orderAdapterType) {
        Log.d(TAG, String.valueOf(getSupportFragmentManager().getBackStackEntryCount()));
        while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("Map");
        if (mapFragment != null) {
            new HoldingTask(mapFragment, orderAdapterType).execute();
        }
    }

    private static class HoldingTask extends AsyncTask<Void, OrderAdapterType, Void> {
        private final static String TAG = "HoldingTask";
        private OrderAdapterType orderAdapterType;
        private MapFragment mapFragment;

        HoldingTask(MapFragment mapFragment, OrderAdapterType orderAdapterType) {
            this.mapFragment = mapFragment;
            this.orderAdapterType = orderAdapterType;
        }

        @Override
        protected Void doInBackground(Void... voids) {
                try {
                    while (!mapFragment.isResumed())
                        Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mapFragment.drawDirectionCallBack(orderAdapterType);
        }
    }

    private class OrderPagesBuilder {
        private List<OrderPage> orderPages;

        private OrderPagesBuilder() {
            orderPages = new ArrayList<>();
        }

        private OrderPagesBuilder addFragment(String url, String action, int orderAdapterType, String title) {
            OrderPageFragment orderPageFragment = new OrderPageFragment();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", action);
            Bundle bundle = new Bundle();
            bundle.putString("url", url);
            bundle.putString("action", jsonObject.toString());
            bundle.putInt("orderAdapterType", orderAdapterType);
            orderPageFragment.setArguments(bundle);
            orderPages.add(new OrderPage(orderPageFragment, title));
            return this;
        }

        private List<OrderPage> build() {
            return orderPages;
        }
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
        SharedPreferences preferences = getSharedPreferences(Constants.preference, MODE_PRIVATE);
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
                                              .fromJson(jsonObject.get("driver"), Driver.class);
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
                    return false;
                }
            }
        }

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
            ActivityCompat.requestPermissions(this, permissionRequest.toArray(new String[0]), PERMISSION_REQUEST);
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

    @Override
    public Driver driverCallBack() {
        return driver;
    }

    @Override
    public List<OrderPage> orderPagesCallBack() {
        return orderPages;
    }
}
