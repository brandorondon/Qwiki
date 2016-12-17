package com.qwiki.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import edu.umd.cloud9.collection.wikipedia.WikipediaForwardIndex;
import edu.umd.cloud9.collection.wikipedia.WikipediaPage;

public class ArticleFetcher {
	public String indexPath;
	public String dataPath;
	WikipediaPage page;
	Configuration conf = new Configuration();
	WikipediaForwardIndex forwardIndex;
	static String localCoreSitePath = "C:\\Users\\Aidan\\Desktop\\hadoop-2.3.0\\etc\\hadoop\\core-site.xml";
	static String localHdfsitePath = "C:\\Users\\Aidan\\Desktop\\hadoop-2.3.0\\etc\\hadoop\\hdfs-site.xml";
	
	public ArticleFetcher(String indexPath, String dataPath, String coreSitePath, String hdfsSitePath) 
			throws IllegalArgumentException, IOException {
		this.indexPath = indexPath;
		this.dataPath = dataPath;

		conf.addResource(new Path(coreSitePath));
		conf.addResource(new Path(hdfsSitePath));
		forwardIndex = new WikipediaForwardIndex(conf);
		forwardIndex.loadIndex(new Path(indexPath), new Path(dataPath), FileSystem.get(conf));
	}
	
	public ArticleFetcher() 
			throws IllegalArgumentException, IOException {
		this("enwiki_index.dat", "enwiki_docno.dat", localCoreSitePath, localHdfsitePath);
	}

	public WikipediaPage getPage(String id) {
		return forwardIndex.getDocument(id);
	}
}
