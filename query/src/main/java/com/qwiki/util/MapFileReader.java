package com.qwiki.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;


public class MapFileReader {
	private String docPath;
	private Configuration conf;
	private FileSystem fs;
	
	static String coreSitePath = "C:\\Users\\Aidan\\Desktop\\hadoop-2.3.0\\etc\\hadoop\\core-site.xml";
	static String hdfsSitePath = "C:\\Users\\Aidan\\Desktop\\hadoop-2.3.0\\etc\\hadoop\\hdfs-site.xml";
	MapFile.Reader reader;
	
	
	public MapFileReader() throws IOException {
		this.docPath = "/inv-wiki-map3";
		this.conf = new Configuration();
		this.fs = FileSystem.get(conf);
		conf.addResource(new Path(coreSitePath));
		conf.addResource(new Path(hdfsSitePath));
		reader = new MapFile.Reader(fs, docPath , conf);
	}
	
	public String getValue(String word) throws IOException{
		Text result = new Text();
		reader.get(new Text(word), result);
		return result.toString();
	}
}
