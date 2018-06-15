/*
 
Copyright (c) 2013-2014 Hiroaki Tateshita

Permission is hereby granted, free of charge, to any person obtaining a copy 
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
IN THE SOFTWARE.

 */

package org.braincopy.gnss.plot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

/**
 * This thread is waiting for NMEA message via TCP connection.
 * 
 * @author Hiroaki Tateshita
 * @version 0.5
 * 
 */
public class TCPClientThread extends Thread {
	/**
	 * 
	 */
	private boolean isRunning = false;

	/**
	 * 
	 */
	private String hostname;

	/**
	 * 
	 */
	private int portNumber;

	/**
	 * 
	 */
	private Socket socket;

	/**
	 * 
	 */
	private MessageListener messageListener;

	/**
	 * 
	 */
	public final void run() {
		try {
			InetAddress address = InetAddress.getByName(hostname);
			socket = new Socket(address, portNumber);
			isRunning = true;
			String tempstr = "";
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			while (isRunning) {
				tempstr = bufferedReader.readLine();
				if (this.messageListener != null && tempstr != null) {
					this.messageListener.sendMessage(tempstr);
				}
			}
		} catch (UnknownHostException e) {
			Log.e("error",
					"something happnens in connection thread.unhostname? " + e);
		} catch (IOException e) {
			Log.e("error", "something happnens in connection thread. " + e);
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				Log.e("error", "something happnens in connection thread. " + e);
			}
		}
	}

	/**
	 * 
	 */
	public final void stopClient() {
		isRunning = false;

	}

	/**
	 * 
	 * @param hn
	 *            hostname
	 */
	public final void setHostName(final String hn) {
		this.hostname = hn;

	}

	/**
	 * 
	 * @param portNumber0
	 *            port Number
	 */
	public final void setPortNumber(final int portNumber0) {
		this.portNumber = portNumber0;

	}

	/**
	 * 
	 * @param ml
	 *            MessageLister
	 */
	public final void setMessageListener(final MessageListener ml) {
		this.messageListener = ml;

	}

	/**
	 * 
	 * @return running or not
	 */
	public final boolean isRunning() {
		return this.isRunning;
	}

}
