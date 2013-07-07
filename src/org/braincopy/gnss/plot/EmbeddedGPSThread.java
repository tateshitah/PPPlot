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

import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

/**
 * 
 * @author Hiroaki Tateshita
 * @version 0.6
 * 
 */
public class EmbeddedGPSThread extends Thread implements LocationListener,
		NmeaListener {
	/**
	 * 
	 */
	private boolean isRunning = false;

	/**
	 * 
	 */
	private MessageListener messageListener;

	/**
	 * 
	 */
	private LocationManager locationManager;
	
	/**
	 * 
	 * @param lm location manager
	 */
	public EmbeddedGPSThread(final LocationManager lm) {
		this.locationManager = lm;
		LocationProvider p = locationManager
				.getProvider(LocationManager.GPS_PROVIDER);
		locationManager.addNmeaListener(this);
		locationManager.requestLocationUpdates(p.getName(), 0, 0,
				(LocationListener) this);
	}

	/**
	 * 
	 */
	public final void run() {

	}

	/**
	 * 
	 */
	public final void stopClient() {
		isRunning = false;

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

	@Override
	public final void onNmeaReceived(final long timestamp, final String nmea) {
		if (isRunning) {
			this.messageListener.sendMessage(nmea);
		}

	}

	@Override
	public void onLocationChanged(final Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(final String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(final String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(final String arg0, final int arg1, final Bundle arg2) {

	}

	/**
	 * 
	 */
	public final void startRunning() {
		this.isRunning = true;
		
	}

	/**
	 * 
	 */
	public final void stopRunning() {
		this.isRunning = false;
		locationManager.removeNmeaListener(this);
		locationManager.removeUpdates(this);
	}

	/**
	 * 
	 * @param lm
	 */
	public final void setLocationManager(final LocationManager lm) {
		this.locationManager = lm;
	}
}
