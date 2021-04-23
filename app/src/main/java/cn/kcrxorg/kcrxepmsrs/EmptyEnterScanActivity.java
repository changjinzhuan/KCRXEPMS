package cn.kcrxorg.kcrxepmsrs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo.EmptyEnterScanCMD;
import cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo.EmptyEnterScanPackInfo;
import cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo.ViewCmdInfo;
import cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo.paymentSack;
import cn.kcrxorg.kcrxepmsrs.businessmodule.datainfo.EmptyEnterScanData;
import cn.kcrxorg.kcrxepmsrs.mbutil.DecimalTool;
import cn.kcrxorg.kcrxepmsrs.mbutil.TXTReader;
import cn.kcrxorg.kcrxepmsrs.mbutil.TXTWriter;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.TagEpcData;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.EpcReader;

public class EmptyEnterScanActivity extends BisnessBaseActivity {

    TextView tv_cmdinfo;
    TextView saninfo;
    EmptyEnterScanCMD emptyEnterScanCMD;
    EmptyEnterScanData emptyEnterScanData;

    List<EmptyEnterScanPackInfo> emptyEnterScanPackInfoList;
    Spinner sp_stackInfo;//库间选择
    String businessid="";

    int isgood=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        allcarddata=new ArrayList<>();
        //开始处理业务数据
        TXTReader tr = new TXTReader();
        businessid=getIntent().getStringExtra("businessid");
        String cmddata = tr.getCmdById(EmptyEnterScanActivity.this, businessid);
        emptyEnterScanCMD= JSONObject.parseObject(cmddata, EmptyEnterScanCMD.class);

        emptyEnterScanData=new EmptyEnterScanData();
        emptyEnterScanData.setCode(163848);
        emptyEnterScanData.setError("");

        emptyEnterScanPackInfoList=new ArrayList<EmptyEnterScanPackInfo>();

        line_businfo.setOrientation(LinearLayout.VERTICAL);//设置布局方向
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight=1;

        tv_cmdinfo=new TextView(this);
        saninfo =new TextView(this);
        String  cmdinfo="任务信息:\r\n";
        cmdinfo+="待扫描"+emptyEnterScanCMD.getPackInfoList().length+"包";
//        for(EmptyEnterScanPackInfo emptyEnterScanPackInfo:emptyEnterScanCMD.getPackInfoList())
//        {
//            cmdinfo+=emptyEnterScanPackInfo.getSackNo()+" "+emptyEnterScanPackInfo.getVoucherTypeName()+"\r\n";
//        }
        tv_cmdinfo.setText(cmdinfo);
        tv_cmdinfo.setTextSize(30);
        // tv_cmdinfo.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        tv_cmdinfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        tv_cmdinfo.setLayoutParams(params);
        scro_businfo.addView(tv_cmdinfo);

        saninfo.setText("已扫描:0袋");
        saninfo.setTextSize(30);
        saninfo.setLayoutParams(params);

        saninfo.setBackground(getResources().getDrawable(R.drawable.tv_border));
        line_businfo.addView(saninfo);

        line_kun.setOrientation(LinearLayout.VERTICAL);

