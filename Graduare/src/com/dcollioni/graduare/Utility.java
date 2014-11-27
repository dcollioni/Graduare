package com.dcollioni.graduare;

import java.math.RoundingMode;
import java.text.NumberFormat;

public class Utility {

	public static String parseDouble(double d) {
		NumberFormat nf= NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setRoundingMode(RoundingMode.HALF_UP);

		return nf.format(d);
	}
}