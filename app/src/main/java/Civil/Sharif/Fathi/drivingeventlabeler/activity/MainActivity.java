package Civil.Sharif.Fathi.drivingeventlabeler.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.sharif.armin.drivingeventlabeler.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String sensor_frequency = "com.drivingeventlabeler.mainActivity.sensor_frequency";
    public static final String gps_delay = "com.drivingeventlabeler.mainActivity.gps_delay";
    public static final String TestFlag = "com.drivingeventlabeler.mainActivity.TestFlag";
    public static final String Direction = "com.drivingeventlabeler.mainActivity.Direction";

    public static File directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_mainactivity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        requestForPermissions();

        directory = new File(Environment.getExternalStorageDirectory()+File.separator+"DrivingEventLabeler");
        if (! directory.exists()) {
            directory.mkdir();
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, (Environment.getExternalStorageDirectory()+File.separator+"SensorCollector"), duration);
            toast.show();
        }
    }

    public void requestForPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    public void manualLabeling(View view){
        Intent intent = new Intent(this, ManualLabeling.class);
        String sns_f = ((EditText)findViewById(R.id.sns_te)).getText().toString();
        String gps_f = ((EditText)findViewById(R.id.gps_te)).getText().toString();

        intent.putExtra(sensor_frequency, sns_f);
        intent.putExtra(gps_delay, gps_f);
        startActivity(intent);
    }

    public void visualization(View view){
        Intent intent = new Intent(this, Visualization.class);
        String sns_f = ((EditText)findViewById(R.id.sns_te)).getText().toString();
        String gps_f = ((EditText)findViewById(R.id.gps_te)).getText().toString();

        intent.putExtra(sensor_frequency, sns_f);
        intent.putExtra(gps_delay, gps_f);
        startActivity(intent);
    }

    public void guidedLabeling(View view){
        Intent intent = new Intent(this, GuidedLabeling.class);
        String sns_f = ((EditText)findViewById(R.id.sns_te)).getText().toString();
        String gps_f = ((EditText)findViewById(R.id.gps_te)).getText().toString();
        String dir = ((EditText)findViewById(R.id.dir)).getText().toString();
        CheckBox check = ((CheckBox)findViewById(R.id.TestFlag));
        boolean flag = check.isChecked();


        intent.putExtra(sensor_frequency, sns_f);
        intent.putExtra(gps_delay, gps_f);
        intent.putExtra(TestFlag, flag);
        intent.putExtra(Direction, dir);
        startActivity(intent);
    }

    public void biasEstimation(View view){
        Intent intent = new Intent(this, BiasEstimation.class);
        startActivity(intent);
    }
}
