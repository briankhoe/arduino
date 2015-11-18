package com.example.arduinoblinker;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;
import android.widget.TextView;

import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbAccessory;


@SuppressLint("NewApi")
public class Lightblinker extends Activity implements SensorEventListener {

    //accelerometer code
    private float lastX, lastY, lastZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float deltaXMax = 0;
    private float deltaX = 0;
    private float vibrateThreshold = 0;
    private TextView currentX, maxX;
<<<<<<< HEAD
    private int x;
=======
>>>>>>> FETCH_HEAD

    public Vibrator v;

    // TAG is used to debug in Android logcat console
    private static final String TAG = "ArduinoAccessory";
    private static final String ACTION_USB_PERMISSION = "com.example.arduinoblinker.action.USB_PERMISSION";
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;
    private ToggleButton buttonLED;
    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory "
                                + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                {
                    if (accessory != null && accessory.equals(mAccessory)) {
                        closeAccessory();
                    }
                }
            }
        }
    };


    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();


        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        if (getLastNonConfigurationInstance() != null) {
            mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
            openAccessory(mAccessory);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fai! we dont have an accelerometer!
        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        buttonLED = (ToggleButton) findViewById(R.id.toggleButton);

    }


<<<<<<< HEAD
=======

>>>>>>> FETCH_HEAD
    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        maxX = (TextView) findViewById(R.id.maxX);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);

        // if the change is below 2, it is just plain noise
        if (deltaX < 2)
            deltaX = 0;
    }


    public void displayCleanValues() {
        currentX.setText("0.0");
    }

    // display the current x accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(Float.toString(deltaXMax));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object onRetainNonConfigurationInstance() {
        if (mAccessory != null) {
            return mAccessory;
        } else {
            return super.onRetainNonConfigurationInstance();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mInputStream != null && mOutputStream != null) {
            return;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onPause() {
        super.onPause();
        closeAccessory();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    private void openAccessory(UsbAccessory mAccessory2) {
        mFileDescriptor = mUsbManager.openAccessory(mAccessory2);
        if (mFileDescriptor != null) {
            mAccessory = mAccessory2;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Log.d(TAG, "accessory opened");
        } else {
            Log.d(TAG, "accessory open fail");
        }
    }


    private void closeAccessory() {
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    public void blinkLED(View v) {
        byte[] buffer = new byte[1];
<<<<<<< HEAD
        if (buttonLED.isChecked()) {
            buffer[0] = (byte) 0; // button says on, light is off

            while (mInputStream != null) {
                try {

                    int temp = mInputStream.read();

                    if (temp > x) {
                        x = temp;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            buffer[0] = (byte) 1; // button says off, light is on

        }
=======
        if(buttonLED.isChecked())
            buffer[0]=(byte)0; // button says on, light is off

        while (mInputStream != null) {
            try {

                if (mInputStream.read() == currentX); {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return 'a';


        else
            buffer[0]=(byte)1; // button says off, light is on
>>>>>>> FETCH_HEAD

        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
<<<<<<< HEAD

    }
}
=======
    }
>>>>>>> FETCH_HEAD


