package cn.kcrxorg.kcrxepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.kcrxepms.pasmutil.DESHelper;

public class Cmd2002 extends BaseCmd {
    String id;
    String timestamp;
    String data;

    public Cmd2002(BaseCmd baseCmd,String skey)throws Exception
    {
        if(baseCmd.getLength().startsWith("7"))
        {
            String mPayloads=baseCmd.getPayloads();
            String Payloads= DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
            Log.e("kcrx","解密后数据"+Payloads);
            baseCmd.setPayloads(Payloads);
            setId("0"+baseCmd.getPayloads().substring(1,8));
            setTimestamp(baseCmd.getPayloads().substring(8,24));
            setData(baseCmd.getPayloads().substring(24));
        }else
        {
            setId("0"+baseCmd.getPayloads().substring(1,8));
            setTimestamp(baseCmd.getPayloads().substring(8,24));
            setData(baseCmd.getPayloads().substring(24));
        }

    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
