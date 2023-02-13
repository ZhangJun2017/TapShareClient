package sn.zhang.deskAround.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.trigger_cast_type_mssample).setOnClickListener(this::triggerCast);
        findViewById(R.id.trigger_cast_type_demo).setOnClickListener(this::triggerCast);
        findViewById(R.id.trigger_cast_benchmark).setOnClickListener(view -> startActivity(new Intent().setData(Uri.parse(String.format("castto://%s/%s?type=%s", getString(R.string.demo_server_service_uuid), getString(R.string.demo_server_characteristic_uuid), "TYPE_DEMO")))));
    }

    private void triggerCast(View view) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.sheet_manual_cast);
        bottomSheetDialog.show();
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        TextInputEditText serviceUUIDEditText = bottomSheetDialog.findViewById(R.id.target_service_uuid_edittext);
        TextInputEditText characteristicUUIDEditText = bottomSheetDialog.findViewById(R.id.target_characteristic_uuid_edittext);
        Button manualCastButton = bottomSheetDialog.findViewById(R.id.manual_cast_button);
        int triggerId = view.getId();
        if (triggerId == R.id.trigger_cast_type_mssample) {
            serviceUUIDEditText.setText(R.string.sample_server_service_uuid);
            characteristicUUIDEditText.setText(R.string.sample_server_characteristic_uuid);
            manualCastButton.setOnClickListener(view1 -> startActivity(new Intent().setData(Uri.parse(String.format("castto://%s/%s?type=%s", ((TextInputEditText) bottomSheetDialog.findViewById(R.id.target_service_uuid_edittext)).getText(), ((TextInputEditText) bottomSheetDialog.findViewById(R.id.target_characteristic_uuid_edittext)).getText(), "TYPE_MS_SAMPLE_REQUIRE_NUMBER")))));
        } else if (triggerId == R.id.trigger_cast_type_demo) {
            serviceUUIDEditText.setText(R.string.demo_server_service_uuid);
            characteristicUUIDEditText.setText(R.string.demo_server_characteristic_uuid);
            manualCastButton.setOnClickListener(view1 -> startActivity(new Intent().setData(Uri.parse(String.format("castto://%s/%s?type=%s", ((TextInputEditText) bottomSheetDialog.findViewById(R.id.target_service_uuid_edittext)).getText(), ((TextInputEditText) bottomSheetDialog.findViewById(R.id.target_characteristic_uuid_edittext)).getText(), "TYPE_DEMO_REQUIRE_MESSAGE")))));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //Snackbar.make(getWindow().getDecorView(),"Setting clicked",Snackbar.LENGTH_SHORT);
            Toast.makeText(this, "Setting clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}