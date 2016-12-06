package qwiki.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;

@RestController
public class SearchAjaxController {
    @JsonView(Views.Public.class)
    @RequestMapping(value = "/get_all_matching_articles")
    public SearchResult processAJAXRequest(@RequestBody String query) throws IOException {
    	QueryProcessor p = new QueryProcessor();
    	SearchResult sr = new SearchResult();
    	sr.result = p.evaluateQuery(query);
    	return sr;
    }
    
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
