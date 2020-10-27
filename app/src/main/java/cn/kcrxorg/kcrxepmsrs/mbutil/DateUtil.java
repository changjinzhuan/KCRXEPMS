
package cn.kcrxorg.kcrxepmsrs.mbutil;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class DateUtil {
    public static boolean setDateTime(String datatime, Context contex)
    {
        try {
            Log.e("kcrx","root结果"+requestRootPermission(contex.getPackageCodePath())+"");

            execSuCmd("date " + datatime
                    + "\n busybox hwclock -w\n");
            return true;
        }catch (Exception e)
        {
            return  false;
        }

    }
    /**
     * 请求root权限
     * @return 应用程序是/否获取Root权限
     */
    public static boolean requestRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.e("kcrx",e.toString());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                Log.e("kcrx",e.toString());
            }
        }
        return true;
    }
    /**
     * 执行Android命令
     * @param cmd  命令
     */
    private static void execSuCmd(String cmd) {
        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            int aa = process.waitFor();
            is = new DataInputStream(process.getInputStream());
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String out = new String(buffer);
        } catch (Exception e) {
           Log.e("kcrx",e.toString());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                if (process != null){
                    process.destroy();
                }

            } catch (Exception e) {
                Log.e("kcrx",e.toString());
            }
        }
    }
}
