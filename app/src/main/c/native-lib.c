#include <jni.h>
#include <unistd.h>
#include <stdio.h>
#include <malloc.h>
#include <android/log.h>

static const int MAX_PATH = 4;
static const char* TAG = "DetectMagiskNative";
static char* blacklistedPaths[MAX_PATH] = {"/sbin/.magisk/", "/sbin/.core/mirror", "/sbin/.core/img", "/sbin/.core/db-0/magisk.db"};

JNIEXPORT jboolean
Java_com_darvin_security_IsolatedService_isMagiskPresentNative(
        JNIEnv *env,
        jobject this) {
    jboolean bRet = JNI_FALSE;
    int pid = getpid();
    char ch[100];
    memset(ch, '\0', 100 * sizeof(char));

    sprintf(ch,"/proc/%d/mounts", pid);

    FILE* fp = fopen(ch,"r");
    if(fp == NULL)
        goto exit;

    fseek(fp,0L,SEEK_END);
    long size = ftell(fp);
    __android_log_print(ANDROID_LOG_INFO, TAG , "Opening Mount file :%s: size: %ld", ch, size);
    /* For some reason size comes as zero */
    if( size == 0)
        size = 3000;  /*This will differ for different devices */
    char* buffer = malloc(size * sizeof(char));
    if(buffer == NULL)
        goto exit;

    size_t read = fread(buffer, 1, 3000, fp);
    //__android_log_print(ANDROID_LOG_INFO, TAG, "Reading Mount file :%d: %s", read, buffer);
    int count = 0;
    for(int i=0; i < MAX_PATH; i++){
        __android_log_print(ANDROID_LOG_INFO, TAG, "Path  :%s", blacklistedPaths[i]);
        char* rem = strstr(buffer, blacklistedPaths[i]);
        if(rem != NULL) {
            count++;
            __android_log_print(ANDROID_LOG_INFO, TAG, "Found Path");
        }
    }
    if(count > 0)
        bRet = JNI_TRUE;

    exit:

    if(buffer != NULL)
        free(buffer);
    if( fp != NULL)
        fclose(fp);
    return bRet;
}
