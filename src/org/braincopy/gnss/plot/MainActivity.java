/*
 
Copyright (c) 2013 braincopy.org

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

import java.io.IOException;
import java.net.UnknownHostException;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Main Activity class of this android application PPPlot.
 * 
 * @author Hiroaki Tateshita
 * @version 0.60
 * 
 */
public class MainActivity extends Activity {
	/**
	 * for sharedpreference in DialogInterface.onClickLister.
	 */
	private final Context context = this;

	/**
	 * menu item of "connect".
	 */
	private MenuItem menuConnect;

	/**
	 * menu item of "disconnect".
	 */
	private MenuItem menuDisconnect;

	/**
	 * textview for "status".
	 */
	private TextView statusTextView = null;

	/**
	 * custom view for plot area.
	 */
	private PlotView plotview = null;

	/**
	 * scroll view for status textview.
	 */
	private ScrollView scrollview = null;

	/**
	 * thread for tcp/ip connection.
	 */
	private TCPClientThread tcpClientThread = null;

	private EmbeddedGPSThread embeddedGPSThread = null;

	/**
	 * for zoom in and out by two fingers.
	 */
	private double pinchStartDistance = 0;

	/**
	 * for zoom in and out by two fingers.
	 */
	private double pinchEndDistance = 0.0;

	/**
	 * for move by one finger.
	 */
	private double moveStartX = 0.0;

	/**
	 * for move by one finger.
	 */
	private double moveStartY = 0.0;

	/**
	 * define waiting mili second.
	 */
	private final int waitingMiliSec = 1000;

	/**
	 * 
	 */
	private LocationManager locationManager;

	/**
	 * one of stream type. get stream data from embedded GPS
	 */
	public static final int EMBEDDED_GPS_STREAM_TYPE = 1;

