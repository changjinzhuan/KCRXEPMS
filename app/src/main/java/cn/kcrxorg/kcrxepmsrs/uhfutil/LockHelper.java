package cn.kcrxorg.kcrxepmsrs.uhfutil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.BRMicro.Tools;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import cn.kcrxorg.kcrxepmsrs.Util;
import cn.kcrxorg.kcrxepmsrs.mbutil.MyLog;
import cn.kcrxorg.kcrxepmsrs.pasmutil.HexUtil;
import cn.kcrxorg.kcrxepmsrs.pasmutil.PsamError;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import cn.kcrxorg.kcrxepmsrs.pasmutil.PsamCmdUtil;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.TagEpcData;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.EpcReader;

public  class LockHelper {
    static String logtag = "kcrxlog";

    private static PsamCmdUtil psam;

    private static int psamCard = 1;//PSAM卡座号

    public static UHFRManager mUhfrManager;//uhf


    public static int UHF_WHAT=1;
    private final int LOCK_WHAT=2;
    private final int LOCK_LOG_WHAT=3;

    private Handler mHandler;
    private MyLog myLog;
    public LockHelper(Handler handler,PsamCmdUtil mpsam,UHFRManager UhfrManager)
    {
        mHandler=handler;
        psam=mpsam;
        mUhfrManager=UhfrManager;
    }


