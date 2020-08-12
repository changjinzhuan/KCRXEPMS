package cn.kcrxorg.kcrxepms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cn.kcrxorg.kcrxepms.businessmodule.cmdinfo.OutScanCMD;
import cn.kcrxorg.kcrxepms.businessmodule.cmdinfo.User;
import cn.kcrxorg.kcrxepms.businessmodule.cmdinfo.UserCommand;
import cn.kcrxorg.kcrxepms.businessmodule.mapper.UserMapper;
import cn.kcrxorg.kcrxepms.communicationmodule.BaseCmd;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd1001;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd1002;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd2002;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd2003;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd2004;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd2005;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd4002;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd8001;
import cn.kcrxorg.kcrxepms.communicationmodule.Cmd8002;
import cn.kcrxorg.kcrxepms.communicationmodule.CmdSelector;
import cn.kcrxorg.kcrxepms.communicationmodule.L4vtype;
import cn.kcrxorg.kcrxepms.communicationmodule.Replay2004;
import cn.kcrxorg.kcrxepms.communicationmodule.Replay4002;
import cn.kcrxorg.kcrxepms.communicationmodule.Replay8001;
import cn.kcrxorg.kcrxepms.communicationmodule.Replay8002;
import cn.kcrxorg.kcrxepms.communicationmodule.Reply1001;
import cn.kcrxorg.kcrxepms.mbutil.MyLog;
import cn.kcrxorg.kcrxepms.mbutil.MyTools;
import cn.kcrxorg.kcrxepms.mbutil.NetChecker;
import cn.kcrxorg.kcrxepms.mbutil.TXTReader;
import cn.kcrxorg.kcrxepms.mbutil.TXTWriter;
import cn.kcrxorg.kcrxepms.mbutil.UserLGtool;
import cn.kcrxorg.kcrxepms.pasmutil.Config;
import cn.kcrxorg.kcrxepms.pasmutil.DESHelper;
import cn.kcrxorg.kcrxepms.pasmutil.HexUtil;
import cn.kcrxorg.kcrxepms.pasmutil.PsamCmdUtil;
import cn.kcrxorg.kcrxepms.pasmutil.PsamError;
import cn.kcrxorg.kcrxepms.pasmutil.SHA1Util;
import cn.kcrxorg.kcrxepms.ui.login.LoginActivity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.alibaba.fastjson.JSONObject;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientDelegate;
import com.vilyever.socketclient.helper.SocketResponsePacket;
import com.vilyever.socketclient.util.CharsetUtil;

