package Civil.Sharif.Fathi.drivingeventlabeler.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sharif.armin.drivingeventlabeler.R;
import com.sharif.armin.drivingeventlabeler.sensor.SensorSample;
import com.sharif.armin.drivingeventlabeler.sensor.Sensors;
import com.sharif.armin.drivingeventlabeler.sensor.SensorsObserver;
import com.sharif.armin.drivingeventlabeler.write.Writer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ManualLabeling extends AppCompatActivity implements SensorsObserver{
    private TextView txtCounter;
    private int sensor_f, gps_delay;
    private Sensors sensors;
    private Writer writer;
    private long accelerateStart, brakeStart, turnRightStart,
            turnLeftStart, uTurnStart, laneChangeStart;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_manual_labeling);
        txtCounter = (TextView) findViewById(R.id.textTimer);
        CountUpTimer timer = new CountUpTimer(24 * 60 * 60 * 1000) {
            public void onTick(int second) {
                String txt = String.format("%d", second / 60) + ":" + String.format("%02d", second % 60);
                txtCounter.setText(txt);
            }
        };
        timer.start();
        Intent intent = getIntent();
        initButton((Button) findViewById(R.id.lane_change_button));
        initButton((Button) findViewById(R.id.accelerate_button));
        initButton((Button) findViewById(R.id.brake_button));
        initButton((Button) findViewById(R.id.u_turn_button));
        initButton((Button) findViewById(R.id.turn_right_button));
        initButton((Button) findViewById(R.id.turn_left_button));
        sensor_f = Integer.parseInt(intent.getStringExtra(MainActivity.sensor_frequency));
        gps_delay = Integer.parseInt(intent.getStringExtra(MainActivity.gps_delay));
        writer = new Writer(MainActivity.directory.getPath());
        sensors = Sensors.getInstance();
        sensors.setSensorManager((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        sensors.setLocationManager((LocationManager) getSystemService((Context.LOCATION_SERVICE)));
        sensors.setGpsDelay(gps_delay);
        sensors.setSensorFrequency(sensor_f);
        sensors.registerObserver(this);
        sensors.start();
        filename = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".zip";
    }

    protected void onStop(){
        super.onStop();
        sensors.removeObserver(this);
        sensors.stop();
    }
    protected void onDestroy(){
        super.onDestroy();
        sensors.removeObserver(this);
        sensors.stop();
    }

    public void stop(View view){
        this.sensors.stop();
        this.writer.saveAndRemove(filename);
        Context context = getApplicationContext();
        CharSequence text = "Data Saved into " + MainActivity.directory.getPath() + filename + ".";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorSample sample){
        switch (sample.type){
            case Sensors.TYPE_LINEAR_ACCELERATION_PHONE:
                writer.writeLinearAccelerationPhone(sample);
                break;
            case Sensors.TYPE_LINEAR_ACCELERATION_VEHICLE:
                writer.writeLinearAccelerationVehicle(sample);
                break;
            case Sensors.TYPE_RAW_ACCELERATION_PHONE:
                writer.writeRawAccelerationPhone(sample);
                break;
            case Sensors.TYPE_ANGULAR_VELOCITY_PHONE:
                writer.writeAngularVelocityPhone(sample);
                break;
            case Sensors.TYPE_ANGULAR_VELOCITY_EARTH:
                writer.writeAngularVelocityEarth(sample);
                break;
            case Sensors.TYPE_MAGNETIC_PHONE:
                writer.writeMagneticPhone(sample);
                break;
            case Sensors.TYPE_GRAVITY_PHONE:
                writer.writeGravityPhone(sample);
                break;
            case Sensors.TYPE_ROTATION_VECTOR_EARTH:
                writer.writeRotationVectorEarth(sample);
                break;
            case Sensors.TYPE_ROTATION_VECTOR_VEHICLE:
                writer.writeRotationVectorVehicle(sample);
                break;
            case Sensors.TYPE_HEADING_ANGLE_VEHICLE:
                writer.writeHeadingAngleVehicle(sample);
                break;

            // start to remove
            case Sensors.TYPE_ROTATION_VECTOR_EARTH_ANDROID:
                writer.writeRotationVectorEarthAndroid(sample);
                break;
            case Sensors.TYPE_LINEAR_ACCELERATION_PHONE_ANDROID:
                writer.writeLinearAccelerationPhoneAndroid(sample);
                break;
            // end to remove
        }
    }
    @Override
    public void onLocationChanged(Location location){
        writer.writeGPS(location);
    }

    public void initButton(Button btn) {
        btn.setTag("0");
        ViewCompat.setBackgroundTintList(btn, getResources().getColorStateList(R.color.green));
    }

    public void changeButtonColor(Button btn, String flag) {
        if (flag == "0") {
            btn.setTag("1");
            ViewCompat.setBackgroundTintList(btn, getResources().getColorStateList(R.color.orange));
        } else {
            btn.setTag("0");
            ViewCompat.setBackgroundTintList(btn, getResources().getColorStateList(R.color.green));
        }
    }

    public void laneChange(View view) {
        Button btn = (Button) findViewById(R.id.lane_change_button);
        String flag = (String) btn.getTag();
        if (flag == "0") {
            laneChangeStart = System.currentTimeMillis();
        }
        else {
            writer.writeLabel("lane_change", laneChangeStart, System.currentTimeMillis());
        }
        changeButtonColor(btn, flag);
    }

    public void turnRight(View view) {
        Button btn = (Button) findViewById(R.id.turn_right_button);
        String flag = (String) btn.getTag();
        if (flag == "0") {
            turnRightStart = System.currentTimeMillis();
        }
        else {
            writer.writeLabel("turn_rigth", turnRightStart, System.currentTimeMillis());
        }
        changeButtonColor(btn, flag);
    }

    public void turnLeft(View view) {
        Button btn = (Button) findViewById(R.id.turn_left_button);
        String flag = (String) btn.getTag();
        if (flag == "0") {
            turnLeftStart = System.currentTimeMillis();
        }
        else {
            writer.writeLabel("turn_left", turnLeftStart, System.currentTimeMillis());
        }
        changeButtonColor(btn, flag);
    }

    public void uTurn(View view) {
        Button btn = (Button) findViewById(R.id.u_turn_button);
        String flag = (String) btn.getTag();
        if (flag == "0") {
            uTurnStart = System.currentTimeMillis();
        }
        else {
            writer.writeLabel("u_turn", uTurnStart, System.currentTimeMillis());
        }
        changeButtonColor(btn, flag);
    }

    public void accelerate(View view) {
        Button btn = (Button) findViewById(R.id.accelerate_button);
        String flag = (String) btn.getTag();
        if (flag == "0") {
            accelerateStart = System.currentTimeMillis();
        }
        else {
            writer.writeLabel("acceleration", accelerateStart, System.currentTimeMillis());
        }
        changeButtonColor(btn, flag);
    }

    public void brake(View view) {
        Button btn = (Button) findViewById(R.id.brake_button);
        String flag = (String) btn.getTag();
        if (flag == "0") {
            brakeStart = System.currentTimeMillis();
        }
        else {
            writer.writeLabel("brake", brakeStart, System.currentTimeMillis());
        }
        changeButtonColor(btn, flag);
    }

    public abstract class CountUpTimer extends CountDownTimer {
        private static final long INTERVAL_MS = 1000;
        private final long duration;

        protected CountUpTimer(long durationMs) {
            super(durationMs, INTERVAL_MS);
            this.duration = durationMs;
        }

        public abstract void onTick(int second);

        @Override
        public void onTick(long msUntilFinished) {
            int second = (int) ((duration - msUntilFinished) / 1000);
            onTick(second);
        }

        @Override
        public void onFinish() {
            onTick(duration / 1000);
        }
    }
}
