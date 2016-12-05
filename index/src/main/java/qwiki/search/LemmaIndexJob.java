package qwiki.search;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import qwiki.search.GetArticlesMapred;
import qwiki.search.GetArticlesMapred.GetArticlesMapper;
import framework.util.StringIntegerList;
import framework.util.StringIntegerList.StringInteger;
import framework.util.Tokenizer;
import framework.util.WikipediaPageInputFormat;
import edu.umd.cloud9.collection.wikipedia.WikipediaPage;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.process.*;
//import org.apache.xerces.*;

public class LemmaIndexJob {

	public static class LemmaIndexMapper extends Mapper<LongWritable, WikipediaPage, Text, StringIntegerList> {
		protected static Tokenizer tokenizer = new Tokenizer();
		protected static Properties props = new Properties();
		protected static StanfordCoreNLP pipeline;
		
		private FileSystem dfs;
		private String unique = (new BigInteger(256, new Random())).toString();
		private BufferedWriter br;
		StringBuilder buffer = new StringBuilder();
		String dir;

		//We write each word->lemma to an hdfs file, so we can then convert into a MapFile
		//for our query processor 
		protected void setup(Context context) throws IOException, InterruptedException {
			this.props.put("annotators", "tokenize, ssplit, pos, lemma");
			this.pipeline = new StanfordCoreNLP(props);
			
			Configuration config = new Configuration();
			   config.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
			   config.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"));
			   config.set("fs.hdfs.impl", 
				            org.apache.hadoop.hdfs.DistributedFileSystem.class.getName()
				        );
				       config.set("fs.file.impl",
				            org.apache.hadoop.fs.LocalFileSystem.class.getName()
				        );
			  this.dfs = FileSystem.get(config);
			  dir = dfs.getWorkingDirectory()+ "/wordToLem/";
			  Path filePath = new Path(dir + this.unique);
			  this.br = new BufferedWriter(new OutputStreamWriter(dfs.create(filePath, true)));
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
		
		@Override
		public void map(LongWritable offset, WikipediaPage page, Context context) throws IOException,
				InterruptedException {
			String content = page.getContent();
			Map<String, List<Integer>> docIndex = tokenizer.tokenize(content);
			String cleanedDoc = tokenizer.cleanDoc(content);
			Map<String,String> wordToLem = getLemmas(cleanedDoc);
			Map<String, List<StringInteger>> wordToLemList = new HashMap<String, List<StringInteger>>();
			
			int[] positions;
			String lem;
			StringInteger entry;
			for(String word : docIndex.keySet()){				
				positions = convToIntArr(docIndex.get(word));
				lem = (positions != null) ? wordToLem.get(word) : word;
				entry = new StringInteger(lem, positions.length, positions);
				if(wordToLemList.containsKey(word)){
					wordToLemList.get(word).add(entry);
				} else {
					List<StringInteger> l = new ArrayList<StringInteger>();
					l.add(entry);
					wordToLemList.put(word, l);
				}
			}
			List<StringInteger> sil = new ArrayList<StringInteger>();
			StringInteger si;
			for(String word : wordToLemList.keySet()){	
				si = reduceSI(wordToLemList.get(word));
				sil.add(si);
				this.buffer.append(word + "->" + si.getString() + "\n");
			}
			br.append(this.buffer.toString());
			buffer.setLength(0); //clear the buffer to avoid heap overflow or GC problems
			StringIntegerList finalSIL = new StringIntegerList(sil);
			
			context.write(new Text(page.getDocid()), finalSIL);					
		}
		
		private StringInteger reduceSI(List<StringInteger> sil){
			Iterator<StringInteger> iter = sil.iterator();
			StringInteger result = (StringInteger) iter.next();
			int[] concat;
			while(iter.hasNext()){
				StringInteger si = (StringInteger) iter.next();
				concat = (int[]) ArrayUtils.addAll(result.getPositions(), si.getPositions());
				result = new StringInteger(result.getString(), concat.length, concat);
			}
			return result;
		}
		
		private Map<String,String> getLemmas(String doc){
			Map<String,String> lemmas = new HashMap<String,String>();
	        Annotation document = new Annotation(doc);
	        this.pipeline.annotate(document);

	        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	        for(CoreMap sentence: sentences) {
	            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	               //only valid lemmas
	               String lem = token.get(LemmaAnnotation.class).toLowerCase();
	               String ts = token.originalText().toLowerCase();
	               if (!lem.equals("null") || ts.contains("null") ){ 
	            	   lemmas.put(ts, lem); 
	               } 
	            }
	        }
	        return lemmas;
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
		
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2){
			System.err.println("Usage: <jar> <in> <out>");		
		}
		System.out.println("********** RUNNING LEMMATIZE ************");
		Job job = new Job(conf, "Lemmatize");
		job.setJarByClass(LemmaIndexMapper.class);
		job.setMapperClass(LemmaIndexMapper.class); 
		job.setInputFormatClass(WikipediaPageInputFormat.class);
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(StringIntegerList.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(StringIntegerList.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true)? 0: 1);	
	}

}
