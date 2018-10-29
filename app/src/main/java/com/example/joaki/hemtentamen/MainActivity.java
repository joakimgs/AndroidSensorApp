package com.example.joaki.hemtentamen;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A class that utilizes sensors.
 * It counts how many steps you take, how far you are above sealevel (altitude) and it even shows which direction
 * north is!
 */
public class MainActivity extends Activity implements SensorEventListener {

    private boolean on = false;
    private boolean on1 = false;
    private TextView count, tvSealevel, tvReference, tvRelativeHeight, tvRel;
    private EditText etSealevel;
    private Button btnReference;
    boolean activityRunning;
    private ImageView image;
    private SensorManager mSensorManager;
    private SensorManager sensorManager;
    private SensorManager sSensorManager;
    private TextView tvDirection;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Sensor mCountSensor;
    private Sensor sSensor;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private float altitude = 0;
    private float altitude2 = 0;
    private float altitude3 = 0;
    private float firstAltitude = 0;
    private float secondAltitude = 0;


    /**
     * Calls method to initialize the components of the application, also registers listeners (buttons).
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        registerListeners();
    }

    /**
     * Necessary components for the application to run, uses sensors to gather data from the physical hardware.
     * Various GUI-components as well.
     */
    public void initComponents(){
        count = (TextView) findViewById(R.id.count);
        image = (ImageView) findViewById(R.id.imageViewCompass);
        tvReference = (TextView) findViewById(R.id.tvReference);
        btnReference = (Button) findViewById(R.id.btnReference);
        tvSealevel = (TextView) findViewById(R.id.tvSealevel);
        tvDirection = (TextView) findViewById(R.id.tvDirection);
        tvRelativeHeight = (TextView) findViewById(R.id.tvRelativeHeight);
        tvRel = (TextView) findViewById(R.id.tvRel);

        tvRel.setMovementMethod(new ScrollingMovementMethod());

        //different managers for each sensor, one for stepcounter, one for sealevel, one for compass
        sSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sSensor = sSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }

    /**
     * Register sensors. Uses SensorManager to define which "mode" the sensor shall utilize.
     */
    @Override
    protected void onResume() {
        super.onResume();
        SensorEvent sensor;
        activityRunning = true;
        if (mCountSensor != null) {
            sensorManager.registerListener(this, mCountSensor, SensorManager.SENSOR_DELAY_UI);
        }

        if (sSensor != null) {
            sSensorManager.registerListener(this, sSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        //define the correct sensors, also use "this" so it uses the correct data
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
   }

    /**
     * Method to calculate the altitude, it is equal to Android Documentation "getAltitude".
     * @param p0
     * @param p
     * @return
     */

    public static float altitudeEquation(float p0, float p) {
        final float coef = 1.0f / 5.255f;
        return 44330.0f * (1.0f - (float) Math.pow(p / p0, coef));
    }

    /**
     * Main part of the program. If-statements to manipulate correct data from correct sensor. These are critical.
     * Else the stepcounter for example might use the altitude instead of the STEP_COUNTER.
     */

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);

        if (event.sensor == sSensor) {

            //pressure
            float pressure = event.values[0];
            float pressure2 = event.values[0];
            float pressure3 = event.values[0];
            float finalPressure = 0;

            //variable to track the altitude of where we are
            altitude = altitudeEquation(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);
            altitude2 = altitudeEquation(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure2);
            altitude3 = altitude - altitude2;
            tvSealevel.setText("Meter över havet = " + altitude);
        }

        // stepcounter
        if (event.sensor == mCountSensor) {
            count.setText(String.valueOf(event.values[0]));
        }

        // the compass, rotates the image in the northern direction.
        if (event.sensor == mAccelerometer)

        {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer)

        {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet) {

            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(250);
            ra.setFillAfter(true);

            image.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
            tvDirection.setText("Direction: " + Float.toString(azimuthInDegress) + " degrees ");
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void registerListeners() {
        btnReference.setOnClickListener(new BL());
    }

    /**
     *  Classic class to handle buttons and listeners.
     */
    private class BL implements View.OnClickListener {
        public void onClick(View v) {
            float finalAltitude = 0;
            String showFinalAltitude = "";
            String strAltitude = "";
            tvReference.append(strAltitude + "" + "\n");

            if(on == false) {
                on = true;
                firstAltitude = (altitude);

            }

            else{
                on = false;
                secondAltitude = (altitude);
            }


            finalAltitude = secondAltitude - firstAltitude;
            showFinalAltitude = String.valueOf(finalAltitude);

            tvRel.append(showFinalAltitude + "" + "\n");


        }
    }
}