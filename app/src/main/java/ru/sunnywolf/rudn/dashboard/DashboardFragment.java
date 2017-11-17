package ru.sunnywolf.rudn.dashboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by whiteraven on 9/4/17.
 */

public class DashboardFragment extends Fragment {
    private BatAccelView mBatAccelView;
    private SeekBar seekBar;
    private TextView mTextView_Speed;
    private ImageView mImageView_TL;
    private ImageView mImageView_TR;
    private ImageView mImageView_Fan;

    private ConnectionService mService;
    private boolean mBound = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Dashboard");

        seekBar = (SeekBar) getActivity().findViewById(R.id.seekBar2);
        mBatAccelView = (BatAccelView) getActivity().findViewById(R.id.view_battery_and_acc);
        mTextView_Speed = (TextView) getActivity().findViewById(R.id.textSpeed);
        mImageView_TL = (ImageView) getActivity().findViewById(R.id.imageTLeft);
        mImageView_TR = (ImageView) getActivity().findViewById(R.id.imageTRight);
        mImageView_Fan = (ImageView) getActivity().findViewById(R.id.imageFan);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBatAccelView.setBatteryValue(progress);
                mBatAccelView.setAccelValue(progress);
                mTextView_Speed.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Intent intent = new Intent(this.getContext(), ConnectionService.class);
        getContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);


    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) service;
            mService = binder.getService();
            mService.setDataListener(mDataListener);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private ConnectionService.DataListener mDataListener = new ConnectionService.DataListener() {
        @Override
        public void turn_light(int state) {
            int colorOn = ContextCompat.getColor(getContext(), R.color.colorAccent);
            int colorOff = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
            switch (state){
                case 0:
                    mImageView_TL.setColorFilter(colorOff);
                    mImageView_TR.setColorFilter(colorOff);
                    break;
                case 1:
                    mImageView_TL.setColorFilter(colorOn);
                    mImageView_TR.setColorFilter(colorOff);
                    break;
                case 2:
                    mImageView_TL.setColorFilter(colorOff);
                    mImageView_TR.setColorFilter(colorOn);
            }
        }

        @Override
        public void light(int state) {

        }

        @Override
        public void fan(int state) {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mService.setDataListener(null);
        getContext().unbindService(mServiceConnection);
    }
}