        if (emptyEnterScanCMD.getStackInfoList().length > 0)//如果需要选择库间的话
        {
            if(emptyEnterScanCMD.getStackInfoList()[0].getSstackName()!=null)//如果集合不是空再添加选择
            {
                sp_stackInfo=new Spinner(this);//库间选择
                sp_stackInfo.setLayoutParams(params);
                List<String> stackInfoList=new ArrayList<String>();
                for(cn.kcrxorg.kcrxepmsrs.businessmodule.cmdinfo.stackInfo stackInfo:emptyEnterScanCMD.getStackInfoList())
                {
                    stackInfoList.add(stackInfo.getSstackCode()+":"+stackInfo.getSstackName());
                }
                String[] stackInfos=new String[stackInfoList.size()];
                stackInfoList.toArray(stackInfos);

                ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.sp_items, R.id.tv_stackInfo,stackInfos);
                sp_stackInfo.setAdapter(aa);
                sp_stackInfo.setBackgroundResource(R.drawable.tv_border);
                line_kun.addView(sp_stackInfo);
            }
        }
        //初始化查看任务列表
        viewCmdInfoList=new ArrayList<ViewCmdInfo>();
        for(EmptyEnterScanPackInfo stockPackInfo:emptyEnterScanCMD.getPackInfoList())
        {
            // tagidlist.add(stockPackInfo.getSackNo());
            ViewCmdInfo viewCmdInfo=new ViewCmdInfo();
            viewCmdInfo.setSackNo(stockPackInfo.getSackNo());
            viewCmdInfo.setPaperTypeName("无");
            viewCmdInfo.setVoucherTypeName(stockPackInfo.getVoucherTypeName());
            viewCmdInfo.setVal(stockPackInfo.getVal());
            viewCmdInfoList.add(viewCmdInfo);
        }

        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what)
                {
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
                            EmptyEnterScanPackInfo thispackinfo=GetJobInfo(tagEpcData.getTagid());
                            if(thispackinfo==null)//如果在任务列表
                            {
                              addRsinfo(tagEpcData.getTagid()+"不在任务列表",false);
                              Util.playErr();
                            break;
                            }
                            isgood++;
                            addRsinfo("读取到封签:" + tagEpcData.getTagid(),true);
                            saninfo.setText("已扫描"+isgood+"袋");
                            Util.playOk();

                            setGoodViewCmdInfo(tagEpcData.getTagid()+"");//设置任务列表

                            EmptyEnterScanPackInfo emptyEnterScanPackInfo=new EmptyEnterScanPackInfo();
                            emptyEnterScanPackInfo.setSackNo(tagEpcData.getTagid()+"");
                            emptyEnterScanPackInfo.setVal(thispackinfo.getVal());
                            emptyEnterScanPackInfo.setVoucherTypeID(thispackinfo.getVoucherTypeID());
                            emptyEnterScanPackInfo.setVoucherTypeName(thispackinfo.getVoucherTypeName());

                            emptyEnterScanPackInfoList.add(emptyEnterScanPackInfo);
                        }
                        break;
                    case INFO_MES:
                        mylog.Write(msg.getData().getString("message").toString());
                        break;
                }
                super.handleMessage(msg);
            }
        };
        mylog.Write("初始化完成");

    }
    public void initView()
    {
        tv_header.setCenterString("空袋入库");
        tv_operinfo.setText("请按【扫描】进行登记或按【取消】结束任务");
        tv_footer.setText("请按【扫描】进行登记或按【取消】结束任务");
        line_kun.removeAllViews();//清除捆数栏，准备添加库间选择列表
    }
  private EmptyEnterScanPackInfo GetJobInfo(Long tedid)
  {
       for(EmptyEnterScanPackInfo e:emptyEnterScanCMD.getPackInfoList())
       {
           if(tedid==Long.parseLong(e.getSackNo()))
           {
               return e;
           }
       }
       return null;
  }

    //两次退出退出任务
    private static final int TIME_EXIT=2000;
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if(mBackPressed+TIME_EXIT>System.currentTimeMillis()){
            super.onBackPressed();

            if(emptyEnterScanPackInfoList.size()>0)//业务数据存在，生成业务文件
        {
            EmptyEnterScanPackInfo[] emptyEnterScanPackInfos=new EmptyEnterScanPackInfo[emptyEnterScanPackInfoList.size()];
            emptyEnterScanPackInfoList.toArray(emptyEnterScanPackInfos);
            emptyEnterScanData.setPackInfoList(emptyEnterScanPackInfos);

            TXTWriter tw=new TXTWriter();
            String datajson= JSON.toJSONString(emptyEnterScanData);

            long timestamp=new Date().getTime();
            String timestampstr= DecimalTool.addZeroForNum(Long.toHexString(timestamp),16).toUpperCase();
            String datafilename="Data_"+businessid+"_"+timestampstr+".json";
            mylog.Write("退出业务,生成任务数据文件="+datafilename);
            mylog.Write("生成任务数据="+datajson);
            try {
                tw.writeDataFile(this,datafilename,datajson.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                mylog.Write("生成数据文件失败,原因:"+e.getMessage());
                Toast.makeText(this,"生成数据文件失败,原因:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }else{
        Toast.makeText(this,"再点击一次返回退出本业务",Toast.LENGTH_SHORT).show();
        mBackPressed=System.currentTimeMillis();
    }

    }
}
