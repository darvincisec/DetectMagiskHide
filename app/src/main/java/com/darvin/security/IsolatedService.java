package com.darvin.security;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.system.Os;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class IsolatedService extends Service{

    private static final String[] blackListedMountPaths = { "magisk", "core/mirror", "core/img"};
    private static final String TAG = "DetectMagisk-Isolated";
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IIsolatedService.Stub mBinder = new IIsolatedService.Stub(){
        public boolean isMagiskPresent(){

            Log.d(TAG, "Isolated UID:"+ Os.getuid());

            boolean isMagiskPresent = false;

            File file = new File("/proc/self/mounts");

            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String str;
                int count =0;
                while((str = reader.readLine()) != null && (count==0)){
                    //Log.d(TAG, "MountPath:"+ str);
                    for(String path:blackListedMountPaths){
                        if(str.contains(path)){
                            Log.d(TAG, "Blacklisted Path found "+ path);
                            count++;
                            break;
                        }
                    }
                }
                reader.close();
                fis.close();
                Log.d(TAG, "Count of detected paths "+ count);
                if(count > 0){
                    Log.d(TAG, "Found magisk in atleast 1 mount path ");
                    isMagiskPresent = true;
                }else {
                    /*Incase the java calls are hooked, there is 1 more level
                    of check in the native to detect if the same blacklisted paths are
                    found in the proc maps along with checks for su files when accessed
                    from native.Native functions can also be hooked.But requires some effort
                    if it is properly obfuscated and syscalls are used in place of libc calls
                     */
                    isMagiskPresent = Native.isMagiskPresentNative();
                    Log.d(TAG, "Found Magisk in Native " + isMagiskPresent);
                }



            } catch (IOException e) {
                e.printStackTrace();
            }
            return isMagiskPresent;
        }
    };
}
