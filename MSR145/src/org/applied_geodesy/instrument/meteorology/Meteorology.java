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

package org.applied_geodesy.instrument.meteorology;

/**
 * <p>Zur Bestimmung der ersten Geschwindigkeitskorrektur aus meteorologischen Messungen
 * stellt diese Klasse entsprechende Methoden zur Verfuegung. Die Formeln zur Berechnung
 * entstammen:</p>
 * 
 * <ul>
 * <li>Joeckel, R., Stober M.: Elektronische Entfernungs- und Richtungsmessung, 
 * 4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999.</li>
 * <li>Joeckel, R., Stober, M., Huep, W.: Elektronische Entfernungs- und Richtungsmessung, 
 * 5. Auflage, Wichmann, Berlin/Heidelberg, 2008.</li>
 * <li>R&uuml;eger, J.M.: Electronic Distance Measurement - An Introduction 
 * 3. Auflage, Springer, Berlin/Heidelberg, 1990</li>
 * </ul>
 * 
 * @author Michael Loesler
 *
 */
public class Meteorology {
	/** Konstante fuer Feuchte-Thermometer (nass) */
	public final static double K     =   0.67;
	/** Konstante fuer Feuchte-Thermometer (nass) */
	public final static double ALPHA =   7.5;
	/** Konstante fuer Feuchte-Thermometer (nass) aus Joeckel/Stober/Huep 2008*/
	public final static double BETA  = 237.3;
	/** Konstante fuer Feuchte-Thermometer (nass) */
	public final static double GAMMA =   0.78571;
	
	private Meteorology() { };
	
	/**
	 * <p>Liefert den Saettigungsdampfdruck <code>E</code> fuer die uebergebene Temperatur <code>t</code></p>
	 *
	 * <p><code>E(t) = 10<sup>(a*t/(b+t)+c)</sup></code></p>
	 * 
	 * <ul>
	 * <li>a = 7.5</li>
	 * <li>b = 237.3</li>
	 * <li>c = 0.78571</li>
	 * </ul>
	 * 
	 * @see Joeckel, R., Stober M.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                              4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999
	 *                              Seite 76
	 *
	 * @param t   Temperatur  [&#8451;]
	 * @return E  Saettigungsdampfdruck [hPa]
	 */
	public static double getSaturationVapourPressure(double t) {
		return Math.pow(10.0, (ALPHA*t)/(BETA+t)+GAMMA);
	}
	
