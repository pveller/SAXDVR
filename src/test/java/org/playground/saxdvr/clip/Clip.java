package org.playground.saxdvr.clip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Clip {

	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	public String title;
	public String category;
	public Date date;

	public Clip() {
	}

}
