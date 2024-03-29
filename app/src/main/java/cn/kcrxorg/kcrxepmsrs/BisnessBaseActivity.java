package cn.kcrxorg.kcrxepmsrs;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.kcrxepmsrs.mbutil.MyLog;
import cn.kcrxorg.kcrxepmsrs.mbutil.XToastUtils;
import cn.kcrxorg.kcrxepmsrs.pasmutil.Config;
import cn.kcrxorg.kcrxepmsrs.pasmutil.PsamCmdUtil;
import cn.kcrxorg.kcrxepmsrs.uhfutil.LockHelper;
import cn.kcrxorg.kcrxepmsrs.ui.login.LoginActivity;
import cn.kcrxorg.kcrxepmsrs.views.CmdInfoListView;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;
import com.warkiz.widget.IndicatorSeekBar;
import com.xuexiang.xpage.base.XPageActivity;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.dialog.bottomsheet.BottomSheet;
import com.xuexiang.xui.widget.dialog.materialdialog.GravityEnum;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.progress.loading.RotateLoadingView;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;
import com.xuexiang.xui.widget.toast.XToast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//public class BisnessBaseActivity extends AppCompatActivity {
public class BisnessBaseActivity extends AppCompatActivity {
    SuperTextView tv_header;
    TextView tv_operinfo;
    TextView tv_footer;
    TextView tv_kuncount;

    LinearLayout line_businfo;
    LinearLayout line_kun;
    LinearLayout line_rsinfo;
    LinearLayout line_power;
    ScrollView scro_businfo;
    ScrollView line_scorllrsinfo;

    Spinner sp_stackInfo;//选择列表

    private IndicatorSeekBar sbpower;//功率设置
    //语音
    private TextToSpeech textToSpeech;
    public MyLog mylog;

    public Handler mHandler;//消息中心
    public static final int uhfwhat=1;//标签消息
    public static final int INFO_MES = 0;//其他消息
    public static final int LOCK_WHAT=2;//操作锁结果
    public static final int LOCK_LOG_WHAT=3;//日志消息
    //UHF天线
    private UHFRManager uHFRManager;
    private static PsamCmdUtil psamCmdUtil;
    private static int psamCard = 1;//PSAM卡座号
    public LockHelper lockHelper;

    //签封可执行任务列表;
    public List<String> tagidlist;
    //扫描捆号过滤
    public  List<String> allcarddata;

    //捆数
    public int kuncount=20;
    //功率配置
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    //查看任务列表
    List<ViewCmdInfo> viewCmdInfoList;
    ProgressDialog mypDialog;