	/**
	 * <p>Liefert den Gruppenbrechungsindex <code>n<sub>Gr</sub></code></p> 
	 * 
	 * <p><code>n<sub>Gr</sub> = 1.0 + 10<sup>-6</sup>*(a + 3*b/&lambda;<sub>T</sub><sup>2</sup> + 5*c/&lambda;<sub>T</sub><sup>4</sup>)</sup></code></p>
	 * <p>Die Parameter <code>a</code>, <code>b</code> und <code>c</code> haengen vom gewaehlten Modell ab.</p>
	 * 
	 * <p>Edlen (1953)</p>
	 * <ul>
	 * <li>a = 287.569</li>
	 * <li>b =  1.6206</li>
	 * <li>c =  0.0139</li>
	 * </ul>
	 * 
	 * <p>Edlen (1966)</p>
	 * <ul>
	 * <li>a = 287.583</li>
	 * <li>b =  1.6134</li>
	 * <li>c =  0.0144</li>
	 * </ul>
	 * 
	 * <p>CIDDOR (1996)</p>
	 * <ul>
	 * <li>a = 287.6155</li>
	 * <li>b =  1.62887</li>
	 * <li>c =  0.01360</li>
	 * </ul>
	 * 
	 * <p>Barrell &amp; Sears (1939)</p>
	 * <ul>
	 * <li>a = 287.604</li>
	 * <li>b =  1.6288</li>
	 * <li>c =  0.0136</li>
	 * </ul>
	 *
	 * <p>Formel ist definiert fuer</p>
	 * <ul>
	 * <li>t = 0&#8451;</li>
	 * <li>p = 1013.25hPa</li>
	 * <li>CO<sub>2</sub> = 0.03% (CIDDOR (1996) 0.0375%</li>
	 * <li>trockene Luft</li>
	 * </ul>
	 * 
	 * <p>Geltungsbereich des Dispersionsmodells</p>
	 * <ul>
	 * <li>0.18 &lt; &lambda;<sub>T</sub> &lt; 0.65 Edlen (1953)</li> 
	 * <li>0.18 &lt; &lambda;<sub>T</sub> &lt; 2.10 Edlen (1966)</li>
	 * <li>0.43 &lt; &lambda;<sub>T</sub> &lt; 0.65 Barrell &amp; Sears (1939)</li>
	 * </ul>
	 * 
	 * @see Joeckel, R., Stober, M.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                               4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999
	 *                               Seite 72f
	 * @see Joeckel, R., Stober, M., Huep, W.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                               5. Auflage, Wichmann, Berlin/Heidelberg, 2008
	 *                               Seite 97f
	 * @param model           Empirische Parameter zur Bestimmung des Gruppenbrechungsindex
	 * @param lamdaT          Traegerwellenlaenge &lambda;<sub>T</sub> [&mu;m]
	 * @return n<sub>Gr</sub> Brechungsindex
	 */
	public static double getGroupRefractiveIndex(DispersionModel model, double lamdaT) {
		double a,b,c;
		switch(model) {
			case EDLEN_1953:
				a = 287.569;
				b =  1.6206;
				c =  0.0139;
			break;
			case EDLEN_1966:
				a = 287.583;
				b =  1.6134;
				c =  0.0144;
			break;
			case CIDDOR_1996:
				a = 287.6155;
				b =  4.88660/3.0; // ~1.62887;
				c =  0.01360;
			break;
			default: // BARRELL_AND_SEARS_1939
				a = 287.604;
				b =  1.6288;
				c =  0.0136;
			break;
		}
		return 1.0 + 1.0E-6*(a + 3.0*b/Math.pow(lamdaT,2) + 5.0*c/Math.pow(lamdaT,4));
	}
	
	/**
	 * <p>Liefert den Partialdruck des Wasserdampfs <code>e</code></p>
	 * 
	 * <p><code>e = E<sub>w</sub> - (t-tw) * K/1006.6*p</code> mit Saettigungsdampfdruck
	 * <code>E<sub>w</sub></code> und <code>K = 0.67</code></p>
	 * 
	 * @see Joeckel, R., Stober M.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                              4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999
	 *                              Seite 75
	 *                              
	 * @param t  Trockentemperatur [&#8451;]
	 * @param tw Feuchttemperatur [&#8451;]
	 * @param p  Luftdruck [hPa]
	 * @return e Partialdruck [hPa]
	 */
	public static double getPartialPressure(double t, double tw, double p) {
		double Ew = getSaturationVapourPressure(tw);
		return Ew - (t-tw)*K/1006.6*p;
	}
	
	/**
	 * <p>Liefert den Partialdruck des Wasserdampfs <code>e</code></p>
	 * 
	 * <p><code>e = E * r/100</code> mit Saettigungsdampfdruck <code>E</code></p>
	 * 
	 * @see Joeckel, R., Stober M.: Elektronische Entfernungs- und Richtungsmessung, 
	 *                              4. Auflage, Verlag Konrad Wittwer Stuttgart, 1999
	 *                              Seite 76
	 *                              
	 * @param t  Trockentemperatur [&#8451;]
	 * @param r  Relative Luftfeuchte [%]
	 * @return e Partialdruck [hPa]
	 */
	public static double getPartialPressure(double t, double r) {
		double E = getSaturationVapourPressure(t);
		return E * 0.01*r;
	}
		
