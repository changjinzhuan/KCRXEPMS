package cn.kcrxorg.kcrxepmsrs.communicationmodule;

import android.util.Log;

import com.BRMicro.Tools;

import cn.kcrxorg.kcrxepmsrs.pasmutil.DESHelper;

public class Cmd4002 extends BaseCmd {

    public Cmd4002(BaseCmd baseCmd,String skey) throws Exception {
        String mPayloads=baseCmd.getPayloads();
        String Payloads= DESHelper.decryptStr(mPayloads.substring(0,mPayloads.length()-8),skey);
        Log.e("kcrx","解密后数据"+Payloads);
        setError(new String(Tools.HexString2Bytes(Payloads.substring(8)),"UTF-8"));
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    String error;
}
