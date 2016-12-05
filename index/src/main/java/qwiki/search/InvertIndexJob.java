package qwiki.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import qwiki.search.InvertIndexJob;
import qwiki.search.InvertIndexJob.InvertedIndexMapper;
import qwiki.search.InvertIndexJob.InvertedIndexReducer;
import framework.util.StringIntegerList;
import framework.util.StringIntegerList.StringInteger;

public class InvertIndexJob {

	public static class InvertedIndexMapper extends Mapper<Text, Text, Text, Text> {

		@Override
		public void map(Text docID, Text indices, Context context) throws IOException,
				InterruptedException {
			
			StringIntegerList sil = new StringIntegerList();
			sil.readFromString(indices.toString().trim());
			
			for (StringInteger index : sil.getIndices()) {
				context.write(new Text(index.getString()), new Text(new StringInteger(docID.toString(), index.getValue(), index.getPositions()).toString()));				
			}
		}
	}

	public static class InvertedIndexReducer extends
			Reducer<Text, Text, Text, StringIntegerList> {
		private static Pattern pat = Pattern.compile("(.+),(\\d+),\\[(.*?)\\]");


		@Override
		public void reduce(Text lemma, Iterable<Text> indices, Context context)throws IOException, InterruptedException {
			
			List<StringInteger> indexList = new ArrayList<StringInteger>();
			Iterator<Text> iter = indices.iterator();
			StringInteger si;
			//use hash set cause there was a strange repeated index entries problem
			Set<String> docs = new HashSet<String>();
			String docId;
			Matcher m;

			while(iter.hasNext()){
				m = pat.matcher(iter.next().toString());
				while (m.find()) {
					si = new StringInteger(m.group(1), Integer.parseInt(m.group(2)), convertPos(m.group(3)));
					docId = si.getString().trim();
					if(!docs.contains(docId)){
						docs.add(docId);
						indexList.add(si);
					}

				}
			}						
			context.write(lemma, new StringIntegerList(indexList));
		}
		
		protected  int[] convertPos(String positions){
			String[] split = positions.split(",");
			int[] intPos = new int[split.length];
			int c = 0;
			for(String s: split){
				intPos[c] = Integer.parseInt(s.trim());
				c++;
			}
			return intPos;
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		
		System.out.println("****** INVERTING INDEX *******");
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		Job job = Job.getInstance(conf);
		job.setJobName("Invert Index");
		job.setJarByClass(InvertIndexJob.class);
		job.setMapperClass(InvertedIndexMapper.class); 
		job.setReducerClass(InvertedIndexReducer.class);

		job.setInputFormatClass(KeyValueTextInputFormat.class);
		conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator","\t");
		
		job.setMapOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		
		//job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(StringIntegerList.class); // TODO: keep in mind this change
		job.setOutputFormatClass(MapFileOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true)? 0: 1);
		
	}

}