	/**
	 *
	 * <p>Liefert den Brechungsindex <code>n<sub>L</sub></code></p>
	 * <p><code>n<sub>L</sub> = 1 + (n<sub>Gr</sub>-1) * 273.15/1013.25 * p/(t+273.15) - 11.27*10<sup>-6</sup>/(t+273.15) * e</code></p>
	 * <p>mit Partialdruck des Wasserdampfs <code>e</code>, Temperatur <code>t</code>, Druck <code>p</code> und Gruppenbrechungsindex <code>n<sub>Gr</sub></code></p>
	 *
	 * @see R&uuml;eger, J.M.: Electronic Distance Measurement - An Introduction 
	 *                         3. Auflage, Springer, Berlin/Heidelberg, 1990
	 *                         Seite 55
	 *
	 * @param lamdaT  Traegerwellenlaenge [&mu;m]
	 * @param t       Temperatur [&#8451;]
	 * @param p       Luftdruck [hPa]
	 * @param e       Partialdruck des Wasserdampfs [hPa]
	 * @return nL     Brechungsindex
	 */
	public static double getRefractiveIndex(DispersionModel model, double lamdaT, double t, double p, double e) {
		double ng = getGroupRefractiveIndex(model, lamdaT);
		double T = t + 273.15;
		return 1.0 + (ng - 1.0) * 273.15/1013.25 * p/T - 11.27E-6/T * e;
	}
	
	/**
	 * <p>Konvertiert aus der Trockentemperatur, dem Luftdruck und der relativen Luftfeuchte die zugehoerige Feuchttemperatur.</p>
	 * <p>Umkehrung der Berechnung <code>r = 100*e/E</code> mit dem Saettigungsdampfdruck <code>E</code> und dem Partialdruck <code>e</code>.</p>
	 * 
	 * @param temperature      Trockentemperatur [&#8451;]
	 * @param pressure         Luftdruck [hPa]
	 * @param relativeHumidity Relative Luftfeuchte [%] 
	 * @return tw              Feuchttemperatur  [&#8451;]
	 * @throws ArithmeticException Wenn max. Anzahl an Iterationen (100) erreicht sind.
	 */
	public static double humidity2WetBulbTemperature(double temperature, double pressure, double relativeHumidity) throws ArithmeticException {
		if (relativeHumidity < 0)
			return 0.0;

		final double EPS = 1.0E-5;
		int maxIteration = 100;
		double currentHumidity = 100.0;

		double twUpper = temperature;
		double twLower = temperature;
		double E = getSaturationVapourPressure(temperature);

		// Bestimmung des unteren Grenzbereiches twLower
		while ( currentHumidity > relativeHumidity && maxIteration-- > 0) {
			twLower -= 10;
			double e = getPartialPressure(temperature, twLower, pressure);
			currentHumidity = e/E*100.0;
		}
		
		if(maxIteration<0)
			throw new ArithmeticException("Fehler beim Berechnen der Feuchttemperatur! Differenz: " + relativeHumidity);
		    
		maxIteration = 100;
		// Berechnung der Feuchttemperatur
		do {
			double tw = 0.5*(twUpper+twLower);
			double e = getPartialPressure(temperature, tw, pressure);
			currentHumidity = e/E*100.0;

			if (currentHumidity > relativeHumidity)
				twUpper = tw;
			else if (currentHumidity < relativeHumidity) 
				twLower = tw;
			// System.out.println( (currentHumidity - relativeHumidity)+"  "+currentHumidity + "  " +relativeHumidity);
		}
		while ( Math.abs(currentHumidity - relativeHumidity) > EPS && maxIteration-- > 0);
		
		if(maxIteration<0)
			throw new ArithmeticException("Fehler beim Berechnen der Feuchttemperatur! Differenz: " + Math.abs(currentHumidity - relativeHumidity));
		return 0.5*(twUpper+twLower);
	}
	
	/**
	 * <p>Konvertiert aus der Trockentemperatur, dem Luftdruck und der Feuchttemperatur die zugehoerige relative Luftfeuchte.</p>
	 * <p><code>r = 100*e/E</code> mit dem Saettigungsdampfdruck <code>E</code> und dem Partialdruck <code>e</code>.</p>
	 * 
	 * @param temperature        Trockentemperatur [&#8451;]
	 * @param pressure           Luftdruck [hPa]
	 * @param wetBulbTemperature Feuchttemperatur  [&#8451;]
	 * @return relativeHumidity  Relative Luftfeuchte [%]
	 */
	public static double wetBulbTemperature2Humidity(double temperature, double pressure, double wetBulbTemperature) {
		double E = getSaturationVapourPressure(temperature);
		double e = getPartialPressure(temperature, wetBulbTemperature, pressure);
		return e/E*100.0;
	}

}
