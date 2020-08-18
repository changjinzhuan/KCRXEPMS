package cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo;

public class DailyCheckCMD extends BaseCommand {
    public dailyCheckStock[] getStockList() {
        return stockList;
    }

    public void setStockList(dailyCheckStock[] stockList) {
        this.stockList = stockList;
    }

    private  dailyCheckStock[] stockList;
}
