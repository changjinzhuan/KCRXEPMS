package cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo;

public class EnterCheckCMD extends BaseCommand {
    EnterCheckPackInfo[] packInfoList;

    public EnterCheckPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(EnterCheckPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }
}
