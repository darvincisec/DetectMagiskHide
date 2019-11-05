package com.darvin.security;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DetectMagisk extends AppCompatActivity {

    private  IIsolatedService serviceBinder;
    private boolean bServiceBound;
    private static final String TAG = "DetectMagisk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_magisk);

        Button btnMagisk = findViewById(R.id.button);
        btnMagisk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.button:
                        if(bServiceBound){
                            boolean bIsMagisk = false;
                            try {
                                bIsMagisk = serviceBinder.isMagiskPresent();
                                if(bIsMagisk)
                                    Toast.makeText(getApplicationContext(),"Magisk Found", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Magisk Not Found", Toast.LENGTH_LONG).show();

                                //getApplicationContext().unbindService(mIsolatedServiceConnection);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), "Isolated Service not bound", Toast.LENGTH_SHORT).show();
                        }

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this,IsolatedService.class);
        /*Binding to an isolated service */
        getApplicationContext().bindService(intent, mIsolatedServiceConnection, BIND_AUTO_CREATE);
    }


    private ServiceConnection mIsolatedServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceBinder = IIsolatedService.Stub.asInterface(iBinder);
            bServiceBound = true;
            Log.d(TAG, "Service bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bServiceBound = false;
            Log.d(TAG, "Service Unbound");
        }
    };

}