    public static LOCK_ERR Lockws(String operator1str, String operator2str,boolean lock) {

        //启动天线读取
        mUhfrManager = UHFRManager.getInstance();

        mUhfrManager.setCancleFastMode();

        List<Reader.TAGINFO> list1;
        list1 = mUhfrManager.tagInventoryByTimer((short) 50);
        //过滤非法及未上电标签
        String epc = "";
        String hasElecEpc="";
        List<Reader.TAGINFO> list2;

        if (list1 != null && list1.size() > 0) {
            list2=new ArrayList<Reader.TAGINFO>();
            for (Reader.TAGINFO tfs : list1)
            {
                byte[] epcdata = tfs.EpcId;
                epc = Tools.Bytes2HexString(epcdata, epcdata.length);

                TagEpcData ted= EpcReader.readEpc(epc);
                sendMes("读取到EPC"+epc);
                sendMes("包号:"+ted.getTagid()+" 上电状态:"+ted.getHasElec()+" 锁状态:"+ted.getLockstuts());
                if(ted!=null)
                {
                    if(ted.getTagid()>1000000000L&&ted.getHasElec())//如果存在大于0的包号并且已经上电
                    {
                        list2.add(tfs);
                        hasElecEpc=epc;
                    }
                }
            }
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR;
        }
        //查找信号最强的标签
        Reader.TAGINFO Maxtfs=null;
        if (list2 != null && list2.size() > 0)
        {
            int rssi=list2.get(0).RSSI;
            int maxnum=0;
            for(int i=0;i<list2.size();i++)
            {
                if(list2.get(i).RSSI>rssi)
                {
                    rssi=list2.get(i).RSSI;
                    maxnum=i;
                }
            }
            Maxtfs=list2.get(maxnum);
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR;
        }
        byte[] Maxepcdata = Maxtfs.EpcId;
        String Maxepc= Tools.Bytes2HexString(Maxepcdata, Maxepcdata.length);
        sendMes("获取到最佳信号标签为:"+Maxepc+" 信号:"+Maxtfs.RSSI);
        sendMes("包号"+EpcReader.readEpc(Maxepc).getTagid());
        //关闭UHF天线
        mUhfrManager.stopTagInventory();
        //关闭读取延时200ms;
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUhfrManager.close();
        mUhfrManager = null;

       //打开PSAM卡
        psam = new PsamCmdUtil();
        if (psam.openRfid() != null) {
            sendMes( "打开PSAM模块成功");
        }
        PsamError err = new PsamError();
        byte[] result = null;
        //复位
        result = psam.resetCard(psamCard);
        if (result != null) {
            sendMes( "复位PSAM卡：" + Tools.Bytes2HexString(result, result.length));
        } else {
            sendMes( "复位PSAM卡失败");
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //用户验证
        boolean flag = psam.verifyUser(psamCard,
                Tools.HexString2Bytes("5053414D49303031"),
                Tools.HexString2Bytes("4D494D49535F5053414D5F55534552"), err);
        if (flag) {
            sendMes( "用户验证成功：");
        } else {
            sendMes("用户验证失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //获取RFID访问密码
        String RfidPwd = "";
        result = psam.getRFIDPassword(psamCard, Tools.HexString2Bytes(Maxepc), err);
        if (result != null) {
            sendMes("获取RFID密码成功：" + Tools.Bytes2HexString(result, result.length));
            RfidPwd = Tools.Bytes2HexString(result, result.length);
        } else {

            sendMes("获取RFID密码失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //获取加密命令
        String cmd = "";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeStr=sdf.format(new Date());
        if (lock) {
            result = psam.genCloseElsCmd(1,
                    Tools.HexString2Bytes(Maxepc),
                    20000,
                    Tools.HexString2Bytes(operator1str),
                    Tools.HexString2Bytes(operator2str),
                    DateTimeStr,
                    err
            );
        } else {
            result = psam.genOpenElsCmd(1,
                    Tools.HexString2Bytes(Maxepc),
                    0,
                    Tools.HexString2Bytes(operator1str),
                    Tools.HexString2Bytes(operator2str),
                    DateTimeStr,
                    err
            );
        }
        if (result != null) {
            cmd = Tools.Bytes2HexString(result, result.length);
            sendMes("获取开锁指令成功：" + Tools.Bytes2HexString(result, result.length));
        } else {

            sendMes("获取指令失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        psam.closeRfid();
        sendMes("关闭PSAM卡");

        //开始写入指令操作
        mUhfrManager = UHFRManager.getInstance();
        mUhfrManager.setCancleFastMode();

        int mbank = 3;//user区
        int startaddr = 76;
        int datalen = "6618B05CF3E99DEC4EE0747D83890C40FA01075AE5976A32DD51".length() / 2;
        byte rdata[] = new byte[datalen];
        byte[] password = Tools.HexString2Bytes(RfidPwd);
        short timeout = 500;
        //读取指令区判断是否可操作
        Reader.READER_ERR reader_err = mUhfrManager.getTagData(mbank, startaddr, datalen, rdata, password, timeout);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendMes("指令区读取成功:" + Tools.Bytes2HexString(rdata, rdata.length));
        } else {
            sendMes("指令区读取失败!");
            return LOCK_ERR.LOCK_READUSER_ERR;
        }

        if(!Tools.Bytes2HexString(rdata, rdata.length).startsWith("BB"))//如果不是BB开头，不可以操作写入指令
        {
            sendMes("不是BB开头，不可以操作写入指令!");
            return LOCK_ERR.LOCK_LOCKRRS_ERR;
        }
        //指令区可操作，开始写入
        byte[] cmdB = Tools.HexString2Bytes("6618" + cmd);
        sendMes("写入指令区:" + ("6618" + cmd) + " 长度:" + cmdB.length);
        mUhfrManager.setPower(10, 10);
        sendMes("设置读取功率：" + 10 + " 写入功率：" + 10);
        reader_err = mUhfrManager.writeTagData((char) mbank, startaddr, cmdB, cmdB.length, password, timeout);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendMes("指令区写入成功:");
        } else {
            sendMes("指令区写入失败!");
            return LOCK_ERR.LOCK_WRITEUSER_ERR;
        }
        //写入成功，开始判断操作结果

        mUhfrManager.setPower(30,10);
        //设置只盘点当前标签
        byte[] fdata=Tools.HexString2Bytes(Maxepc.substring(0,8));
        int fbank=1;
        int fstartaddr=0;
        boolean matching=true;
     //   mUhfrManager.setInventoryFilter(fdata,fbank,fstartaddr,matching);
        //盘点5秒钟

        List<Reader.TAGINFO> listrs;
        listrs=  mUhfrManager.tagInventoryByTimer((short)5000);
        if(listrs!=null&&listrs.size()>0)
        {
            sendMes("盘点到结果标签"+listrs.size()+"次");
            for (Reader.TAGINFO tfs : listrs)
            {
                byte[] epcdata = tfs.EpcId;
                String epcrs = Tools.Bytes2HexString(epcdata, epcdata.length);

                sendMes("Maxepc="+Maxepc);
                sendMes("epcrs="+epcrs);
                if(Maxepc.substring(0,8).equals(epcrs.substring(0,8)))
                {
                    TagEpcData ted= EpcReader.readEpc(epcrs);
                    sendMes("盘存到操作后标签："+epcrs);
                    sendMes("包号:"+ted.getTagid());
                    sendMes("状态:"+(ted.getHasElec()?"上电":"未上电 ")+(ted.getLockstuts().equals("Lock")?"关锁":"开锁"));
                    String lockstatus="";
                    if(lock)
                    {
                        lockstatus="Lock";

                    }else
                    {
                        lockstatus="unLock";
                    }
                    if(ted.getHasElec()==false&&ted.getLockstuts().equals(lockstatus))//状态正常
                    {
                        sendMes("锁状态正常！");
                        Util.play(2, 0);
                        return LOCK_ERR.LOCK_OK;
                    }
                }


            }
        }else
        {
            sendMes("未盘点到结果标签");
            return LOCK_ERR.LOCK_LOCKRRS_ERR;
        }
        return LOCK_ERR.LOCK_LOCKRRS_ERR;
    }


    public static LOCK_ERR RFID_Active(byte Volume)
    {
        //启动天线读取
        mUhfrManager.setCancleInventoryFilter();
        mUhfrManager.setCancleFastMode();
        List<Reader.TAGINFO> list1;
        list1 = mUhfrManager.tagInventoryByTimer((short) 50);
        //过滤非法及未上电标签
        String epc = "";
        String hasElecEpc="";
        List<Reader.TAGINFO> list2;

        if (list1 != null && list1.size() > 0) {
            list2=new ArrayList<Reader.TAGINFO>();
            for (Reader.TAGINFO tfs : list1)
            {
                byte[] epcdata = tfs.EpcId;
                epc = Tools.Bytes2HexString(epcdata, epcdata.length);

                TagEpcData ted= EpcReader.readEpc(epc);
                sendMes("读取到EPC"+epc);
                sendMes("包号:"+ted.getTagid()+" 上电状态:"+ted.getHasElec());
                if(ted!=null)
                {
                    if(ted.getTagid()>1000000000L&&ted.getHasElec())//如果存在大于0的包号并且已经上电
                    {
                        list2.add(tfs);
                        hasElecEpc=epc;
                    }
                }
            }
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR;
        }
        //查找信号最强的标签
        Reader.TAGINFO Maxtfs=null;
        if (list2 != null && list2.size() > 0)
        {
            int rssi=list2.get(0).RSSI;
            int maxnum=0;
            for(int i=0;i<list2.size();i++)
            {
                if(list2.get(i).RSSI>rssi)
                {
                    rssi=list2.get(i).RSSI;
                    maxnum=i;
                }
            }
            Maxtfs=list2.get(maxnum);
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR;
        }
        byte[] Maxepcdata = Maxtfs.EpcId;
        String Maxepc= Tools.Bytes2HexString(Maxepcdata, Maxepcdata.length);
        sendMes("获取到最佳信号标签为:"+Maxepc+" 信号:"+Maxtfs.RSSI);
        sendMes("包号"+EpcReader.readEpc(Maxepc).getTagid());
        //关闭UHF天线
        mUhfrManager.stopTagInventory();
        //关闭读取延时200ms;
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUhfrManager.close();
        mUhfrManager = null;

        //打开PSAM卡
        psam = new PsamCmdUtil();
        if (psam.openRfid() != null) {
            sendMes( "打开PSAM模块成功");
        }
        PsamError err = new PsamError();
        byte[] result = null;
        //复位
        result = psam.resetCard(psamCard);
        if (result != null) {
            sendMes( "复位PSAM卡：" + Tools.Bytes2HexString(result, result.length));
        } else {
            sendMes( "复位PSAM卡失败");
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //用户验证
        boolean flag = psam.verifyUser(psamCard,
                Tools.HexString2Bytes("5053414D49303031"),
                Tools.HexString2Bytes("4D494D49535F5053414D5F55534552"), err);
        if (flag) {
            sendMes( "用户验证成功：");
        } else {
            sendMes("用户验证失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //获取RFID访问密码
        String RfidPwd = "";
        result = psam.getRFIDPassword(psamCard, Tools.HexString2Bytes(Maxepc), err);
        if (result != null) {
            sendMes("获取RFID密码成功：" + Tools.Bytes2HexString(result, result.length));
            RfidPwd = Tools.Bytes2HexString(result, result.length);
        } else {

            sendMes("获取RFID密码失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //获取激活加密命令
        String cmd = "";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeStr=sdf.format(new Date());

            result = psam.genTagActiveElsCmd(1,
                    Tools.HexString2Bytes(Maxepc),
                    Volume,
                    err);


        if (result != null) {
            cmd = Tools.Bytes2HexString(result, result.length);
            sendMes("获取激活指令成功：" + Tools.Bytes2HexString(result, result.length));
        } else {
            sendMes("获取激活指令失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        psam.closeRfid();
        sendMes("关闭PSAM卡");

        //开始写入指令操作
        mUhfrManager = UHFRManager.getInstance();
        mUhfrManager.setCancleFastMode();

        int mbank = 3;//user区
        int startaddr = 76;
        int datalen = "6618B05CF3E99DEC4EE0747D83890C40FA01075AE5976A32DD51".length() / 2;
        byte rdata[] = new byte[datalen];
        byte[] password = Tools.HexString2Bytes(RfidPwd);
        short timeout = 500;
        //读取指令区判断是否可操作
        Reader.READER_ERR reader_err = mUhfrManager.getTagData(mbank, startaddr, datalen, rdata, password, timeout);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendMes("指令区读取成功:" + Tools.Bytes2HexString(rdata, rdata.length));
        } else {
            sendMes("指令区读取失败!");
            return LOCK_ERR.LOCK_READUSER_ERR;
        }

        if(!Tools.Bytes2HexString(rdata, rdata.length).startsWith("BB"))//如果不是BB开头，不可以操作写入指令
        {
            sendMes("不是BB开头，不可以操作写入指令!");
            return LOCK_ERR.LOCK_LOCKRRS_ERR;
        }
        //指令区可操作，开始写入
        byte[] cmdB = Tools.HexString2Bytes("66"+intToHex(cmd.length())+ cmd);
        sendMes("写入指令区:" + "66"+intToHex(cmd.length())+ cmd + " 长度:" + cmdB.length);
        mUhfrManager.setPower(10, 10);
        sendMes("设置读取功率：" + 10 + " 写入功率：" + 10);
        reader_err = mUhfrManager.writeTagData((char) mbank, startaddr, cmdB, cmdB.length, password, timeout);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendMes("指令区写入成功:");
            return LOCK_ERR.LOCK_OK;
        } else {
            sendMes("指令区写入失败!");
            return LOCK_ERR.LOCK_WRITEUSER_ERR;
        }
//        //写入成功，开始判断操作结果//激活指令不判断结果
//
//        mUhfrManager.setPower(30,10);
//        //设置只盘点当前标签
//        byte[] fdata=Tools.HexString2Bytes(Maxepc.substring(0,8));
//        int fbank=1;
//        int fstartaddr=0;
//        boolean matching=true;
//        mUhfrManager.setInventoryFilter(fdata,fbank,fstartaddr,matching);
//        //盘点5秒钟
//
//        List<Reader.TAGINFO> listrs;
//        listrs=  mUhfrManager.tagInventoryByTimer((short)5000);
//        if(listrs!=null&&listrs.size()>0)
//        {
//            sendMes("盘点到结果标签"+listrs.size()+"次");
//            for (Reader.TAGINFO tfs : listrs)
//            {
//                byte[] epcdata = tfs.EpcId;
//                String epcrs = Tools.Bytes2HexString(epcdata, epcdata.length);
//                TagEpcData ted= EpcReader.readEpc(epcrs);
//                sendMes("盘存到操作后标签："+epc);
//                sendMes("包号:"+ted.getTagid());
//                sendMes("激活后券别");
//                String lockstatus="";
//
//                if(ted.getHasElec()==false&&ted.getLockstuts().equals(lockstatus))//状态正常
//                {
//                    sendMes("锁状态正常！");
//                    return LOCK_ERR.LOCK_OK;
//                }
//
//            }
//        }else
//        {
//            sendMes("未盘点到结果标签");
//            return LOCK_ERR.LOCK_LOCKRRS_ERR;
//        }

    }

    public static String RFID_Trace()
    {
        //启动天线读取
        //启动天线读取
        mUhfrManager.setCancleInventoryFilter();
        mUhfrManager.setPower(5,5);
        List<Reader.TAGINFO> list1;
        list1 = mUhfrManager.tagInventoryByTimer((short) 100);
        //过滤非法及未上电标签
        String epc = "";
        String hasElecEpc="";
        List<Reader.TAGINFO> list2;

        if (list1 != null && list1.size() > 0) {
            list2=new ArrayList<Reader.TAGINFO>();
            for (Reader.TAGINFO tfs : list1)
            {
                byte[] epcdata = tfs.EpcId;
                epc = Tools.Bytes2HexString(epcdata, epcdata.length);

                TagEpcData ted= EpcReader.readEpc(epc);
                sendMes("读取到EPC"+epc);

                if(ted!=null)
                {
                    sendMes("包号:"+ted.getTagid()+" 上电状态:"+ted.getHasElec());
                   // if(ted.getTagid()>1000000000L&&ted.getHasElec())//如果存在大于0的包号并且已经上电
                    if(ted.getTagid()>1000000000L)//如果存在大于0的包号 不需要上电
                    {
                        list2.add(tfs);
                        hasElecEpc=epc;
                    }
                }
            }
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR+"";
        }
        //查找信号最强的标签
        Reader.TAGINFO Maxtfs=null;
        if (list2 != null && list2.size() > 0)
        {
            int rssi=list2.get(0).RSSI;
            int maxnum=0;
            for(int i=0;i<list2.size();i++)
            {
                if(list2.get(i).RSSI>rssi)
                {
                    rssi=list2.get(i).RSSI;
                    maxnum=i;
                }
            }
            Maxtfs=list2.get(maxnum);
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR+"";
        }
        byte[] Maxepcdata = Maxtfs.EpcId;
        String Maxepc= Tools.Bytes2HexString(Maxepcdata, Maxepcdata.length);
        sendMes("获取到最佳信号标签为:"+Maxepc+" 信号:"+Maxtfs.RSSI);
        sendMes("包号"+EpcReader.readEpc(Maxepc).getTagid());
        PsamError err = new PsamError();
        byte[] result = null;
        //复位
        result = psam.resetCard(psamCard);
        if (result != null) {
            sendMes( "复位PSAM卡：" + Tools.Bytes2HexString(result, result.length));
        } else {
            sendMes( "复位PSAM卡失败");
            return LOCK_ERR.LOCK_PSAM_ERR+"";
        }
        //用户验证
        boolean flag = psam.verifyUser(psamCard,
                Tools.HexString2Bytes("5053414D49303031"),
                Tools.HexString2Bytes("4D494D49535F5053414D5F55534552"), err);
        if (flag) {
            sendMes( "用户验证成功：");
        } else {
            sendMes("用户验证失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR+"";
        }
        //获取RFID访问密码
        String RfidPwd = "";
        result = psam.getRFIDPassword(psamCard, Tools.HexString2Bytes(Maxepc), err);
        if (result != null) {
            sendMes("获取RFID密码成功：" + Tools.Bytes2HexString(result, result.length));
            RfidPwd = Tools.Bytes2HexString(result, result.length);
        } else {

            sendMes("获取RFID密码失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR+"";
        }
     //读取user区
        mUhfrManager.setPower(5,5);
        int mbank = 3;//user区
        int startaddr =0;
        int datalen = 64;
        byte[] rdata = new byte[datalen];
        byte[] password = Tools.HexString2Bytes(RfidPwd);
        short timeout = 500;

        sendMes("盘点过滤值:" + hasElecEpc.substring(0, 8));
        byte[] fdata = Tools.HexString2Bytes(hasElecEpc.substring(0, 8));
        int fbank = 1;
        int fstartaddr = 2;
        boolean matching = true;

        StringBuilder stringBuilder=new StringBuilder();


        for(int i=0;i<2;i++)
        {
            sendMes("第"+(1+i)+"次读取");
            if(i==3)
            {
                 datalen = 12;
                 rdata = new byte[datalen];
            }
          //  Reader.READER_ERR reader_err = mUhfrManager.getTagData(mbank, startaddr+(i*64)/2, datalen, rdata, password, timeout);
            byte[] userdata = mUhfrManager.getTagDataByFilter(mbank, startaddr+(i*64), datalen, password, timeout, fdata, fbank, fstartaddr, matching);
            if (userdata != null) {
                sendMes("用户区读取成功:" + Tools.Bytes2HexString(userdata, userdata.length));
                stringBuilder.append(Tools.Bytes2HexString(userdata, userdata.length));
            } else {
                sendMes("用户区读取失败!");
                return LOCK_ERR.LOCK_READUSER_ERR+"";
            }
        }
        String  reTraceData=stringBuilder.toString()+":"+Maxepc;
        return reTraceData;
    }

    public static LOCK_ERR RFID_Recovery(String operator1str, String operator2str)
    {
        //启动天线读取
        mUhfrManager = UHFRManager.getInstance();

        mUhfrManager.setCancleFastMode();

        List<Reader.TAGINFO> list1;
        list1 = mUhfrManager.tagInventoryByTimer((short) 50);
        //过滤非法及未上电标签
        String epc = "";
        String hasElecEpc="";
        List<Reader.TAGINFO> list2;

        if (list1 != null && list1.size() > 0) {
            list2=new ArrayList<Reader.TAGINFO>();
            for (Reader.TAGINFO tfs : list1)
            {
                byte[] epcdata = tfs.EpcId;
                epc = Tools.Bytes2HexString(epcdata, epcdata.length);

                TagEpcData ted= EpcReader.readEpc(epc);
                sendMes("读取到EPC"+epc);
                sendMes("包号:"+ted.getTagid()+" 上电状态:"+ted.getHasElec());
                if(ted!=null)
                {
                    if(ted.getTagid()>1000000000L&&ted.getHasElec())//如果存在大于0的包号并且已经上电
                    {
                        list2.add(tfs);
                        hasElecEpc=epc;
                    }
                }
            }
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR;
        }
        //查找信号最强的标签
        Reader.TAGINFO Maxtfs=null;
        if (list2 != null && list2.size() > 0)
        {
            int rssi=list2.get(0).RSSI;
            int maxnum=0;
            for(int i=0;i<list2.size();i++)
            {
                if(list2.get(i).RSSI>rssi)
                {
                    rssi=list2.get(i).RSSI;
                    maxnum=i;
                }
            }
            Maxtfs=list2.get(maxnum);
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR;
        }
        byte[] Maxepcdata = Maxtfs.EpcId;
        String Maxepc= Tools.Bytes2HexString(Maxepcdata, Maxepcdata.length);
        sendMes("获取到最佳信号标签为:"+Maxepc+" 信号:"+Maxtfs.RSSI);
        sendMes("包号"+EpcReader.readEpc(Maxepc).getTagid());
        //关闭UHF天线
        mUhfrManager.stopTagInventory();
        //关闭读取延时200ms;
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUhfrManager.close();
        mUhfrManager = null;

        //打开PSAM卡
        psam = new PsamCmdUtil();
        if (psam.openRfid() != null) {
            sendMes( "打开PSAM模块成功");
        }
        PsamError err = new PsamError();
        byte[] result = null;
        //复位
        result = psam.resetCard(psamCard);
        if (result != null) {
            sendMes( "复位PSAM卡：" + Tools.Bytes2HexString(result, result.length));
        } else {
            sendMes( "复位PSAM卡失败");
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //用户验证
        boolean flag = psam.verifyUser(psamCard,
                Tools.HexString2Bytes("5053414D49303031"),
                Tools.HexString2Bytes("4D494D49535F5053414D5F55534552"), err);
        if (flag) {
            sendMes( "用户验证成功：");
        } else {
            sendMes("用户验证失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //获取RFID访问密码
        String RfidPwd = "";
        result = psam.getRFIDPassword(psamCard, Tools.HexString2Bytes(Maxepc), err);
        if (result != null) {
            sendMes("获取RFID密码成功：" + Tools.Bytes2HexString(result, result.length));
            RfidPwd = Tools.Bytes2HexString(result, result.length);
        } else {

            sendMes("获取RFID密码失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }

        //获取恢复加密命令
        String cmd = "";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeStr=sdf.format(new Date());

        result = psam.genRecoverElsCmd(psamCard,Tools.HexString2Bytes(Maxepc),0,Tools.HexString2Bytes(operator1str),Tools.HexString2Bytes(operator2str),DateTimeStr,err);


        if (result != null) {
            cmd = Tools.Bytes2HexString(result, result.length);
            sendMes("获取恢复指令成功：" + Tools.Bytes2HexString(result, result.length));
        } else {
            sendMes("获取恢复指令失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        psam.closeRfid();
        sendMes("关闭PSAM卡");

        //开始写入指令操作
        mUhfrManager = UHFRManager.getInstance();
        mUhfrManager.setCancleFastMode();

        int mbank = 3;//user区
        int startaddr = 76;
        int datalen = "6618B05CF3E99DEC4EE0747D83890C40FA01075AE5976A32DD51".length() / 2;
        byte rdata[] = new byte[datalen];
        byte[] password = Tools.HexString2Bytes(RfidPwd);
        short timeout = 500;
        //读取指令区判断是否可操作
        Reader.READER_ERR reader_err = mUhfrManager.getTagData(mbank, startaddr, datalen, rdata, password, timeout);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendMes("指令区读取成功:" + Tools.Bytes2HexString(rdata, rdata.length));
        } else {
            sendMes("指令区读取失败!");
            return LOCK_ERR.LOCK_READUSER_ERR;
        }

        if(!Tools.Bytes2HexString(rdata, rdata.length).startsWith("BB"))//如果不是BB开头，不可以操作写入指令
        {
            sendMes("不是BB开头，不可以操作写入指令!");
            return LOCK_ERR.LOCK_LOCKRRS_ERR;
        }
        //指令区可操作，开始写入
        byte[] cmdB = Tools.HexString2Bytes("66"+intToHex(cmd.length())+ cmd);
        sendMes("写入指令区:" + "66"+intToHex(cmd.length())+ cmd+ " 长度:" + cmdB.length);
        mUhfrManager.setPower(10, 10);
        sendMes("设置读取功率：" + 10 + " 写入功率：" + 10);
        reader_err = mUhfrManager.writeTagData((char) mbank, startaddr, cmdB, cmdB.length, password, timeout);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendMes("指令区写入成功:");
            return LOCK_ERR.LOCK_OK;
        } else {
            sendMes("指令区写入失败!");
            return LOCK_ERR.LOCK_WRITEUSER_ERR;
        }
    }

    public static LOCK_ERR RFID_Delete(String operator1str, String operator2str)
    {
        //启动天线读取
        mUhfrManager = UHFRManager.getInstance();

        mUhfrManager.setCancleFastMode();

        List<Reader.TAGINFO> list1;
        list1 = mUhfrManager.tagInventoryByTimer((short) 50);
        //过滤非法及未上电标签
        String epc = "";
        String hasElecEpc="";
        List<Reader.TAGINFO> list2;

        if (list1 != null && list1.size() > 0) {
            list2=new ArrayList<Reader.TAGINFO>();
            for (Reader.TAGINFO tfs : list1)
            {
                byte[] epcdata = tfs.EpcId;
                epc = Tools.Bytes2HexString(epcdata, epcdata.length);

                TagEpcData ted= EpcReader.readEpc(epc);
                sendMes("读取到EPC"+epc);
                sendMes("包号:"+ted.getTagid()+" 上电状态:"+ted.getHasElec());
                if(ted!=null)
                {
                    if(ted.getTagid()>1000000000L&&ted.getHasElec())//如果存在大于0的包号并且已经上电
                    {
                        list2.add(tfs);
                        hasElecEpc=epc;
                    }
                }
            }
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR;
        }
        //查找信号最强的标签
        Reader.TAGINFO Maxtfs=null;
        if (list2 != null && list2.size() > 0)
        {
            int rssi=list2.get(0).RSSI;
            int maxnum=0;
            for(int i=0;i<list2.size();i++)
            {
                if(list2.get(i).RSSI>rssi)
                {
                    rssi=list2.get(i).RSSI;
                    maxnum=i;
                }
            }
            Maxtfs=list2.get(maxnum);
        }else
        {
            return LOCK_ERR.LOCK_NOEPC_ERR;
        }
        byte[] Maxepcdata = Maxtfs.EpcId;
        String Maxepc= Tools.Bytes2HexString(Maxepcdata, Maxepcdata.length);
        sendMes("获取到最佳信号标签为:"+Maxepc+" 信号:"+Maxtfs.RSSI);
        sendMes("包号"+EpcReader.readEpc(Maxepc).getTagid());
        //关闭UHF天线
        mUhfrManager.stopTagInventory();
        //关闭读取延时200ms;
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUhfrManager.close();
        mUhfrManager = null;

        //打开PSAM卡
        psam = new PsamCmdUtil();
        if (psam.openRfid() != null) {
            sendMes( "打开PSAM模块成功");
        }
        PsamError err = new PsamError();
        byte[] result = null;
        //复位
        result = psam.resetCard(psamCard);
        if (result != null) {
            sendMes( "复位PSAM卡：" + Tools.Bytes2HexString(result, result.length));
        } else {
            sendMes( "复位PSAM卡失败");
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //用户验证
        boolean flag = psam.verifyUser(psamCard,
                Tools.HexString2Bytes("5053414D49303031"),
                Tools.HexString2Bytes("4D494D49535F5053414D5F55534552"), err);
        if (flag) {
            sendMes( "用户验证成功：");
        } else {
            sendMes("用户验证失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        //获取RFID访问密码
        String RfidPwd = "";
        result = psam.getRFIDPassword(psamCard, Tools.HexString2Bytes(Maxepc), err);
        if (result != null) {
            sendMes("获取RFID密码成功：" + Tools.Bytes2HexString(result, result.length));
            RfidPwd = Tools.Bytes2HexString(result, result.length);
        } else {

            sendMes("获取RFID密码失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }

        //获取删除加密命令
        String cmd = "";
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String DateTimeStr=sdf.format(new Date());

        result = psam.genClenLogElsCmd(psamCard,Tools.HexString2Bytes(Maxepc),Tools.HexString2Bytes(operator1str),Tools.HexString2Bytes(operator2str),DateTimeStr,err);


        if (result != null) {
            cmd = Tools.Bytes2HexString(result, result.length);
            sendMes("获取删除指令成功：" + Tools.Bytes2HexString(result, result.length));
        } else {
            sendMes("获取删除指令失败，错误码：" + err.getErrCode());
            return LOCK_ERR.LOCK_PSAM_ERR;
        }
        psam.closeRfid();
        sendMes("关闭PSAM卡");

        //开始写入指令操作
        mUhfrManager = UHFRManager.getInstance();
        mUhfrManager.setCancleFastMode();

        int mbank = 3;//user区
        int startaddr = 76;
        int datalen = "6618B05CF3E99DEC4EE0747D83890C40FA01075AE5976A32DD51".length() / 2;
        byte rdata[] = new byte[datalen];
        byte[] password = Tools.HexString2Bytes(RfidPwd);
        short timeout = 500;
        //读取指令区判断是否可操作
        Reader.READER_ERR reader_err = mUhfrManager.getTagData(mbank, startaddr, datalen, rdata, password, timeout);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendMes("指令区读取成功:" + Tools.Bytes2HexString(rdata, rdata.length));
        } else {
            sendMes("指令区读取失败!");
            return LOCK_ERR.LOCK_READUSER_ERR;
        }

        if(!Tools.Bytes2HexString(rdata, rdata.length).startsWith("BB"))//如果不是BB开头，不可以操作写入指令
        {
            sendMes("不是BB开头，不可以操作写入指令!");
            return LOCK_ERR.LOCK_LOCKRRS_ERR;
        }
        //指令区可操作，开始写入
        byte[] cmdB = Tools.HexString2Bytes("66"+intToHex(cmd.length())+ cmd);
        sendMes("写入指令区:" + "66"+intToHex(cmd.length())+ cmd+ " 长度:" + cmdB.length);
        mUhfrManager.setPower(10, 10);
        sendMes("设置读取功率：" + 10 + " 写入功率：" + 10);
        reader_err = mUhfrManager.writeTagData((char) mbank, startaddr, cmdB, cmdB.length, password, timeout);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendMes("指令区写入成功:");
            return LOCK_ERR.LOCK_OK;
        } else {
            sendMes("指令区写入失败!");
            return LOCK_ERR.LOCK_WRITEUSER_ERR;
        }
    }

    String epc = "481F22811100008200662700";
    public  void operateLockGetrs(Handler mHandler, List<String> tagidlist, String operatorstr, String auditorstr, int num, boolean lock)
    {
        sendLockMes(mHandler, "开始执行关锁*******",LOCK_LOG_WHAT);
        sendLockMes(mHandler, "1、开始寻锁",LOCK_LOG_WHAT);
        mUhfrManager.setCancleInventoryFilter();
//        mUhfrManager = UHFRManager.getInstance();
        mUhfrManager.setPower(5, 5);
        List<Reader.TAGINFO> list1;
        list1 = mUhfrManager.tagInventoryByTimer((short) 50);

        String hasElecEpc = "";

        if (list1 != null && list1.size()==1) {//只能读取1个标签
            for (Reader.TAGINFO tfs : list1) {
                byte[] epcdata = tfs.EpcId;
                epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                TagEpcData ted = EpcReader.readEpc(epc);
                if (ted != null) {
                    if (ted.getHasElec())//如果是上电标签
                    {
                       if(checkCMDList(ted.getTagid(),tagidlist))
                       {
                           hasElecEpc=epc;
                       }else
                       {
                           sendLockMes(mHandler, "签封"+ted.getTagid()+"不在任务列表",LOCK_WHAT);
                           return;
                       }

                    }
                }
            }
        } else {
            sendLockMes(mHandler, "未读取到电子签封",LOCK_WHAT);
            return;
        }

        if (hasElecEpc == null || hasElecEpc.equals("")) {
            sendLockMes(mHandler, "未读取到上电的标签",LOCK_WHAT);
            return ;
        }
        sendLockMes(mHandler, "扫描已停止！",LOCK_LOG_WHAT);
        sendLockMes(mHandler, "2、开始操作PSAM获命令...",LOCK_LOG_WHAT);

        //开始PSAM操作
        PsamError err = new PsamError();
        byte[] result = null;
        //复位
        result = psam.resetCard(psamCard);
        if (result != null) {
            sendLockMes(mHandler, "复位PSAM卡：" + Tools.Bytes2HexString(result, result.length),LOCK_LOG_WHAT);
        } else {
            sendLockMes(mHandler, "复位PSAM卡失败!",LOCK_WHAT);
            return ;
        }
        //用户验证
        boolean flag = psam.verifyUser(psamCard,
                Tools.HexString2Bytes("5053414D49303031"),
                Tools.HexString2Bytes("4D494D49535F5053414D5F55534552"), err);
        if (flag) {
            sendLockMes(mHandler, "用户验证成功：",LOCK_LOG_WHAT);
        } else {
            sendLockMes(mHandler, "用户验证失败，错误码：" + err.getErrCode(),LOCK_WHAT);
            //Util.play(3, 0);
            return ;
        }
        //获取RFID访问密码
        String RfidPwd = "";
        result = psam.getRFIDPassword(psamCard, Tools.HexString2Bytes(hasElecEpc), err);
        if (result != null) {
            sendLockMes(mHandler, "获取RFID密码成功：" + Tools.Bytes2HexString(result, result.length),LOCK_LOG_WHAT);
            RfidPwd = Tools.Bytes2HexString(result, result.length);
        } else {
            sendLockMes(mHandler, "获取RFID密码失败，错误码：" + err.getErrCode(),LOCK_WHAT);
            return;
        }

        //获取加密命令
        String cmd = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime=sdf.format(new Date());
        if (lock) {
            result = psam.genCloseWriteElsCmd(1,
                    Tools.HexString2Bytes(hasElecEpc),
                    num,
                    HexUtil.intToBytes(Integer.parseInt(operatorstr)),
                    HexUtil.intToBytes(Integer.parseInt(auditorstr)),
                    datetime,
                    err
            );
        } else {
            result = psam.genOpenWriteElsCmd(1,
                    Tools.HexString2Bytes(hasElecEpc),
                    0,
                    HexUtil.intToBytes(Integer.parseInt(operatorstr)),
                    HexUtil.intToBytes(Integer.parseInt(auditorstr)),
                    datetime,
                    err
            );
        }
        if (result != null) {
            cmd = Tools.Bytes2HexString(result, result.length);
            sendLockMes(mHandler, "获取开关锁指令成功：" + Tools.Bytes2HexString(result, result.length),LOCK_LOG_WHAT);
        } else {
           // sendMes(mHandler, "获取指令失败，错误码：" + err.getErrCode());
            sendLockMes(mHandler, "获取开关锁指令失败，错误码：" + err.getErrCode(),LOCK_WHAT);
            return;
        }
        sendLockMes(mHandler, "3、开始写入指令...",LOCK_LOG_WHAT);

        mUhfrManager.setPower(5, 10);
        sendLockMes(mHandler, "设置读取功率：" + 5 + " 写入功率：" + 10,LOCK_LOG_WHAT);
        int mbank = 3;//user区
        int startaddr = 76;
        int datalen = "6618B05CF3E99DEC4EE0747D83890C40FA01075AE5976A32DD51".length() / 2;
        byte rdata[] = new byte[datalen];
        byte[] password = Tools.HexString2Bytes(RfidPwd);
        short timeout =100;//读写100ms
        sendLockMes(mHandler, "盘点过滤值:" + hasElecEpc.substring(0, 8),LOCK_LOG_WHAT);
        byte[] fdata = Tools.HexString2Bytes(hasElecEpc.substring(0, 8));
        int fbank = 1;
        int fstartaddr = 2;
        boolean matching = true;
        byte[] usercmddata = mUhfrManager.getTagDataByFilter(mbank, startaddr, datalen, password, timeout, fdata, fbank, fstartaddr, matching);
        if (usercmddata != null) {
            sendLockMes(mHandler, "指令区读取成功:" + Tools.Bytes2HexString(usercmddata, usercmddata.length),LOCK_LOG_WHAT);
        } else {
            sendLockMes(mHandler, "指令区读取失败!请重试...",LOCK_WHAT);
            return;
        }
        byte[] cmdB = Tools.HexString2Bytes("6618" + cmd);
//        sendMes(mHandler, "写入指令区:" + ("6618" + cmd) + " 长度:" + cmdB.length);
        Reader.READER_ERR reader_err = mUhfrManager.writeTagDataByFilter((char) mbank, startaddr, cmdB, cmdB.length, password, timeout, fdata, fbank, fstartaddr, matching);
        if (reader_err == Reader.READER_ERR.MT_OK_ERR) {
            sendLockMes(mHandler, "指令区写入成功:",LOCK_LOG_WHAT);
        } else {
            sendLockMes(mHandler, "指令区写入失败!请重试...",LOCK_WHAT);
            return;
        }
        try {
            sendLockMes(mHandler, "延时2秒后读取结果....",LOCK_LOG_WHAT);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendLockMes(mHandler, "4、读取操作结果....",LOCK_LOG_WHAT);
        mUhfrManager.setInventoryFilter(fdata,fbank,fstartaddr,matching);
        List<Reader.TAGINFO> listrs=new ArrayList<Reader.TAGINFO>();
        int scantimes = 0;
        while (scantimes < 30) {
            scantimes++;
            listrs = mUhfrManager.tagInventoryByTimer((short) 100);
            if (listrs != null && listrs.size() > 0) {
                sendLockMes(mHandler, "盘点到结果标签" + listrs.size() + "个",LOCK_LOG_WHAT);
                for (Reader.TAGINFO tfs : listrs) {
                    byte[] epcdata = tfs.EpcId;
                    String epcrs = Tools.Bytes2HexString(epcdata, epcdata.length);
                    sendLockMes(mHandler, "hasElecEpc=" + hasElecEpc,LOCK_LOG_WHAT);
                    sendLockMes(mHandler, "epcrs=" + epcrs,LOCK_LOG_WHAT);
                    if (hasElecEpc.substring(0, 8).equals(epcrs.substring(0, 8))) {
                        TagEpcData ted = EpcReader.readEpc(epcrs);
                        String lockstatus = "";
                        if (lock) {
                            lockstatus = "Lock";

                        } else {
                            lockstatus = "unLock";
                        }
                        if (ted.getHasElec() == false && ted.getLockstuts().equals(lockstatus))//状态正常
                        {
                            sendLockMes(mHandler, ted.getTagid()+":锁状态正常:"+lock,LOCK_WHAT);
                            return;
                        }
                    }
                }
                sendLockMes(mHandler, "第"+scantimes+"次未盘点到结果标签",LOCK_LOG_WHAT);

            } else {
                sendLockMes(mHandler, "第"+scantimes+"次未盘点到结果标签",LOCK_LOG_WHAT);
            }
        }
        sendLockMes(mHandler, "读取到操作结果：操作失败",LOCK_WHAT);
    }
    private static String intToHex(int n) {
        StringBuffer s = new StringBuffer();
        String a;
        char []b = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        while(n != 0){
            s = s.append(b[n%16]);
            n = n/16;
        }
        a = s.reverse().toString();
        return a;
    }
    private static void sendMes(String message)
    {
        Log.e(logtag,message);
    }
    private boolean checkCMDList(long tagid,List<String> tagidlist)
    {
         for(String tagidstr:tagidlist)
         {
             if(tagid==Long.parseLong(tagidstr))
             {
                 return true;
             }
         }
         return false;
    }
    private void sendLockMes(android.os.Handler mHandler, String mes,int what) {
        // mylog.Write（mes);
        Message message = new Message();
        message.what = what;
        Bundle data = new Bundle();
        data.putString("lockmessage", mes);
        message.setData(data);
        mHandler.sendMessage(message);
    }
    public  static void readCARD(Handler mHandler,int readpower,int writepower)
    {
        mUhfrManager.setCancleInventoryFilter();
        mUhfrManager.setPower(readpower,writepower);
        sendMes("UHF开始扫描:功率:"+readpower);
        List<Reader.TAGINFO> list1;
        int tagcount=0;
        for(int i=0;i<5;i++)
        {
            list1 = mUhfrManager.tagInventoryByTimer((short) 200);
            // mylog.Write("开始读取标签,读取功率:"+mUhfrManager.getPower()[0]);
            if (list1 != null && list1.size() > 0)
            {
                for (Reader.TAGINFO tfs : list1)
                {
                    byte[] epcdata = tfs.EpcId;
                    String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                    sendUHFMes(mHandler,epc);
                    tagcount++;
                }
            }
        }
        if(tagcount==0)//未读取到标签，发送读取错误信息
        {
            sendUHFMes(mHandler,"");
        }
    }

    private static void sendUHFMes(Handler mHandler, String mes) {
        // mylog.Write（mes);
        Message message = new Message();
        message.what = UHF_WHAT;
        Bundle data = new Bundle();
        data.putString("tagmessage", mes);
        message.setData(data);
        mHandler.sendMessage(message);
    }
    public static enum LOCK_ERR {
        LOCK_OK(0),
        LOCK_NOEPC_ERR(1),
        LOCK_PSAM_ERR(2),
        LOCK_READUSER_ERR(3),
        LOCK_WRITEUSER_ERR(4),
        LOCK_LOCKRRS_ERR(5),
        LOCK_NOCMD_ERR(6);
        private int value = 0;

        private LOCK_ERR(int value) {
            this.value = value;
        }

        public static LOCK_ERR valueOf(int value) {
            switch (value) {
                case 0:
                    return LOCK_OK;
                case 1:
                    return LOCK_NOEPC_ERR;
                case 2:
                    return LOCK_PSAM_ERR;
                case 3:
                    return LOCK_READUSER_ERR;
                case 4:
                    return LOCK_WRITEUSER_ERR;
                case 5:
                    return LOCK_LOCKRRS_ERR;
                case 6:
                    return LOCK_NOCMD_ERR;
                default:
                    return LOCK_LOCKRRS_ERR;
            }
        }
        public int value() {
            return this.value;
        }
    }
}
