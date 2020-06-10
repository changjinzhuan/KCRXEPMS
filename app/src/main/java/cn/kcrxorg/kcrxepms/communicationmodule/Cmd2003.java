package cn.kcrxorg.kcrxepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.kcrxepms.pasmutil.DESHelper;

public class Cmd2003 extends BaseCmd {
    String id;//要求执行的任务id

    public Cmd2003(BaseCmd baseCmd,String skey) throws Exception {
        String mPayloads=baseCmd.getPayloads();
        String Payloads= DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
        Log.e("kcrx","解密后数据"+Payloads);
        baseCmd.setPayloads(Payloads);
        setId(Payloads);
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }




}
