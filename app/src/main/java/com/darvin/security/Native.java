package com.darvin.security;

class Native {

    static {
        System.loadLibrary("native-lib");
    }

    static native boolean isMagiskPresentNative();

}