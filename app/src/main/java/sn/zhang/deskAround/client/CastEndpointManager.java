package sn.zhang.deskAround.client;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import no.nordicsemi.android.ble.BleManager;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class CastEndpointManager extends BleManager {
    private String TAG = "CastEndpointManager";
    public String castTargetServiceUUID;
    private BluetoothGattService castTargetService;
    public String castTargetCharUUID;
    private BluetoothGattCharacteristic castTargetChar;
    public String deviceName;

    public CastEndpointManager(@NonNull final Context context, String castTargetServiceUUID, String castTargetCharUUID) {
        super(context);
        this.castTargetServiceUUID = castTargetServiceUUID;
        this.castTargetCharUUID = castTargetCharUUID;
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

    public void cast(int value) {
        writeCharacteristic(castTargetChar, ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                .done(device -> Messager.announce(getContext(), Messager.ACTION_CAST_DONE))
                .fail((device, status) -> Messager.announce(getContext(), Messager.ACTION_CAST_FAILED, String.valueOf(status)))
                .enqueue();
    }
}
