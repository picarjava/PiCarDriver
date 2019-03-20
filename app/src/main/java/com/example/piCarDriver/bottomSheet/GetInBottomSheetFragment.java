package com.example.piCarDriver.bottomSheet;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.piCarDriver.Contents;
import com.example.piCarDriver.QRCodeEncoder;
import com.example.piCarDriver.R;
import com.example.piCarDriver.model.OrderAdapterType;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class GetInBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_in, container, false);
        ImageView qrCode = view.findViewById(R.id.imageView);
        Bundle bundle = getArguments();
        assert bundle != null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("driverID", bundle.getString("driverID"));
        switch (bundle.getInt("viewType")) {
            case OrderAdapterType.SINGLE_ORDER:
            case OrderAdapterType.LONG_TERM_ORDER:
                jsonObject.addProperty("orderID", bundle.getString("orderID"));
                break;
            case OrderAdapterType.GROUP_ORDER:
            case OrderAdapterType.LONG_TERM_GROUP_ORDER:
                jsonObject.addProperty("groupID", bundle.getString("groupID"));
                break;
        }
        try {
            Bitmap qrCodeImage = new QRCodeEncoder(jsonObject.toString(), null,
                                            Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), 1500).encodeAsBitmap();
            qrCode.setImageBitmap(qrCodeImage);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return view;
    }

}
