#include <jni.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_dvabk_inpaintingapp_Engine_inverseColor(JNIEnv *env, jclass clazz, jintArray img, jintArray mask) {
    jsize imgLength = env->GetArrayLength(img);
    jint *imgArray = env->GetIntArrayElements(img, NULL);
    jint *maskArray = env->GetIntArrayElements(mask, NULL);

    for (int i = 0; i < imgLength; ++i) {
        if (maskArray[i] != 0) {
            jint alpha = (imgArray[i] >> 24) & 0xFF;
            jint red = (imgArray[i] >> 16) & 0xFF;
            jint green = (imgArray[i] >> 8) & 0xFF;
            jint blue = imgArray[i] & 0xFF;
            red = 255 - red;
            green = 255 - green;
            blue = 255 - blue;
            imgArray[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
        }
    }

    env->ReleaseIntArrayElements(img, imgArray, 0);
}
