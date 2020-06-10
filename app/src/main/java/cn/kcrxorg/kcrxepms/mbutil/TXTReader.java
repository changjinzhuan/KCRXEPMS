package cn.kcrxorg.kcrxepms.mbutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.BRMicro.Tools;

public class TXTReader {
	//任务数据文件目录
	private static final String CmdDir = "/Cmd/";
	private static final String DataDir = "/Data/";

	public String read(File f)
	{
		StringBuilder sb=new StringBuilder();
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(f),"UTF-8");
			BufferedReader br = new BufferedReader(read);
			String s;
			while ((s = br.readLine()) != null) {
		//	Log.d("read",s);
				sb.append(s);
			}		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
	public String findLastFile(Context context,String dir) throws UnsupportedEncodingException {
		String lastfiledata="";
		File logRoot = context.getFilesDir();
		File CmdDirF =new File(logRoot.getPath()+dir);
        Long lasttime=0L;
        File lastFile=null;
        if(CmdDirF.listFiles().length>0)//如果存在文件
		{
			for(File f:CmdDirF.listFiles())
			{
                if(f.lastModified()>lasttime)
				{
					lasttime=f.lastModified();
					lastFile=f;
				}
			}
		}else
		{
			return null;
		}
		byte[] lastfiledatestr=(read(lastFile)).getBytes("UTF-8");
		lastfiledata=lastFile.getName().split("_")[1]+lastFile.getName().split("_")[2].replace(".json","")+ Tools.Bytes2HexString(lastfiledatestr,lastfiledatestr.length);

		return lastfiledata;
	}
	public String getCmdById(Context context,String id)
	{
		File logRoot = context.getFilesDir();
		File CmdDirF =new File(logRoot.getPath()+CmdDir);
		File DataDirF =new File(logRoot.getPath()+DataDir);

		for(File f:CmdDirF.listFiles())
		{
			if(f.getName().split("_")[1].equals(id.toUpperCase()))
			{
				return read(f);
			}
		}
		return null;

	}
    public boolean delCmdDataFile(Context context,String id)
	{
		boolean flags=false;
		File logRoot = context.getFilesDir();
		File CmdDirF =new File(logRoot.getPath()+CmdDir);
		File DataDirF =new File(logRoot.getPath()+DataDir);


		for(File f:CmdDirF.listFiles())
		{
			if(f.getName().split("_")[1].equals(id))
			{
				flags=f.delete();
			}
		}
		for(File f:DataDirF.listFiles())
		{
			if(f.getName().split("_")[1].equals(id))
			{
				flags=f.delete();
			}
		}
		return  flags;
	}
	public boolean delDataFile(Context context)
	{
		boolean flags=false;
		File logRoot = context.getFilesDir();

		File DataDirF =new File(logRoot.getPath()+DataDir);

		for(File f:DataDirF.listFiles())
		{

				flags=f.delete();

		}
		return  flags;
	}
}
