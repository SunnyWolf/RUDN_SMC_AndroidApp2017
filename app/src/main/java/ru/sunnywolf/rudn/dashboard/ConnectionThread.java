package ru.sunnywolf.rudn.dashboard;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;
import java.util.Vector;

/**
 * Created by whiteraven on 11/11/17.
 */

public class ConnectionThread extends Thread {
    interface ConnectionListener {
        void onPocketRecived(DataPocket pocket);
        void onError(int error);
    }

    public final static int ERROR_CONNECTION = 1;
    public final static int ERROR_RECEIVE = 2;
    public final static int ERROR_SEND = 3;

    private final static int MSG_POCKET = 1;
    private final static int MSG_ERROR = 2;

    private final String TAG = ConnectionThread.class.getSimpleName();
    private final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectionListener listener;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private InputStream streamIn;
    private OutputStream streamOut;

    private class PacketHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_POCKET:
                    DataPocket pocket = (DataPocket)msg.obj;
                    listener.onPocketRecived(pocket);
                    break;
                case MSG_ERROR:
                    listener.onError(ERROR_RECEIVE);
                    break;
            }
        }
    }

    final Messenger messenger = new Messenger(new PacketHandler());

    public ConnectionThread(BluetoothDevice device, ConnectionListener listener) {
        super();

        this.device = device;
        this.listener = listener;

        if (listener == null){
            return;
        }

        try {
            socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            start();
        } catch (IOException e){
            Log.d(TAG, e.getLocalizedMessage());
            listener.onError(ERROR_CONNECTION);
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "run");

        try {
            socket.connect();

            InputStream tempIn = socket.getInputStream();
            OutputStream tempOut = socket.getOutputStream();
            streamIn = tempIn;
            streamOut = tempOut;

        } catch (IOException ex){
            listener.onError(ERROR_CONNECTION);
        }

        DataPocket pocket;
        ByteBuffer temp = ByteBuffer.allocate(DataPocket.POCKET_LENGTH);

        final byte[] buffer = new byte[1024];
        int bytes;

        int state = 0;
        int b_count = 0;

        while(true){
            try{
                bytes = streamIn.read(buffer);

                for (int i = 0; i<bytes; i++) {
                    switch (state){
                        case 0:
                            if (buffer[i] == DataPocket.MAGIC_1){
                                temp.clear();
                                temp.put(buffer[i]);

                                b_count = 1;
                                state = 1;
                            }
                            break;
                        case 1:
                            if (buffer[i] == DataPocket.MAGIC_2){
                                temp.put(buffer[i]);
                                state = 2;
                                b_count++;
                            } else {
                                state = 0;
                            }
                            break;
                        case 2:
                            temp.put(buffer[i]);
                            b_count++;

                            if (b_count == DataPocket.POCKET_LENGTH){
                                state = 0;
                                pocket = new DataPocket(temp.array());
                                if (pocket.is_CRC_valid()){
                                    messenger.send(Message.obtain(null, MSG_POCKET, pocket));
                                } else {
                                    pocket = null;
                                }
                            }
                            break;
                    }
                }

            } catch (IOException e){
                Log.d(TAG, "run: " + e.getLocalizedMessage());
                try{
                    messenger.send(Message.obtain(null, MSG_ERROR, null));
                } catch (RemoteException ex){
                    Log.d(TAG, "run: can't send message");
                }
                break;
            } catch (RemoteException e){
                Log.d(TAG, "run: can't send message");
            }
        }
    }

    public void send(DataPocket pocket){
        try{
            streamOut.write(pocket.getBytes());
        } catch (IOException e){
            Log.d(TAG, "send: " + e.getLocalizedMessage());
            listener.onError(ERROR_SEND);
        }
    }
}
