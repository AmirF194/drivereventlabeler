package Civil.Sharif.Fathi.drivingeventlabeler.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sharif.armin.drivingeventlabeler.R;
import com.sharif.armin.drivingeventlabeler.sensor.Sensors;
import com.sharif.armin.drivingeventlabeler.util.Utils;

public class Visualization extends AppCompatActivity{
    private Sensors sensors;
    private LineChart mChart;
    private Thread thread = null;
    private int sensor_f, gpsDelay;
    private boolean plotData = true;

    private SensorListener sensorListener;
    private SensorManager sensorManager;

    private String to_plot = "acc";
    private boolean x = true, y = true, z = true;
    private int x_pos = 0;
    ILineDataSet set, set1, set2;
    LineData data;

    private TextView tvX, tvY, tvZ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_visualization);
        Intent intent = getIntent();

        tvX = (TextView) findViewById(R.id.xVal);
        tvY = (TextView) findViewById(R.id.yVal);
        tvZ = (TextView) findViewById(R.id.zVal);

        sensor_f = Integer.parseInt(intent.getStringExtra(MainActivity.sensor_frequency));
        gpsDelay = Integer.parseInt(intent.getStringExtra(MainActivity.gps_delay));

        sensors = Sensors.getInstance();
        sensors.setSensorManager((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        sensors.setLocationManager((LocationManager) getSystemService((Context.LOCATION_SERVICE)));
        sensors.setGpsDelay(gpsDelay);
        sensors.setSensorFrequency(sensor_f);

        sensors.start();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorListener = new SensorListener();
        sensorManager.registerListener((SensorEventListener) this.sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                Utils.freq2delay(this.sensor_f));

        mChart = findViewById(R.id.chart1);
        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("Sensor Data");
        mChart.setDrawGridBackground(false);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setTextColor(Color.BLACK);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.GRAY);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        startPlot();

        CheckBox xCheck = (CheckBox)findViewById(R.id.x_check);
        CheckBox yCheck = (CheckBox)findViewById(R.id.y_check);
        CheckBox zCheck = (CheckBox)findViewById(R.id.z_check);
        xCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                x = ! x;
                set.setVisible(x);
            }
        });
        yCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                y = ! y;
                set1.setVisible(y);
            }
        });
        zCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                z = ! z;
                set2.setVisible(z);
            }
        });
    }

    private void startPlot() {
        if(thread != null){
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        plotData = true;
                        Thread.sleep(1000 / sensor_f);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void addEntry(float[] sensor_data) {
        data = mChart.getData();
        if(data != null){
            set = data.getDataSetByLabel("x", true);
            set1 = data.getDataSetByLabel("y", true);
            set2 = data.getDataSetByLabel("z", true);
            if(set == null){
                set = createSet();
                data.addDataSet(set);
                if(!x){
                    set.setVisible(x);
                }
            }
            if(set1 == null) {
                set1 = createSet1();
                data.addDataSet(set1);
                if(!y){
                    set1.setVisible(y);
                }
            }
            if(set2 == null){
                set2 = createSet2();
                data.addDataSet(set2);
                if(!z){
                    set2.setVisible(z);
                }
            }
            if(x){
                tvX.setText(String.format("%.02f", sensor_data[0]));
            }
            if(y){
                tvY.setText(String.format("%.02f", sensor_data[1]));
            }
            if(z){
                tvZ.setText(String.format("%.02f", sensor_data[2]));
            }
            data.addEntry(new Entry(x_pos, sensor_data[0]), data.getIndexOfDataSet(set));
            data.addEntry(new Entry(x_pos, sensor_data[1]), data.getIndexOfDataSet(set1));
            data.addEntry(new Entry(x_pos, sensor_data[2]), data.getIndexOfDataSet(set2));

            if(set.getEntryCount() >= 200){
                set.removeFirst();
            }
            if(set1.getEntryCount() >= 200){
                set1.removeFirst();
            }
            if(set2.getEntryCount() >= 200){
                set2.removeFirst();
            }

            x_pos += 1;
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setMaxVisibleValueCount(200);
            mChart.moveViewToX(data.getEntryCount());
            mChart.setVisibleXRangeMaximum(200);
        }
    }

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null,"X Axis");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(Color.MAGENTA);
        set.setHighLightColor(Color.MAGENTA);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setLabel("x");
        return  set;
    }
    private LineDataSet createSet1(){
        LineDataSet set = new LineDataSet(null,"Y Axis");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(Color.BLUE);
        set.setHighLightColor(Color.BLUE);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setLabel("y");
        return  set;
    }
    private LineDataSet createSet2(){
        LineDataSet set = new LineDataSet(null,"Z Axis");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(Color.RED);
        set.setHighLightColor(Color.RED);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setLabel("z");
        return  set;
    }
    @Override
    public void onPause() {
        super.onPause();
        if (thread != null){
            thread.interrupt();
        }
        sensors.stop();
    }
    @Override
    public void onResume() {
        super.onResume();
        sensors.start();
    }
    @Override
    public void onDestroy() {
        sensors.stop();
        thread.interrupt();
        super.onDestroy();
    }

    public void change(String to_plot){
        if(this.to_plot != to_plot) {
            this.to_plot = to_plot;
            data.clearValues();
            mChart.clearValues();
        }
    }

    public void linearAcc(View view){
        change("lac");
    }
    public void ownLinearAcc(View view){
        change("acc");
    }
    public void rawAcc(View view){
        change("rac");
    }
    public void gyro(View view){
        change("rotv");
    }
    public void magno(View view){
        change("rot");
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (plotData) {
                if (to_plot == "rac") {
                    addEntry(sensors.getRawAccelerationPhone().values);
                } else if (to_plot == "acc") {
                    addEntry(sensors.getLinearAccelerationPhone().values);
                } else if (to_plot == "rotv") {
                    addEntry(Utils.quaternion2euler(sensors.getRotationVectorVehicle().values));
                } else if (to_plot == "rot") {
                    addEntry(Utils.quaternion2euler(sensors.getRotationVectorEarth().values));
                } else if (to_plot == "lac") {
//                    addEntry(sensors.getGravityPhone().values);
                    addEntry(sensors.getLinearAccelerationPhoneAndroid().values);
//                    addEntry(sensors.getMagneticPhone().values);
                }
            }
            plotData = false;
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }
}
