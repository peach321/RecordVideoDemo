package com.taozi.recordvideodemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.mabeijianxi.smallvideorecord2.DeviceUtils;
import com.mabeijianxi.smallvideorecord2.JianXiCamera;
import com.mabeijianxi.smallvideorecord2.model.MediaRecorderConfig;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button button;
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSmallVideo();
        // Example of a call to a native method

        button = findViewById(R.id.button);
        button.setText(stringFromJNI());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 录制
                MediaRecorderConfig config = new MediaRecorderConfig.Buidler()

                        .fullScreen(true)
                        //.smallVideoWidth(360)
                        //.smallVideoHeight(480)
                        .recordTimeMax(6000)
                        .recordTimeMin(1500)
                        .maxFrameRate(20)
                        .videoBitrate(600000)
                        .captureThumbnailsTime(1)
                        .build();
                RecorderActivity.goSmallVideoRecorder(MainActivity.this, SendSmallVideoActivity.class.getName(), config);
            }
        });

        /**
         * 第 1 步: 检查是否有相应的权限
         */
        boolean isAllGranted = checkPermissionAllGranted(
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );
        // 如果这3个权限全都拥有, 则直接执行备份代码
        if (isAllGranted) {
            return;
        }

        /**
         * 第 2 步: 请求权限
         */
        // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
        ActivityCompat.requestPermissions(
                this,
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                MY_PERMISSION_REQUEST_CODE
        );
    }

    public static void initSmallVideo() {
        // 设置拍摄视频缓存路径
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                JianXiCamera.setVideoCachePath(dcim + "/mabeijianxi/");
            } else {
                JianXiCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/",
                        "/sdcard-ext/")
                        + "/mabeijianxi/");
            }
        } else {
            JianXiCamera.setVideoCachePath(dcim + "/mabeijianxi/");
            // 初始化拍摄
            JianXiCamera.initialize(false, null);
        }
    }




    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /**
     * 第 3 步: 申请权限结果返回处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码

            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
            }
        }
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
