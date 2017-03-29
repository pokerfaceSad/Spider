package pockerface.sad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;


public class dbUtil {
	static{
		//注册驱动
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	/**
	 * 获取连接
	 * @return
	 */
	public static Connection getCoon(){
		
		Properties pro = new Properties();
		try {
			pro.load(new FileInputStream("db.properties"));
		} catch (FileNotFoundException e) {
			System.out.println("\n cann't find db.properties\n");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("\ndb.properties load fail��\n");
			e.printStackTrace();
		}
		
		Connection coon = null;
		try {
			coon = DriverManager.getConnection(pro.getProperty("url"), pro.getProperty("user"), pro.getProperty("password"));
		} catch (SQLException e) {
			System.out.println("\nget Coonnection fail!\n ");
			e.printStackTrace();
		}
		
		return coon;
		
	}

	/**
	 * 关闭资源
	 * @param coon
	 * @param ps
	 */
	public static void closeStream(Connection coon,PreparedStatement ps){
		
		if(coon!=null)
		{
			try {
				coon.close();
			} catch (SQLException e) {
				System.out.println("\nclose coon failed!\n");	
				e.printStackTrace();
			}
		}
		if(ps!=null)
		{
			try {
				ps.close();
			} catch (SQLException e) {
				System.out.println("\nclose PreparedStatement failed!\n");
				e.printStackTrace();
			}
		}
		System.out.println("\nStream close successfully!\n");
	}
	
}
