package com.qwiki.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.qwiki.util.MapFileReader;
import com.qwiki.util.QueryProcessor;
import com.qwiki.web.jsonview.Views;
import com.qwiki.web.model.SearchResult;
import com.qwiki.web.model.WikipediaListResult;
import com.qwiki.web.model.WikipediaResult;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;

@RestController
public class SearchAjaxController {
	
	@Autowired
	private MapFileReader mapreader;
	
    @JsonView(Views.Public.class)
    @RequestMapping(value = "/get_all_matching_articles")
    public SearchResult processAJAXRequest(@RequestBody String query) throws IOException {
    	QueryProcessor p = new QueryProcessor(mapreader);
    	SearchResult sr = new SearchResult();
    	sr.result = p.evaluateQuery(query);
    	return sr;
    }
    
    /*
    @PostConstruct
    public void createMapFileReader() {
    	try {
			r = new MapFileReader();
		} catch (IOException e) {
			e.printStackTrace();
			r = null;
		}
    }
    */
    
    @JsonView(Views.Public.class)
    @RequestMapping(value = "/retrieve_wiki_articles")
    public WikipediaListResult getArticles(@RequestBody String articles) 
    		throws IllegalArgumentException, IOException {
    	
    	ArticleFetcher fetcher = new ArticleFetcher();
    	String[] articleIDs = articles.split(",");
    	WikipediaListResult wlr = new WikipediaListResult();
    	wlr.result = new ArrayList<WikipediaResult>();
    	for (String articleID : articleIDs) {
    		WikipediaPage page = fetcher.getPage(articleID);
    		WikipediaResult wr = new WikipediaResult();
    		wr.articleTitle = page.getTitle();
    		wr.articleID = articleID;
    		String content = page.getContent();
    		int sampleSize;
    		if (content.length() > 500) {
    			sampleSize = 500;
    		} else {
    			sampleSize = content.length()-1;
    		}
    		wr.contentSample = content.substring(0, sampleSize) + "...";
    		wlr.result.add(wr);
    	}
    	return wlr;
    }
}
