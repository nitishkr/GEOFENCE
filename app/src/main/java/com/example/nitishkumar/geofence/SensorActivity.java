package com.example.nitishkumar.geofence;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SensorActivity extends Activity implements SensorEventListener {

    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 0.0;
    private String TAG="Sensor", email;
    private String TAG2="interval";
    private int count;
    private float sumx , sumy, sumz, accl;
    private double x;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        if(this.getIntent().getExtras().getInt("kill")==1)
            finish();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensorscreen);
        email =   this.getIntent().getExtras().getString("Email");
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        count = 0;
        sumx = sumy = sumz = (float) 0.0;

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView tvX= (TextView)findViewById(R.id.x_axis);
        TextView tvY= (TextView)findViewById(R.id.y_axis);
        TextView tvZ= (TextView)findViewById(R.id.z_axis);
        TextView tva= (TextView)findViewById(R.id.avg);

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            tvX.setText("0.0");
            tvY.setText("0.0");
            tvZ.setText("0.0");
            tva.setText("0.0");
            mInitialized = true;
            Log.v(TAG2,"0.0");
        } else {
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);
            if (deltaX < NOISE) deltaX = (float)0.0;
            if (deltaY < NOISE) deltaY = (float)0.0;
            if (deltaZ < NOISE) deltaZ = (float)0.0;
            Log.v(TAG, "X -> " + Float.toString(deltaX));
            Log.v(TAG, "Y -> " + Float.toString(deltaY));
            Log.v(TAG, "Z -> " + Float.toString(deltaZ));
            tvX.setText(Float.toString(deltaX));
            tvY.setText(Float.toString(deltaY));
            tvZ.setText(Float.toString(deltaZ));

            sumx +=deltaX;
            sumy+=deltaY;
            sumz +=deltaZ;
            count++;
            mLastX = x;
            mLastY = y;
            mLastZ = z;

            while(count==1500)
            {

                double xa = Math.sqrt(sumx * sumx + sumy * sumy + sumz * sumz) / count;
                tva.setText(Double.toString(xa));
                Log.v(TAG2, Double.toString(xa));

                   if (xa > 2.0)
                   {
                       String to = email;
                       String subject = "Email from phone";
                       String message = "Acceleration exceeded 2 m/s^2";

                       Intent email = new Intent(Intent.ACTION_SEND);
                       email.putExtra(Intent.EXTRA_EMAIL, new String[]{ to});
                       email.putExtra(Intent.EXTRA_SUBJECT, subject);
                       email.putExtra(Intent.EXTRA_TEXT, message);

                       email.setType("message/rfc822");

                       startActivity(Intent.createChooser(email, "Choose an Email client :"));
                   }
                    count = 0;
                    sumx = sumy = sumz = (float) 0.0;
            }

        }
    }



}