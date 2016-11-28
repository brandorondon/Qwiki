package qwiki.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import edu.umd.cloud9.collection.wikipedia.WikipediaPage;
import framework.util.WikipediaPageInputFormat;




public class GetArticlesMapred {

	public static class GetArticlesMapper extends Mapper<LongWritable, WikipediaPage, Text, Text> {


		@Override
		public void map(LongWritable offset, WikipediaPage inputPage, Context context)
				throws IOException, InterruptedException {
			// TODO: You should implement getting article mapper here
			Text title = new Text();
			Text xml = new Text();
			try {
				if(inputPage != null){
					if(inputPage.isArticle() && inputPage.getContent() != null && inputPage.getDocid() != null){ 
						context.write(new Text(inputPage.getDocid()), new Text(inputPage.getContent()));				
					}
				}	
			} catch (NullPointerException e){
			}

		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
		// TODO: you should implement the Job Configuration and Job call
		// here
		Configuration conf = new Configuration();
		//assuming we have people.txt at the hdfs root 
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2){
			System.err.println("Usage: <get articles jar> <in> <out>");		
		}
		Job job = new Job(conf, "Get Articles");
		job.setJarByClass(GetArticlesMapred.class);
		job.setMapperClass(GetArticlesMapper.class); 
		job.setInputFormatClass(WikipediaPageInputFormat.class);
		//no shuffling, combining or any sort of reducing occurs
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true)? 0: 1);	
	}
}
