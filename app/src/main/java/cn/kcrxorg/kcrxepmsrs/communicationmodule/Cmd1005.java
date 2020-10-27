package cn.kcrxorg.kcrxepmsrs.communicationmodule;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.kcrxorg.kcrxepmsrs.pasmutil.DESHelper;

public class Cmd1005 extends BaseCmd {

public Cmd1005(BaseCmd baseCmd,String skey)throws Exception
{
    if(baseCmd.getLength().startsWith("7"))//加密包
    {
        String mPayloads=baseCmd.getPayloads();
        String Payloads= DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
        Log.e("kcrx","解密后数据"+Payloads);
        baseCmd.setPayloads(Payloads);
        setSession(baseCmd.getPayloads().substring(0,2));
        Log.e("kcrx","UTC时间为："+Long.parseLong(baseCmd.getPayloads().substring(2),16));
        setDatetime(Long.parseLong(baseCmd.getPayloads().substring(2),16));
    }else
    {
        setSession(baseCmd.getPayloads().substring(0,2));
        setDatetime(Long.parseLong(baseCmd.getPayloads().substring(2),16));
    }
}

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Long getDatetime() {
        return datetime;
    }

    public void setDatetime(Long datetime) {
        this.datetime = datetime;
    }

    private String session;
   private Long datetime;

    /**
     * utc时间转成local时间
     * @param utcTime
     * @return
     */
    public static Date utcToLocal(String utcTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date utcDate = null;
        try {
            utcDate = sdf.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdf.setTimeZone(TimeZone.getDefault());
        Date locatlDate = null;
        String localTime = sdf.format(utcDate.getTime());
        try {
            locatlDate = sdf.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return locatlDate;
    }
}
