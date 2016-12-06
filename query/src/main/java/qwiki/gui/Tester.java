package qwiki.gui;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;

public class Tester {
	public static void main(String[] args) throws IOException {
		String word = "apple";
		MapFile.Reader reader = null;
		Text result = new Text();
		Configuration conf = new Configuration();
		String path  = "wordToLemMap";
		String coreSitePath = "$HADOOP_HOME/etc/hadoop/core-site.xml";
		String hdfsSitePath = "$HADOOP_HOME/etc/hadoop/hdfs-site.xml";
		try {
			
			conf.addResource(new Path(coreSitePath));
			conf.addResource(new Path(hdfsSitePath));
			FileSystem fs = FileSystem.get(conf);
			reader = new MapFile.Reader(fs, path , conf);
			reader.get(new Text(word), result);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader!=null){
				reader.close();
			}
		}
		System.out.println(result.toString());
	}
}
