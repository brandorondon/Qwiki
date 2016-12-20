package com.qwiki.util;

import java.util.List;

public class QueryData implements Comparable  {
	private String docId;
	private Integer freq;
	private int[] sampleArea;
	private List<String> tokens;
	
	public QueryData(String docId, Integer freq, int[] sampleArea){
		this.sampleArea = sampleArea;
		this.docId = docId;
		this.freq = freq;
		this.tokens = null;
	}
	
	public QueryData(String docId, Integer freq, int[] sampleArea, List<String> tokens){
		this.sampleArea = sampleArea;
		this.docId = docId;
		this.freq = freq;
		this.tokens = tokens;
	}
	
	public int compareTo(Object o){
		QueryData other = (QueryData) o;		
		if(other.getFreq() > this.freq){
			return 1;
		} else if (other.getFreq() < this.freq){
			return -1;
		} else 
			return 0;
	}
	
	public String getDocId(){
		return this.docId;
	}
	
	public Integer getFreq(){
		return this.freq;
	}
	
	public int[] getSampleArea(){
		return this.sampleArea;
	}
	
	public List<String> getTokens(){
		return this.tokens;
	}
}
