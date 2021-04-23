package cn.kcrxorg.kcrxepmsrs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.BRMicro.Tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.kcrxorg.kcrxepmsrs.adapter.TraceDataAdapter;
import cn.kcrxorg.kcrxepmsrs.businessmodule.datainfo.superScanPackinfo;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.TagEpcData;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.TagUserdata;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.UserTraceData;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.EpcReader;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.PervalueHelper;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.UserReader;

import static cn.kcrxorg.kcrxepmsrs.R.drawable.tv_operinfo_border;

public class SackTraceActivity extends BisnessBaseActivity{
    TextView tv_cmdinfo;
    ListView listViewTraceData;
    ScrollView line_scorllrsinfo;

    TraceDataAdapter traceDataAdapter ;
    UserTraceData[] userTraceDatas;
    List<UserTraceData> userTraceDataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        allcarddata=new ArrayList<>();
        tv_cmdinfo=new TextView(this);
      //  listViewTraceData=new ListView(this);
        line_scorllrsinfo=findViewById(R.id.line_scorllrsinfo);
        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;
      //  tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(32);
        tv_cmdinfo.setGravity(Gravity.CENTER);
        // tv_cmdinfo.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        line_businfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);

//        LinearLayout.LayoutParams params1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,150);
//        params1.weight=1;
//       // userTraceDatas=new UserTraceData[8];


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
        tv_header.setCenterString("签封追溯");
        tv_operinfo.setText("请按【扫描】进行追溯或按【取消】退出");
        tv_footer.setText("请按【扫描】进行追溯或按【取消】退出");
        line_kun.removeAllViews();//清除捆数栏，准备添加库间选择列表
    }

    public void readCard() {
        mylog.Write("开始追溯扫描款包,读取功率："+12);
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
           // mylog.Write("读取到追溯数据:"+tracedata);;
            TagEpcData ted=EpcReader.readEpc(epcstr);
            tv_cmdinfo.setText("读取到签封："+ted.getTagid()+"\r\n"+
                    "券别:"+PervalueHelper.getVal(ted.getPervalueid())+"\r\n"+
                    "锁状态:"+ted.getLockstuts());
            TagUserdata tagUserdata= UserReader.readTagUser(Tools.HexString2Bytes(tracedata));
            userTraceDatas = tagUserdata.getUserTraceData();
            line_rsinfo.removeAllViews();
          //  line_scorllrsinfo.removeAllViews();
//            if(userTraceDatas==null)//first add
//            {
//                userTraceDatas = tagUserdata.getUserTraceData();
//                userTraceDataList= Arrays.asList(userTraceDatas);
//                mylog.Write("首次读取到追溯数据:"+userTraceDatas.length+"条");;
//                traceDataAdapter=new TraceDataAdapter(SackTraceActivity.this,userTraceDataList);
//                listViewTraceData.setAdapter(traceDataAdapter);
//                listViewTraceData.setBackgroundResource(tv_operinfo_border);
//                line_scorllrsinfo.removeAllViews();
//                //  line_scorllrsinfo.setLayoutParams(params1);
//                line_scorllrsinfo.addView(listViewTraceData);
//            }else
//            {
//                userTraceDataList.clear();
//                userTraceDatas = tagUserdata.getUserTraceData();
//                //userTraceDataList.addAll(userTraceDatas);
//                userTraceDataList= Arrays.asList(userTraceDatas);
//
//                mylog.Write("后次读取到追溯数据:"+userTraceDatas.length+"条:"+userTraceDataList.get(0).getOpdatetime());;
//            }
//            traceDataAdapter.notifyDataSetChanged();
//            fixListViewHeight(listViewTraceData);
            for(int i=0;i<userTraceDatas.length;i++)
            {
                if(userTraceDatas[i].getCommandid().startsWith("B"))
                {
                    addRsinfo((i+1)+":操作:"+(userTraceDatas[i].getCommandid().equals("B4")?"开袋":"关袋")+" 操作员:"+userTraceDatas[i].getOperator1()+" 复核员:"+userTraceDatas[i].getOperator2()+" 时间:20"+userTraceDatas[i].getOpdatetime(),true);
                }else
                {
                    addRsinfo((i+1)+":"+"操作:"+userTraceDatas[i].getCommandid(),true);
                }
            }
            Util.playOk();
        }catch (Exception e)
        {
            line_rsinfo.removeAllViews();
            addRsinfo("未读取到追溯数据"+e.toString(),false);
            mylog.Write("未读取到追溯数据"+e);
            Util.playErr();
        }



    }
    public void fixListViewHeight(ListView listView) {
        // 如果没有设置数据适配器，则ListView没有子项，返回。
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        if (listAdapter == null) {
            return;
        }
        for (int index = 0, len = listAdapter.getCount(); index < len; index++) {
            View listViewItem = listAdapter.getView(index , null, listView);
            // 计算子项View 的宽高
            listViewItem.measure(0, 0);
            // 计算所有子项的高度和
            totalHeight += listViewItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // listView.getDividerHeight()获取子项间分隔符的高度
        // params.height设置ListView完全显示需要的高度
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
