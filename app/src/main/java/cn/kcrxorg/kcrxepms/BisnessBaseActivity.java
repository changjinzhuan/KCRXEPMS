package cn.kcrxorg.kcrxepms;

import androidx.appcompat.app.AppCompatActivity;
import cn.kcrxorg.kcrxepms.mbutil.MyLog;
import cn.kcrxorg.kcrxepms.uhfutil.LockHelper;
import cn.kcrxorg.kcrxepms.uhfutil.UHFHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class BisnessBaseActivity extends AppCompatActivity {

    TextView tv_header;
    TextView tv_operinfo;
    TextView tv_footer;
    TextView tv_kuncount;

    LinearLayout line_businfo;
    LinearLayout line_kun;
    LinearLayout line_rsinfo;
    ScrollView scro_businfo;
    ScrollView line_scorllrsinfo;

    Spinner sp_stackInfo;//选择列表
    //语音
    private TextToSpeech textToSpeech;
    public MyLog mylog;

    public Handler mHandler;//消息中心
    public static final int uhfwhat=1;//标签消息
    public static final int INFO_MES = 0;//其他消息
    public static final int LOCK_WHAT=2;//操作锁结果
    public static final int LOCK_LOG_WHAT=3;//日志消息
    //UHF天线
    public UHFHelper uhfHelper;
    public LockHelper lockHelper;

    //签封可执行任务列表;
    public List<String> tagidlist;
    //扫描捆号过滤
    public  List<String> allcarddata;

    //捆数
    public int kuncount=20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_bisness_base);

        tv_header=findViewById(R.id.tv_header);
        tv_operinfo=findViewById(R.id.tv_operinfo);
        tv_footer=findViewById(R.id.tv_footer);
        tv_kuncount=findViewById(R.id.tv_kuncout);
        scro_businfo=findViewById(R.id.scro_businfo);

        line_businfo=findViewById(R.id.line_businfo);
        line_kun=findViewById(R.id.line_kun);
        line_rsinfo=findViewById(R.id.line_rsinfo);
        line_scorllrsinfo=findViewById(R.id.line_scorllrsinfo);


        //初始化声音
        Util.initSoundPool(this);
        //初始化日志
        mylog=new MyLog(this,10000,1);
        //mylog.Write(this.getClass()+"业务启动！*****************************");

        //注册按钮
        //注册按键广播
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.rfid.FUN_KEY");
        registerReceiver(keyReceiver,filter);

        //初始化语音
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == textToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE){
                       mylog.Write( "TTS暂时不支持这种语音的朗读！");
                    }
                }
            }

        });
        tv_operinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(tv_operinfo.getText().toString(),
                        TextToSpeech.QUEUE_FLUSH, null);
            }
        });

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
 //           Log.e("key ","keyCode = " + keyCode) ;
            boolean keyDown = intent.getBooleanExtra("keydown", false) ;
//			Log.e("key ", "down = " + keyDown);
            if(keyUpFalg&&keyDown && System.currentTimeMillis() - startTime > 500){
                keyUpFalg = false;
                startTime = System.currentTimeMillis() ;
                if (keyCode == KeyEvent.KEYCODE_F3) {
                    readCard();
                }
                if(keyCode==KeyEvent.KEYCODE_F1)
                {
                   // addRsinfo("开始执行关锁...",true);
                    if(tagidlist==null)
                    {
                        addRsinfo("本业务不支持开关锁",false);
                        return;
                    }

                    operateLockGetrs(true);
                }
                if(keyCode==KeyEvent.KEYCODE_F2)
                {
                   // addRsinfo("开始执行开锁...",true);
                    if(tagidlist==null)
                    {
                        addRsinfo("本业务不支持开关锁",false);
                        return;
                    }

                    operateLockGetrs(false);
                }

                return ;
            }else if (keyDown){
                startTime = System.currentTimeMillis() ;
            }else {
                keyUpFalg = true;
            }

        }
    } ;

    public void readCard() {
        uhfHelper=new UHFHelper(mHandler,this);
        uhfHelper.readCARD(18,10);
    }

    //show tips
    private Toast toast;
    private void showToast(String info) {
        if (toast==null) toast =  Toast.makeText(this, info, Toast.LENGTH_SHORT);
        else toast.setText(info);
        toast.show();
    }
    public void addRsinfo(final String message, boolean rs)
    {

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        AlwaysMarqueeTextView rstv=new AlwaysMarqueeTextView(this);
        rstv.setText(message);
        rstv.setTextSize(24);
        rstv.setMarqueeRepeatLimit(Integer.MAX_VALUE);
//        rstv.setFocusable(true);
        rstv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        rstv.setSingleLine();
//        rstv.setFocusableInTouchMode(true);
//        rstv.setHorizontallyScrolling(true);

        rstv.setLayoutParams(params);
        rstv.setAnimation(AnimationUtils.makeInAnimation(this,true));
        if(rs)
        {
            rstv.setBackground(getDrawable(R.drawable.tv_goodinfo));

        }else
        {
            rstv.setBackground(getDrawable(R.drawable.tv_badinfo));

        }
        //语音播报提示
        rstv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(message,
                        TextToSpeech.QUEUE_ADD, null);
            }
        });
        line_rsinfo.addView(rstv);
       // line_scorllrsinfo.fullScroll(ScrollView.FOCUS_DOWN);

        line_scorllrsinfo.post(new Runnable() {
            public void run() {
                line_scorllrsinfo.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    public void operateLockGetrs(boolean lock)
    {

    }
    public  Boolean checkrepeat(String nowcarddata)
    {
        for (String s : allcarddata)
        {
            if (s == nowcarddata || s.equals(nowcarddata))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(uhfHelper!=null)
        {
            uhfHelper.close();
        }
        unregisterReceiver(keyReceiver);//


    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                kuncount--;
                mylog.Write("左键按下");
                tv_kuncount.setText(kuncount+"");
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                kuncount++;
                mylog.Write("右键按下");
                tv_kuncount.setText(kuncount+"");
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            if(sp_stackInfo!=null)
            {
                sp_stackInfo.setSelection(sp_stackInfo.getSelectedItemPosition()-1);
            }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if(sp_stackInfo!=null)
                {
                    sp_stackInfo.setSelection(sp_stackInfo.getSelectedItemPosition()+1);
                }
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
