package Civil.Sharif.Fathi.drivingeventlabeler.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sharif.armin.drivingeventlabeler.R;
import com.sharif.armin.drivingeventlabeler.detection.Detector;
import com.sharif.armin.drivingeventlabeler.detection.DetectorObserver;
import com.sharif.armin.drivingeventlabeler.detection.Event;
import com.sharif.armin.drivingeventlabeler.detection.SensorTest;
import com.sharif.armin.drivingeventlabeler.sensor.Sensors;
import com.sharif.armin.drivingeventlabeler.write.Writer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class GuidedLabeling extends AppCompatActivity implements DetectorObserver {
    private Thread thread = null;
    private TextView txttimer, txtlabel;
    private int sensor_f, gps_delay;
    private Sensors sensors;
    private Writer writer;
    boolean TestFlag = false, flag = false;
    private String TestDir;
    private String filename;
    private Detector detector;
    private LinkedList<Event> upcomingEvents;
    private SensorTest sensorTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guided_labeling);
        txtlabel = (TextView) findViewById(R.id.textlabel);
        txttimer = (TextView) findViewById(R.id.textTimer);
        CountUpTimer timer = new CountUpTimer(24 * 60 * 60 * 1000) {
            public void onTick(int second) {
                String txt = String.format("%d", second / 60) + ":" + String.format("%02d", second % 60);
                txttimer.setText(txt);
            }
        };
        timer.start();

        Intent intent = getIntent();
        sensor_f = Integer.parseInt(intent.getStringExtra(MainActivity.sensor_frequency));
        gps_delay = Integer.parseInt(intent.getStringExtra(MainActivity.gps_delay));
        TestFlag = intent.getBooleanExtra(MainActivity.TestFlag, false);
        TestDir = intent.getStringExtra(MainActivity.Direction);

        writer = new Writer(MainActivity.directory.getPath());
        filename = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".zip";
        if (TestFlag) {
            sensorTest = new SensorTest(TestDir);
            detector = new Detector(sensor_f, sensorTest);
            detector.registerObserver(this);
            detector.start();
            sensorTest.start();
            filename = new String(TestDir + "test.zip");
        }

        else {
            sensors = Sensors.getInstance();
            sensors.setSensorManager((SensorManager) getSystemService(Context.SENSOR_SERVICE));
            sensors.setLocationManager((LocationManager) getSystemService((Context.LOCATION_SERVICE)));
            sensors.setGpsDelay(gps_delay);
            sensors.setSensorFrequency(sensor_f);
            detector = new Detector(sensor_f, sensors);
            detector.registerObserver(this);
            detector.start();
            sensors.start();

        }
        upcomingEvents = new LinkedList<>();
    }

    @Override
    public void onEventDetected(Event event) {
        //TODO inja felan faghat write kon label e bedas omade ro bad ba python moqayese konim yeki bashe
        // feedback o inaro badan ok mikonim
        upcomingEvents.add(event);
        if (TestFlag && event.getEventLable().compareTo("Finish") == 0){
            this.writer.saveAndRemove(filename);
            this.sensorTest.stop();
            this.detector.stop();
            Context context = getApplicationContext();
            CharSequence text = "Data Saved into " + MainActivity.directory.getPath() + filename + ".";
            int duration = Toast.LENGTH_LONG;
//            Toast toast = Toast.makeText(context, text, duration);
//            toast.show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        writer.writeLabel(event.getEventLable(), event.getStart(), event.getEnd());
//        showEvent();
    }

    private void showEvent() {
        if(thread != null && (flag || upcomingEvents.size() == 0)) {
            return;
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    flag = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtlabel.setBackgroundResource(R.color.red);
                            if (upcomingEvents.getFirst().getEventLable().compareTo("turn") == 0)
                                txtlabel.setText(R.string.turn);
                            else if (upcomingEvents.getFirst().getEventLable().compareTo("brake") == 0)
                                txtlabel.setText(R.string.brake);
                            else if (upcomingEvents.getFirst().getEventLable().compareTo("lane_change") == 0)
                                txtlabel.setText(R.string.lane_change);
                        }
                    });
                    Thread.sleep(2000);
                    if (flag){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                upcomingEvents.getFirst().setEventLable(upcomingEvents.getFirst().getEventLable() + "/" + upcomingEvents.getFirst().getEventLable());
                                writeLable(upcomingEvents.getFirst());
                                showEvent();
                            }
                        });
                    flag = false;
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }



    private void writeLable(Event event) {
        writer.writeLabel(event.getEventLable(), event.getStart(), event.getEnd());
        //TODO
//        String[] s = upcomingEvents.getFirst().getEventLable().split("/");
//        if (s[0].equals(s[1])) {
//            txtlabel.setBackgroundResource(R.color.green);
//        }
//        else
//            txtlabel.setBackgroundResource(R.color.orange);
//        upcomingEvents.removeFirst();
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    protected void onStop(){
        super.onStop();
        if (!TestFlag)
            sensors.stop();
        detector.stop();
    }
    protected void onDestroy(){
        super.onDestroy();
        if (!TestFlag)
            sensors.stop();
        detector.stop();
    }

    public void stop(View view){
        this.writer.saveAndRemove(filename);
        this.sensors.stop();
        this.detector.stop();
        Context context = getApplicationContext();
        CharSequence text = "Data Saved into " + MainActivity.directory.getPath() + filename + ".";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void laneChange(View view) {
        if(thread == null || !flag){
            return;
        }
        upcomingEvents.getFirst().setEventLable("lane_change/"+upcomingEvents.getFirst());
        writeLable(upcomingEvents.getFirst());
        flag = false;
        showEvent();
    }

    public void turn(View view) {
        if(thread == null || !flag){
            return;
        }
        upcomingEvents.getFirst().setEventLable("turn/"+upcomingEvents.getFirst());
        writeLable(upcomingEvents.getFirst());
        flag = false;
        showEvent();
    }

    public void brake(View view) {
        if(thread == null || !flag){
            return;
        }
        upcomingEvents.getFirst().setEventLable("brake/"+upcomingEvents.getFirst());
        writeLable(upcomingEvents.getFirst());
        flag = false;
        showEvent();
    }

    abstract class CountUpTimer extends CountDownTimer {
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


