package com.example.administrator.smsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.thinkive.framework.util.Log;
import com.mob.tools.utils.ResHelper;
import com.mob.tools.utils.UIHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * @author: sq
 * @date: 2017/9/12
 * @corporation: 深圳市思迪信息技术股份有限公司
 * @description: 用户输入验证码，验证界面
 */
public class IdentifyNumActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private TextView mPhoneNumTv;
    private EditText mIdentifyNumEt;
    private Button mIdentifyNextBtn;
    private EventHandler mEventHandler;
    private String phone;
    private String code;
    private TextView mCountTv;
    private int time = 60;
    private Button mGetCodeBtn;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            switch (event) {
                case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE :
                    afterSubmit(result, data);
                    break;
                case SMSSDK.EVENT_GET_VERIFICATION_CODE :
                    afterGet(result, data);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_num);
        initView();
        initEvent();
        initObject();
        countDown();
    }

    private void countDown() {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (time == 0){
                    mGetCodeBtn.setVisibility(View.VISIBLE);
                    time = 60;
                    String tips = "重新获取短信验证码（" + time + "）S";
                    mCountTv.setText(tips);
                }else {
                    time--;
                    String tips = "重新获取短信验证码（" + time + "）S";
                    mCountTv.setText(tips);
                    runOnUIThread(this, 1000L);
                }
            }
        },1000L);

    }



    public void runOnUIThread(final Runnable r, long delayMillis) {
        UIHandler.sendEmptyMessageDelayed(0, delayMillis, new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                r.run();
                return false;
            }
        });
    }

    private void initView() {
        mPhoneNumTv = ((TextView) findViewById(R.id.tv_phone_number));
        mIdentifyNumEt = ((EditText) findViewById(R.id.et_identify_num));
        mIdentifyNextBtn = ((Button) findViewById(R.id.btn_identify_next));
        mCountTv = ((TextView) findViewById(R.id.tv_count));
        mGetCodeBtn = ((Button) findViewById(R.id.btn_get_code_again));
    }

    private void initEvent() {

        mIdentifyNextBtn.setOnClickListener(this);
        mGetCodeBtn.setOnClickListener(this);
        mIdentifyNumEt.addTextChangedListener(this);
    }

    private void initObject() {
        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        code = intent.getStringExtra("code");
        String tips = "重新获取短信验证码（" + time + "）S";
        String formatedPhone = intent.getStringExtra("formatedPhone");
        mPhoneNumTv.setText(formatedPhone);
        mCountTv.setText(tips);
        mEventHandler = new EventHandler(){
            @Override
            public void afterEvent(final int event, final int result, final Object data) {

                Message message = new Message();
                message.arg1 = event;
                message.arg2 = result;
                message.obj = data;
                mHandler.sendMessage(message);

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE){
//                            afterSubmit(result, data);
//                        }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
//                            afterGet(result, data);
//                        }
//                    }
//                });

            }
        };
    }

    private void afterGet(final int result, Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    int throwable = ResHelper.getStringRes(IdentifyNumActivity.this, "smssdk_virificaition_code_sent");
                    if(throwable > 0) {
                        // 提示 ：验证码已发送
                        Toast.makeText(IdentifyNumActivity.this, throwable, Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else{

                }
            }
        });
    }

    private void afterSubmit(int result, Object data) {
        if (result == SMSSDK.RESULT_COMPLETE) {
            HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
            String country = (String) phoneMap.get("country");
            String phone = (String) phoneMap.get("phone");
            Toast.makeText(this,"验证成功",Toast.LENGTH_LONG).show();
            Log.e(country+"======="+phone);
            // 提交用户信息（此方法可以不调用）
//            registerUser(country, phone);
        }else{
            ((Throwable)data).printStackTrace();
            String message1 = ((Throwable)data).getMessage();
            int resId = 0;

            try {
                JSONObject e = new JSONObject(message1);
                int status = e.getInt("status");
                //提示：<--验证码验证失败原因-->
                resId = ResHelper.getStringRes(IdentifyNumActivity.this, "smssdk_error_detail_" + status);
            } catch (JSONException var5) {
                var5.printStackTrace();
            }

            if(resId == 0) {
                //提示：验证码不正确，请重新输入
                resId = ResHelper.getStringRes(IdentifyNumActivity.this, "smssdk_virificaition_code_wrong");
            }

            if(resId > 0) {
                Toast.makeText(IdentifyNumActivity.this, resId, Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SMSSDK.registerEventHandler(mEventHandler);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_identify_next :
                String identifyNum = mIdentifyNumEt.getText().toString().trim();
                // 提交短信验证码，在监听中返回
                SMSSDK.submitVerificationCode(code,phone,identifyNum);
                break;
            case R.id.btn_get_code_again :
                // 请求获取短信验证码，在监听中返回
                SMSSDK.getVerificationCode(code,phone.trim());
                break;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(mEventHandler);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (s.length() > 0) {
            mIdentifyNextBtn.setEnabled(true);
            //TODO 背景样式调整

        }else{
            mIdentifyNextBtn.setEnabled(false);
            //TODO 背景样式调整

        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
