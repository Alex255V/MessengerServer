package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientApp {
	
	private static JTextArea ta;
	private static JTextField t;
	private static BufferedReader reader;
	private static PrintWriter writer;
	private static String login;

	public static void main(String[] args) {
		login = JOptionPane.showInputDialog("Enter login");
		go();
	}
	
	private static void go() {
		JFrame frame = new JFrame ("Simple messenger");
		frame.setResizable(false);
		frame.setSize(500,450);
		frame.setLocationRelativeTo(null);
		JPanel panel = new JPanel();
		ta = new JTextArea(25,40);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setEditable(false);
		JScrollPane sp = new JScrollPane(ta);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		t = new JTextField(33);
		t.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					String msg = login + ": " + t.getText();
					writer.println(msg);
					writer.flush();
					
					t.setText("");
					t.requestFocus();
				}
			}
		});
		
		JButton send = new JButton("send");
		
		send.addActionListener(new Send());
		
		panel.add(sp);
		panel.add(t);
		panel.add(send);
		
		setNet();
		
		Thread thread = new Thread(new Listener());
		thread.start();
		
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static class Listener implements Runnable {
		public void run() {
			String msg;
			try {
				while ((msg = reader.readLine()) != null) {
					ta.append(msg + "\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class Send implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			String msg = login + ": " + t.getText();
			writer.println(msg);
			writer.flush();
			
			t.setText("");
			t.requestFocus();
		}
	}
	
	private static void setNet () {
		try {
			Socket sock = new Socket("127.0.0.1", 5500);
			InputStreamReader isr = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(isr);
			writer = new PrintWriter(sock.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
