package cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo;

public class SuperScanCMD104 extends BaseCommand {

    public superScanBusiInfo[] getBusiInfoList() {
        return busiInfoList;
    }

    public void setBusiInfoList(superScanBusiInfo[] busiInfoList) {
        this.busiInfoList = busiInfoList;
    }

    public SuperScanPackInfo[] packInfoList;

    public SuperScanPackInfo[] getPackInfoList() {
        return packInfoList;
    }

    public void setPackInfoList(SuperScanPackInfo[] packInfoList) {
        this.packInfoList = packInfoList;
    }

//   // public stackInfo[] getStackInfoList() {
//        return stackInfoList;
//    }

//  //  public void setStackInfoList(stackInfo[] stackInfoList) {
//        this.stackInfoList = stackInfoList;
//    }

    private  superScanBusiInfo[] busiInfoList;
   // private stackInfo[] stackInfoList;
}
