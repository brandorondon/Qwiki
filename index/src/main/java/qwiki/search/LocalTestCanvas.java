package qwiki.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import framework.util.StringIntegerList;
import framework.util.StringIntegerList.StringInteger;
import qwiki.search.DocAndFreq;

public class LocalTestCanvas {
		
	public static List<String> sortDocMap(HashMap<String, Integer> map){
		List<String> sortedDocs = new ArrayList<String>();
		List<DocAndFreq> l = new ArrayList<DocAndFreq>();
		for(String key : map.keySet()){
			l.add(new DocAndFreq(key, map.get(key)));
		}
		Collections.sort(l);
		for(DocAndFreq df :l){
			sortedDocs.add(df.getDocId());
		}
		return sortedDocs;
	}

	public static void main(String[] args) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("doc1", 1);
		map.put("doc2", 50);
		map.put("doc3", 10);
		System.out.println(sortDocMap(map));
	}

}
