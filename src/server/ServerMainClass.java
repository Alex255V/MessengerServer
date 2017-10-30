package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerMainClass {
	
	private static java.sql.Statement st;
	private static ArrayList streams;
	private static Connection c;
	private static PrintWriter writer;
	
	public static void main(String[]args) throws Exception{
			go();
	}
	
	public static void go() throws Exception {
		streams  = new ArrayList<PrintWriter>();
		setDB();
		try {
			ServerSocket ss = new ServerSocket(5500);
			
			while(true) {
				Socket sock = ss.accept();
				
				System.out.println("Got user");

				writer = new PrintWriter(sock.getOutputStream());
				sendHistory();
				streams.add(writer);
				
				Thread thread = new Thread(new Listener(sock));
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void sendHistory() throws Exception {
		ArrayList list = new ArrayList<String>();
		String SQL = "SELECT msg FROM `messages`";
		ResultSet rs = st.executeQuery(SQL);
		
		while(rs.next()) {
			writer.println(rs.getString("msg"));
			writer.flush();
		}
	}

	
	private static  void tellEveryone(String msg ) throws Exception {
		int  x = msg.indexOf(':');
		String login = msg.substring(0,x);
		
		save(login, msg);
		
		Iterator iter = (Iterator) streams.iterator();
		while(iter.hasNext()) {
			try {
				writer = (PrintWriter) iter.next();
				writer.println(msg);
				writer.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void save(String login, String msg) throws Exception {
		
		String SQL = "INSERT INTO `db_for_messenger`.`messages` (`login`, `msg`) VALUES ('"+ login +"','"+ msg +"')";
		st.executeUpdate(SQL);
	}
	
	private static void setDB() throws Exception {
		String url = "jdbc:mysql://localhost/db_for_messenger";
		String login = "root";
		String pass = "x8y3 219p";
		
		Class.forName("../com.mysql.jdbc.Driver");
		c = DriverManager.getConnection(url,login,pass);
		st = c.createStatement();
				
	}
	
	private static class Listener implements Runnable {
		BufferedReader reader;
		
		Listener(Socket sock) {
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		public void run() {
			String msg;
			try {
				while((msg = reader.readLine())!=null) {
					System.out.println(msg);
					tellEveryone(msg);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
