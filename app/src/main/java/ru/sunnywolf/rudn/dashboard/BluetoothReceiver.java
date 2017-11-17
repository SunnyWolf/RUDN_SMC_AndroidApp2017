package ru.sunnywolf.rudn.dashboard;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by whiteraven on 11/1/17.
 */

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
            int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (mode){
                case BluetoothAdapter.STATE_ON:
                    context.startService(new Intent(context, ConnectionService.class).setAction("BT_ON"));
                    break;
                case BluetoothAdapter.STATE_OFF:
                    context.startService(new Intent(context, ConnectionService.class).setAction("BT_OFF"));
                    break;
            }
        }
    }
}
