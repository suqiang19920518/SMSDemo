package com.example.administrator.smsdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mob.MobSDK;

import java.util.ArrayList;
import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

/**
 * @author: sq
 * @date: 2017/6/11
 * @corporation: 深圳市思迪信息科技有限公司
 * @description:
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String appKey = "2094a749da600";
    private static final String appSecret = "2f109bd27306301256dccc54bc5962d5";

    private Button mStartRegisterBtn;
    private Context mContext;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;

            switch (event) {
                case SMSSDK.EVENT_GET_VERIFICATION_CODE:
                    Toast.makeText(mContext,"获取短信验证码",Toast.LENGTH_LONG).show();
                    break;
                case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE :
                    Toast.makeText(mContext,"提交短信验证码",Toast.LENGTH_LONG).show();
                    break;
                case SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES:
                    Toast.makeText(mContext,"获取短信支持的国家",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    // registerEventHandler用来往SMSSDK中注册一个事件接收器，SMSSDK允许开发者注册任意数量的接收器，
    // 所有接收器都会在事件 被触发时收到消息。
    private EventHandler mEventHandler = new EventHandler(){
        @Override
        public void afterEvent(int event, int result, Object data) {
            Message message = new Message();
            message.arg1 = event;
            message.arg2 = result;
            message.obj = data;
            mHandler.sendMessage(message);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initEvent();
        checkForPermission();
        // 配置AppKey和AppSecret
        MobSDK.init(this, appKey, appSecret);
        // 注册监听器
        SMSSDK.registerEventHandler(mEventHandler);


    }

    private void checkForPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int readPhone = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
            int receiveSms = checkSelfPermission(Manifest.permission.RECEIVE_SMS);
            int readSms = checkSelfPermission(Manifest.permission.READ_SMS);
            int readContacts = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            int readSdcard = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

            int requestCode = 0;
            ArrayList<String> permissions = new ArrayList<String>();
            if (readPhone != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 0;
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (receiveSms != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 1;
                permissions.add(Manifest.permission.RECEIVE_SMS);
            }
            if (readSms != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 2;
                permissions.add(Manifest.permission.READ_SMS);
            }
            if (readContacts != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 3;
                permissions.add(Manifest.permission.READ_CONTACTS);
            }
            if (readSdcard != PackageManager.PERMISSION_GRANTED) {
                requestCode |= 1 << 4;
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (requestCode > 0) {
                String[] permission = new String[permissions.size()];
                this.requestPermissions(permissions.toArray(permission), requestCode);
                return;
            }
        }
    }

    private void initEvent() {
        mStartRegisterBtn.setOnClickListener(this);
    }

    private void initView() {
        mStartRegisterBtn = ((Button) findViewById(R.id.btn_send));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send :

                Intent intent = new Intent(this,PhoneInputActivity.class);
                startActivity(intent);

//                doOpenRegister();//打开注册界面
        }
    }


    private void doOpenRegister() {
        //打开注册页面
        RegisterPage registerPage = new RegisterPage();
        registerPage.setRegisterCallback(new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                super.afterEvent(event, result, data);
                // 解析注册结果
                if (result == SMSSDK.RESULT_COMPLETE) {
//                    @SuppressWarnings("unchecked")
                    HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                    String country = (String) phoneMap.get("country");
                    String phone = (String) phoneMap.get("phone");
                    Toast.makeText(mContext,"country:"+country+",phone:"+phone,Toast.LENGTH_LONG).show();
                    // 提交用户信息（此方法可以不调用）
//                    registerUser(country, phone);
                }
            }
        });
        registerPage.show(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁回调监听接口
        SMSSDK.unregisterEventHandler(mEventHandler);
//        SMSSDK.unregisterAllEventHandler();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }
}