    //加密设置
    public  boolean encrypt=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XUI.initTheme(this);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_bisness_base);
        //初始化配置
        mSharedPreferences = getSharedPreferences("UHF",MODE_PRIVATE);

        encrypt=mSharedPreferences.getBoolean("encrypt",true);
        //showToast("目前加密模式是:"+encrypt);



        tv_header=findViewById(R.id.tv_header);
        tv_header.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(encrypt)
                {
                    showToast("当前为加密模式");
                }else
                {
                    showToast("当前为解密模式");
                }
                return true;
            }
        });
        tv_header.setRightTvClickListener(new SuperTextView.OnRightTvClickListener() {
            @Override
            public void onClick(TextView textView) {
                if(viewCmdInfoList==null)
                {
                    XToastUtils.error("当前任务不存在任务列表!");
                    return;
                }
                String[] cmdstrs=new String[viewCmdInfoList.size()];
                if(viewCmdInfoList.size()>0)
                {
//                    BottomSheet.BottomListSheetBuilder builder=new BottomSheet.BottomListSheetBuilder(BisnessBaseActivity.this);
//                     for(int i=0;i<viewCmdInfoList.size();i++)
//                    {
//                        ViewCmdInfo v= viewCmdInfoList.get(i);
//                        cmdstrs[i]=(v.getSackNo()+" "+v.getPaperTypeName()+" "+v.getVoucherTypeName());
//                        Log.e("kcrx",v.getSackNo());
//                        builder.addItem(cmdstrs[i]);
//                    }
//                    builder.setTitle("任务列表信息"+"总数:"+viewCmdInfoList.size());
//                    builder.build().show();
                    CmdInfoListView cmdInfoListView=new CmdInfoListView(BisnessBaseActivity.this,viewCmdInfoList);
                    new MaterialDialog.Builder(BisnessBaseActivity.this)
                            .titleGravity(GravityEnum.CENTER)
                            .titleColorRes(R.color.xpage_default_actionbar_color)
                            .customView(cmdInfoListView,false)
                            .title("任务列表信息"+"总数:"+viewCmdInfoList.size())
                            .positiveText("确定")
                            .show();
                }
                else
                {
                    XToast.error(BisnessBaseActivity.this,"无任务列表信息");
                }
            }
        });


        tv_operinfo=findViewById(R.id.tv_operinfo);
        tv_footer=findViewById(R.id.tv_footer);
        tv_kuncount=findViewById(R.id.tv_kuncout);
        scro_businfo=findViewById(R.id.scro_businfo);

        line_businfo=findViewById(R.id.line_businfo);
        line_kun=findViewById(R.id.line_kun);
        line_rsinfo=findViewById(R.id.line_rsinfo);
        line_scorllrsinfo=findViewById(R.id.line_scorllrsinfo);
        line_power=findViewById(R.id.line_power);

        sbpower=findViewById(R.id.seekBar_power);//功率设置

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
                    //readCard();
                    new ReadTagTask().execute();
                }
                if(keyCode==KeyEvent.KEYCODE_F1)
                {
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
                if(keyCode==135)//右侧上按键
                {
                    if(line_power.getVisibility()==View.GONE)
                    {
                        line_power.setVisibility(View.VISIBLE);
                        sbpower.setProgress(uHFRManager.getPower()[0]);
                        showToast("选择要设置的功率大小，并按右侧上键确认设置");

                    }else
                    {
                        line_power.setVisibility(View.GONE);
                        int[] nowpower=uHFRManager.getPower();
                        Reader.READER_ERR err=uHFRManager.setPower(sbpower.getProgress(),nowpower[1]);
                        if(err== Reader.READER_ERR.MT_OK_ERR)
                        {
                            line_power.setVisibility(View.GONE);
                            showToast("设置扫描功率为:"+sbpower.getProgress());
                            mEditor=mSharedPreferences.edit();
                            mEditor.putInt("readPower", sbpower.getProgress());
                            mEditor.putInt("writePower", nowpower[1]);
                            mEditor.commit();
                        }else
                        {
                            line_power.setVisibility(View.GONE);
                            showToast("设置功率失败！");
                        }
                    }
                }

                return ;
            }else if (keyDown){
                startTime = System.currentTimeMillis() ;
            }else {
                keyUpFalg = true;
            }

        }
    } ;


    @Override
    protected void onResume() {
        super.onResume();
        bootDevice();
        mylog.Write("恢复完成");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    //    Intent mainintent=new Intent(this, MainActivity.class);
    }

    public void bootDevice()
    {
        new Thread(){
            @Override
            public void run() {
                super.run();
                //启动天线
                uHFRManager=UHFRManager.getInstance();
                psamCmdUtil=new PsamCmdUtil();
                psamCmdUtil.openRfid();

                lockHelper=new LockHelper(mHandler,psamCmdUtil,uHFRManager);
            }
        }.start();
    }
    public void readCard() {
        int readpower=mSharedPreferences.getInt("readPower",25);
        Log.e("kcrx","开始扫描款包,读取功率："+readpower);
        lockHelper.readCARD(mHandler,readpower,10);
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
            rstv.setTextColor(getResources().getColor(R.color.Black));
        }else
        {

            rstv.setBackground(getDrawable(R.drawable.tv_badinfo));
            rstv.setTextColor(getResources().getColor(R.color.TextWhite));
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
        if(mLoadingDialog!=null)
        {
            mLoadingDialog.recycle();
        }
        unregisterReceiver(keyReceiver);//
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_LEFT:

                AlertDialog builder = new AlertDialog.Builder(this)
                        .setTitle("提示:")
                        .setMessage("确定要更改款袋捆数？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                kuncount--;
                                if(kuncount<1)
                                    kuncount=1;//至少1捆
                                mylog.Write("左键按下，kuncount="+kuncount);
                                tv_kuncount.setText(kuncount+"");
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                //修改 确定取消 按钮的字体大小
                builder.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(24);
                builder.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(24);

                try {
                    //获取mAlert对象
                    Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                    mAlert.setAccessible(true);
                    Object mAlertController = mAlert.get(builder);

                    //获取mTitleView并设置大小颜色
                    Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
                    mTitle.setAccessible(true);
                    TextView mTitleView = (TextView) mTitle.get(mAlertController);
                    mTitleView.setTextSize(24);
                    mTitleView.setTextColor(getResources().getColor(R.color.DarkGreen));

                    //获取mMessageView并设置大小颜色
                    Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                    mMessage.setAccessible(true);
                    TextView mMessageView = (TextView) mMessage.get(mAlertController);
                    mMessageView.setTextColor(getResources().getColor(R.color.Black));
                    mMessageView.setTextSize(24);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();

                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:

                AlertDialog builder1 = new AlertDialog.Builder(this)
                        .setTitle("提示:")
                        .setMessage("确定要更改款袋捆数？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                kuncount++;
                                mylog.Write("右键按下，kuncount="+kuncount);
                                tv_kuncount.setText(kuncount+"");
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                //修改 确定取消 按钮的字体大小
                builder1.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(24);
                builder1.getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(24);

                try {
                    //获取mAlert对象
                    Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
                    mAlert.setAccessible(true);
                    Object mAlertController = mAlert.get(builder1);

                    //获取mTitleView并设置大小颜色
                    Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
                    mTitle.setAccessible(true);
                    TextView mTitleView = (TextView) mTitle.get(mAlertController);
                    mTitleView.setTextSize(24);
                    mTitleView.setTextColor(getResources().getColor(R.color.DarkGreen));

                    //获取mMessageView并设置大小颜色
                    Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
                    mMessage.setAccessible(true);
                    TextView mMessageView = (TextView) mMessage.get(mAlertController);
                    mMessageView.setTextColor(getResources().getColor(R.color.Black));
                    mMessageView.setTextSize(24);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();

                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

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
    LoadingDialog mLoadingDialog;
    boolean isreading=false;
    public class ReadTagTask extends AsyncTask<String, Integer, Boolean>
    {
       // ProgressDialog mypDialog;
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mLoadingDialog.cancel();
            isreading=false;
         //   mypDialog.cancel();
        }
        @Override
        protected Boolean doInBackground(String... strings) {
            isreading=true;
            readCard();
            return true;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(isreading==true)
            {
                mLoadingDialog.updateMessage("正在扫描，请勿重复操作...");
                return;
            }
              //扫描进度提示
                mLoadingDialog = WidgetUtils.getLoadingDialog(BisnessBaseActivity.this)
                        .setLoadingIcon(R.drawable.cblogo)
                        .setLoadingSpeed(8);
                mLoadingDialog.setCanceledOnTouchOutside(true);
                mLoadingDialog.updateMessage("正在扫描中请稍后...");
                mLoadingDialog.show();
//            mypDialog = new ProgressDialog(BisnessBaseActivity.this);
//            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            mypDialog.setMessage("正在读取请稍后...");
//            mypDialog.setCanceledOnTouchOutside(false);
//            mypDialog.show();
        }
    }
   public void setGoodViewCmdInfo(String sackno)
   {
         for(ViewCmdInfo v:viewCmdInfoList)
         {
             if(v.getSackNo().equals(sackno))
             {
                 v.setDone(true);
             }
         }
   }

}
