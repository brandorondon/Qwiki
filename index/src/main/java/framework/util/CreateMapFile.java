package framework.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.MapFile.Writer;
import org.apache.hadoop.io.Text;
import org.apache.commons.logging.impl.LogFactoryImpl;

/**
 * Converts the wordToLemResult word->lemma mapping file into an actual
 * Hadoop MapFile. The query processor then parses and converts keywords
 * to their respective lemmas by indexing into the MapFile
 * 
 */
public class CreateMapFile {

    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException{
		
		Configuration conf = new Configuration();
		FileSystem fs;
		
		try {
			fs = FileSystem.get(conf);
			
			Path inputFile = new Path(args[0]);
			Path outputFile = new Path(args[1]);

      		Text key = new Text();
  			Text value = new Text();

			String line = "";
			MapFile.Writer writer = null;
			
			FSDataInputStream inputStream = fs.open(inputFile);

			try {
				writer = new Writer(conf, fs, outputFile.toString(), Text.class, Text.class);
				writer.setIndexInterval(1);
				int count = 0;
				while (inputStream.available() > 0) {
					line = inputStream.readLine().trim();
					//TODO: remember changes made here to make it covnert the inverted index to map file form
					int comma = line.indexOf("<");
					String k = line.substring(0, comma).trim();
					String v = line.substring(comma).trim();
					if(!k.isEmpty()){
						key.set(k);
						value.set(v);
						writer.append(key, value);
						count++;
						if(count%100000==0){
							System.out.println("appending: " + key + " -> " + value );
						}
					}
				}
			} finally {
				IOUtils.closeStream(writer);
        			System.out.println("Map file created successfully!!");
  		}
	} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}