package sn.zhang.deskAround.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import no.nordicsemi.android.ble.BleManager;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CastEndpointManager extends BleManager {
    private String TAG = "CastEndpointManager";
    public String castTargetServiceUUID;
    private BluetoothGattService castTargetService;
    public String castTargetCharUUID;
    private BluetoothGattCharacteristic castTargetChar;
    public String deviceName;
    public Uri sourceUri;
    public final String castType;
    public String content = "";
    public boolean isPrepared = true;

    public CastEndpointManager(@NonNull final Context context, String castTargetServiceUUID, String castTargetCharUUID) {
        super(context);
        this.castTargetServiceUUID = castTargetServiceUUID.toLowerCase();
        this.castTargetCharUUID = castTargetCharUUID.toLowerCase();
        castType = "TYPE_DEMO";
    }

    public CastEndpointManager(@NonNull final Context context, Uri uri) {
        super(context);
        sourceUri = uri;
        this.castTargetServiceUUID = uri.getHost().toLowerCase();
        this.castTargetCharUUID = uri.getLastPathSegment().toLowerCase();
        if (sourceUri.getQueryParameterNames().contains("type")) {
            castType = sourceUri.getQueryParameter("type");
            switch (castType) {
                case "TYPE_MS_SAMPLE_REQUIRE_NUMBER":
                case "TYPE_DEMO_REQUIRE_MESSAGE": {
                    isPrepared = false;
                    new Handler().postDelayed(() -> Messager.announce(context, Messager.ACTION_CONTENT_PREPARATION_REQUIRED_INTERNAL), 0);
                    //Messager.announce(context, Messager.ACTION_CONTENT_PREPARATION_REQUIRED);
                    break;
                }
                case "TYPE_EXTERNAL_PROVIDER_PREFERRED": {
                    isPrepared = false;
                    new Handler().postDelayed(() -> Messager.announce(context, Messager.ACTION_CONTENT_PREPARATION_REQUIRED), 0);
                    new Handler().postDelayed(() -> Messager.announce(context, Messager.ACTION_CONTENT_PREPARATION_REQUIRED_CLIPBOARD), 500);
                    break;
                }
            }
        } else {
            castType = "TYPE_DEMO";
        }
    }

    @Override
    protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
        castTargetService = gatt.getService(UUID.fromString(castTargetServiceUUID));
        if (castTargetService != null) {
            castTargetChar = castTargetService.getCharacteristic(UUID.fromString(castTargetCharUUID));
        }
        Log.d(TAG, "isRequiredServiceSupported: " + (castTargetChar != null));
        return castTargetChar != null;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void initialize() {
        deviceName = getBluetoothDevice().getName();
    }

    @Override
    protected void onServicesInvalidated() {
        castTargetService = null;
        castTargetChar = null;
    }

    public void cast() {
        if (isPrepared && isReady()) {
            byte[] data;
            switch (castType) {
                case "TYPE_MS_SAMPLE_REQUIRE_NUMBER": {
                    data = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(Integer.parseInt(content)).array();
                    break;
                }
                case "TYPE_DEMO":
                case "TYPE_DEMO_REQUIRE_MESSAGE":
                case "TYPE_EXTERNAL_PROVIDER_PREFERRED": {
                    data = content.getBytes(StandardCharsets.UTF_8);
                    break;
                }
                default: {
                    Messager.announce(getContext(), Messager.ACTION_CAST_FAILED, "未知的投送类型");
                    return;
                }
            }
            writeCharacteristic(castTargetChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                    .done(device -> Messager.announce(getContext(), Messager.ACTION_CAST_DONE))
                    .fail((device, status) -> Messager.announce(getContext(), Messager.ACTION_CAST_FAILED, String.valueOf(status)))
                    .enqueue();
        }
    }
}
