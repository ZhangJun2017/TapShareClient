package sn.zhang.deskAround.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.trigger_cast).setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(R.layout.sheet_manual_cast);
            bottomSheetDialog.show();
            bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            bottomSheetDialog.findViewById(R.id.manual_cast_button).setOnClickListener(view1 -> startActivity(new Intent().setData(Uri.parse("castto://" + ((TextInputEditText) bottomSheetDialog.findViewById(R.id.target_service_uuid_edittext)).getText() + "/" + ((TextInputEditText) bottomSheetDialog.findViewById(R.id.target_characteristic_uuid_edittext)).getText()))));
        });

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