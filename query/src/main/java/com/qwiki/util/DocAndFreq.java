package com.qwiki.util;

public class DocAndFreq implements Comparable  {
	private String docId;
	private Integer freq;
	private int[] sampleArea;
	
	public DocAndFreq(String docId, Integer freq, int[] sampleArea){
		this.sampleArea = sampleArea;
		this.docId = docId;
		this.freq = freq;
	}
	
	public int compareTo(Object o){
		DocAndFreq other = (DocAndFreq) o;		
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
}
