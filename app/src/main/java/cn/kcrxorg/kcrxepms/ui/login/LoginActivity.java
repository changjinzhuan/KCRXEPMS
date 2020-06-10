package cn.kcrxorg.kcrxepms.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import cn.kcrxorg.kcrxepms.MainActivity;
import cn.kcrxorg.kcrxepms.R;
import cn.kcrxorg.kcrxepms.Util;
import cn.kcrxorg.kcrxepms.mbutil.MyLog;
import cn.kcrxorg.kcrxepms.uhfutil.UHFHelper;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private MyLog mylog;
    //UHF天线
    private UHFHelper uhfHelper;


    private Handler mHandler;//消息中心
    public static final int uhfwhat=1;//标签消息
    private static final int INFO_MES = 0;//其他消息


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        //初始化日志
        mylog=new MyLog(this,10000,1);
        mylog.Write("程序已启动！*****************************");
        //启动声音
        Util.initSoundPool(this);


        //注册按钮
        //注册按键广播
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.rfid.FUN_KEY");
        registerReceiver(keyReceiver,filter);



        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
               // finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(LoginActivity.this,usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(LoginActivity.this,usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://扫描到卡号
                        String tagmessage=msg.getData().getString("tagmessage");
                        if(tagmessage.equals(""))
                        {
                            Util.play(3, 0);
                            showToast("未读取到工作证");
                        }
                        else
                        {
                           mylog.Write("读取到卡号="+tagmessage);
                           if(tagmessage.length()<10)//不是工作证卡
                           {
                               showToast(tagmessage+"不是工作证卡，请检查");
                               Util.play(3, 0);
                               break;
                                                }
                        if(usernameEditText.getText().toString().equals(""))
                        {
                            usernameEditText.setText(tagmessage.substring(0,10));
                            Util.play(2,0);
                        }else if(!usernameEditText.getText().toString().equals(""))
                        {
                            passwordEditText.setText(tagmessage.substring(0,10));
                            Util.play(2,0);
                        }
                }
                    break;
                    case INFO_MES:
                        mylog.Write(msg.getData().getString("message").toString());
                        break;

                }
                super.handleMessage(msg);
            }
        };

        //启动天线
        uhfHelper=new UHFHelper(mHandler,this);
        mylog.Write("初始化完成");
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome);
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent mainintent=new Intent(LoginActivity.this, MainActivity.class);
        if(model.getDisplayName().split(":").length>1)//卡号登录
        {
            mainintent.putExtra("operator",model.getDisplayName().split(":")[0]);
            mainintent.putExtra("auditor",model.getDisplayName().split(":")[1]);
        }else
        {
            mainintent.putExtra("operator","00000000");
            mainintent.putExtra("auditor","00000000");
        }
        startActivity(mainintent);
        finish();

    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uhfHelper.close();
        unregisterReceiver(keyReceiver);//
    }

    //key receiver
    private  long startTime = 0 ;
    private boolean keyUpFalg= true;
    private BroadcastReceiver keyReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            //   if (f1hidden) return;
            int keyCode = intent.getIntExtra("keyCode", 0) ;
            if(keyCode == 0){//H941
                keyCode = intent.getIntExtra("keycode", 0) ;
            }
//            Log.e("key ","keyCode = " + keyCode) ;
            boolean keyDown = intent.getBooleanExtra("keydown", false) ;
//			Log.e("key ", "down = " + keyDown);
            if(keyUpFalg&&keyDown && System.currentTimeMillis() - startTime > 500){
                keyUpFalg = false;
                startTime = System.currentTimeMillis() ;
                if (keyCode == KeyEvent.KEYCODE_F3) {
                    uhfHelper.readCARD(10,10);
                }
                if(keyCode==KeyEvent.KEYCODE_F1)
                {

                }
                if(keyCode==KeyEvent.KEYCODE_F2)
                {

                }
                return ;
            }else if (keyDown){
                startTime = System.currentTimeMillis() ;
            }else {
                keyUpFalg = true;
            }

        }
    } ;
    //show tips
    private Toast toast;
    private void showToast(String info) {
        if (toast==null) toast =  Toast.makeText(this, info, Toast.LENGTH_SHORT);
        else toast.setText(info);
        toast.show();
    }
}
