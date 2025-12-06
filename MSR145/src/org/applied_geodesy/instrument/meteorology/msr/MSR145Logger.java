/***********************************************************************
* Copyright by Michael Loesler, https://software.applied-geodesy.org   *
*                                                                      *
* This program is free software; you can redistribute it and/or modify *
* it under the terms of the GNU General Public License as published by *
* the Free Software Foundation; either version 3 of the License, or    *
* at your option any later version.                                    *
*                                                                      *
* This program is distributed in the hope that it will be useful,      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of       *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
* GNU General Public License for more details.                         *
*                                                                      *
* You should have received a copy of the GNU General Public License    *
* along with this program; if not, see <http://www.gnu.org/licenses/>  *
* or write to the                                                      *
* Free Software Foundation, Inc.,                                      *
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.            *
*                                                                      *
***********************************************************************/

package org.applied_geodesy.instrument.meteorology.msr;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

import org.applied_geodesy.instrument.meteorology.msr.util.CRC8;
import org.applied_geodesy.io.rxtx.ReceiveDataType;
import org.applied_geodesy.io.rxtx.ReceiverExchangeable;
import org.applied_geodesy.io.rxtx.RxTx;
import org.applied_geodesy.io.rxtx.RxTxReturnable;

public class MSR145Logger extends MSR145Sensor implements RxTxReturnable, ReceiverExchangeable  {
	
	private boolean isBusy = false;
	private long timeOut = 2000L;
	private int responseLength = 0;
	private int responseData[] = new int[8];
	private RxTx connRxTx;
	
	public MSR145Logger(RxTx connRxTx) {
		this.connRxTx = connRxTx;
		this.connRxTx.setReceiveDataType(ReceiveDataType.INTEGER);
	}

	@Override
	public synchronized double[] getMeasuredValues(int sensortType1, int sensortType2, int sensortType3) {
		this.isBusy = true;
		try {
			// Ermittle Messwerte
			this.detectMeasuredValues();
			synchronized( this.connRxTx ) {
				try {
					if (this.timeOut > 0)
						this.connRxTx.wait(this.timeOut);
					else
						this.connRxTx.wait();
				}
				catch ( Exception e ){
					e.printStackTrace();
				}
			}

			int crc8 = CRC8.calc(this.responseData, 7);
			if (!(this.responseData[7] - crc8 == 0 || this.responseData[7] - (crc8 & 0xff) == 0)) {
				System.err.println(this.getClass().getSimpleName()+" MSR-Uebertragungsfehler, Widerspruch in CRC8-Byte! " + this.responseData[7] + "  (" + crc8 + ")");
				return null;	
			}
			
			// Warte, bis Messwerte ermittelt
			Thread.sleep(500);
			
			// Ist die Verbindung in der Zwischenzeit beendet worden?
			if (!this.isBusy)
				return null;
			
			// Frage Messwerte ab
			this.readMeasuredValues(sensortType1, sensortType2, sensortType3);
			synchronized( this.connRxTx ) {
				try {
					if (this.timeOut > 0)
						this.connRxTx.wait(this.timeOut);
					else
						this.connRxTx.wait();
				}
				catch ( Exception e ){
					e.printStackTrace();
				}
			}

			crc8 = CRC8.calc(this.responseData, 7);
			if (!(this.responseData[7] - crc8 == 0 || this.responseData[7] - (crc8 & 0xff) == 0)) {
				System.err.println(this.getClass().getSimpleName()+" MSR-Uebertragungsfehler, Widerspruch in CRC8-Byte! " + this.responseData[7] + "  (" + crc8 + ")");
				return null;	
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			this.isBusy = false;
		}
		
		return new double[] {
	             this.getValue(sensortType1, this.responseData[1], this.responseData[2]),
	             this.getValue(sensortType2, this.responseData[3], this.responseData[4]),
	             this.getValue(sensortType3, this.responseData[5], this.responseData[6]),
	    };
	}

	/**
	 * Weise den MSR-Sensor an, Messwerte zu erfassen und
	 * diese in seinem internen Puffer abzulegen.
	 * 
	 * @throws IOException
	 */
	private void detectMeasuredValues() throws IOException {
		String requestHex = "860300FFFF000079";
		byte byteData[] = this.to8ByteArray(requestHex);
		this.connRxTx.transmit(byteData);
	}
	
	/**
	 * Liest die letzten ermittelten Messwerte aus 
	 * dem Puffer des MSR-Sensors aus.
	 * 
	 * @throws IOException
	 */
	private void readMeasuredValues(int sensortType1, int sensortType2, int sensortType3) throws IOException {
		String requestHex = String.format(Locale.ENGLISH, "8202%2d%2d%2d000000", sensortType1, sensortType2, sensortType3).replace(' ', '0');
		// Bestimme Daten Array der ersten 7 Byte
		byte byteData[] = this.to8ByteArray(requestHex);
		// Bestimme CRC8 und fuege als 8. Byte ins Array
		byte crc8 = CRC8.calc(byteData, 7);
		byteData[7] = crc8;
		this.connRxTx.transmit(byteData);
	}
	
	private byte[] to8ByteArray(String str) {
		byte[] bts = new BigInteger(str, 16).toByteArray();
    	
    	if (bts.length == 8)
    		return bts;
    	
    	byte[] byteData = new byte[8];
    	for (int i=0; i<8; i++)
    		byteData[i] = bts[i+1];
    	
    	return byteData;
	}

	@Override
	public void receive(byte[] bytesRX) throws IOException {
		throw new IOException("Error, unsupported method call. Use receive(int intRX) for data transfer.");
	}
	
	@Override
	public void receive(int intRX) throws IOException {
		this.responseData[this.responseLength++] = intRX & 0xff;
		if (this.responseLength == this.responseData.length) {
			this.responseLength = 0;
			synchronized(this.connRxTx){
				this.connRxTx.notify();
        	}
		}
	}

	@Override
	public RxTx getRxTx() {
		return this.connRxTx;
	}
}
