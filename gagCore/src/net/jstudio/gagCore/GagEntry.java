package net.jstudio.gagCore;

public class GagEntry {
	private int _id;
	private String _entryName, _entryUrl;
	private EntryType _type;
	
	public GagEntry(int id, 
			String entryName,
			String entryUrl,
			EntryType type
			){
		this._id = id;this._entryName = entryName;
		this._entryUrl = entryUrl;this._type = type;
	}
	
	public int getID(){return _id;}
	public String getEntryName(){return _entryName;}
	public String getEntryUrl(){return _entryUrl;}
	public EntryType getEntryType(){return _type;}
}
