package qwiki.search;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import framework.util.StringIntegerList;
import framework.util.StringIntegerList.StringInteger;

public class LocalTestCanvas {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pattern p = Pattern.compile("<([^>]+),(\\d+),\\[(.*?)\\]>");
		Pattern p2 = Pattern.compile("(.+),(\\d+),\\[(.*?)\\]");
		Matcher m = p.matcher("<german,4,[4771, 7795, 7971, 11381]>,<moderna,2,[12131, 12156]>,<numerous,1,[4274]>,<speak,1,[11363]>");
		int[] a = {1,2,3};
		StringIntegerList.StringInteger si = new StringInteger("STRING",45,a);
		System.out.println(si);
		Matcher m2 = p2.matcher(si.toString());
		

		while (m2.find()) {
			System.out.println(m2);
			System.out.print(m2.group(1) + " " + m2.group(2) + " " + m2.group(3));

		}

	}

}
