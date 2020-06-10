package cn.kcrxorg.kcrxepms.uhfutil;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.BRMicro.Tools;
import com.alibaba.fastjson.JSONObject;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import java.util.List;

import cn.kcrxorg.kcrxepms.mbutil.MyLog;
import cn.kcrxorg.kcrxepms.pasmutil.cn.kcrx.bean.TagEpcData;
import cn.kcrxorg.kcrxepms.pasmutil.rfidtool.EpcReader;

public class UHFHelper {

    public static int uhfwhat=1;
    public static UHFRManager mUhfrManager;//uhf
    private static Handler mHandler;
    private static MyLog mylog;
    public UHFHelper(Handler handler, Context context)
    {
        mHandler=handler;
        mylog=new MyLog(context,10000,1);
    }
    public void close()
    {
        if(mUhfrManager!=null)
        {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                mylog.Write(e.getMessage());
                e.printStackTrace();

            }
            mUhfrManager.close();
            mylog.Write("UHF天线已经关闭");
        }
    }
    public  static void readCARD(int readpower,int writepower)
    {

        mUhfrManager=UHFRManager.getIntance();
        mylog.Write("UHF天线已经打开");
        mUhfrManager.setCancleFastMode();
        mUhfrManager.setPower(readpower,writepower);
        List<Reader.TAGINFO> list1;

        list1 = mUhfrManager.tagInventoryByTimer((short) 100);
        mylog.Write("开始读取标签,读取功率:"+mUhfrManager.getPower()[0]);
        if (list1 != null && list1.size() > 0)
        {
            for (Reader.TAGINFO tfs : list1)
            {
                byte[] epcdata = tfs.EpcId;
                String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
              //  mylog.Write("读取到EPC"+epc);
                sendMes(mHandler,epc);
            }
        }
        else//未读取到标签，发送读取错误信息
        {
            sendMes(mHandler,"");
        }
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUhfrManager.close();
        mUhfrManager = null;
    }

    private static void sendMes(Handler mHandler, String mes) {
       // mylog.Write（mes);
        Message message = new Message();
        message.what = uhfwhat;
        Bundle data = new Bundle();
        data.putString("tagmessage", mes);
        message.setData(data);
        mHandler.sendMessage(message);
    }
}
