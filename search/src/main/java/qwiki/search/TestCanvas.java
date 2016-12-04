package qwiki.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import qwiki.search.InvertIndexJob;
import qwiki.search.InvertIndexJob.InvertedIndexMapper;
import qwiki.search.InvertIndexJob.InvertedIndexReducer;
import framework.util.StringIntegerList;
import framework.util.StringIntegerList.StringInteger;

public class TestCanvas {

	public static class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, StringInteger> {
		@Override
		public void map(LongWritable x, Text line, Context context) throws IOException,
		InterruptedException {
			String e = line.toString();
			if(e.indexOf(" ") != -1){
				String docID = e.substring(0, e.indexOf(" "));
				String indices = e.toString().substring(e.indexOf("<"));
				StringIntegerList sil = new StringIntegerList();
				sil.readFromString(indices);
				for (StringInteger index : sil.getIndices()) {
					context.write(new Text(index.getString()), new StringInteger(docID.toString(), index.getValue(), index.getPositions()));
				}
			}
		}
	}

	public static class InvertedIndexReducer extends
			Reducer<Text, StringInteger, Text, StringIntegerList> {

		@Override
		public void reduce(Text lemma, Iterable<StringInteger> indices, Context context)throws IOException, InterruptedException {
			
			List<StringInteger> indexList = new ArrayList<StringInteger>();
			Iterator iter = indices.iterator();
			StringInteger si;
			//use hash set cause there was a strange repeated index entries problem
			Set<String> docs = new HashSet<String>();
			String docId;
			while(iter.hasNext()){
				si = (StringInteger) iter.next();
				docId = si.getString().trim();
				if(!docs.contains(docId)){
					docs.add(docId);
					indexList.add(si);
				}
			}		
			StringIntegerList result = new StringIntegerList(indexList);
			context.write(lemma, result);
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		
		System.out.println("****** INVERTING INDEX *******");
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		Job job = Job.getInstance(conf);
		job.setJobName("Invert Index");
		job.setJarByClass(TestCanvas.class);
		job.setMapperClass(InvertedIndexMapper.class); 
		job.setReducerClass(InvertedIndexReducer.class);

		job.setInputFormatClass(TextInputFormat.class);
		//conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator","\t");
		job.setMapOutputValueClass(StringInteger.class);
		job.setMapOutputKeyClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(StringIntegerList.class);
		//job.getConfiguration().set("mapreduce.job.queuename", "hadoop07");
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true)? 0: 1);
		
	}

}
