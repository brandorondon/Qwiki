package framework.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

public class StringIntegerList implements Writable {
	
	protected  int[] convertPos(String positions){
		String[] split = positions.replaceAll(" ", "").split(",");
		int[] intPos = new int[split.length];
		int c = 0;
		for(String s: split){
			intPos[c] = Integer.parseInt(s.trim());
			c++;
		}
		return intPos;
	}
	
	public static class StringInteger implements Writable {
		private String s;
		private int t;
		private int[] pos;
		private static Pattern pat = Pattern.compile("(.+),(\\d+),\\[(.*?)\\]");
		
		protected  int[] convertPos(String positions){
			String[] split = positions.split(",");
			int[] intPos = new int[split.length];
			int c = 0;
			for(String s: split){
				intPos[c] = Integer.parseInt(s.trim());
				c++;
			}
			return intPos;
		}

		public StringInteger() {
		}
		
		//constructor for actual StringInteger pair; used in Tokenizer
		public StringInteger(String s, int t){
			this.s = s;
			this.t = t;
			this.pos = null;
		}

		public StringInteger(String s, int t, int[] positions) {
			this.s = s;
			this.t = t;
			this.pos = positions;
		}

		public String getString() {
			return s;
		}

		public int getValue() {
			return t;
		}
		
		public int[] getPositions(){
			return pos;
		}

		public void readFields(DataInput arg0) throws IOException {
			String indexStr = arg0.readUTF();

			Matcher m = pat.matcher(indexStr);
			if (m.matches()) {
				this.s = m.group(1);
				this.t = Integer.parseInt(m.group(2));
				this.pos = convertPos(m.group(3));
			}
		}
		
		public void write(DataOutput arg0) throws IOException {
			StringBuffer sb = new StringBuffer();
			sb.append(this.s);
			sb.append(",");
			sb.append(this.t);
			sb.append(",");
			sb.append(Arrays.toString(this.pos));
			arg0.writeUTF(sb.toString());
		}

		@Override
		public String toString() {
			return this.s + "," + this.t + "," + Arrays.toString(this.pos);
		}
	}
	//TODO: make changes to SIL to support the new positions array in StringInteger
	private List<StringInteger> indices;
	private Map<String, Integer> freqMap;
	private Pattern p = Pattern.compile("<([^>]+),(\\d+),\\[(.*?)\\]>");

	public StringIntegerList() {
		indices = new Vector<StringInteger>();
	}

	public StringIntegerList(List<StringInteger> indices) {
		this.indices = indices;
	}
	
	//modify this to form the SIL with the new positions array
//	public StringIntegerList(Map<String, Integer> indiceMap) {
//		this.indiceMap = indiceMap;
//		this.indices = new Vector<StringInteger>();
//		for (String index : indiceMap.keySet()) {
//			this.indices.add(new StringInteger(index, indiceMap.get(index), null)); //TODO: !! NULL for now
//		}
//	}

//	public Map<String, Integer> getMap() {
//		if (this.indiceMap == null) {
//			indiceMap = new HashMap<String, Integer>();
//			for (StringInteger index : this.indices) {
//				indiceMap.put(index.s, index.t);
//			}
//		}
//		return indiceMap;
//	}
	
	public Map<String, Integer> getFreqMap(){
		if (this.freqMap == null){
			this.freqMap = new HashMap<String, Integer>();
			for(StringInteger index : this.indices){
				this.freqMap.put(index.s, index.t);
			}
		}
		return this.freqMap;
	}

	public void readFields(DataInput arg0) throws IOException {
		String indicesStr = WritableUtils.readCompressedString(arg0);
		readFromString(indicesStr);
	}

	public void readFromString(String indicesStr) throws IOException {
		List<StringInteger> tempoIndices = new Vector<StringInteger>();
		Matcher m = p.matcher(indicesStr);
		while (m.find()) {
			StringInteger index = new StringInteger(m.group(1), Integer.parseInt(m.group(2).trim()), convertPos(m.group(3))); 
			tempoIndices.add(index);
		}
		this.indices = tempoIndices;
	}

	public List<StringInteger> getIndices() {
		return Collections.unmodifiableList(this.indices);
	}

	public void write(DataOutput arg0) throws IOException {
		WritableUtils.writeCompressedString(arg0, this.toString());
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < indices.size(); i++) {
			StringInteger index = indices.get(i);
			if (index == null || index.getString() == null)
				continue;
			if (index.getString().contains("<") || index.getString().contains(">"))
				continue;
			sb.append("<");
			sb.append(index.getString());
			sb.append(",");
			sb.append(index.getValue());
			sb.append(",");
			sb.append(Arrays.toString(index.getPositions()));
			sb.append(">");
			if (i != indices.size() - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
}
