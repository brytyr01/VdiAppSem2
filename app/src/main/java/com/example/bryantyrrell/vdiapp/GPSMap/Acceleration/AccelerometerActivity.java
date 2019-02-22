package com.example.bryantyrrell.vdiapp.GPSMap.Acceleration;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.AngleCalcAsync;
import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.MeanProcessing;
import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.SignalProcessing;
import com.example.bryantyrrell.vdiapp.R;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener,
        View.OnClickListener {

    private SensorManager sensorManager;
    private MeanProcessing MeanProcessing;
    private  Sensor accel;
    private MaxAccelService service;
    private Sensor magnet;
    private Button btnStart, btnStop, btnUpload;
    private LinearLayout layout;
    private SignalProcessing processing;
    private int count = 0;
    private int AngleCount=0;
    private double TotalAngle=0;
    private boolean started = false;
    private long startTime;
    private SignalProcessing signalProcessing;
    private static ArrayList<AccelData> sensorData;
    private static ArrayList<AccelerationObject> accelLine;
    private ArrayList<AccelData> MeanFilterList;
    private float[] gravity,geomagnetic;
    private View mChart;
    private double CurrAngle;
    private AngleCalcAsync angleCalc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        layout = (LinearLayout) findViewById(R.id.ChartContainer);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorData = new ArrayList();
        MeanFilterList = new ArrayList();

        MeanProcessing = new MeanProcessing(MeanFilterList);


        btnStart = (Button) findViewById(R.id.button0);
        btnStop = (Button) findViewById(R.id.button1);
        btnUpload = (Button) findViewById(R.id.button2);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        layout = (LinearLayout) findViewById(R.id.ChartContainer);

        //if (sensorData == null || sensorData.size() == 0) {
        btnUpload.setEnabled(false);
        //}
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
                count++;
                MeanFilterList.add(new AccelData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
            }

            if(count>2&&CurrAngle!=0) {
                processing=new SignalProcessing(this, 0.5, startTime,MeanFilterList,CurrAngle,MeanProcessing);
                processing.execute(event);
                count=0;
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                gravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                geomagnetic = event.values;



            if (AngleCount<5&&gravity != null && geomagnetic != null) {
                 AngleCount++;
                 angleCalc = new AngleCalcAsync(this,gravity,geomagnetic);
                 angleCalc.execute();
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (started == true) {
            sensorManager.unregisterListener(this);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button0:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(false);
                sensorData = new ArrayList();
                accelLine = new ArrayList();
                // save prev data if available
                started = true;
                startTime = System.nanoTime();


                service = new MaxAccelService(accelLine,this);


                accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_NORMAL);

                break;


            case R.id.button1:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnUpload.setEnabled(true);
                started = false;
                sensorManager.unregisterListener(this);
                layout.removeAllViews();


                AngleCount=0;
                TotalAngle=0;
                processing.resetStaticValues();
                count=0;
                for (int index = MeanFilterList.size() - 1; index >= 0; index--) {
                    MeanFilterList.remove(index);
                }
                openChart();

                // show data in chart
                break;
            case R.id.button2:
                // upload data to server (next post)
                break;
            default:
                break;
        }

    }

    private void openChart() {
        if (sensorData != null || sensorData.size() > 0) {
            long t = sensorData.get(0).getTimestamp();
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");
            XYSeries TrueAccel = new XYSeries("TrueAccel");

            for (AccelData data : sensorData) {
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
            }
            for (AccelerationObject data : accelLine) {
                TrueAccel.add(data.getTimeStamp() - t, data.getTrueAccel());
            }

            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);
            dataset.addSeries(TrueAccel);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

            XYSeriesRenderer AccelRenderer = new XYSeriesRenderer();
            AccelRenderer.setColor(Color.YELLOW);
            AccelRenderer.setPointStyle(PointStyle.CIRCLE);
            AccelRenderer.setFillPoints(true);
            AccelRenderer.setLineWidth(1);
            AccelRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)accel");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < sensorData.size(); i++) {

                multiRenderer.addXTextLabel(i + 1, ""
                        + (sensorData.get(i).getTimestamp() - t));
            }
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, "" + i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);
            multiRenderer.addSeriesRenderer(AccelRenderer);

            // Getting a reference to LinearLayout of the MainActivity Layout

            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);

        }
    }

    public void addDataPoint(AccelData data) {
        sensorData.add(data);

    }
    public void addAccelPoint(AccelerationObject data) {
        accelLine.add(data);
         service.CheckAcceleration();

    }
    public void setCurrAngle(Double angle) {
        TotalAngle=TotalAngle+angle;
        CurrAngle=TotalAngle/AngleCount;

    }

    public void SetDangerousAcceleration() {
//        try {
//            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.beep);
//            mp.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}

