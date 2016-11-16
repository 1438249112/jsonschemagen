package com.rj.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DataField {
	private String _name;
	private DataType _type;
	private boolean _canbenull;
	private Set<String> _samples;
	private double _max;
	private double _min;
	private long _count;
	private List<DataField> _children = null;
	
	public DataField(String name, DataType type){
		this._name = name;
		this._type= type;
		this._count = 0;
		this._max = Double.MIN_VALUE;
		this._min = Double.MAX_VALUE;
		this._canbenull = false;
		this._children = new ArrayList<DataField>();
		this._samples = new TreeSet<>();
	}
	
	public void setMin(double min){
		if(_min > min)
			_min = min;
	}
	
	public void setMax(double max){
		if(_max < max)
			_max = max;
	}
	
	public void setCanBeNull(){
		_canbenull = true;
	}
	
	public void addSample(String sample){
		if(_samples.size() < 10)
			_samples.add(sample);
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
}
