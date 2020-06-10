package cn.kcrxorg.kcrxepms.communicationmodule;

import android.util.Log;

import cn.kcrxorg.kcrxepms.pasmutil.DESHelper;

public class Cmd2005 extends BaseCmd {


    public Cmd2005(BaseCmd baseCmd,String skey) throws Exception {

        String mPayloads=baseCmd.getPayloads();
        String Payloads= DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
        Log.e("kcrx","解密后数据"+Payloads);
        baseCmd.setPayloads(Payloads);
        int userdatalen=Integer.parseInt(baseCmd.getPayloads().substring(0,8),16);
        setMetadata(baseCmd.getPayloads().substring(8,8+userdatalen*2));
        setMetamark(baseCmd.getPayloads().substring(8+userdatalen*2));
    }

    String metadata;
    String metamark;

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getMetamark() {
        return metamark;
    }

    public void setMetamark(String metamark) {
        this.metamark = metamark;
    }
}
