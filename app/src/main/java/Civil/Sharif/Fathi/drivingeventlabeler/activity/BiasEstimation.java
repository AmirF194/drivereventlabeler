package Civil.Sharif.Fathi.drivingeventlabeler.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.opencsv.CSVWriter;
import com.sharif.armin.drivingeventlabeler.R;
import com.sharif.armin.drivingeventlabeler.sensor.Sensors;
import com.sharif.armin.drivingeventlabeler.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

public class BiasEstimation extends AppCompatActivity {

    private int gyrN, racN;
    private float[] gyrMu, racMu;
    private FileOutputStream fileout;
    OutputStreamWriter outputWriter;
    private SensorManager sensorManager;
    private SensorListener sensorListener;
    static final int READ_BLOCK_SIZE = 100;
    private String fn = "biases.csv";
    private CSVWriter writer;
    final static int msecs = 20000;
    boolean finished = false;
    File f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bias_estimation);
        gyrMu = new float[] {0, 0, 0};
        racMu = new float[] {0, 0, 0};
        gyrN = 0;
        racN = 0;
    }

    public void start(View view){
        Button btn = (Button) findViewById(R.id.button);
        btn.setEnabled(false);
        ViewCompat.setBackgroundTintList(btn, getResources().getColorStateList(R.color.gray));
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorListener = new SensorListener();
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(msecs);
                        finished = true;
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void stop(){
        f = new File(MainActivity.directory.getPath() + File.separator + fn);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer = new CSVWriter(new FileWriter(f, false));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] line = new String [] {"sensor", "X", "Y", "Z"};
        writer.writeNext(line);
        sensorManager.unregisterListener(sensorListener);
        float gyrBX = 0, gyrBY = 0, gyrBZ = 0;
        if (gyrN != 0){
            gyrBX = gyrMu[0] / gyrN;
            gyrBY = gyrMu[1] / gyrN;
            gyrBZ = gyrMu[2] / gyrN;
        }
        float racBX = 0, racBY = 0, racBZ = 0;
        if (racN != 0){
            racBX = racMu[0] / racN;
            racBY = racMu[1] / racN;
            racBZ = (racMu[2] / racN) - SensorManager.GRAVITY_EARTH;
        }

        line = new String [] {"gyr", String.valueOf(gyrBX), String.valueOf(gyrBY), String.valueOf(gyrBZ)};
        writer.writeNext((line));
        line = new String [] {"rac", String.valueOf(racBX), String.valueOf(racBY), String.valueOf(racBZ)};
        writer.writeNext((line));
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Context context = getApplicationContext();
        CharSequence text = "Bias estimation has finished.";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch(event.sensor.getType()) {
                case Sensor.TYPE_GYROSCOPE:
                    gyrMu[0] += event.values[0];
                    gyrMu[1] += event.values[1];
                    gyrMu[2] += event.values[2];
                    gyrN += 1;
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    racMu[0] += event.values[0];
                    racMu[1] += event.values[1];
                    racMu[2] += event.values[2];
                    racN += 1;
                    break;
            }
            if (finished)
                stop();
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

}
