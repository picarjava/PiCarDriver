package com.example.piCarDriver.bottomSheet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.piCarDriver.MapFragment;
import com.example.piCarDriver.R;
import com.example.piCarDriver.model.OrderAdapterType;
import com.example.piCarDriver.task.CommonTask;
import com.google.gson.JsonObject;

public class GetOffBottomSheetFragment extends BottomSheetDialogFragment {
    private final static String TAG = "GetOffBottomSheetFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_off, container, false);
        Button getOff = view.findViewById(R.id.getOff);
        Bundle bundle = getArguments();
        assert bundle != null;
        getOff.setOnClickListener(v -> {
            String url = null;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getOffPiCar");
            jsonObject.addProperty("memID", bundle.getString("memID"));
            switch (bundle.getInt("viewType")) {
                case OrderAdapterType.SINGLE_ORDER:
                    jsonObject.addProperty("singleOrder", "singleOrder");
                case OrderAdapterType.LONG_TERM_ORDER:
                    url = "/singleOrderApi";
                    jsonObject.addProperty("orderID", bundle.getString("orderID"));
                    Log.d("sss", bundle.getString("orderID"));
                    break;
                case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                    jsonObject.addProperty("startTime", bundle.getLong("startTime"));
                case OrderAdapterType.GROUP_ORDER:
                    url = "/groupOrderApi";
                    jsonObject.addProperty("groupID", bundle.getString("groupID"));
                    break;
            }

            Log.d(TAG, jsonObject.toString());
            new CommonTask().execute(url, jsonObject.toString());
            assert getParentFragment() != null;
            MapFragment mapFragment = ((MapFragment)getParentFragment());
            mapFragment.setOnlineButtonVisible();
            mapFragment.getNewLocationWebSocket();
            dismiss();

        });
        return view;
    }
}
