package com.example.administrator.smsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.tools.utils.ResHelper;

import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.UserInterruptException;
import cn.smssdk.utils.SMSLog;

/**
 * @author: sq
 * @date: 2017/6/12
 * @corporation: 深圳市思迪信息科技有限公司
 * @description:  用户输入手机号码，获取短信验证码界面
 */
public class PhoneInputActivity extends Activity implements View.OnClickListener, TextWatcher {

    private Button mInputNextBtn;
    private EditText mCountryEt;
    private EditText mPhoneNumEt;
    private EventHandler mEventHandler;
    private TextView mCodeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_input);
        initView();
        initEvent();
        initObject();

    }

    private void initObject() {

        mEventHandler = new EventHandler(){
            @Override
            public void afterEvent(final int event, final int result, final Object data) {

                switch (event) {
                    case SMSSDK.EVENT_GET_VERIFICATION_CODE :
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (result == SMSSDK.RESULT_COMPLETE) {
                                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                                        boolean status = ((Boolean) data).booleanValue();
                                        afterVerificationCodeRequested(status);// 发送验证码请求后的操作
                                    }
                                }else {
                                    //TODO 后续完善
                                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE && data != null
                                            && data instanceof UserInterruptException){
                                        return;
                                    }

                                    // 根据服务器返回的网络错误，给toast提示
                                    int status = 0;
                                    try {
                                        ((Throwable) data).printStackTrace();
                                        Throwable throwable = (Throwable) data;
                                        JSONObject object = new JSONObject(throwable.getMessage());
                                        String des = object.optString("detail");// 错误描述
                                        status = object.optInt("status");// 错误代码
                                        if(!TextUtils.isEmpty(des)) {
                                            Toast.makeText(PhoneInputActivity.this, des, Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                    } catch (Exception e) {
                                        SMSLog.getInstance().w(e);
                                    }

                                    int resId;
                                    if(status >= 400) {
                                        //提示：<--验证码发送失败原因-->
                                        resId = ResHelper.getStringRes(PhoneInputActivity.this, "smssdk_error_desc_" + status);
                                    } else {
                                        // 提示：网络异常，请稍后重试
                                        resId = ResHelper.getStringRes(PhoneInputActivity.this, "smssdk_network_error");
                                    }

                                    if(resId > 0) {
                                        Toast.makeText(PhoneInputActivity.this, resId, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                }
            }
        };
    }

    private void afterVerificationCodeRequested(boolean smart) {
        String phone = mPhoneNumEt.getText().toString().trim().replaceAll("\\s*", "");
        String code = mCodeTv.getText().toString().trim();
        if(code.startsWith("+")) {
            code = code.substring(1);
        }

        String formatedPhone = "+" + code + " " + this.splitPhoneNum(phone);
        if(smart) {
            //TODO 智能验证
//            SmartVerifyPage page = new SmartVerifyPage();
//            page.setPhone(phone, code, formatedPhone);
//            page.showForResult(PhoneInputActivity.this, (Intent)null, this);
        } else {

            Intent intent = new Intent(PhoneInputActivity.this, IdentifyNumActivity.class);
            intent.putExtra("phone", phone);
            intent.putExtra("code", code);
            intent.putExtra("formatedPhone", formatedPhone);
            startActivity(intent);
//            IdentifyNumPage page1 = new IdentifyNumPage();
//            page1.setPhone(phone, code, formatedPhone);
//            page1.showForResult(PhoneInputActivity.this, (Intent)null, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SMSSDK.registerEventHandler(mEventHandler);
    }

    private void initEvent() {
        mInputNextBtn.setOnClickListener(this);
        mPhoneNumEt.addTextChangedListener(this);
    }

    private void initView() {
        mInputNextBtn = ((Button) findViewById(R.id.btn_input_next));
        mCountryEt = ((EditText) findViewById(R.id.et_country));
        mPhoneNumEt = ((EditText) findViewById(R.id.et_phone_number));
        mCodeTv = ((TextView) findViewById(R.id.tv_country_code));
        if (mPhoneNumEt.getText().length() > 0) {
            mInputNextBtn.setEnabled(true);
            //TODO 背景样式调整
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_input_next :
                final String phone = mPhoneNumEt.getText().toString().trim().replaceAll("\\s*", "");
                final String code = mCodeTv.getText().toString().trim();
                String phoneNum = code + " " + splitPhoneNum(phone);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("确认手机号码");
                builder.setMessage("平台将发送验证码短信到这个号码："+"\n"+ phoneNum);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 请求获取短信验证码，在监听中返回
                        SMSSDK.getVerificationCode(code,phone.trim());
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setCancelable(false);
                builder.show();
        }
    }

    private String splitPhoneNum(String phone) {
        StringBuilder builder = new StringBuilder(phone);
        builder.reverse();
        int i = 4;

        for(int len = builder.length(); i < len; i += 5) {
            builder.insert(i, ' ');
        }

        builder.reverse();
        return builder.toString();
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

        if (s.length()>0){
            mInputNextBtn.setEnabled(true);
            //TODO 背景样式调整

        } else {
            mInputNextBtn.setEnabled(false);
            //TODO 背景样式调整

        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
