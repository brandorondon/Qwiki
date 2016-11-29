package qwiki.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;
import framework.util.StringIntegerList;
import framework.util.StringIntegerList.StringInteger;
import framework.util.Tokenizer;
import framework.util.WikipediaPageInputFormat;
import qwiki.search.GetArticlesMapred.GetArticlesMapper;

public class TestCanvas {
	public static class TestMapper extends Mapper<LongWritable, WikipediaPage, Text, StringIntegerList> {
		
		@Override
		public void map(LongWritable offset, WikipediaPage inputPage, Context context)
				throws IOException, InterruptedException {
		
			Tokenizer t = new Tokenizer();
			Map<String, List<Integer>> tp = t.tokenize(inputPage.getContent());
			List<StringInteger> si = new ArrayList<StringInteger>();
			for(String key : tp.keySet()){
				int size = tp.get(key).size();
				int[] pos = convToIntArr(tp.get(key));
				si.add(new StringInteger(key, size, pos));
			}
			tp = null;
			context.write(new Text(inputPage.getDocid()), new StringIntegerList(si));
		}
		
		private int[] convToIntArr(List<Integer> l){
			int[] arr = new int[l.size()];
			int c = 0;
			for(Integer i : l){
				arr[c] = i.intValue();
				c++;
			}
			return arr;
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
		//assuming we have people.txt at the hdfs root 
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2){
			System.err.println("Usage: <get articles jar> <in> <out>");		
		}
		Job job = new Job(conf, "Test Canvas");
		job.setJarByClass(TestCanvas.class);
		job.setMapperClass(TestMapper.class); 
		job.setInputFormatClass(WikipediaPageInputFormat.class);
		//no shuffling, combining or any sort of reducing occurs
		job.setNumReduceTasks(0);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(StringIntegerList.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true)? 0: 1);	
	}

}
