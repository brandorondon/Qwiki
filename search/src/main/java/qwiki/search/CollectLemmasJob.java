package qwiki.search;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import qwiki.search.CollectLemmasJob;
import qwiki.search.CollectLemmasJob.CollectLemmasMapper;
import qwiki.search.CollectLemmasJob.CollectLemmasReducer;
import framework.util.StringIntegerList;
import framework.util.StringIntegerList.StringInteger;

public class CollectLemmasJob {
	
	public static class CollectLemmasMapper extends Mapper<LongWritable, Text, Text, Text> {


		@Override
		public void map(LongWritable offset, Text line, Context context) throws IOException,
				InterruptedException {
			String[] split = line.toString().split("->");
			if(!split[0].contains(".") && (!split[0].equals("null") && !split[1].equals("null"))){
				context.write(new Text(split[0]), new Text(split[1]));
			}		
		}
	
	}
	
	public static class CollectLemmasReducer extends Reducer<Text, Text, Text, Text> {
		
		@Override
		public void reduce(Text word, Iterable<Text> lemmas, Context context) throws IOException, InterruptedException{
			StringBuilder sb = new StringBuilder();
			Iterator<Text> iter = lemmas.iterator();
			Set<String> seen = new HashSet<String>();
			String next;
			while(iter.hasNext()){
				next = iter.next().toString();
				if(!seen.contains(next)){
					seen.add(next);
					sb.append(","+ next);
				}
			}
			context.write(word, new Text(sb.toString()));
		}
		
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		Job job = Job.getInstance(conf);
		job.setJarByClass(CollectLemmasJob.class);
		job.setMapperClass(CollectLemmasMapper.class); 
		job.setReducerClass(CollectLemmasReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setMapOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		//job.getConfiguration().set("mapreduce.job.queuename", "hadoop08");

		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true)? 0: 1);
		
	}

}
