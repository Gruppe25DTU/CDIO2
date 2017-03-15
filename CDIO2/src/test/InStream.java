package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class InStream extends Thread {

	BufferedReader in;
	Socket s;
	public InStream(Socket in) {
		try {
			s = in;
			this.in = new BufferedReader(new InputStreamReader(in.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run()
	{
		String inMsg;
		try {
			while((inMsg = in.readLine()) !=null)
			{

				System.out.println(inMsg);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