import org.apache.ftpserver.ftplet.FtpException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_connect;
    Button btn_select;
    Button btn_dobis;//执行待办
    TextView tv_connectstate;
    TextView tv_loginstate;
    private SocketClient socketClient;
    private SocketClientDelegate delegate;

    private MyLog mylog;

    private PsamCmdUtil psam;//pasm卡
    private int psamCard = 1;//PSAM卡座号

    //默认加解密key
    private String skey = "817340064B866F15196F5934BC930462";//存储解密
    private String mkey = "DA8D9AC02FB413BD9E4CAD626850473F";//存储加密
    private String session = "91";
    private String sequence = "0000000000000000";
    //任务数据文件目录
    private static final String CmdDir = "/Cmd/";
    private static final String DataDir = "/Data/";
    File CmdDirF;
    File DataDirF;

    //本地数据库
    UserMapper userMapper;

    //登录的用户
    String operator = "00000000";
    String auditor = "00000000";

    //业务状态
    int businessstate = 0;//0 等待命令状态 1 提交状态

    //FTP服务
     UserLGtool userLGtool;

     //handler
    Handler mHandler;
    Config config;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        //初始化日志
        mylog = new MyLog(this, 10000, 1);
        mylog.Write("用户已登录！*****************************");

        intView();
        //启动ftp服务
        userLGtool=new UserLGtool();
        try {
            mylog.Write("正在启动ftp服务...");
            userLGtool.startFtpServer(this);
            mylog.Write("ftp服务启动成功！");
        } catch (FtpException e) {
            mylog.Write("ftp服务启动失败!"+e.getMessage());
           // e.printStackTrace();
        }
        //配置文件读取
        config=new Config(this);
        config.load();

        //生成数据文件目录
        File logRoot = getFilesDir();
        CmdDirF = new File(logRoot.getPath() + CmdDir);
        DataDirF = new File(logRoot.getPath() + DataDir);

        if (!CmdDirF.exists()) {
            CmdDirF.mkdir();
        }
        if (!DataDirF.exists()) {
            DataDirF.mkdir();
        }

        //初始化数据库
        userMapper = new UserMapper(this);


        //获取登录用户 UID
        String operatorCID = getIntent().getStringExtra("operator");
        String auditorCID = getIntent().getStringExtra("auditor");

        User operatorUser = userMapper.getUseruid(operatorCID);
        User auditorUser = userMapper.getUseruid(auditorCID);

        if (operatorUser != null && auditorUser != null) {
            operator = operatorUser.getUid();
            auditor = auditorUser.getUid();
        }


        mylog.Write("operator=" + operator);
        mylog.Write("auditor=" + auditor);
        if (!operator.equals("00000000")) {
            tv_loginstate.setText("操作员:" + operatorUser.getName() + " 已登录\r\n复核员:"+auditorUser.getName()+" 已登录");
            tv_loginstate.setTextColor(getResources().getColor(R.color.green));
        }


        //启动网络调试
        Runtime mRuntime = Runtime.getRuntime();
        try {
            mRuntime.exec("setprop service.adb.tcp.port 5555 ");
            mRuntime.exec("stop adbd ");
            mRuntime.exec("start adbd ");
            Log.e("kcrx", "网络调试已经打开");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 100:
                        mylog.Write("网路状态:" + msg.getData().getBoolean("netstate"));
                        if (msg.getData().getBoolean("netstate"))
                        {
                            try {
                                Thread.sleep(3000);
                               // connect();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        break;
                }
            }
        };

        //启动网络检查
        NetChecker netChecker = new NetChecker(mHandler);
        netChecker.start();
        mylog.Write("网络监控已经启动");
    }

    private void intView() {
        btn_connect = findViewById(R.id.btn_connect);
        btn_select = findViewById(R.id.btn_select);
        btn_dobis = findViewById(R.id.btn_dobis);

        tv_connectstate = findViewById(R.id.tv_connectstate);
        tv_loginstate = findViewById(R.id.tv_loginstate);

        btn_connect.setOnClickListener(this);
        btn_select.setOnClickListener(this);
        btn_dobis.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mylog.Write("程序已关闭!");
        if (socketClient != null) {
            socketClient.disconnect();
        }
        if(userLGtool!=null)
        {
            userLGtool.stop();
        }
    }

    public void connect()
    {
        String info = config.getValue("serverIp")+"_50002";
        try {
            mylog.Write(R.string.connecting+"ip="+info);
            startSocketClient(info);
            tv_connectstate.setText(R.string.connecting);
            mylog.Write(getResources().getString(R.string.connecting));
        } catch (IOException e) {
            tv_connectstate.setText("连接失败！");
            mylog.Write("连接失败!");
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect://连接按钮
                connect();
                break;
            case R.id.btn_select://提交按钮
                if (operator.equals("00000000")) {
                    showToast("操作员未登录，不可办理业务！请操作员登录...");
                    break;
                }
                if (socketClient == null) {
                    showToast("通讯组件未连接，无法提交！请先连接通讯组件");
                    break;
                }
                businessstate = 1;
                if (businessstate == 1)//如果是业务完成状态
                {

                    TXTReader tr = new TXTReader();
                    try {
                        //  tr.findLastFile(this,DataDir);
                        String data = tr.findLastFile(MainActivity.this, DataDir);
                        if (data == null) {
                            showToast("没有任务需要提交!");
                            break;
                        }
                        if (!data.equals(""))//如果最近任务存在
                        {
                            String id = data.substring(0, 8);
                            Random rnd = new Random();
                            int fragmentint = rnd.nextInt(999999);
                            String fragment = String.format("%08d", fragmentint);
                            String timestamp = data.substring(8, 24);
                            data = data.substring(24);
                            Replay4002 replay4002 = new Replay4002();
                            int oper = Integer.parseInt(operator);
                            int aduit = Integer.parseInt(auditor);

                            String operhex = Tools.Bytes2HexString(HexUtil.intToBytes(oper), HexUtil.intToBytes(oper).length);
                            String aduithex = Tools.Bytes2HexString(HexUtil.intToBytes(aduit), HexUtil.intToBytes(aduit).length);
                            ;
                            mylog.Write("operhex=" + operhex);
                            mylog.Write("aduithex=" + aduithex);

                            String payloads = id + fragment + timestamp + new L4vtype(data) + operhex + aduithex;
                            mylog.Write("加密前数据" + payloads);
                            String mPayloads = DESHelper.encryptStr(payloads, skey);
                            String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                            String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                            mPayloads = mPayloads + MAC;

                            replay4002.setPayloads(mPayloads);

                            int len = replay4002.getPayloads().length() / 2;
                            String hex = Integer.toHexString(len).toUpperCase();
                            replay4002.setLength("F" + MyTools.addZeroForNum(hex, 7));
                            replay4002.setSequence(sequence);//
                            String reply4002data = replay4002.toDataString().toUpperCase();
                            mylog.Write("replay4002=" + reply4002data);
                            mylog.Write("replay4002长度检查=" + replay4002.checkLenth());
                            socketClient.sendData(Tools.HexString2Bytes(reply4002data));
                            mylog.Write("发送replay4002");

                            showToast("任务提交完成!id=" + id);

                        } else {
                            showToast("没有任务需要提交!");
                        }

                    } catch (Exception e) {
                        mylog.Write("提交失败！" + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }
                break;
            case R.id.btn_dobis:
                TXTReader tr = new TXTReader();
                try {
                    String cmddata = tr.findLastFile(this, CmdDir);//找到最近的任务
                    String id = cmddata.substring(0, 8);
                    mylog.Write("收到待办业务指令，查找到待办业务ID=" + id);
                    doBusiness(id);

                } catch (UnsupportedEncodingException e) {
                    showToast("无待办业务!执行失败");
                    mylog.Write("无待办业务!执行失败");
                    e.printStackTrace();
                }
                break;
        }
    }


    //socket通信
    private void startSocketClient(String info) throws IOException {
        String[] array = info.split("_");
        socketClient = new SocketClient();
        socketClient.getAddress().setRemoteIP(array[0]);//设置IP,这里设置的是本地IP
        socketClient.getAddress().setRemotePort(Integer.parseInt(array[1]));//设置端口
        socketClient.getAddress().setConnectionTimeout(15 * 1000);//设置超时时间


        socketClient.setCharsetName(CharsetUtil.UTF_8);//设置编码格式，默认为UTF-8
        socketClient.connect(); // 连接，异步进行

        socketClient.registerSocketClientDelegate(delegate = new SocketClientDelegate() {
            @Override
            public void onConnected(SocketClient client) {
                tv_connectstate.setText(R.string.connecting);
              //  tv_connectstate.setTextColor(getResources().getColor(R.color.green));
                mylog.Write("socket连接成功=" + client.getState());
            }

            @Override
            public void onDisconnected(SocketClient client) {
                mylog.Write("socket连接断开");
                tv_connectstate.setText("通讯组件未连接,请检查连接...");
                tv_connectstate.setTextColor(getResources().getColor(R.color.red));
            }

            @Override
            public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
                byte[] data = responsePacket.getData();
                //  MyLogger.show("kcrx","data=" + Tools.Bytes2HexString(data,data.length));
                mylog.Write("recv<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                mylog.Write("收到通讯组件data=" + Tools.Bytes2HexString(data, data.length));
                BaseCmd baseCmd = CmdSelector.makeCmd(Tools.Bytes2HexString(data, data.length));
                if (baseCmd != null) {
                    mylog.Write("收到通讯组件命令:" + baseCmd.getTransport());
                    if (!baseCmd.checkLenth())//命令检查未通过
                    {
                        // MyLogger.show("kcrx","长度检查:"+baseCmd.checkLenth()) ;
                        mylog.Write("长度检查:命令长度错误");

                        if(true)//所有长度有问题命令都检查粘包
                        {
                            mylog.Write("可能粘包，拆分任务...");
                            try {
                            String hexdata= Tools.Bytes2HexString(data, data.length);
                            int lenpayload4002=Integer.parseInt(hexdata.substring(1,8),16);

                            String dataone=hexdata.substring(0,28+lenpayload4002*2);
                            String datatwo=hexdata.substring(28+lenpayload4002*2);
                            mylog.Write("dataone="+dataone);
                            mylog.Write("datatwo="+datatwo);
                            BaseCmd cmdone=CmdSelector.makeCmd(dataone);
                            BaseCmd cmdtwo=CmdSelector.makeCmd(datatwo);
                            mylog.Write("cmdone="+cmdone.getTransport());
                            mylog.Write("cmdtwo="+cmdtwo.getTransport());

                                if(cmdone.checkLenth())
                                {
                                    executeCmd(cmdone,dataone);
                                }

                                if(cmdtwo.checkLenth())
                                {
                                    executeCmd(cmdtwo,datatwo);
                                }

                            }catch (Exception e)
                            {
                               // mylog.Write("执行命令失败:" + Tools.Bytes2HexString(data, data.length));
                                mylog.Write("执行命令失败:" + e.getStackTrace());
                                showToast("执行命令失败!");
                            }

                        }else
                        {
                            mylog.Write("长度检查:" + baseCmd.checkLenth());
                            showToast("通讯组件下发命令有误：数据长度错误");
                        }
                    }
                    //命令长度检查通过，开始执行
                 else
                    {
                        try {

                            executeCmd(baseCmd, Tools.Bytes2HexString(data, data.length));
                        } catch (Exception e) {
                            mylog.Write("执行命令失败:" + Tools.Bytes2HexString(data, data.length));
                            mylog.Write("执行命令失败:" + e.getStackTrace());
                            showToast("执行命令失败!");
                        }
                    }

                } else {
                    mylog.Write("通讯组件下发命令有误！basecmd=null");
                    showToast("通讯组件下发命令有误:数据长度错误");
                }
            }
        });
    }

    private void executeCmd(BaseCmd basecmd, String cmddata) throws Exception {

        int transport = Integer.parseInt(basecmd.getTransport());

        switch (transport) {
            case 1001://1001
                mylog.Write("收到1001指令:" + cmddata);
                Cmd1001 cmd1001 = new Cmd1001(basecmd);
                mylog.Write("1001版本号:" + cmd1001.getVersion());

                //开始PSAM操作
                psam = new PsamCmdUtil();
                if (psam.openRfid() != null) {
                    mylog.Write("打开PSAM模块成功");
                }
                PsamError err = new PsamError();
                byte[] result = null;
                //复位
                result = psam.resetCard(psamCard);
                if (result != null) {
                    mylog.Write("复位PSAM卡：" + Tools.Bytes2HexString(result, result.length));
                } else {

                    mylog.Write("复位PSAM卡失败");//  Util.play(3, 0);
                }
                String psaminfo = "";
                result = psam.getPSAMinfo(psamCard, err);
                if (result != null) {
                    psaminfo = Tools.Bytes2HexString(result, result.length);
                    byte[] psaminfob= psaminfo.getBytes("utf-8");
                    psaminfo=Tools.Bytes2HexString(psaminfob,psaminfob.length);
                    mylog.Write("获取PSAM卡信息：" + psaminfo);
                } else {

                    mylog.Write("获取PSAM卡信息失败");
                }
             //   psam.closeRfid();
                mylog.Write("PSAM不下电");
                Reply1001 reply1001 = new Reply1001(psaminfo, Tools.Bytes2HexString(cmd1001.getVersion().getBytes(), cmd1001.getVersion().getBytes().length));
                String reply1001data = reply1001.getReplydata();
                mylog.Write("reply1001=" + reply1001data);
                mylog.Write("reply1001长度检查=" + reply1001.checkLenth());
                socketClient.sendData(Tools.HexString2Bytes(reply1001data));
                mylog.Write("发送reply1001");
                tv_connectstate.setText("初始化连接......");
                break;
            case 1002:
                mylog.Write("收到1002指令:" + cmddata);
                Cmd1002 cmd1002=new Cmd1002(basecmd);
                if(cmd1002.getLevel().equals("02"))
                {
                     tv_connectstate.setText("错误:"+cmd1002.getMessage());
                    mylog.Write("错误:"+cmd1002.getMessage());
                }else if(cmd1002.getLevel().equals("01"))
                {
                    tv_connectstate.setText("警告:"+cmd1002.getMessage());
                    mylog.Write("警告:"+cmd1002.getMessage());
                }else//正常
                {
                    mylog.Write("2001任务：正常");
                }
                break;
            case 4002:
                mylog.Write("收到4002响应:" + cmddata);
                Cmd4002 cmd4002=new Cmd4002(basecmd,skey);
                mylog.Write("收到4002响应error="+cmd4002.getError());
                tv_connectstate.setText("提交响应:"+cmd4002.getError());
                break;
            case 8001:
                mylog.Write("收到8001指令:" + cmddata);
                Cmd8001 cmd8001 = new Cmd8001(basecmd, skey);
                if (cmd8001.getAction().equals("01"))//上电
                {
                    mylog.Write("上电指令");
                    //开始PSAM操作
                    psam = new PsamCmdUtil();
                    if (psam.openRfid() != null) {
                        mylog.Write("打开PSAM模块成功");
                    }
                    // PsamError  err8001 = new PsamError();
                    byte[] result8001 = null;
                    //复位
                    result8001 = psam.resetCard(psamCard);
                    if (result8001 != null) {
                        mylog.Write("复位PSAM卡：" + Tools.Bytes2HexString(result8001, result8001.length));
                    } else {
                        mylog.Write("复位PSAM卡失败");
                    }
                    if (basecmd.getLength().startsWith("7"))//加密回复
                    {
                        Replay8001 replay8001 = new Replay8001();
                        replay8001.setAtr(Tools.Bytes2HexString(result8001, result8001.length));
                        replay8001.setSequence(basecmd.getSequence());
                        String payloads = replay8001.getError() + Integer.toHexString(replay8001.getAtr().length() / 2) + replay8001.getAtr();
                        String mPayloads = DESHelper.encryptStr(payloads, skey);
                        String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                        String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                        mPayloads = mPayloads + MAC;
                        replay8001.setPayloads(mPayloads);
                        int len = replay8001.getPayloads().length() / 2;
                        String hex = Integer.toHexString(len).toUpperCase();
                        replay8001.setLength("F" + MyTools.addZeroForNum(hex, 7));
                        String reply8001data = replay8001.toDataString().toUpperCase();
                        mylog.Write("reply8001=" + reply8001data);
                        mylog.Write("reply8001长度检查=" + replay8001.checkLenth());
                        socketClient.sendData(Tools.HexString2Bytes(reply8001data));
                        mylog.Write("发送reply8001");
                    } else//不加密回复
                    {
                        Replay8001 replay8001 = new Replay8001();
                        replay8001.setAtr(Tools.Bytes2HexString(result8001, result8001.length));
                        replay8001.setSequence(basecmd.getSequence());
                        replay8001.setPayloads(replay8001.getError() + Integer.toHexString(replay8001.getAtr().length() / 2) + replay8001.getAtr());
                        int len = replay8001.getPayloads().length() / 2;
                        String hex = Integer.toHexString(len).toUpperCase();
                        replay8001.setLength("8" + MyTools.addZeroForNum(hex, 7));
                        String reply8001data = replay8001.toDataString().toUpperCase();
                        ;
                        mylog.Write("reply8001=" + reply8001data);
                        mylog.Write("reply8001长度检查=" + replay8001.checkLenth());
                        socketClient.sendData(Tools.HexString2Bytes(reply8001data));
                        mylog.Write("发送reply8001");
                    }

                } else if (cmd8001.getAction().equals("02"))//下电
                {
                    mylog.Write("下电指令");
                    psam.closeRfid();
                    mylog.Write("下电指令完成");

                    if (basecmd.getLength().startsWith("7"))//加密回复
                    {
                        Replay8001 replay8001 = new Replay8001();
                        replay8001.setSequence(basecmd.getSequence());
                        replay8001.setAtr("");
                        replay8001.setPayloads(replay8001.getError() + "00");//atr L1-V为空
                        mylog.Write("下电加密前payloads=" + replay8001.getError() + "00");//atr L1-V为空
                        String mPayloads = DESHelper.encryptStr(replay8001.getPayloads(), skey);
                        String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                        String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                        mPayloads = mPayloads + MAC;
                        replay8001.setPayloads(mPayloads);
                        int len = replay8001.getPayloads().length() / 2;
                        String hex = Integer.toHexString(len).toUpperCase();
                        replay8001.setLength("F" + MyTools.addZeroForNum(hex, 7));
                        String reply8001data = replay8001.toDataString().toUpperCase();
                        mylog.Write("reply8001=" + reply8001data);
                        mylog.Write("reply8001长度检查=" + replay8001.checkLenth());
                        socketClient.sendData(Tools.HexString2Bytes(reply8001data));
                        mylog.Write("发送reply8001");
                    }else//不加密回复
                    {
                        Replay8001 replay8001 = new Replay8001();
                        replay8001.setSequence(basecmd.getSequence());
                        replay8001.setAtr("");
                        replay8001.setPayloads(replay8001.getError() + "00");//atr L1-V为空
                        int len = replay8001.getPayloads().length() / 2;
                        String hex = Integer.toHexString(len).toUpperCase();
                        replay8001.setLength("8" + MyTools.addZeroForNum(hex, 7));
                        String reply8001data = replay8001.toDataString().toUpperCase();
                        mylog.Write("reply8001=" + reply8001data);
                        mylog.Write("reply8001长度检查=" + replay8001.checkLenth());
                        socketClient.sendData(Tools.HexString2Bytes(reply8001data));
                        mylog.Write("发送reply8001");
                        tv_connectstate.setText("正在进行安全令牌验证....");
                    }

                } else {
                    throw new Exception("8001命令解析失败！");
                }
                break;
            case 8002:
                mylog.Write("收到8002指令:" + cmddata);
                Cmd8002 cmd8002 = new Cmd8002(basecmd, skey);
                mylog.Write("APDU指令:" + cmd8002.getCAPDU());
                PsamError err8002 = new PsamError();
                byte[] result8002 = null;
                String rapdu = "";
                if (cmd8002.getCAPDU().equals("8010020000"))//证书第一部分
                {
                    result8002 = psam.getPSAMCertifiPart1(psamCard, err8002);
                    if (result8002 != null) {
                        mylog.Write("8002证书第一部分：" + Tools.Bytes2HexString(result8002, result8002.length));
                        rapdu = Tools.Bytes2HexString(result8002, result8002.length) + "6310";
                    } else {
                        mylog.Write("8002证书第一部分执行失败");
                    }
                } else if (cmd8002.getCAPDU().equals("8010020100"))//证书第二部分
                {
                    result8002 = psam.getPSAMCertifiPart2(psamCard, err8002);
                    if (result8002 != null) {
                        mylog.Write("8002证书第二部分：" + Tools.Bytes2HexString(result8002, result8002.length));
                        rapdu = Tools.Bytes2HexString(result8002, result8002.length);
                    } else {
                        mylog.Write("8002证书第二部分执行失败");
                    }
                } else if (cmd8002.getCAPDU().startsWith("8023"))//在此处获取key，实际环境测试
                {
                    String cmdstr = cmd8002.getCAPDU();
                    rapdu = psam.excute(psamCard, cmdstr, err8002);
                    if (rapdu.endsWith("9000"))//获取成功
                    {
                        session = rapdu.substring(2, 4);
                        skey = rapdu.substring(4, 36);
                        mkey = rapdu.substring(36, 68);
                        mylog.Write("获取到session=" + session);
                        mylog.Write("获取到skey=" + skey);
                        mylog.Write("获取到mkey=" + mkey);
                    }
                } else if (cmd8002.getCAPDU().startsWith("8032"))//SHA-1运算回复 控制数据处理
                {
                    //这里需要寻找最近的任务文件数据 id+timestamp+data(文件内容)+oper1+oper2
                    if (businessstate == 0)//领取任务状态
                    {
                        mylog.Write("APDU8032指令" + "业务状态:" + businessstate);
                        TXTReader tr = new TXTReader();
                        String data = tr.findLastFile(MainActivity.this, CmdDir);
                        if (!data.equals(""))//如果最近任务存在
                        {
                            String id = data.substring(0, 8);
                            String fragment = cmd8002.getCAPDU().substring(18, 26);
                            String timestamp = data.substring(8, 24);
                            data = data.substring(24);

                            //operator="00000000";
                            // auditor="00000000";
                            mylog.Write("sha1源数据=" + id + fragment + timestamp + new L4vtype(data).toString() + "00000000" + "00000000");
                            String sha1 = SHA1Util.shaEncode(Tools.HexString2Bytes(id + fragment + timestamp + new L4vtype(data).toString() + "00000000" + "00000000"));
                            mylog.Write("sha1=" + sha1);
                            String cmdstr = cmd8002.getCAPDU().substring(0, 34) + sha1;
                            mylog.Write("cmdstr=" + cmdstr);
                            rapdu = psam.excute(psamCard, cmdstr, err8002);
                        } else {
                            throw new Exception("任务不存在！无法完成控制数据处理");
                        }
                    } else if (businessstate == 1)//提交任务状态
                    {
                        mylog.Write("APDU8032指令" + "业务状态:" + businessstate);
                        TXTReader tr = new TXTReader();
                        String data = tr.findLastFile(MainActivity.this, DataDir);
                        if (!data.equals(""))//如果最近任务存在
                        {
                            String id = data.substring(0, 8);
                            String fragment = cmd8002.getCAPDU().substring(18, 26);//fragment
                            String timestamp = data.substring(8, 24);
                            data = data.substring(24);
                            int oper = Integer.parseInt(operator);
                            int aduit = Integer.parseInt(auditor);
                            String operhex = Tools.Bytes2HexString(HexUtil.intToBytes(oper), HexUtil.intToBytes(oper).length);
                            String aduithex = Tools.Bytes2HexString(HexUtil.intToBytes(aduit), HexUtil.intToBytes(aduit).length);
                            mylog.Write("sha1源数据=" + id + fragment + timestamp + new L4vtype(data).toString() + operhex + aduithex);
                            String sha1 = SHA1Util.shaEncode(Tools.HexString2Bytes(id + fragment + timestamp + new L4vtype(data).toString() + operhex + aduithex));
                            mylog.Write("sha1=" + sha1);
                            String cmdstr = cmd8002.getCAPDU().substring(0, 34) + sha1;
                            mylog.Write("cmdstr=" + cmdstr);
                            rapdu = psam.excute(psamCard, cmdstr, err8002);
                        } else {
                            throw new Exception("任务不存在！无法完成控制数据处理");
                        }
                    }
                } else//其他命令直接执行返回
                {
                    String cmdstr = cmd8002.getCAPDU();
                    rapdu = psam.excute(psamCard, cmdstr, err8002);
                }
                //回复通讯组件
                if (basecmd.getLength().startsWith("7"))//加密回复
                {
                    Replay8002 replay8002 = new Replay8002();
                    replay8002.setrAPDU(rapdu);
                    replay8002.setSequence(basecmd.getSequence());
                    replay8002.setPayloads(replay8002.getError() + new L4vtype(replay8002.getrAPDU()).toString());
                    String mPayloads = DESHelper.encryptStr(replay8002.getPayloads(), skey);
                    String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                    String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                    mPayloads = mPayloads + MAC;
                    replay8002.setPayloads(mPayloads);
                    int len = replay8002.getPayloads().length() / 2;
                    String hex = Integer.toHexString(len).toUpperCase();
                    replay8002.setLength("F" + MyTools.addZeroForNum(hex, 7));
                    String reply8002data = replay8002.toDataString().toUpperCase();
                    ;
                    mylog.Write("reply8002=" + reply8002data);
                    mylog.Write("reply8002长度检查=" + replay8002.checkLenth());
                    socketClient.sendData(Tools.HexString2Bytes(reply8002data));
                    mylog.Write("发送reply8002完成");

                } else//不加密回复
                {
                    Replay8002 replay8002 = new Replay8002();
                    replay8002.setrAPDU(rapdu);
                    replay8002.setSequence(basecmd.getSequence());
                    replay8002.setPayloads(replay8002.getError() + new L4vtype(replay8002.getrAPDU()).toString());
                    int len = replay8002.getPayloads().length() / 2;
                    String hex = Integer.toHexString(len).toUpperCase();
                    replay8002.setLength("8" + MyTools.addZeroForNum(hex, 7));
                    String reply8002data = replay8002.toDataString();
                    mylog.Write("reply8002=" + reply8002data);
                    mylog.Write("reply8002长度检查=" + replay8002.checkLenth());
                    socketClient.sendData(Tools.HexString2Bytes(reply8002data));
                    mylog.Write("发送reply8002完成");
                }
                tv_connectstate.setText("正在进行安全令牌验证....");
                break;
            case 1004:
                mylog.Write("收到1004指令:" + cmddata);
                String replay1004data = "80000004" + basecmd.getSequence() + "100400000000";
                socketClient.sendData(Tools.HexString2Bytes(replay1004data));
                mylog.Write("reply1004=" + replay1004data);
                mylog.Write("发送reply1004完成");
                tv_connectstate.setText("正在进行1004检查环境....");
                break;
            case 1005://同步时间
                mylog.Write("收到1005指令:" + cmddata);
                String replay1005data = "80000004" + basecmd.getSequence() + "100500000000";
                socketClient.sendData(Tools.HexString2Bytes(replay1005data));
                mylog.Write("reply1005=" + replay1005data);
                mylog.Write("发送reply1005完成");
                tv_connectstate.setText("正在进行时间同步....");
                break;
            case 2002://接收任务数据
                mylog.Write("收到2002指令:" + cmddata);
                tv_connectstate.setText("正在进行接收任务数据....");
                Cmd2002 cmd2002 = new Cmd2002(basecmd,skey);
                String cmdjason=new String(Tools.HexString2Bytes(cmd2002.getData().substring(8)), "utf-8");
                TXTWriter.writeBinFile(CmdDirF + "/" + "CMD_" + cmd2002.getId() + "_" + cmd2002.getTimestamp() + ".json", cmdjason.getBytes());
                mylog.Write("生成任务文件=" + "CMD_" + cmd2002.getId() + "_" + cmd2002.getTimestamp() + ".json");
                mylog.Write("写入任务文件内容=" + cmdjason);
                Date date = new Date(Long.parseLong(cmd2002.getTimestamp(), 16));//时间戳计算
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");
                mylog.Write(sdf.format(date));
                String replay2002data = "80000004" + basecmd.getSequence() + "200200000000";
                socketClient.sendData(Tools.HexString2Bytes(replay2002data));
                mylog.Write("reply2002=" + replay2002data);
                // mylog.Write("reply1004长度检查="+replay8002.checkLenth());
                mylog.Write("发送reply2002完成");
                tv_connectstate.setText("接收任务数据完成....");
                break;
            case 2003:
                mylog.Write("收到2003指令:" + cmddata);
                Cmd2003 cmd2003 = new Cmd2003(basecmd, skey);
                mylog.Write("要求执行ID为" + cmd2003.getId() + "的任务");
                String cmd2003payloads="00000000";
                String cmd2003mPayloads = DESHelper.encryptStr(cmd2003payloads, skey);
                String cmd2003allMAC = DESHelper.encryptStr(cmd2003mPayloads, mkey);
                String cmd2003MAC = cmd2003allMAC.substring(cmd2003allMAC.length() - 16, cmd2003allMAC.length() - 8);
                cmd2003mPayloads = cmd2003mPayloads + cmd2003MAC;
                String replay2003data = "7000000C" + basecmd.getSequence() + "2003"+cmd2003mPayloads;
                socketClient.sendData(Tools.HexString2Bytes(replay2003data));
                mylog.Write("replay2003data=" + replay2003data);
                mylog.Write("发送replay2003完成");
                tv_connectstate.setText("正在准备执行任务..."+cmd2003.getId());
                //先不等待
//                if (socketClient != null) {
//                    socketClient.disconnect();
//                    mylog.Write("正在准备执行任务..."+cmd2003.getId()+"通讯组件已断开");
//                }
                //这里开始执行业务任务
                doBusiness(cmd2003.getId());
                break;
            case 2004:
                mylog.Write("收到2004指令:" + cmddata);
                Cmd2004 cmd2004 = new Cmd2004(basecmd, skey);
                mylog.Write("收到2004指令" + cmd2004.getId() + "任务已经" + cmd2004.getComplete());
                //这里可以删除任务及数据文件
                if (cmd2004.getComplete().equals("8000"))//要求删除
                {
                    TXTReader tr = new TXTReader();
                    boolean rs = tr.delCmdDataFile(MainActivity.this, cmd2004.getId());
                    mylog.Write("删除任务=" + cmd2004.getId() + "的任务与结果文件" + rs);
                } else {
                    mylog.Write("任务未完成,不要求删除任务文件");
                }
                if(cmd2004.getComplete().equals("0000"))
                {
                    businessstate = 0;//提交完成,修改业务状态为等待任务
                    tv_connectstate.setText("任务数据提交完成...");
                   // tv_connectstate.setTextColor(getResources().getColor(R.color.red,null));
                    mylog.Write( cmd2004.getId()+"任务数据提交完成...");
                    mylog.Write( "businessstate="+businessstate);
                }
                //回复通讯组件
                if (basecmd.getLength().startsWith("7"))//加密回复
                {
                    Replay2004 replay2004 = new Replay2004();
                    replay2004.setSequence(basecmd.getSequence());
                    replay2004.setPayloads("00000000");//error
                    String mPayloads = DESHelper.encryptStr(replay2004.getPayloads(), skey);
                    String allMAC = DESHelper.encryptStr(mPayloads, mkey);
                    String MAC = allMAC.substring(allMAC.length() - 16, allMAC.length() - 8);
                    mPayloads = mPayloads + MAC;
                    replay2004.setPayloads(mPayloads);
                    int len = replay2004.getPayloads().length() / 2;
                    String hex = Integer.toHexString(len).toUpperCase();
                    replay2004.setLength("F" + MyTools.addZeroForNum(hex, 7));
                    String reply8002data = replay2004.toDataString().toUpperCase();
                    mylog.Write("reply2004=" + reply8002data);
                    mylog.Write("reply2004长度检查=" + replay2004.checkLenth());
                    socketClient.sendData(Tools.HexString2Bytes(reply8002data));
                    mylog.Write("发送reply2004完成");

                }else//不加密回复
                {
                    Replay2004 replay2004 = new Replay2004();
                    replay2004.setSequence(basecmd.getSequence());
                    replay2004.setPayloads("00000000");//error
                    int len = replay2004.getPayloads().length() / 2;
                    String hex = Integer.toHexString(len).toUpperCase();
                    replay2004.setLength("8" + MyTools.addZeroForNum(hex, 7));
                    String reply8002data = replay2004.toDataString().toUpperCase();
                    mylog.Write("reply2004=" + reply8002data);
                    mylog.Write("reply2004长度检查=" + replay2004.checkLenth());
                    socketClient.sendData(Tools.HexString2Bytes(reply8002data));
                    mylog.Write("发送reply2004完成");
                }
                break;
            case 2005://元数据同步，获取用户
                mylog.Write("收到2005指令:" + cmddata);
                businessstate = 0;//元数据同步完成,修改业务状态为等待
                Cmd2005 cmd2005 = new Cmd2005(basecmd, skey);
                String usercmdstr = new String(Tools.HexString2Bytes(cmd2005.getMetadata()), "utf-8");
                mylog.Write("收到2005用户数据:" + usercmdstr);
                UserCommand userCommand = JSONObject.parseObject(usercmdstr, UserCommand.class);
                tv_connectstate.setText("正在元数据同步...");
                if (userCommand.getUserInfoList().length > 0)//如果存在用户数据
                {
                    mylog.Write("用户数量:" + userCommand.getUserInfoList().length);
                    userMapper.delete();
                    for (User user : userCommand.getUserInfoList()) {
                        userMapper.insert(user);
                        mylog.Write("存储用户数据:" + user.getUid());

                    }
                }
                tv_connectstate.setText("正在元数据同步完成...");
                sequence = basecmd.getSequence();
                String replay2005data = "80000004" + basecmd.getSequence() + "200500000000";
                socketClient.sendData(Tools.HexString2Bytes(replay2005data));
                mylog.Write("reply2005=" + replay2005data);
                // mylog.Write("reply1004长度检查="+replay8002.checkLenth());
                mylog.Write("发送reply2005完成");
                tv_connectstate.setText("通讯组件已接入.......");
                tv_connectstate.setTextColor(getResources().getColor(R.color.green,null));
                break;
        }
    }

    private void doBusiness(String businessid) {
        if (operator.equals("00000000")) {
            showToast("操作员未登录，不可办理业务！请操作员登录...");
            return;
        }
        mylog.Write("开始办理业务ID=" + businessid);
        //首先删除所有data文件
        Util.play(1,0);//收到业务开始提醒
        TXTReader tr = new TXTReader();

        boolean deldatars = tr.delDataFile(this);
        mylog.Write("开始清除历史数据文件=" + deldatars);
        String cmd = tr.getCmdById(MainActivity.this, businessid);
        mylog.Write("查找到业务数据=" + cmd);
        JSONObject jsonObject = JSONObject.parseObject(cmd);
        int cmdcode = jsonObject.getIntValue("code");
        mylog.Write("查找到业务数据code=" + cmdcode);

        switch (cmdcode) {
            case 131073://电子签封登记业务
                Intent superscanintent = new Intent(MainActivity.this, SuperScanActivty.class);
                superscanintent.putExtra("operator", operator);
                superscanintent.putExtra("auditor", auditor);
                superscanintent.putExtra("cmddata", cmd);
                superscanintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封登记业务界面:");
                startActivity(superscanintent);
                break;
            case 131074://扫描出库业务
                Intent outScanintent=new Intent(MainActivity.this, OutScanActivity.class);
                outScanintent.putExtra("operator", operator);
                outScanintent.putExtra("auditor", auditor);
                outScanintent.putExtra("cmddata", cmd);
                outScanintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封扫描出库界面:");
                startActivity(outScanintent);
                break;
            case 131075://扫描入库业务
                Intent enterScanintent=new Intent(MainActivity.this,EnterScanActivity.class);
                enterScanintent.putExtra("operator", operator);
                enterScanintent.putExtra("auditor", auditor);
                enterScanintent.putExtra("cmddata", cmd);
                enterScanintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封扫描入库界面:");
                startActivity(enterScanintent);
                break;
            case 131076://签封交接
                Intent transferintent=new Intent(MainActivity.this,TransferActivity.class);
                transferintent.putExtra("operator", operator);
                transferintent.putExtra("auditor", auditor);
                transferintent.putExtra("cmddata", cmd);
                transferintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封签封交接界面:");
                startActivity(transferintent);
                break;
            case 131077://签封分配
                Intent allotUnPackintent=new Intent(MainActivity.this,AllotUnPackActivity.class);
                allotUnPackintent.putExtra("operator", operator);
                allotUnPackintent.putExtra("auditor", auditor);
                allotUnPackintent.putExtra("cmddata", cmd);
                allotUnPackintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封签封核对界面:");
                startActivity(allotUnPackintent);
                break;
            case 131078://签封核对
                Intent dailyCheckintent=new Intent(MainActivity.this,DailyCheckActivity.class);
                dailyCheckintent.putExtra("operator", operator);
                dailyCheckintent.putExtra("auditor", auditor);
                dailyCheckintent.putExtra("cmddata", cmd);
                dailyCheckintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封签封核对界面:");
                startActivity(dailyCheckintent);
                break;
            case 65537://封装业务
                Intent packageintent = new Intent(MainActivity.this, PackageActivity.class);
                packageintent.putExtra("operator", operator);
                packageintent.putExtra("auditor", auditor);
                packageintent.putExtra("cmddata", cmd);
                packageintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封封装业务界面:");
                startActivity(packageintent);
                break;
            case 65538://拆封业务
                Intent unpackageintent = new Intent(MainActivity.this, UnPackageActivity.class);
                unpackageintent.putExtra("operator", operator);
                unpackageintent.putExtra("auditor", auditor);
                unpackageintent.putExtra("cmddata", cmd);
                unpackageintent.putExtra("businessid", businessid);
                mylog.Write("启动电子签封拆封业务界面:");
                startActivity(unpackageintent);
                break;
        }


    }

    //发送消息
    private void socketSendMessage(String info) {
        String status = String.valueOf(socketClient.getState());
        Log.e("kcrx", "status=" + status);
        if (socketClient != null && status == "Connected") {
            mylog.Write("send>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            socketClient.sendData(info.getBytes()); // 发送byte[]消息
        }
    }

    //前台请求socket连接状态
    private void requestGameSocketConnectFlg() {
        String status = String.valueOf(socketClient.getState());
        // launcher.callExternalInterface("gameSocketFlg", status);
    }

    //前台主动断开gamesocket
    private void closeGameSocket() {
        if (socketClient != null) {
            socketClient.removeSocketClientDelegate(delegate);
            socketClient.disconnect();
        }
    }

    //两次退出返回登录
    private static final int TIME_EXIT = 2000;
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_EXIT > System.currentTimeMillis()) {
            super.onBackPressed();
            Intent loginintent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginintent);
            finish();
        } else {
            Toast.makeText(this, "再点击一次返回退出登录", Toast.LENGTH_SHORT).show();
            mBackPressed = System.currentTimeMillis();
        }
    }

    //show tips
    private Toast toast;

    private void showToast(String info) {
        if (toast == null) toast = Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT);
        else toast.setText(info);
        toast.show();
    }
    public static String stringToAscii(String value)
    {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(i != chars.length - 1)
            {
                sbu.append((byte)chars[i]);
            }
            else {
                sbu.append((byte)chars[i]);
            }
        }
        return sbu.toString();
    }
}
