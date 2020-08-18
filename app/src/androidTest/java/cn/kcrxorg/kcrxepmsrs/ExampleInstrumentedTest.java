package cn.kcrxorg.kcrxepmsrs;

import android.content.Context;
import android.util.Log;

import com.BRMicro.Tools;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import cn.kcrxorg.kcrxepmsrs.businessmodule.mapper.UserMapper;
import cn.kcrxorg.kcrxepmsrs.communicationmodule.BaseCmd;
import cn.kcrxorg.kcrxepmsrs.communicationmodule.Cmd2002;
import cn.kcrxorg.kcrxepmsrs.communicationmodule.CmdSelector;
import cn.kcrxorg.kcrxepmsrs.mbutil.MyLog;
import cn.kcrxorg.kcrxepmsrs.pasmutil.cn.kcrx.bean.TagEpcData;
import cn.kcrxorg.kcrxepmsrs.pasmutil.rfidtool.EpcReader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private String skey;//存储解密
    private String mkey;//存储加密
    private String session;

    private MyLog mylog;
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("cn.kcrxorg.kcrxepms", appContext.getPackageName());

//        mylog=new MyLog(appContext,10000,1);
//        mylog.Write("程序已启动！*****************************");
//
//        mylog.Write("初始化完成");


        String cmd2002str="700000948000000005F5E1132002EB900938486FFAB01E27381A31AF8C9E37C0F10FED71562D8FD6059230565FDF4124334AB314638A2B57042A01B12F4B21AAC837A31AF76002E868DBB787E93853C48F8E2F494CA499CFE63310453448B21661B7410B3A4890E934B83E70E84EF28A83088BE7DB00CD6F51221EBB374D37514FD5835D43DF7DD6EF9156AE5CFE01830F1FCF74A4BE291045AFA9DFAE27DE2A5537";
        BaseCmd baseCmd = CmdSelector.makeCmd(cmd2002str);
        String skey="0F9A4806C75B891FF5D0253D212D2822";
        try {
            Cmd2002 cmd2002=new Cmd2002(baseCmd,skey);
            String cmdjason=new String(Tools.HexString2Bytes(cmd2002.getData().substring(8)), "utf-8");

            Log.e("kcrx","cmdjason="+cmdjason);
        } catch (Exception e) {
            e.printStackTrace();
        }


//        String msg = null;
//        try {
//            msg = SHA1Util.shaEncode(HexUtil.hexStringToBytes("000000270000000000000171CA0F5FC3000000BF7B22636F6465223A3133313037332C2262757369496E666F4C697374223A5B7B22766F7563686572547970654944223A22313031303031222C22766F7563686572547970654E616D65223A22E7BAB8313030E58583222C2276616C223A223130302E30227D2C7B22766F7563686572547970654944223A22313031303032222C22766F7563686572547970654E616D65223A22E7BAB83530E58583222C2276616C223A2235302E30227D5D2C22737461636B496E666F4C697374223A5B5D7D0000000000000000"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mylog.Write("msg"+msg.toUpperCase());

//        String rapdu="0291817340064B866F15196F5934BC930462DA8D9AC02FB413BD9E4CAD626850473F9000";
//        session=rapdu.substring(2,4);
//        skey=rapdu.substring(4,36);
//        mkey=rapdu.substring(36,68);
//        Log.e("test","获取到session="+session);
//        Log.e("test","获取到skey="+skey);
//        Log.e("test","获取到mkey="+mkey);

