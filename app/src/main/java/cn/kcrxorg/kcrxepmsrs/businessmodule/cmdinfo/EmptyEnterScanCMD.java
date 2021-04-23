package cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo;

public class EmptyEnterScanCMD extends BaseCommand {

    private EmptyEnterScanPackInfo[] packInfoList;

    private stackInfo[] stackInfoList;

    public EmptyEnterScanPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(EmptyEnterScanPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

    public stackInfo[] getStackInfoList() {
        return stackInfoList;
    }

    public void setStackInfoList(stackInfo[] stackInfoList) {
        this.stackInfoList = stackInfoList;
    }
}
