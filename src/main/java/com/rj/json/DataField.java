package com.rj.json;

import java.util.*;

public class DataField {
	private String _name, _subName;
	private DataType _type;
	private boolean _canbenull;
	private static String[] sampleIgnores= {"business_id", "user_id", "review_id", "tip_id", "text", "friends"};
	private Set<String> _samples;
	private int _maxsamplesize;
	private double _max;
	private double _min;
	private long _avg;
	private long _count;
	private List<DataField> _children = null;
	
	public DataField(String name, DataType type){
		this._name = name;
		this._type= type;
		this._count = 0;
		this._avg = 0;
		this._max = Double.MIN_VALUE;
		this._min = Double.MAX_VALUE;
		this._canbenull = false;
		this._children = new ArrayList<DataField>();
		this._samples = new TreeSet<>();
		this._maxsamplesize = 0;
		this._subName = _name.lastIndexOf("_")>0?_name.substring(0,_name.lastIndexOf("_")):null;
	}
	
	public void setMin(double min){
		if(_min > min)
			_min = min;
	}
	
	public void setMax(double max){
		if(_max < max)
			_max = max;
	}
	
	public void setAvg(int len){
		_avg=(_avg+len);
	}
	
	public void setCanBeNull(){
		_canbenull = true;
	}
	
	private boolean ignoreSample(){
	  if(_subName != null && Arrays.asList(sampleIgnores).contains(_subName))
	    return true;
	  return Arrays.asList(sampleIgnores).contains(_name);
	}
	public void addSample(String sample){
		if(_samples.size() < 10 || (_type == DataType.STRING && !ignoreSample())){
			_maxsamplesize=sample.length()>_maxsamplesize?sample.length():_maxsamplesize;
			_samples.add(sample);
		}
	}
	
	public Set<String> getSamples(){
		return _samples;
	}
	
	public boolean canBeNull(){
		return _canbenull;
	}
	
	public synchronized void incrementCount(){
		_count++;
	}
	
	public long getCount(){
		return _count;
	}
	
	public void addChild(DataField child){
		_children.add(child);
	}
	
	public List<DataField> getChildren(){
		return _children;
	}
	
	public DataField getChild(String name){
		for(DataField child:_children){
			if(child.getName().equals(name))
				return child;
		}
		return null;
	}
	
	public DataField getChild(DataType type){
		for(DataField df:_children){
			if(df.getType() == type)
				return df;
		}
		return null;
	}
	
	public DataType getType(){
		return _type;
	}
	
	public double getMin(){
		return _min;
	}
	
	public String getName(){
		return _name;
	}
	
	public double getMax(){
		return _max;
	}
	
	public long getAvg(){
		return _avg/_count;
	}
}
