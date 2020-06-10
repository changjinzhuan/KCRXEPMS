package cn.kcrxorg.kcrxepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.kcrxepms.pasmutil.DESHelper;

public class Cmd8001 extends BaseCmd {


     public Cmd8001(BaseCmd baseCmd,String skey) throws Exception {
         if(baseCmd.getLength().startsWith("7"))//加密包
         {
            String mPayloads=baseCmd.getPayloads();
            String Payloads=DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
            Log.e("kcrx","解密后数据"+Payloads);
            baseCmd.setPayloads(Payloads);

            setAction(baseCmd.getPayloads().substring(0,2));
            setFlags(baseCmd.getPayloads().substring(2));
         }else//未加密包
         {
             setAction(baseCmd.getPayloads().substring(0,2));
             setFlags(baseCmd.getPayloads().substring(2));
         }
     }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    String action;
    String flags;


}