//        String cmd2002data="7B22636F6465223A3133313037382C2273746F636B4C697374223A5B7B227361636B4E6F223A2231323030303039333536222C227061706572547970654944223A2232222C227061706572547970654E616D65223A22E5B7B2E6B885E58886E5AE8CE695B4E588B8222C2262756E646C6573223A223230222C22746965223A2230222C227361636B4D6F6E6579223A22323030303030302E3030222C22766F7563686572547970654944223A22313031353131222C22766F7563686572547970654E616D65223A22E7BAB8313030E58583EFBC883035E78988EFBC89222C2276616C223A223130302E3030222C2273737461636B436F6465223A223733323031303031303130313032222C2273737461636B4E616D65223A22222C226973556E5061636B6574223A2231222C226973436865636B223A2230222C226F70724454223A22227D5D7D";
//        try {
//            String cmd2002str=new String(Tools.HexString2Bytes(cmd2002data),"utf-8");
//
//            mylog.Write("获取到cmd2002str="+cmd2002str);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        PsamCmdUtil psam = new PsamCmdUtil();
//        if (psam.openRfid() != null) {
//            Log.d("test","打开PSAM模块成功");
//        }
//        PsamError err = new PsamError();
//        byte[] result = null;
//        //复位
//        result = psam.resetCard(1);
//        if (result != null) {
//            Log.e("test","复位PSAM卡：" + Tools.Bytes2HexString(result, result.length));
//        } else {
//
//            Log.e("test","复位PSAM卡失败");
//            // Util.play(3, 0);
//        }
//
//
//
//        //用户验证
//        boolean flag = psam.verifyUser(1,
//                Tools.HexString2Bytes("5053414D49303031"),
//                Tools.HexString2Bytes("4D494D49535F5053414D5F55534552"), err);
//        if (flag) {
//            Log.e("test", "用户验证成功：");
//        } else {
//            Log.e("test", "用户验证失败，错误码：");
//            //Util.play(3, 0);
//            return ;
//        }
//        String tracedata="6AB522964ED273A8A6334F9DDBAE314023C9008EF6A178C8";
//        byte[] rs=psam.decryptElsData(1,Tools.HexString2Bytes("481FCBEE114E200E0061B641"),Tools.HexString2Bytes("2365FDAE8A9AFC90AF760EA4F775B2E541AECCC8B9B98C2E35595AC7DB152EFB"),err);
//
//        Log.e("test", "追溯数据为:"+Tools.Bytes2HexString(rs,rs.length));

//        byte Volume=0x01;
//        String cmd="";
//        result = psam.genTagActiveElsCmd(1,
//                Tools.HexString2Bytes("3B9CD92A114E00AF09BA934D"),
//                Volume,
//                err);
//        if (result != null) {
//            cmd = Tools.Bytes2HexString(result, result.length);
//            Log.e("test",Tools.Bytes2HexString(result, result.length));
//        } else {
//            Log.e("test", err.getErrCode());
//
//           // return LockHelper.LOCK_ERR.LOCK_PSAM_ERR;
//        }
//        byte[] cmdB = Tools.HexString2Bytes("66"+intToHex(cmd.length()/2)+ cmd);
//
//        Log.e("test","写入指令区:" + "66"+intToHex(cmd.length()/2)+ cmd + " 长度:" + cmdB.length);
//        String EPC="481F52F1114E20D30007B340";
//        //481F52F1110000D30007B340 47869C5F11000047017E1740
//        byte[] id=new byte[2];
//        id[0]=0x00;
//        id[1]=0x01;
//        byte[] item=new byte[1];
//        item[0]=0x01;
//        byte[] logflag=new byte[1];
//        logflag[0]=0x00;
//
//        //获取RFID访问密码
//        String RfidPwd = "";
//        result = psam.getRFIDPassword(1, Tools.HexString2Bytes(EPC), err);
//        if (result != null) {
//            Log.e("test", "获取RFID密码成功：" + Tools.Bytes2HexString(result, result.length));
//            RfidPwd = Tools.Bytes2HexString(result, result.length);
//        } else {
//            Log.e("test", "获取RFID密码失败，错误码：" + err.getErrCode());
//            return;
//        }
//
//       //3B9CD92A114E00AF09BA934D
//        //3B9CD92A114E00AF09BA934D
//
//
//        result =  psam.genTraceElsCmd(1, Tools.HexString2Bytes(EPC),id,item,logflag,err);
//        if (result != null) {
//            Log.e("test","执行命令获取追溯命令结果：" + Tools.Bytes2HexString(result,result.length));
//        }else
//        {
//            Log.e("test","执行命令失败");
//        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String datetime=sdf.format(new Date());
//        result=psam.genRecoverElsCmd(1,Tools.HexString2Bytes(EPC),0,   HexUtil.intToBytes(Integer.parseInt("12345")),HexUtil.intToBytes(Integer.parseInt("12345")),datetime,err);
//
//        if (result != null) {
//            Log.e("test","执行命令获取恢复命令结果：" + Tools.Bytes2HexString(result,result.length));
//        }else
//        {
//            Log.e("test","执行命令失败");
//        }
////        //80210000
////        String  resultstr= psam.excute(1,"802000001700000000000000014D494D49535F5053414D5F55534552",err);
////        resultstr= psam.excute(1,"8032000120000000270000000000010014FB8A5B2296399EF6C138C734D1C54611F599B1E4",err);
////        if (result != null) {
////            Log.e("test","执行命令80320000结果：" + resultstr);
////        }else
////        {
////            Log.e("test","执行命令失败");
////        }
//
//        result=  psam.decryptElsData(1,Tools.HexString2Bytes(EPC),Tools.HexString2Bytes("A56FCB97635AC992E5EE5573612A4650C0D366F9716C5EA5BDA1A75B6DFD2F3B"),err);
//        if (result != null) {
//            Log.e("test","解密命令结果:" + Tools.Bytes2HexString(result,result.length));
//        }else
//        {
//            Log.e("test","执行命令失败111");
//        }
//
//        result=psam.genOpenElsCmd(1,Tools.HexString2Bytes(EPC),20000,HexUtil.intToBytes(Integer.parseInt("12345")),HexUtil.intToBytes(Integer.parseInt("54321")),datetime,err);
//        if (result != null) {
//          //  cmd = Tools.Bytes2HexString(result, result.length);
//            Log.e("test", "获取开关锁指令成功：" + Tools.Bytes2HexString(result, result.length));
//        } else {
//            // sendMes(mHandler, "获取指令失败，错误码：" + err.getErrCode());
//            Log.e("test","获取开关锁指令失败，错误码：" + err.getErrCode());
//            return;
//        }
//
//        psam.closeRfid();
//        //
//        resultstr= psam.excute(1,"80228000849F40008052D8DD6C1643468D4C62FE30C063E0B3E732712A754F66E62AA49CA3CD92AB5DC433EB5747B9D953C9D7D344563C206AC78F28DD5C3A1B214259C6AA734F97FED099B52A3C42CEEDC6E9C949D5A179E7D3B64325648E1FD58FE9B172CECE3CC7263D2B5DB94DD2431F0C0B5422573DF451AAE144374CEC6402DD7FDE9FD3357F",err);
//        if (result != null) {
//            Log.e("test","执行命令：80228000结果" + resultstr);
//        }else
//        {
//            Log.e("test","执行命令失败");
//        }

