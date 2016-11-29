package qwiki.search;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.umd.cloud9.collection.wikipedia.WikipediaPage;
import framework.util.Tokenizer;
import framework.util.StringIntegerList;
import framework.util.WikipediaPageInputFormat;

public class LemmaIndexJob {

	public static class LemmaIndexMapper extends Mapper<LongWritable, WikipediaPage, Text, StringIntegerList> {
		protected static Tokenizer tokenizer = new Tokenizer();
		protected static Properties props = new Properties();
		protected static StanfordCoreNLP pipeline;
		private Text docID = new Text();
		
		public LemmaIndexMapper(){
			super();
			this.props.put("annotators", "tokenize, ssplit, pos, lemma");
			this.pipeline = new StanfordCoreNLP(props);
		}
		
		@Override
		public void map(LongWritable offset, WikipediaPage page, Context context) throws IOException,
				InterruptedException {
			String content = page.getContent();
			String cleanedDoc = tokenizer.tokenize(content);
						
			List<String> lemmas = getLemmas(cleanedDoc);
			Map<String, Integer> lemmaMap = new HashMap<String, Integer>();
			for(String lemma : lemmas){
				if(lemmaMap.containsKey(lemma)){
					lemmaMap.put(lemma, lemmaMap.get(lemma) + 1);
				} else {
					lemmaMap.put(lemma, 1);
				}
			}
			
			docID.set(page.getDocid()); //or title instead?
			StringIntegerList result = new StringIntegerList(lemmaMap);
			context.write(docID, result);					
		}
		
		private List<String> getLemmas(String doc){
			List<String> lemmas = new LinkedList<String>();
	        Annotation document = new Annotation(doc);
	        this.pipeline.annotate(document);

	        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	        for(CoreMap sentence: sentences) {
	            // Iterate over all tokens in a sentence
	            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	                // Retrieve and add the lemma for each word into the
	                // list of lemmas
	                lemmas.add(token.get(LemmaAnnotation.class));
	            }
	        }
	        return lemmas;
		}
	}
	
	//main method not needed if we will be chaining the jobs
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
		// TODO: you should implement the Job Configuration and Job call
		// here
		Configuration conf = new Configuration();
		//assuming we have people.txt at the hdfs root 
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2){
			System.err.println("Usage: <jar> <in> <out>");		
		}
		Job job = new Job(conf, "Lemmatize");
		job.setJarByClass(LemmaIndexMapper.class);
		job.setMapperClass(LemmaIndexMapper.class); 
		job.setInputFormatClass(WikipediaPageInputFormat.class);
		//no shuffling, combining or any sort of reducing occurs
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(StringIntegerList.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true)? 0: 1);	
	}

}
