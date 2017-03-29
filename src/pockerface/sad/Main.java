package pockerface.sad;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

	/**
	 * @author pokerfaceSad
         * 获取URL  
	 * 多线程下载所有分类
	 */
	public static void main(String[] args) {
		
		//获取URL写入数据库
		SpiderUtil.writeClassMapIntoDB(SpiderUtil.getClassMap(SpiderUtil.rootURL, SpiderUtil.classReg));
		SpiderUtil.getPhotoAblumURLsMap();
		
		//读取URL进行下载
		Connection coon = dbUtil.getCoon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = coon.prepareStatement("select classname from t_class_url");
			rs = ps.executeQuery();
			while(rs.next())
			{
				new Thread(new downloadThread(rs.getString("classname"))).start();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	
	
}
/**
 * 下载分类线程
 * 
 *
 */
class downloadThread implements Runnable{
	
	String className;
	
	downloadThread(String className){
		
		this.className = className;
	}
	
	@Override
	public void run() {
		download();
	}
	
	public void download(){
		
		Connection coon = dbUtil.getCoon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps =coon.prepareStatement("select * from t_photoablum_url where classname=?");
			ps.setString(1, this.className);
			rs = ps.executeQuery();
			StringBuffer sb = new StringBuffer();
			while(rs.next())
			{
				sb.append(SpiderUtil.workpath);
				sb.append("/");
				sb.append(rs.getString("classname"));
				sb.append("/");
				sb.append(rs.getString("name"));
				recordRunLog(rs.getString("name")+" begin to download!");
				SpiderUtil.downloadPhotoAblum(rs.getString("url"), sb.toString());
				System.out.println(rs.getString("url")+"------"+ sb.toString());
				recordRunLog("download completely!");
				sb.replace(0, sb.length(), "");
			}
			dbUtil.closeStream(coon, ps);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(rs!=null)
			{
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
    //记录线程运行信息
    public void recordRunLog(String Msg){
    	BufferedWriter bw = null;
    	try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("G:/spiderWorkPath/"+this.className+".txt",true)));
			bw.write(SpiderUtil.getDate());
			bw.newLine();
			bw.write("----------------------------------");
			bw.newLine();
			bw.write(Msg);
			bw.newLine();
			bw.newLine();
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			
			if(bw!=null)
			{
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
	
}
