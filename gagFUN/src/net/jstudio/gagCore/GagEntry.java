package net.jstudio.gagCore;

public class GagEntry {
	private int _id;
	private String _entryName, _entryUrl, _linkImg;
	private EntryType _type;
	
	public GagEntry(int id, 
			String entryName,
			String entryUrl,
			String link,
			EntryType type
			){
		this._id = id;this._entryName = entryName;
		this._entryUrl = entryUrl;this._type = type;
		this._linkImg = link;
	}
	
	public int getID(){return _id;}
	public String getEntryName(){return _entryName;}
	public String getEntryUrl(){return _entryUrl;}
	public EntryType getEntryType(){return _type;}
	public String getLink(){return _linkImg;}
}
