package com.darvin.security;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class IsolatedService extends Service{

    static{
        System.loadLibrary("native-lib");
    }
    public native boolean isMagiskPresentNative();
    private String[] blackListedMountPaths = { "/sbin/.magisk/", "/sbin/.core/mirror", "/sbin/.core/img", "/sbin/.core/db-0/magisk.db"};
    private static final String TAG = "DetectMagisk";
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IIsolatedService.Stub mBinder = new IIsolatedService.Stub(){
        public boolean isMagiskPresent(){
            boolean isMagiskPresent = false;
            int pid = android.os.Process.myPid();
            String cwd = String.format("/proc/%d/mounts", pid);
            File file = new File(cwd);
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String str;
                int count =0;
                while((str = reader.readLine()) != null){
                    //Log.d(TAG, "MountPath:"+ str);
                    for(String path:blackListedMountPaths){
                        if(str.contains(path)){
                            Log.d(TAG, "Blacklisted Path found "+ path);
                            count++;
                        }
                    }
                }
                reader.close();
                fis.close();
                Log.d(TAG, "Count of paths "+ count);
                if(count > 1){
                    Log.d(TAG, "Found atleast more than 1 path ");
                    isMagiskPresent = true;
                }else {
                    /*Incase the java calls are hooked, there is 1 more level
                    of check in the native to detect if the same blacklisted paths are
                    found in the proc maps when accessed from native.
                    Native functions can also be hooked.But requires some effort
                    if it is properly obfuscated and syscalls are used in place
                    of libc calls
                     */
                    isMagiskPresent = isMagiskPresentNative();
                }

                Log.d(TAG, "Found Magisk in Mount " + isMagiskPresent);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return isMagiskPresent;
        }
    };
}
