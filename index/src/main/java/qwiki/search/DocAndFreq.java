package qwiki.search;


public class DocAndFreq implements Comparable{
	private String docId;
	private Integer freq;
	public DocAndFreq(String docId, Integer freq){
		this.docId = docId;
		this.freq = freq;
	}
	
	//return 1 instead of -1 if other is greater than this, because we
	//we want the map sorted from max to min
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
}