package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeProcessing.GyroscopeMeanProcessing;
import com.example.bryantyrrell.vdiapp.GPSMap.VeichleMoving.VeichleInTransit;
import com.example.bryantyrrell.vdiapp.R;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class GyroscopeActivity extends AppCompatActivity implements SensorEventListener,
        View.OnClickListener {

    private SensorManager sensorManager;
    private GyroscopeMeanProcessing MeanProcessing;
    private  Sensor accel;
    private MaxGyroService service;
    private Sensor magnet;
    private Button btnStart, btnStop, btnUpload;
    private LinearLayout layout;
    private GyroProcessing processing;
    private int count;
    private int AngleCount;
    private Sensor gyro;
    private double TotalAngle;
    private boolean started = false;
    private long startTime;
    private static ArrayList<Double> AverageThreshold;
    private static ArrayList<GyroData> sensorData;
    private static ArrayList<GyroscopeObject> GyroLine;
    private ArrayList<GyroData> MeanFilterList;
    private float[] gravity,geomagnetic;
    private View mChart;
    private double CurrAngle;
    private boolean SetUp;
    private VeichleInTransit check;
    private AngleCalcAsync angleCalc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);
        layout = (LinearLayout) findViewById(R.id.ChartContainer);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorData = new ArrayList();
        MeanFilterList = new ArrayList();

        MeanProcessing = new GyroscopeMeanProcessing(MeanFilterList);


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

        check = new VeichleInTransit();
        SetUp=true;
        //}
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {

            if(AngleCount < 5) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    gravity = event.values;
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    geomagnetic = event.values;

                if ( gravity != null && geomagnetic != null) {
                    AngleCount++;
                    angleCalc = new AngleCalcAsync(this, gravity, geomagnetic);
                    angleCalc.execute();
                    return;
                }
            }


            if(SetUp){
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    SetUp = check.checkVeichleMovement(event);
                    System.out.println("The loop is still in set up: "+ SetUp);
                }
            }
            if(!SetUp) {
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    count++;
                    MeanFilterList.add(new GyroData(System.nanoTime(), event.values[0], event.values[1], event.values[2]));
                }

                if (count > 60 && CurrAngle != 0) {
                    processing = new GyroProcessing(this,  MeanFilterList, CurrAngle, MeanProcessing);
                    processing.execute();
                    count = 0;
                }

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
                GyroLine = new ArrayList();

                if(AverageThreshold!=null) {
                    AverageThreshold.clear();
                }
                // save prev data if available
                started = true;
                count = 0;
                AngleCount=0;
                TotalAngle=0;
                SetUp=true;

                service = new MaxGyroService(GyroLine,this);


                gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_FASTEST);

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
            XYSeries ThresholdPositive = new XYSeries("Threshold");
            XYSeries ThresholdNegitive = new XYSeries("Threshold");

            for (GyroData data : sensorData) {
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
            }

            AverageThreshold=service.ReturnAverageThreshold();

            int index=0;
            for (GyroscopeObject data : GyroLine) {
                TrueAccel.add(data.getTimeStamp() - t, data.getTruetrueGyro());
                ThresholdPositive.add(data.getTimeStamp() - t, AverageThreshold.get(index));
                ThresholdNegitive.add(data.getTimeStamp() - t, -AverageThreshold.get(index));
                index++;

            }


            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);
            dataset.addSeries(TrueAccel);
            dataset.addSeries(ThresholdPositive);
            dataset.addSeries(ThresholdNegitive);

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

            XYSeriesRenderer ThresholdRenderer = new XYSeriesRenderer();
            ThresholdRenderer.setColor(Color.BLACK);
            ThresholdRenderer.setPointStyle(PointStyle.CIRCLE);
            ThresholdRenderer.setFillPoints(true);
            ThresholdRenderer.setLineWidth(1);
            ThresholdRenderer.setDisplayChartValues(true);

            XYSeriesRenderer ThresholdRenderer2 = new XYSeriesRenderer();
            ThresholdRenderer2.setColor(Color.BLACK);
            ThresholdRenderer2.setPointStyle(PointStyle.CIRCLE);
            ThresholdRenderer2.setFillPoints(true);
            ThresholdRenderer2.setLineWidth(1);
            ThresholdRenderer2.setDisplayChartValues(true);

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
                multiRenderer.addYTextLabel(i + 1, "test" + i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);
            multiRenderer.addSeriesRenderer(AccelRenderer);
            multiRenderer.addSeriesRenderer(ThresholdRenderer);
            multiRenderer.addSeriesRenderer(ThresholdRenderer2);
            multiRenderer.setYLabelsAlign(Paint.Align.RIGHT);
            multiRenderer.setMargins(new int[]{ 50, 50, 50, 50 });

            // Getting a reference to LinearLayout of the MainActivity Layout

            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);


        }
    }

    public void addDataPoint(GyroData data) {
        sensorData.add(data);

    }
    public void addAccelPoint(GyroscopeObject data) {
        GyroLine.add(data);

        service.CheckSteering();


    }
    public void setCurrAngle(Double angle) {
        TotalAngle=TotalAngle+angle;
        CurrAngle=TotalAngle/AngleCount;

    }


    public void SetDangerousCorneringRight() {
        try {
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.dangerouscorneringrightside);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {

                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    // TODO Auto-generated method stub
                    mp.stop();
                    System.out.println("Media Plyer Is Complete !!!");
                    mp.release();
                    System.out.println("Music is over and Button is enable !!!!!!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetDangerousCorneringLeft() {
        try {
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.dangerouscorneringleftside);
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {

                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    // TODO Auto-generated method stub
                    mp.stop();
                    System.out.println("Media Plyer Is Complete !!!");
                    mp.release();
                    System.out.println("Music is over and Button is enable !!!!!!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


