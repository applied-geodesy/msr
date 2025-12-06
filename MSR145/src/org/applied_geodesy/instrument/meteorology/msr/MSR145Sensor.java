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

import org.applied_geodesy.instrument.meteorology.Meteorology;
import org.applied_geodesy.instrument.meteorology.MeteorologyParameters;
import org.applied_geodesy.instrument.meteorology.MeteorologySensor;

public abstract class MSR145Sensor implements MeteorologySensor {

	public final static int P  = 0,      // Druck [mbar]
	                        TP = 1,      // Temperatur am Drucksensor  [°C]
	                        X  = 2,      // Beschleunigungssensor in X [g]
	                        Y  = 3,      // Beschleunigungssensor in Y [g]
	                        Z  = 4,      // Beschleunigungssensor in Z [g]
	                        RH = 5,      // relative Luftfeuchte  [%]
	                        TRH= 6,      // Temperatur des Feuchtsensors [°C]
	                        T  = 7;      // Temperatur [°C]

	private double correctionValues[] = new double[] {
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
	};
	
	/**
	  * Ermittelt die Parameter der ausgelesenen Sensoren. Gibt <ocde>null</code>
	  * zurueck, wenn keine Werte ermittelt werden konnten.
	  *
	  * @param sensorType1
	  * @param sensorType2
	  * @param sensorType3
	  *
	  * @return values
	  */
	public abstract double[] getMeasuredValues(int sensortType1, int sensortType2, int sensortType3);
		
	/**
	  * Setzt Kalibrierwert fuer den gewaehlten Sensor
	  *
	  * @param sensorType
	  * @param value
	  */
	public void setCorrectionValue(int sensorType, double value) {
		this.correctionValues[sensorType] = value;
	}

	/**
	  * Liefert den Kalibrierwert fuer den gewaehlten Sensor
	  *
	  * @param sensorType
	  * @return value
	  */
	public double getCorrectionValue(int sensorType) {
		return this.correctionValues[sensorType];
	}

	/**
	 * Berechnet die korrekte Messgroesse aus den beiden Kanaelen
	 *
	 * @param sensorType
	 * @param chL
	 * @param chH
	 *
	 * @return value
	 */
	protected double getValue(int sensorType, int chL, int chH) {
		short in = (short)(256*chH + chL);
		//System.out.println(sensorType+"	"+chH+"	"+chL+"	"+in);
		double out = 0.0;
		// Luftdruck P [mbar]
		if (sensorType == MSR145Sensor.P)
			out = 0.1*in;

		// Temperatur des Durckmessers [°C]
		else if (sensorType == MSR145Sensor.TP)
			out = 0.1*in;

		// Beschleunigung X, Y bzw. Z
		else if (sensorType == MSR145Sensor.X || sensorType == MSR145Sensor.Y || sensorType == MSR145Sensor.Z) {
			if ((in & 0x2000) == 0)
				in = (short)(in & 0x1FFF);
			else
				in = (short)(in | 0xE000);
			out = 0.004*in;
		}

		// relative Luftfeuchte [%]
		else if (sensorType == MSR145Sensor.RH)
			out = 0.01*in;

		// Temperatur des Feuchtsenors [°C]
		else if (sensorType == MSR145Sensor.TRH)
			out = 0.01*in;

		// Temperatur [°C]
		else if (sensorType == MSR145Sensor.T) {
			if ((in & 0x4000) == 0) {
				//out = 70.0/2047.0*in-10.0;
				out = (in & 0x7FF)/2047.0*70.0-10.0;
			}
			else {
				//out = (in - 0x4000) * 0.0625;
				if ((in & 0x1000) == 0)
					in = (short)(in & 0xFFF);	//>=0
				else
					in = (short)(in | 0xF000); //<0
				out = 0.0625*in;
			}
		}
		return out + this.correctionValues[sensorType];
	}
	
	@Override
	public MeteorologyParameters getMeteorologyParameters() {
		//double ptrh2[] = this.getMeasuredValues(MSR145Sensor.T, MSR145Sensor.TP, MSR145Sensor.TRH);
		//System.out.println(java.util.Arrays.toString(ptrh2));
		
		double ptrh[] = this.getMeasuredValues(MSR145Sensor.P, MSR145Sensor.T, MSR145Sensor.RH);
		if (ptrh == null || ptrh.length != 3) {
			System.err.println( this.getClass().getSimpleName() + " Sensordatenabgriff nicht moeglich oder unvollstaendig! " + ptrh);
			return null;
		}

		double p  = ptrh[0];
		double dt = ptrh[1];
		double rh = ptrh[2];
		double wt = dt;
		try {
			wt = Meteorology.humidity2WetBulbTemperature(dt, p, rh);
		}
		catch (ArithmeticException e) {
			e.printStackTrace();
			return null;
		}
		return new MeteorologyParameters(p, dt, wt, rh);
	}
}
