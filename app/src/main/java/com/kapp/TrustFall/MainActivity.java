package com.kapp.TrustFall;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.app.NotificationCompat.Builder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kapp.TrustFall.client.ClientActivity;
import com.kapp.TrustFall.util.BluetoothUtils;
import com.kapp.TrustFall.util.StringUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import static com.kapp.TrustFall.Constants.SAVED_ON_OR_OFF_LOCATION;
import static com.kapp.TrustFall.Constants.SAVED_SEEKBAR_VALUE_LOCATION;
import static com.kapp.TrustFall.Constants.SCAN_PERIOD;
import static com.kapp.TrustFall.Constants.SERVICE_UUID;

import static com.kapp.TrustFall.Constants.SAVED_NUMBER_LOCATION_1;
import static com.kapp.TrustFall.Constants.SAVED_NUMBER_LOCATION_2;
import static com.kapp.TrustFall.Constants.SAVED_NUMBER_LOCATION_3;
import static com.kapp.TrustFall.Constants.SAVED_NUMBER_LOCATION_4;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN";

    private FusedLocationProviderClient mFusedLocation;
    private SharedPreferences prefs;
    private Ringtone defaultRingtone;

    //SharedPreferences Strings
    private String SavedContactNumber1 = SAVED_NUMBER_LOCATION_1;
    private String SavedContactNumber2 = SAVED_NUMBER_LOCATION_2;
    private String SavedContactNumber3 = SAVED_NUMBER_LOCATION_3;
    private String SavedContactNumber4 = SAVED_NUMBER_LOCATION_4;
    private String SavedSeekBarValue = SAVED_SEEKBAR_VALUE_LOCATION;
    private String SavedOnOrOFf = SAVED_ON_OR_OFF_LOCATION;

    //Phone number buffers
    private String SavedPhoneNumber1;
    private String SavedPhoneNumber2;
    private String SavedPhoneNumber3;
    private String SavedPhoneNumber4;

    //Default Settings variables
    private Boolean DefaultSavedOnOrOff = false;
    private String DefaultCountdownEventTime = "30";

    //Settings variables
    private int CountdownEventTime;
    private Boolean OnOrOffState;

    //Event Strings
    private String longitude = "0";
    private String latitude = "0";
    private String FallTime = "0";


    Vibrator smsVib;

    CountDownTimer Timer;
    Timer timer;

    private TextView TimerView;
    public Button ConnectButton;
    private Button CancelEventButton;
    //New BLE Stuff

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    //private ActivityMainBinding mBinding;

    private boolean mScanning;
    private Handler mHandler;
    //private Handler mLogHandler;
    private Map<String, BluetoothDevice> mScanResults;

    private boolean mConnected = false;
    private boolean mEchoInitialized;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGatt mGatt;
    private String ExpectedDeviceName = "HMSoft";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        prefs = this.getSharedPreferences("com.kapp.app", Context.MODE_PRIVATE);

        CountdownEventTime = Integer.parseInt(prefs.getString(SavedSeekBarValue, DefaultCountdownEventTime));

        OnOrOffState = prefs.getBoolean(SavedOnOrOFf, DefaultSavedOnOrOff);

        TimerView = (TextView) findViewById(R.id.TimerView);
        ConnectButton = (Button) findViewById(R.id.ConnectButton);
        CancelEventButton = (Button) findViewById(R.id.CancelButton);

        ConnectionButtonCheck();

        CancelEventButton.setEnabled(false);
        CancelEventButton.setClickable(false);

        //New BLE Stuff

        //mLogHandler = new Handler(Looper.getMainLooper());

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                StartEventCountdown();
                // Your worker tells you in the message what to do.
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        CountdownEventTime = Integer.parseInt(prefs.getString(SavedSeekBarValue, DefaultCountdownEventTime));

        //OnOrOffState = prefs.getBoolean(SavedOnOrOFf, DefaultSavedOnOrOff);

        ConnectionButtonCheck();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }

        Log.d(TAG, "onResumeTest");

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Get a newer device
            logError("No LE Support.");
            finish();
        }
        //New BLE Stuff

    }

    protected void StartEventCountdown() {
        Log.d(TAG, "StartEventCountdown: Begin");

        if (OnOrOffState) {
            OnOrOffState = false;
            try
            {
                GetDate();
                GetPhoneNumbers();
                CreateNotification();
                StartVibrate(CountdownEventTime + 1);
                StartCountdownTimer(CountdownEventTime + 1);

                CancelEventButton.setEnabled(true);
                CancelEventButton.setClickable(true);
            }
            catch (Exception e) {
                Log.d(TAG, "An Unexpected Error Occurred",e);
                Utils.ShowToast(getApplicationContext(),"An Unexpected Error Occurred");
            }
        }

        Log.d(TAG, "StartEventCountdown: End");
    }

    public void StartEvent()
    {
        StartAlarm();
        SendGpsSms();
    }

    public void SendSimpleSMS(String PhoneNumber, String LocationText) throws InterruptedException {
        Log.d(TAG, "SendSimpleSms: Begin");
        if (PhoneNumber != null) {
            if (PhoneNumber.length() == 10) {
                android.telephony.SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage("+1 " + PhoneNumber, null, LocationText, null, null);
                Log.d(TAG, "SendSimpleSms: Text sent to " + PhoneNumber);
                Utils.ShowToast(getApplicationContext(), "Text sent to " + PhoneNumber);
            } else {
                Log.d(TAG, "No Message Sent to:" + PhoneNumber);
            }

        } else {
            Log.d(TAG, "SendSimpleSms: No Message Sent");
        }

        Log.d(TAG, "SendSimpleSms: End");
    }

    public void SendGpsSms() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mFusedLocation.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = Double.toString(location.getLatitude());
                                longitude = Double.toString(location.getLatitude());
                                //ShowToast("Fix: " + latitude + "," + longitude);
                            } else {
                                latitude = "Zonk";
                                longitude = "Zonk";
                            }
                        }
                    });
        }
        else
        {
            mFusedLocation.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {

                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                latitude = Double.toString(location.getLatitude());
                                longitude = Double.toString(location.getLongitude());
                                if (latitude != "0" && longitude != "0") {
                                    try {
                                        String FallText = "A fall has been detected at https://maps.google.com/?q=+" + latitude + "," + longitude + " . The fall occurred on " + FallTime;
                                        SendSimpleSMS(SavedPhoneNumber1, FallText);
                                        SendSimpleSMS(SavedPhoneNumber2, FallText);
                                        SendSimpleSMS(SavedPhoneNumber3, FallText);
                                        SendSimpleSMS(SavedPhoneNumber4, FallText);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                latitude = "Zonk";
                                longitude = "Zonk";
                            }
                        }
                    });
        }
    }

    public void GetDate() {
        Date CurrentTime = Calendar.getInstance().getTime();
        FallTime = CurrentTime.toString();
    }

    private void GetPhoneNumbers() {
        SavedPhoneNumber1 = prefs.getString(SavedContactNumber1, "-");
        SavedPhoneNumber2 = prefs.getString(SavedContactNumber2, "-");
        SavedPhoneNumber3 = prefs.getString(SavedContactNumber3, "-");
        SavedPhoneNumber4 = prefs.getString(SavedContactNumber4, "-");
    }

    public void CreateNotification() {
        Log.d(TAG, "CreateNotification: Begin");

        NotificationCompat.Builder mBuilder =
                (Builder) new Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Fall Detection Device")
                        .setContentText("Fall Detected");

        Intent resultIntent = new Intent(this, MainActivity.class);
// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        Log.d(TAG, "CreateNotification: End");
    }

    public void StartAlarm() {
        Log.d(TAG, "StartAlarm: Begin");
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        defaultRingtone = RingtoneManager.getRingtone(getApplicationContext(), Settings.System.DEFAULT_RINGTONE_URI);

        defaultRingtone.play();
/*        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();*/
        Log.d(TAG, "StartAlarm: End");
    }

    public void EndAlarm() {
        Log.d(TAG, "EndAlarm: Begin");
        if (defaultRingtone != null) {
            if (defaultRingtone.isPlaying()) {
                Log.d(TAG, "EndAlarm: Stopping Alarm");
                defaultRingtone.stop();

            }
        }
/*        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();*/
        Log.d(TAG, "EndAlarm: End");
    }


    public void StartVibrate(int VibrateDuration) {
        Log.d(TAG, "StartVibrate: Vibrating for " + VibrateDuration + " Seconds.");
        smsVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        smsVib.vibrate(new long[]{0, 500, 110, 500, 110, 450, 110, 200, 110, 170, 40, 450, 110, 200, 110, 170, 40, 500}, 10);
    }

    public void EndVibrate() {
        Log.d(TAG, "EndVibrate: Begin");
        if (smsVib != null) {
            Log.d(TAG, "EndVibrate: Vibrate Ended");
            smsVib.cancel();
            smsVib = null;
        }
        Log.d(TAG, "EndVibrate: End");
    }

    public void StartCountdownTimer(int TimerTime) {
        Log.d(TAG, "StartCountdownTimer: Begin");

        Timer = new CountDownTimer(TimerTime * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                TimerView.setText("Event Timer: " + millisUntilFinished / 1000);
            }

            public void onFinish() {

                Utils.ShowToast(getApplicationContext(),"Alarm Raised");
                StartEvent();
                TimerView.setText("");
            }
        }.start();

        Log.d(TAG, "StartCountdownTimer: End");
    }

    public void EndCountdownTimer() {
        Log.d(TAG, "EndCountdownTimer: Begin");
        if (Timer != null) {
            Timer.cancel();
            timer = null;
            Utils.ShowToast(getApplicationContext(),"Event Cancelled");
            TimerView.setText("");

        }
        Log.d(TAG, "EndCountdownTimer: End");
    }

    public void CancelEvent(View view) {
        Log.d(TAG, "CancelEvent: Begin");
        //StopTimerTask();
        try {
            EndCountdownTimer();
            EndVibrate();
            EndAlarm();
            Utils.ShowToast(getApplicationContext(),"Event Cancelled");

            OnOrOffState = true;
            CancelEventButton.setEnabled(false);
            CancelEventButton.setClickable(false);
        } catch (Exception e) {
            Utils.ShowToast(getApplicationContext(),"Error During Canceling");
        }
        Log.d(TAG, "CancelEvent: End");
    }

    public void ConnectionButtonCheck()
    {
        if (mConnected) {
            ConnectButton.setEnabled(false);
            ConnectButton.setClickable(false);
            Utils.ShowToast(getApplicationContext(),ExpectedDeviceName + " connected");
        } else {
            ConnectButton.setEnabled(true);
            ConnectButton.setClickable(true);
            Utils.ShowToast(getApplicationContext(),ExpectedDeviceName + " not connected");
        }
    }
    public void OpenBluetooth(View view) {

        Log.d(TAG, "OpenBluetooth: Begin");
            Intent intent = new Intent(this, ClientActivity.class);
        startActivity(intent);
        Log.d(TAG, "OpenBluetooth: End");
    }

    public void OpenEmergencyContacts(View view) {
        Log.d(TAG, "OpenEmergencyContacts: Begin");
        Intent intent = new Intent(this, EmergencyContacts.class);
        startActivity(intent);
        Log.d(TAG, "OpenEmergencyContacts: End");
    }

    public void OpenAlertSettings(View view) {
        Log.d(TAG, "OpenAlertSettings: Begin");
        Intent intent = new Intent(this, AlertSettings.class);
        startActivity(intent);
        Log.d(TAG, "OpenAlertSettings: End");
    }

    //New BLE Stuff
    public void BLEconnect(View view)
    {
        ConnectButton.setEnabled(false);
        ConnectButton.setClickable(false);

        Utils.ShowToast(this, "Connecting to " + ExpectedDeviceName);
        startScan();
    }

    private void startScan() {
        if (!hasPermissions() || mScanning) {
            return;
        }

        disconnectGattServer();

        //mBinding.serverListContainer.removeAllViews();

        mScanResults = new HashMap<>();
        mScanCallback = new MainActivity.BtleScanCallback(mScanResults);

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Note: Filtering does not work the same (or at all) on most devices. It also is unable to
        // search for a mask or anything less than a full UUID.
        // Unless the full UUID of the server is known, manual filtering may be necessary.
        // For example, when looking for a brand of device that contains a char sequence in the UUID


        //.setServiceUuid(new ParcelUuid(SERVICE_UUID))
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(scanFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        mHandler = new Handler();
        mHandler.postDelayed(this::stopScan, SCAN_PERIOD);

        mScanning = true;
        log("Started scanning.");
    }

    private void stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;
        log("Stopped scanning.");
    }

    private void scanComplete() {
        if (mScanResults.isEmpty()) {
            log("Scan was empty");
            return;
        }

        for (String deviceAddress : mScanResults.keySet()) {
            BluetoothDevice device = mScanResults.get(deviceAddress);

            log("Device name: " + device.getName());

/*            GattServerViewModel viewModel = new GattServerViewModel(device);

            ViewGattServerBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this),
                    R.layout.view_gatt_server,
                    mBinding.serverListContainer,
                    true);
            binding.setViewModel(viewModel);*/
            //binding.connectGattServerButton.setOnClickListener(v -> connectDevice(device));


                connectDevice(device);



        }
    }

    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        log("Requested user enables Bluetooth. Try starting the scan again.");
    }

    private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        log("Requested user enable Location. Try starting the scan again.");
    }

    // Gatt connection

    private void connectDevice(BluetoothDevice device) {
        log("Connecting to " + device.getName() +": " +device.getAddress());
        MainActivity.GattClientCallback gattClientCallback = new MainActivity.GattClientCallback();
        mGatt = device.connectGatt(this, false, gattClientCallback);
    }

    // Messaging

    /*private void sendMessage() {
        if (!mConnected || !mEchoInitialized) {
            return;
        }

        BluetoothGattCharacteristic characteristic = BluetoothUtils.findEchoCharacteristic(mGatt);
        if (characteristic == null) {
            logError("Unable to find echo characteristic.");
            disconnectGattServer();
            return;
        }

        String message = mBinding.messageEditText.getText().toString();
        log("Sending message: " + message);

        byte[] messageBytes = StringUtils.bytesFromString(message);
        if (messageBytes.length == 0) {
            logError("Unable to convert message to bytes");
            return;
        }

        characteristic.setValue(messageBytes);
        boolean success = mGatt.writeCharacteristic(characteristic);
        if (success) {
            log("Wrote: " + StringUtils.byteArrayInHexFormat(messageBytes));
        } else {
            logError("Failed to write data");
        }
    }*/

    // Logging

    private void clearLogs() {
        //mLogHandler.post(() -> mBinding.viewClientLog.logTextView.setText(""));
    }

    public void log(String msg) {
        Log.d(TAG, msg);
        //Utils.ShowToast(this, msg);
/*        mLogHandler.post(() -> {
            mBinding.viewClientLog.logTextView.append(msg + "\n");
            mBinding.viewClientLog.logScrollView.post(() -> mBinding.viewClientLog.logScrollView.fullScroll(View.FOCUS_DOWN));
        });*/
    }

    public void logError(String msg) {
        log("Error: " + msg);
    }

    // Gat Client Actions

    public void setConnected(boolean connected) {
        mConnected = connected;

        new Thread()
        {
            public void run()
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        ConnectionButtonCheck();//Do your UI operations like dialog opening or Toast here
                    }
                });
            }
        }.start();

    }

    public void initializeEcho() {
        mEchoInitialized = true;
    }

    public void disconnectGattServer() {
        log("Closing Gatt connection");
        clearLogs();
        setConnected(false);
        mEchoInitialized = false;
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

    // Callbacks

    private class BtleScanCallback extends ScanCallback {

        private Map<String, BluetoothDevice> mScanResults;

        BtleScanCallback(Map<String, BluetoothDevice> scanResults) {
            mScanResults = scanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            logError("BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            mScanResults.put(deviceAddress, device);
        }
    }

    private class GattClientCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            log("onConnectionStateChange newState: " + newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log("Gatt Connection  status success");


            }
            if (status == BluetoothGatt.GATT_FAILURE) {
                logError("Connection Gatt failure status " + status);
                disconnectGattServer();

                return;
            }
             else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                logError("Connection not GATT sucess status " + status);
                //disconnectGattServer();
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                log("Connected to device " + gatt.getDevice().getAddress());
                setConnected(true);
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                log("Disconnected from device");
                disconnectGattServer();

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                log("Device service discovery unsuccessful, status " + status);
                return;
            }

            List<BluetoothGattCharacteristic> matchingCharacteristics = BluetoothUtils.findCharacteristics(gatt);
            if (matchingCharacteristics.isEmpty()) {
                logError("Unable to find characteristics.");

                return;
            }

            log("Initializing: setting write type and enabling notification");
            for (BluetoothGattCharacteristic characteristic : matchingCharacteristics) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                enableCharacteristicNotification(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log("Characteristic written successfully");
            } else {
                logError("Characteristic write unsuccessful, status: " + status);
                disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log("Characteristic read successfully");
                readCharacteristic(characteristic);
            } else {
                logError("Characteristic read unsuccessful, status: " + status);
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            log("Characteristic changed, " + characteristic.getUuid().toString());
            readCharacteristic(characteristic);


        }

        private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
            if (characteristicWriteSuccess) {
                log("Characteristic notification set successfully for " + characteristic.getUuid().toString());
                if (BluetoothUtils.isEchoCharacteristic(characteristic)) {
                    initializeEcho();
                }
            } else {
                logError("Characteristic notification set failure for " + characteristic.getUuid().toString());
            }
        }

        private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
            byte[] messageBytes = characteristic.getValue();
            log("Read: " + StringUtils.byteArrayInHexFormat(messageBytes));
            String message = StringUtils.stringFromBytes(messageBytes);
            if (message == null) {
                logError("Unable to convert bytes to string");
                return;
            }

            log("Received message: " + message);
            Log.d(TAG,"Triggering Event Countdown");

            new Thread()
            {
                public void run()
                {
                    MainActivity.this.runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            StartEventCountdown();
                        }
                    });
                }
            }.start();
        }
    }
}




