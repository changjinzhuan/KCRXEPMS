package cn.kcrxorg.kcrxepms.businessmodule.datainfo;

public class PackageData extends BaseData {
    public packinfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(packinfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

    packinfo[] packInfoList;
}
