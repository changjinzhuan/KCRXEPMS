package cn.kcrxorg.kcrxepms.businessmodule.datainfo;

public class DailyCheckData extends BaseData {
    public DailyCheckPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(DailyCheckPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

    private DailyCheckPackInfo[] packInfoList;
}
