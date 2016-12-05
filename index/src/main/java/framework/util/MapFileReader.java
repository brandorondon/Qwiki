package framework.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.commons.logging.impl.LogFactoryImpl;

public class MapFileReader {
	private String lemmaPath;
	private String docPath;
	private Configuration conf;
	private FileSystem fs;

	
	
	public MapFileReader(){
		this.lemmaPath = "wordToLemMap";
		this.docPath = "inv-wiki-map";
		this.docPath = null; //need to build forward index
		this.conf = new Configuration();

		try {
			this.fs = FileSystem.get(conf);

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public Text getValue(String word, String path) throws IOException{
		MapFile.Reader reader = null;
		Text result = new Text();
		try {
			reader = new MapFile.Reader(fs, path , conf);
			reader.get(new Text(word), result);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader!=null){
				reader.close();
			}
		}
		return result;
	}
}
