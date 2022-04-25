package androidx.fake.stub;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.InMemoryDexClassLoader;

public class e {
    private static String nativePath;
    private static String libcPath;
    private static Context context;
    private static int IS_ROOTED = -1;


    public native static void decodeSo(byte[] buffer,int bufferLen, String path,int pathLen);

    public static boolean checkInit(Context ctx){
        context = ctx;
        nativePath = context.getApplicationInfo().dataDir + "/libnative.so";
        libcPath = context.getApplicationInfo().dataDir + "/libc++_shared.so";

        if( !Build.CPU_ABI.equals("armeabi-v7a") || Build.VERSION.SDK_INT > 30 || Build.VERSION.SDK_INT < 29 || isRooted() == 1) {
            Log.e("Error", "Something wrong!");
            return false;
        }

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static ClassLoader init(){
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            System.loadLibrary("stub");
            inputStream = context.getAssets().open("res.dat");

            byte[] nativeBuffer = new byte[inputStream.available()];
            inputStream.read(nativeBuffer);
            outputStream = new BufferedOutputStream(new FileOutputStream(nativePath));
            outputStream.write(nativeBuffer,0,nativeBuffer.length);
            inputStream.close();
            outputStream.close();
            decodeSo(nativeBuffer,nativeBuffer.length,nativePath,nativePath.length());

            inputStream = context.getAssets().open("libc++_shared.so");
            byte[] libcBuffer = new byte[inputStream.available()];
            inputStream.read(libcBuffer);
            outputStream = new BufferedOutputStream(new FileOutputStream(libcPath));
            outputStream.write(libcBuffer,0,libcBuffer.length);
            inputStream.close();
            outputStream.close();

            inputStream = context.getAssets().open("build.json");
            byte[] dexByte = new byte[inputStream.available()];
            inputStream.read(dexByte);
            for(int i =0; i< dexByte.length;i++){
                dexByte[i] = (byte) (dexByte[i] ^ 0x31);
            }
            inputStream.close();
            ByteBuffer[] byteBuffers = new ByteBuffer[]{ByteBuffer.wrap(dexByte)};
            InMemoryDexClassLoader inMemoryDexClassLoader = new InMemoryDexClassLoader(byteBuffers,context.getApplicationInfo().dataDir,context.getClassLoader());
            replaceClassLoader(context,inMemoryDexClassLoader);
            return inMemoryDexClassLoader;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void fs(){
        File file = new File(context.getApplicationInfo().dataDir + "/shm");
        if( file.exists() ){
            file.delete();
        }
    }
    public static void replaceClassLoader(Context context, ClassLoader classLoader) {
        Class<?> ActivityThreadClass = null;
        try {
            ActivityThreadClass = classLoader.loadClass("android.app.ActivityThread");
            Method currentThread = ActivityThreadClass.getDeclaredMethod("currentActivityThread");
            Object activityThread = currentThread.invoke(null);
            Field mPackages = ActivityThreadClass.getDeclaredField("mPackages");
            mPackages.setAccessible(true);
            ArrayMap mPackagesMap = (ArrayMap) mPackages.get(activityThread);
            WeakReference weakReference = (WeakReference) mPackagesMap.get(context.getPackageName());
            Object loadedApk = weakReference.get();
            Class<?> loadedApkClass = classLoader.loadClass("android.app.LoadedApk");
            Field mClassLoader = loadedApkClass.getDeclaredField("mClassLoader");
            mClassLoader.setAccessible(true);
            mClassLoader.set(loadedApk,classLoader);

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static Context GetContext (){
        return context;
    }

    private static boolean checkRootMethod1() {
        String[] v0 = new String[]{"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su", "/system/usr/we-need-root/su", "/system/bin/.ext/su"};
        int v3;
        for(v3 = 0; v3 < v0.length; ++v3) {
            String v4 = v0[v3];
            if(new File(v4).exists()) {
                Log.i("VS-RootUtils", "rooted file " + v4);
                return true;
            }
        }

        return false;
    }


    private static boolean checkRootMethod2() {
        boolean v0 = false;
        Process v1 = null;
        try {
            v1 = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            String v2 = new BufferedReader(new InputStreamReader(v1.getInputStream())).readLine();
            if(v2 != null) {
                v0 = true;
            }
            v1.destroy();
        }
        catch(Throwable unused_ex) {
            if(v1 != null) {
                v1.destroy();
            }
            return false;
        }
        return v0;
    }

    public static boolean isDeviceRooted() {
        return (checkRootMethod1()) || (checkRootMethod2());
    }

    public static int isRooted() {
        int v0 = IS_ROOTED;

        if(v0 != -1) {
            return v0;
        }

        IS_ROOTED = isDeviceRooted()? 1 : 0;
        return IS_ROOTED;
    }

}
