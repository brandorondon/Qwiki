package com.qwiki.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.qwiki.util.ArticleFetcher;
import com.qwiki.util.QueryData;
import com.qwiki.util.MapFileReader;
import com.qwiki.util.QueryProcessor;
import com.qwiki.web.jsonview.Views;
import com.qwiki.web.model.AjaxSearchRequest;
import com.qwiki.web.model.SearchResult;
import com.qwiki.web.model.WikipediaListResult;
import com.qwiki.web.model.WikipediaResult;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;

@RestController
public class SearchAjaxController {
	
	@Autowired
	private MapFileReader mapreader;
	private ConcurrentHashMap<String, List<QueryData>> cache;
	
    @JsonView(Views.Public.class)
    @RequestMapping(value = "/get_all_matching_articles")
    public SearchResult processAJAXRequest(@RequestBody String query) throws IOException {
    	List<QueryData> result;
    	SearchResult sr = new SearchResult();
    	sr.result = new LinkedList<String>();
    	if (cache.containsKey(query)) {
    		result = cache.get(query);
    	} else {
	    	QueryProcessor p = new QueryProcessor(mapreader);
	    	result = p.evaluateQuery(query);
	    	cache.put(query, result);
    	}
    	
    	for (QueryData qd : result) {
    		sr.result.add(qd.getDocId());
    	}
    	return sr;
    }
    
    @PostConstruct
    public void createCache() {
    	cache = new ConcurrentHashMap<String, List<QueryData>>();
    }
    
    public String highlightArticleSample(String sample, Set<String> queryTokens) {
    	StringBuilder sb = new StringBuilder();
    	String[] tokenizedInput = sample.split("[^a-zA-Z]");
    	for (String token : tokenizedInput) {
    		if (queryTokens.contains(token.toLowerCase())) {
    			sb.append("<kbd>" + token + "</kbd> ");
    		} else {
    			sb.append(token + " ");
    		}
    	}
    	return sb.toString();
    }
    
    @JsonView(Views.Public.class)
    @RequestMapping(value = "/retrieve_wiki_articles")
    public WikipediaListResult getArticles(@RequestBody String jsonString) 
    		throws IllegalArgumentException, IOException {
    	
    	ArticleFetcher fetcher = new ArticleFetcher();
    	Gson gson = new Gson();
    	AjaxSearchRequest asr = gson.fromJson(jsonString, AjaxSearchRequest.class);
    	String[] articleIDs = asr.articleIDs;
    	List<QueryData> cachedQuery = cache.get(asr.query);
    	int curPos = asr.position;
    	WikipediaListResult wlr = new WikipediaListResult();
    	wlr.result = new ArrayList<WikipediaResult>();
    	for (String articleID : articleIDs) {
    		WikipediaPage page = fetcher.getPage(articleID);
    		WikipediaResult wr = new WikipediaResult();
    		wr.articleTitle = page.getTitle();
    		wr.articleID = articleID;
    		String content = page.getContent();
    		int[] areaToHighlight = cachedQuery.get(curPos).getSampleArea();
    		int sampleSize;
    		if (content.length() > 100) {
    			sampleSize = 100;
    		} else {
    			sampleSize = content.length()-1;
    		}
    		
    		Set<String> tokenSet = new HashSet<String>();
    		for (String token : cachedQuery.get(curPos).getTokens()) {
    			tokenSet.add(token);
    		}
    		
    		String chunkWithMostMatches = "";
    		if (areaToHighlight != null) {
    			chunkWithMostMatches = content.substring(areaToHighlight[0], areaToHighlight[1]);
    		}
    		String startHighlight = highlightArticleSample(content.substring(0, sampleSize), tokenSet);
    		String bestMatchesHighlight = highlightArticleSample(chunkWithMostMatches, tokenSet);
    		String contentStartSample = "<i>" + startHighlight + "</i>... <hr> ..." + bestMatchesHighlight;
    		
    		wr.contentSample = contentStartSample;
    		
    		wlr.result.add(wr);
    		curPos++;
    	}
    	return wlr;
    }
}
