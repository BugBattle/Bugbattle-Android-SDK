package bugbattle.io.bugbattle.service;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.IOException;

import bugbattle.io.bugbattle.model.FeedbackModel;

/**
 * Detects the shake gesture of the phone
 */
public class ShakeGestureDetector extends BBDetector implements SensorEventListener {
    private static final float SHAKE_THRESHOLD_GRAVITY = 4F;
    private static final int SHAKE_SLOP_TIME_MS = 600;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;
    private long mShakeTimestamp;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    public ShakeGestureDetector(Activity activity) {
        super(activity);
    }

    @Override
    public void initialize() {
        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void resume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        try {
            Runtime.getRuntime().exec("adb shell input keyevent 82");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float gX = sensorEvent.values[0] / SensorManager.GRAVITY_EARTH;
        float gY = sensorEvent.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ = sensorEvent.values[2] / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement.
        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);
        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();
            // ignore shake events too close to each other (500ms)
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return;
            }

            mShakeTimestamp = now;
            try {
                if (!FeedbackModel.getInstance().isDisabled()) {
                    this.takeScreenshot();
                    pause();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
