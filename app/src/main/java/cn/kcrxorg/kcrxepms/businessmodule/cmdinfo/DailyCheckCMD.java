package cn.kcrxorg.kcrxepms.businessmodule.cmdinfo;

import cn.kcrxorg.kcrxepms.businessmodule.cmdinfo.BaseCommand;

public class DailyCheckCMD extends BaseCommand {
    public dailyCheckStock[] getStockList() {
        return stockList;
    }

    public void setStockList(dailyCheckStock[] stockList) {
        this.stockList = stockList;
    }

    private  dailyCheckStock[] stockList;
}
