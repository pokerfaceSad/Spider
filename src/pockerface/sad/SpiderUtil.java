package pockerface.sad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.omg.CORBA.portable.RemarshalException;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;



public class SpiderUtil {  
	
	//全局变量
	
	//工作目录
	static String workpath = "E:/spiderWorkPath/";
	
	//正则表达式
	static String rootURL = "http://www.ivsky.com/bizhi";
	static String classReg = "<a href=\"/bizhi(/.{3,10}/)\">(.+?)</a></li>";
	static String photoAblumReg = "<a href=\"/bizhi(/.+?)\" title=\"(.+?)\" target=";
	static String pageReg = "(/index_\\d{1,3}\\.html)";
	static String htmlReg = "a href=\"(/bizhi/.+?/pic_.+?\\.html)\"";
	static String imgURLReg = "</script><img id=\"imgis\" src=\\'(http://img.ivsky.com/img/bizhi/.+?\\.jpg)";
	
	/**
	 * 获取url的html（util）
	 * @param URL
	 * @return
	 */
    public static String getHtml(String URL) {  
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        //HttpClient  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        RequestConfig requestConfig = RequestConfig.custom()  
        	    .setConnectionRequestTimeout(5000).setConnectTimeout(5000)  
        	    .setSocketTimeout(5000).build();  
        HttpGet httpGet = new HttpGet(URL);  
        httpGet.setConfig(requestConfig);    
//        System.out.println(httpGet.getRequestLine());  
        String resHtml = null;
        try {  
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);  
            HttpEntity entity = httpResponse.getEntity();  
//            System.out.println("status:" + httpResponse.getStatusLine());  
           
            if (entity != null) {  
            	BufferedReader BR = new BufferedReader(new InputStreamReader(entity.getContent())) ;
            	StringBuffer SB = new StringBuffer();
            	for(String str=null;(str = BR.readLine())!=null;)
            	{
            		SB.append(new String(str.getBytes(), "UTF-8"));
            		SB.append("\n");
              	}
            	resHtml = SB.toString();
            }  
            return resHtml;
        } catch (IOException e) {  
        	recordErrorLog("get "+URL+" html time out!");
        	System.out.println("抓取"+URL+"超时");
        	resHtml = "";
        } finally {  
            try {  
	            closeableHttpClient.close();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
           
        }
		return resHtml;
   }
    /**
     * 将url对应的资源下载到path（util）
     * @param url
     * @param Path
     */
    public static void download(String url,String Path) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        //HttpClient  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        RequestConfig requestConfig = RequestConfig.custom()  
        	    .setConnectionRequestTimeout(5000).setConnectTimeout(5000)  
        	    .setSocketTimeout(5000).build();  
        HttpGet httpGet = new HttpGet(url);  
        httpGet.setConfig(requestConfig);    
//        System.out.println(httpGet.getRequestLine());  
        InputStream in = null;
        FileOutputStream FOS = null;
        try {  
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);  
            HttpEntity entity = httpResponse.getEntity();  
//            System.out.println("status:" + httpResponse.getStatusLine());  
            if(httpResponse.getStatusLine().getStatusCode()!=200)
            {
            	return;
            }
            if (entity != null) {  
            	in = entity.getContent();
            }
            FOS= new FileOutputStream(Path);
            byte[] buf = new byte[1024];
            for(int i=0;(i=in.read(buf))!=-1;)
            {
            	FOS.write(buf, 0, i);
            }
            
        } catch (ConnectTimeoutException e) {
        	recordErrorLog("Download "+Path+" from "+url+" time out!");
        	System.out.println("time out");
		}catch (IOException e) {  
        	e.printStackTrace();  
        } finally {  
            try {  
	            if(FOS!=null)
	            {
	            	FOS.close();
	            }
	            if(in!=null)
	            {
	            	in.close();
	            }
	            closeableHttpClient.close();  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
        }
    }
    /**
     * 抓取分类地址（util）
     * @param URL
     * @param reg
     * @return
     */
    public static Map<String, String> getClassMap(String URL,String reg){
    	
    	//准备正则表达式
    	String Html = getHtml(URL);
    	if(Html.isEmpty())
    	{
    		return null;
    	}
    	Pattern p = Pattern.compile(reg);
    	Matcher m = p.matcher(Html);
    	Map<String,String> ClassMap = new HashMap<String,String>();
    	for(;m.find();)
    	{
    		ClassMap.put(m.group(2),URL+m.group(1));
    	}
    	
    	return ClassMap;
    }
    /**
     * 将分类url写入数据库
     * @param classMap
     */
    public static void writeClassMapIntoDB(Map<String,String> classMap)
    {
    	
    	//get Connection
    	Connection coon = dbUtil.getCoon();
    	PreparedStatement ps = null;
    	Properties pro = new Properties();
    	Set<String> keys = null;
    	List<String> pageList = null; 
    	try {
    		pro.load(new FileInputStream("db.properties"));
    		ps = coon.prepareStatement(pro.getProperty("sql_in1"));
			coon.setAutoCommit(false);
			//insert
			keys = classMap.keySet();
			for(String key:keys)
			{
				ps.setString(1, key);
				ps.setString(2, classMap.get(key));
				if(ps.executeUpdate()!=1)
				{
					System.out.println(key+"---"+classMap.get(key)+"存储异常");
				}else{
					System.out.println(key+"  insert success!");
					coon.commit();
				}
					
			}
    	} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			dbUtil.closeStream(coon, ps);
		}
    }
    
    /**
     * 获取壁纸集信息存入数据库
     * 1.从数据库获取分类的url---getPhotoAblumURLs()
     * 2.获取每个分类所有的壁纸集信息，封装在photoAblumMap中---getPhotoAblumURLs()  
     * 3.将photoAblumMap写入数据库---writePhotoAblumToDB()  
     * */
    public static void getPhotoAblumURLsMap()
    {
    	Connection coon = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	Map<String,String> photoAblumMap = null;
    	
    	try {
    		coon = dbUtil.getCoon();
			ps = coon.prepareStatement("select * from t_class_url");
			rs = ps.executeQuery();
			while(rs.next())
			{
				photoAblumMap = getPhotoAblumURLs(rs.getString("url"), photoAblumReg);
				writePhotoAblumToDB(rs.getString("classname"), photoAblumMap);
			}
    	} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	
    }
    
  
    /**
     * 将壁纸集信息写入数据库（util）
     * @param className
     * @param photoAblum
     */
    public static void writePhotoAblumToDB(String className,Map<String,String> photoAblum){
    	
    	Properties pro = new Properties();
    	Connection coon = dbUtil.getCoon();
    	PreparedStatement ps = null;
    	Set<String> keys = photoAblum.keySet();
    	try {
    		coon.setAutoCommit(false);
			pro.load(new FileInputStream("db.properties"));
			ps = coon.prepareStatement(pro.getProperty("sql_in2"));
			for(String key:keys)
			{
				ps.setString(1,className);
				ps.setString(2,key);
				ps.setString(3,photoAblum.get(key));
				if(ps.executeUpdate()!=1)
				{
					System.out.println(key+" write fail！");
				}else{
					System.out.println(key+" write success!");
					coon.commit();
				}
			}
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    }
    
    /**
     * 获取分类页面中的壁纸集信息（util）
     * @param URL
     * @param Reg
     * @return
     */
    public static Map<String,String> getPhotoAblumURLs(String URL,String Reg){
    	
    	List<String> pageList = getAllPage(URL, pageReg);
    	Map<String,String> pageURLMap = new HashMap<String, String>();
    	Pattern p = Pattern.compile(Reg);
    	Matcher m = null;
    	for(String pageURL:pageList)
    	{
    		m =  p.matcher(getHtml(pageURL));
        	while(m.find())
        	{
        		pageURLMap.put(m.group(2), rootURL+m.group(1));
        	}
    	}
    	return pageURLMap;
    }
    
    /**
     * 下载一个壁纸集url中所有页的壁纸
     * @param URL
     * @param path
     */
    public static void downloadPhotoAblum(String URL,String path)
    {
    	int no = 1;
    	
    	List<String> pageList = getAllPage(URL, pageReg);
    	for(String pageURL:pageList)
    		no = downloadAPage(pageURL, path,htmlReg, imgURLReg,no);
    }
    
    /**
     * 下载壁纸集的一页（util）
     * @param pageURL
     * @param path
     * @param htmlReg
     * @param URLReg
     * @param startNo
     * @return
     */
    public static int downloadAPage(String pageURL,String path,String htmlReg,String URLReg,int startNo)
    {
    	Pattern p1 = Pattern.compile(htmlReg);
    	Matcher m1 = p1.matcher(getHtml(pageURL));
    	Pattern p2 = Pattern.compile(URLReg);
    	Matcher m2 = null;
    	
    	List<String> htmlList = new ArrayList<String>();
    	while(m1.find())
    	{
    		htmlList.add("http://www.ivsky.com"+m1.group(1)); 
    	}
    	
    	htmlList = removeRepetitiveElements(htmlList);
		 
    	List<String> URLList = new ArrayList<String>();
    	for(String html:htmlList)
    	{
    		m2 = p2.matcher(getHtml(html));
    		while(m2.find())
    		{
    			URLList.add(m2.group(1));
    		}
    	}
   
    	for(String URL:URLList)
    	{
    		download(URL, path+"\\"+startNo+".jpg");
    		System.out.println("download "+startNo+" successfully");
    		startNo++;
    	}
    	return startNo;
    }
    
   /**
    * 获取所有页的url（util）
    * @param URL
    * @param reg
    * @return
    */
    public static List<String> getAllPage(String URL,String reg)
    {
    	Pattern p = Pattern.compile(reg);
    	Matcher m = p.matcher(getHtml(URL));
    	List<String> pageList = new ArrayList<String>();
    	pageList.add(URL);
    	while(m.find())
    	{
    		pageList.add(URL+m.group(1));
    	}
    	pageList = removeRepetitiveElements(pageList);
    	return pageList;
    }
    
    /**
     * 删除list中重复的元素（util）
     * @param list
     * @return
     */
    public static List<String> removeRepetitiveElements(List<String> list)
    {
    	List<String> listTemp = new ArrayList<String>();
    	Iterator<String> it=list.iterator();  
    	String a = null;
    	while(it.hasNext()){  
			 
			a=it.next();  
			 if(listTemp.contains(a)){  
			   it.remove();  
			 }  
			 else{  
			   listTemp.add(a);  
			 }  
		}
    	return list;
    }
    
    /**
     * 创建目录
     */
    public static void makeDirs(){
    	
    	Connection coon = dbUtil.getCoon();
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	
    	File file = null;
    	StringBuffer sb = new StringBuffer();
    	sb.append(workpath);
    	try {
			ps = coon.prepareStatement("select * from t_photoablum_url");
			rs = ps.executeQuery();
			while(rs.next())
			{
				sb.append(rs.getString("classname"));
				sb.append("/");
				sb.append(rs.getString("name"));
				file = new File(sb.toString());
				if(!file.exists())
				{
					file.mkdirs();
				}
				sb.replace(0, sb.length(), workpath);
			}
			dbUtil.closeStream(coon, ps);
    	} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
    	
    }

    /**
     * 记录错误信息
     * @param Msg
     */
    public static void recordErrorLog(String Msg){
    	BufferedWriter bw = null;
    	try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("G:/spiderWorkPath/errorLog.txt",true)));
			bw.write(getDate());
			bw.newLine();
			bw.write("----------------------------------");
			bw.newLine();
			bw.write(Msg);
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
    /**
     * 获取当前时间
     * @return
     */
	public static String getDate() {
		Properties pro = new Properties();
		String date = null;
		try {
			pro.load(new FileInputStream("db.properties"));
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat(pro.getProperty("dateFormat"));
			date = sdf.format(d);
			
			return date;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return date;
	}
}  
