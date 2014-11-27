package com.dcollioni.graduare.data;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

public class Db4oHelper {

	private String dir;
	private final String DB4O_FILE = "graduare.db4o";
	protected ObjectContainer db;
	
	public Db4oHelper(String dir) {
		this.dir = dir;
	}
	
	public void openDB() {
		String dbFile = dir + DB4O_FILE;
		db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), dbFile);
	}
	
	public void closeDB() {
		if (db != null) {
			db.close();
		}
	}
	
	public ObjectContainer db() {
		return db;
	}
}