	/**
	 * one of stream type. get stream data from TCP server as TCP client
	 */
	public static final int TCP_CLIENT_STREAM_TYPE = 0;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		plotview = (PlotView) findViewById(R.id.plotView1);
		// plotview.loadImages(MainActivity.this);
		// Resources r = context.getResources();
		// Bitmap background = BitmapFactory.decodeResource(r,
		// R.drawable.background);
		scrollview = (ScrollView) findViewById(R.id.scrollView1);
		// plotview.set_bgImageFile(background);

	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menuConnect = menu.findItem(R.id.menu_connect_item);
		menuDisconnect = menu.findItem(R.id.menu_disconnect_item);
		statusTextView = (TextView) findViewById(R.id.status_textView);
		return true;
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		int streamType = -1;
		streamType = sharedPref.getInt("stream type", -1);
		switch (item.getItemId()) {
		case R.id.menu_connect_item:
			if (menuConnect != null && menuDisconnect != null
					&& streamType == TCP_CLIENT_STREAM_TYPE) {
				String hostname = sharedPref.getString("ip_address", null);
				int portNumber = sharedPref.getInt("port number", 0);
				try {
					connect(hostname, portNumber);
				} catch (NetworkOnMainThreadException e) {
					statusTextView.append("exception: please confirm "
							+ "internet connection.\n");
					Log.e("error", e.toString());
				} catch (UnknownHostException e) {
					statusTextView.append("unknown host: " + hostname + "\n");
					e.printStackTrace();
				} catch (IOException e) {
					statusTextView.append("error: " + e
							+ " might have failed to create socket: "
							+ hostname + "\n");
					e.printStackTrace();
				} catch (InterruptedException e) {
					statusTextView.append("error: " + e
							+ " might have failed to deal with thread:\n");
					e.printStackTrace();
				}
			} else if (streamType == EMBEDDED_GPS_STREAM_TYPE) {
				statusTextView.append("start enbedded GPS sensor mode\n");
				startGPS();
			} else {
				Log.e("error", "couldn't find MenuItem at "
						+ "MainActivity.onOptionsItemSelected()");
			}
			return true;
		case R.id.menu_connectionSetting_item:
			createAndShowConnectionSettingDialog();

			return true;
		case R.id.menu_disconnect_item:
			if (streamType == TCP_CLIENT_STREAM_TYPE) {
				if (menuConnect != null && menuDisconnect != null) {
					try {
						this.disconnect();
					} catch (IOException e) {
						statusTextView.append("failed to disconnect ..");
					}
				}
			} else if (streamType == EMBEDDED_GPS_STREAM_TYPE) {
				this.embeddedGPSThread.stopRunning();
				statusTextView.append("stop enbedded GPS mode\n");
				menuConnect.setEnabled(true);
				menuDisconnect.setEnabled(false);
			}
			return true;
		case R.id.menu_clear_item:
			plotview.clear();
			plotview.plot();
			return true;
		case R.id.menu_fix_track_center_item:
			if (item.isChecked()) {
				item.setChecked(false);
				this.plotview.setFixTrackCenter(false);
			} else {
				item.setChecked(true);
				this.plotview.setFixTrackCenter(true);
			}
			return true;
		case R.id.menu_zoom_up_item:
			plotview.setZoom(2.0);
			plotview.plot();
			return true;
		case R.id.menu_zoom_down_item:
			plotview.setZoom(0.5);
			plotview.plot();
			return true;
		case R.id.menu_exit_item:
			if (tcpClientThread != null && tcpClientThread.isRunning()) {
				tcpClientThread.stopClient();
				try {
					tcpClientThread.join(waitingMiliSec);
				} catch (InterruptedException e) {
					statusTextView.append(e.getMessage());
				}
			}
			if(embeddedGPSThread != null && embeddedGPSThread.isRunning()){
				embeddedGPSThread.stopRunning();
			}
			this.finish();

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 
	 */
	private void createAndShowConnectionSettingDialog() {
		final Dialog connectionSettingDialog = new Dialog(MainActivity.this);
		connectionSettingDialog
				.setContentView(R.layout.dialog_connection_setting2);

		connectionSettingDialog
				.setTitle(R.string.dialog_connectionSetting_message);

		final Button optionButton = (Button) connectionSettingDialog
				.findViewById(R.id.option_button);
		OnClickListener optiononClickListener = new OnClickListener() {

			@Override
			public void onClick(final View arg0) {
				createAndShowTCPClientSettingDialog();
			}
		};

		optionButton.setOnClickListener(optiononClickListener);

		Spinner streamTypeSpinner1 = (Spinner) connectionSettingDialog
				.findViewById(R.id.stream_type_spinner1);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				MainActivity.this, R.array.stream_type, R.layout.listview);
		streamTypeSpinner1.setAdapter(adapter);

		OnItemSelectedListener strTypeSpin1OISL = new OnItemSelectedListener() {

			@Override
			public void onItemSelected(final AdapterView<?> spinner,
					final View textview, final int index, final long indexLong) {
				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putInt("stream type", index);
				editor.commit();

				switch (index) {
				case MainActivity.TCP_CLIENT_STREAM_TYPE:
					optionButton.setEnabled(true);
					break;
				case MainActivity.EMBEDDED_GPS_STREAM_TYPE:
					optionButton.setEnabled(false);
					break;
				default:
					break;
				}
			}

			@Override
			public void onNothingSelected(final AdapterView<?> arg0) {
				Log.d("hiro", "what happens when nothing selected");
			}

		};
		streamTypeSpinner1.setOnItemSelectedListener(strTypeSpin1OISL);

		final Button okButton = (Button) connectionSettingDialog
				.findViewById(R.id.ok_button);
		OnClickListener okonClickListener = new OnClickListener() {

			@Override
			public void onClick(final View arg0) {
				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = sharedPref.edit();
				String tmpIpAddress = "";
				tmpIpAddress = sharedPref.getString("tmp_ip_address", "");
				if (!tmpIpAddress.equals("")) {
					editor.putString("ip_address", tmpIpAddress);

					int tmpPortNumber = -1;
					tmpPortNumber = sharedPref.getInt("tmp_port number", -1);
					if (tmpPortNumber > -1) {
						editor.putInt("port number", tmpPortNumber);
						editor.commit();
						statusTextView.append("set-> ip: " + tmpIpAddress
								+ ", port: " + tmpPortNumber + "\n");
					}
				}
				connectionSettingDialog.dismiss();

			}
		};

		okButton.setOnClickListener(okonClickListener);

		final Button cancelButton = (Button) connectionSettingDialog
				.findViewById(R.id.cancel_button);
		OnClickListener cancelonClickListener = new OnClickListener() {

			@Override
			public void onClick(final View arg0) {
				// do nothing!?
				connectionSettingDialog.dismiss();
			}
		};

		cancelButton.setOnClickListener(cancelonClickListener);

		// Create the AlertDialog object and return it
		connectionSettingDialog.show();

	}

	/**
	 * 
	 */
	private void createAndShowTCPClientSettingDialog() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = sharedPref.edit();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		View dialogView = inflater.inflate(R.layout.dialog_connection_setting,
				null);
		builder.setView(dialogView);
		final EditText ipEditText = (EditText) dialogView
				.findViewById(R.id.ip_editText);
		String hostname = "";
		hostname = sharedPref.getString("ip_address", "");
		if (!hostname.equals("")) {
			ipEditText.setHint("current address: " + hostname);
		}
		final EditText portEditText = (EditText) dialogView
				.findViewById(R.id.port_editText);
		int portNumber = -1;
		portNumber = sharedPref.getInt("port number", -1);
		if (portNumber > 0) {
			portEditText.setHint("current port: " + portNumber);
		}

