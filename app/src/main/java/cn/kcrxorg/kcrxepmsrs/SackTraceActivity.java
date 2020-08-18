package cn.kcrxorg.kcrxepmsrs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.BRMicro.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.kcrxorg.kcrxepmsrs.businessmodule.datainfo.superScanPackinfo;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.TagEpcData;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.TagUserdata;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.UserTraceData;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.EpcReader;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.PervalueHelper;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.UserReader;

public class SackTraceActivity extends BisnessBaseActivity{
    TextView tv_cmdinfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
        tv_cmdinfo=new TextView(this);

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
      //  tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(36);
        tv_cmdinfo.setGravity(Gravity.CENTER);
        // tv_cmdinfo.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        line_businfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://扫描到包号
                        String tagmessage=msg.getData().getString("tagmessage");
                        if(!checkrepeat(tagmessage))//重复过滤
                        {
                            break;
                        }
                        allcarddata.add(tagmessage);
                        TagEpcData tagEpcData= EpcReader.readEpc(tagmessage);

                        if(tagEpcData!=null&&tagEpcData.getTagid()>0)//如果可以读取未报错
                        {
                            mylog.Write("追溯读取到包号："+tagEpcData.getTagid());
                        }
                        break;
                    case INFO_MES:
                        mylog.Write(msg.getData().getString("message").toString());
                        break;
                }
                super.handleMessage(msg);
            }
        };

        mylog.Write("签封追溯界面初始化完成");
    }

    public void initView()
    {
        tv_header.setText("签封追溯");
        tv_operinfo.setText("请按【扫描】进行追溯或按【取消】退出");
        tv_footer.setText("请按【扫描】进行追溯或按【取消】退出");
        line_kun.removeAllViews();//清除捆数栏，准备添加库间选择列表
    }

    public void readCard() {
        mylog.Write("开始追溯扫描款包,读取功率："+10);
        String alldata= lockHelper.RFID_Trace();
        if(alldata.contains("_ERR"))
        {
            Util.playErr();
            mylog.Write("未读取到追溯数据");
            tv_cmdinfo.setText("未读取到追溯数据");
            addRsinfo("未读取到追溯数据",false);
            return;
        }
        try {
            String tracedata=alldata.split(":")[0];
            String epcstr=alldata.split(":")[1];
            mylog.Write("读取到追溯数据:"+tracedata);;

            tv_cmdinfo.setText("读取到签封："+EpcReader.readEpc(epcstr).getTagid());

            TagUserdata tagUserdata= UserReader.readTagUser(Tools.HexString2Bytes(tracedata));
            UserTraceData[] userTraceDatas= tagUserdata.getUserTraceData();
            for(int i=0;i<userTraceDatas.length;i++)
            {
                if(userTraceDatas[i].getCommandid().startsWith("B"))
                {
                    addRsinfo("第"+(i+1)+"条记录："+" 命令码:"+userTraceDatas[i].getCommandid()+"操作人1:"+userTraceDatas[i].getOperator1()+"操作人2:"+userTraceDatas[i].getOperator2()+"时间:"+userTraceDatas[i].getOpdatetime(),true);
                }else
                {
                    addRsinfo("第"+(i+1)+"条记录："+"命令码:"+userTraceDatas[i].getCommandid(),true);
                }
            }
            Util.playOk();
        }catch (Exception e)
        {
            addRsinfo("未读取到追溯数据"+e.toString(),false);
            mylog.Write("未读取到追溯数据"+e);
        }



    }
}