//        //执行命令
//        result=psam.getPSAMCertifiPart1(1,err);
//        if (result != null) {
//            Log.e("test","获取数据证书第一部分：" + Tools.Bytes2HexString(result, result.length));
//        } else {
//
//            Log.e( "test","执行卡命令失败");
//            // Util.play(3, 0);
//        }
//        //执行命令
//        result=psam.getPSAMCertifiPart2(1,err);
//        if (result != null) {
//            Log.e("test","获取数据证书第二部分：" + Tools.Bytes2HexString(result, result.length));
//        } else {
//
//            Log.e( "test","执行卡命令失败");
//            // Util.play(3, 0);
//        }
//          result=psam.getPSAMCertifi(1,err);
//          if(result!=null)
//                {
//                    Log.e("test","获取卡信息命令：" + Tools.Bytes2HexString(result, result.length));
//                }else
//                {
//                    Log.e( "test","获取卡信息命令失败");
//                }

//        result= psam.getAtr(1,err);
//        if(result!=null)
//        {
//            Log.e("test","getAtr命令：" + Tools.Bytes2HexString(result, result.length));
//        }else
//        {
//            Log.e( "test","getAtr命令失败");
//        }

       //
    }
    private static String intToHex(int n) {
        StringBuffer s = new StringBuffer();
        String a;
        char []b = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        while(n != 0){
            s = s.append(b[n%16]);
            n = n/16;
        }
        a = s.reverse().toString();
        return a;
    }
    @Test
    public void testDes() throws IllegalAccessException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        TagEpcData tagEpcData= EpcReader.readEpc("3B9CD92A114E00AF09BA934D");

        Class cls=tagEpcData.getClass();
        Field[] fields=cls.getDeclaredFields();
        for(Field f:fields)
        {
            f.setAccessible(true);
            Log.e( "test","属性名:" + f.getName() + " 属性值:" +f.get(tagEpcData));
        }
    }
    @Test
    public void mapperTest()
    {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        UserMapper userMapper=new UserMapper(appContext);


        assertTrue( userMapper.checkUser("0000000011"));
    }



}
