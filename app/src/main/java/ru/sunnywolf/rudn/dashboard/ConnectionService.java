package ru.sunnywolf.rudn.dashboard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by whiteraven on 10/31/17.
 */

public class ConnectionService extends Service {
    private final String TAG = ConnectionService.class.getSimpleName();

    public interface DataListener{
        void turn_light(int state);
        void light(int state);
        void fan(int state);
    }

    private boolean is_running;
    private boolean is_connected;
    private String btDeviceAddress;

    private BluetoothAdapter btAdapter;
    private BluetoothReceiver btReceiver;
    private BluetoothDevice btDevice;

    private final IBinder binder = new LocalBinder();

    private NotificationManager nm;

    private DataListener datListener;

    private ConnectionThread conThread;
    private ConnectionThread.ConnectionListener conListener = new ConnectionThread.ConnectionListener() {
        @Override
        public void onPocketRecived(DataPocket pocket) {
            if ((datListener != null) && (pocket.getType() == DataPocket.TYPE_STATUS)){
                switch (pocket.getElement()){
                    case DataPocket.ELEMENT_TLIGHT:
                        datListener.turn_light(pocket.getData());
                        break;
                    case DataPocket.ELEMENT_LIGHT:
                        datListener.light(pocket.getData());
                        break;
                }
            }
        }

        @Override
        public void onError(int error) {
            Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            setConnectionState(false);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");        
        
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        is_running = false;

        btReceiver = new BluetoothReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unregisterReceiver(btReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (action == null){
            return START_NOT_STICKY;
        }
        Log.d(TAG, "onStartCommand: " + action);

        if (action.equals("CONNECT") && !is_connected){
            btDeviceAddress = intent.getStringExtra("DEVADDR");
            connect();
        }

        if (action.equals("BT_ON")){

        }

        if (action.equals("BT_OFF")){

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void connect() {
        if (btAdapter==null){
            Log.e(TAG, "connect: no bluetooth adapters found");
            stopSelf();
            return;
        }
        if (!btAdapter.isEnabled()){
            Log.d(TAG, "connect: bluetooth adapter disabled");
            return;
        }
        if (is_connected){
            Log.d(TAG, "connect: already connected");
            return;
        }

        btDevice = btAdapter.getRemoteDevice(btDeviceAddress);
        conThread = new ConnectionThread(btDevice, conListener);

        setConnectionState(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    void showNotification(String msg) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_menu_connect)
                .setContentIntent(pIntent);
        startForeground(1337, builder.build());
    }
    void hideNotification(){
        stopForeground(true);
    }

    class LocalBinder extends Binder{
        ConnectionService getService(){
            return ConnectionService.this;
        }
    }

    public void setDataListener(DataListener listener){
        this.datListener = listener;
    }

    private void setConnectionState(boolean state){
        this.is_connected = state;
        if (state){
            String temp = "Connected to: " + btDevice.getName();
            showNotification(temp);
        } else {
            hideNotification();
        }
    }
}
