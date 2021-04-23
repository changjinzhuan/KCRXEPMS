package cn.kcrxorg.kcrxepmsrs.businessmodule.datainfo;

import cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo.EnterCheckPackInfo;

public class EnterCheckData extends BaseData {
    EnterCheckPackInfo[] packInfoList;

    public EnterCheckPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(EnterCheckPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }
}
