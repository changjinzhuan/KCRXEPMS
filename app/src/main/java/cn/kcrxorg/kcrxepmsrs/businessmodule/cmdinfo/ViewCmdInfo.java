package cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo;

public class ViewCmdInfo {
    public String getSackNo() {
        return sackNo;
    }

    public void setSackNo(String sackNo) {
        this.sackNo = sackNo;
    }

    public String getVoucherTypeName() {
        return voucherTypeName;
    }

    public void setVoucherTypeName(String voucherTypeName) {
        this.voucherTypeName = voucherTypeName;
    }

    public String getPaperTypeName() {
        return paperTypeName;
    }

    public void setPaperTypeName(String paperTypeName) {
        this.paperTypeName = paperTypeName;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isDone=false;

    private String sackNo;
    private String voucherTypeName;
    private String paperTypeName;
    private String val;
}
