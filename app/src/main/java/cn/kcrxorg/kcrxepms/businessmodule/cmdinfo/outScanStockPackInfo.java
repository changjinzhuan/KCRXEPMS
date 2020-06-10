package cn.kcrxorg.kcrxepms.businessmodule.cmdinfo;

public class outScanStockPackInfo extends stockPackInfo{
    public String getMustOutFlag() {
        return mustOutFlag;
    }

    public void setMustOutFlag(String mustOutFlag) {
        this.mustOutFlag = mustOutFlag;
    }

    private String mustOutFlag;

}