		builder.setMessage(R.string.dialog_connectionSetting_message);
		DialogInterface.OnClickListener okOnClickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				if (ipEditText != null && portEditText != null) {
					String hostname = ipEditText.getText().toString();
					try {
						String portNumberStr = portEditText.getText()
								.toString();
						int portNumber = Integer.parseInt(portNumberStr);
						if (!hostname.equals("")) {
							if (portNumber > 0) {
								editor.putString("tmp_ip_address", hostname);
								editor.putInt("tmp_port number", portNumber);
								editor.commit();
								statusTextView.append("set-> ip: " + hostname
										+ ", port: " + portNumber + "\n");
							} else {
								statusTextView
										.append("invalid port number: ["
												+ portEditText.getText()
														.toString()
												+ "] Please set ConnectionSetting again\n");
							}
						} else {
							statusTextView.append("no hostname.\n");
						}
					} catch (NumberFormatException e) {
						Log.e("error", e + portEditText.getText().toString());
						statusTextView.append("invalid port number: ["
								+ portEditText.getText().toString()
								+ "] Please set up again\n");
					}
				} else {
					Log.e("error", "no EditText obj");
				}

			}
		};
		builder.setPositiveButton("OK", okOnClickListener);

		DialogInterface.OnClickListener cancelOnClickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, int which) {
				// do nothing

			}

		};

		builder.setNegativeButton("Cancel", cancelOnClickListener);
		builder.create().show();
	}

	/**
	 * 
	 */
	private void startGPS() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.embeddedGPSThread = new EmbeddedGPSThread(locationManager);
		final Handler handler = new Handler();
		embeddedGPSThread.setMessageListener(new MessageListener() {

			@Override
			public void sendMessage(final String message) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						plotview.setPoint(message);
						plotview.plot();
						// statusTextView.append(message + "\n");
						scrollview.scrollTo(0, statusTextView.getBottom());
					}

				});
			}
		});
		embeddedGPSThread.startRunning();
		menuConnect.setEnabled(false);
		menuDisconnect.setEnabled(true);
	}

	/**
	 * 
	 * @param hostname
	 *            Host name of IP
	 * @param portNumber
	 *            port number
	 * @throws IOException
	 *             connection
	 * @throws InterruptedException
	 *             thread
	 */
	protected final void connect(final String hostname, final int portNumber)
			throws IOException, InterruptedException {
		statusTextView.append("connecting... -> ip: " + hostname + ", port: "
				+ portNumber + "\n");
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		final Handler handler = new Handler();
		if (networkInfo != null && networkInfo.isConnected()) {

			this.tcpClientThread = new TCPClientThread();
			tcpClientThread.setHostName(hostname);
			tcpClientThread.setPortNumber(portNumber);
			tcpClientThread.setMessageListener(new MessageListener() {

				@Override
				public void sendMessage(final String message) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							plotview.setPoint(message);
							plotview.plot();
							// statusTextView.append(message + "\n");
							scrollview.scrollTo(0, statusTextView.getBottom());
						}

					});
				}
			});
			tcpClientThread.start();

			// wait until tcpClientThread set running flag.
			tcpClientThread.join(waitingMiliSec);
			if (tcpClientThread.isRunning()) {
				statusTextView.append("connected.\n");
				menuConnect.setEnabled(false);
				menuDisconnect.setEnabled(true);

			} else {
				statusTextView.append("not connected.\n");
			}
		} else {
			throw new IOException(
					"connection is not available now, please check "
							+ "your configurataion of your smartphone.\n");
		}
	}

	/**
	 * @throws IOException
	 *             connection
	 */
	protected final void disconnect() throws IOException {

		if (this.tcpClientThread != null) {
			this.tcpClientThread.stopClient();
		}
		statusTextView.append("disconnected.\n");
		menuConnect.setEnabled(true);
		menuDisconnect.setEnabled(false);
	}

	@Override
	public final boolean onTouchEvent(final MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 1) {
				moveStartX = event.getX();
				moveStartY = event.getY();
			} else if (event.getPointerCount() >= 2) {
				float x = event.getX(0) - event.getX(1);
				float y = event.getY(0) - event.getY(1);
				pinchStartDistance = Math.sqrt(x * x + y * y);
				// statusTextView.append("down. dis is :" + pinchStartDistance
				// + "\n");
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 1) {
				if (!plotview.isFixTrackCenter()) {
					double deltaX = event.getX() - this.moveStartX;
					double deltaY = event.getY() - this.moveStartY;
					plotview.moveAll(deltaX, deltaY);
					this.moveStartX = event.getX();
					this.moveStartY = event.getY();
					plotview.plot();
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_UP:
			if (event.getPointerCount() >= 2) {
				float x = event.getX(0) - event.getX(1);
				float y = event.getY(0) - event.getY(1);
				pinchEndDistance = Math.sqrt(x * x + y * y);
				// statusTextView.append("up. dis is :" + pinchEndDistance +
				// "\n");
				if (pinchEndDistance > 0 && pinchStartDistance > 0) {

					plotview.setZoom(pinchEndDistance / pinchStartDistance);
					plotview.plot();
					pinchStartDistance = 0.0;
					pinchEndDistance = 0.0;
				}
			}
			break;
		default:
			break;
		}
		return true;
	}
}
