package qwiki.gui;

import com.fasterxml.jackson.annotation.JsonView;

public class WikipediaResult {

	@JsonView(Views.Public.class)
	String contentSample;
	
	@JsonView(Views.Public.class)
	String articleTitle;
	
	@JsonView(Views.Public.class)
	String articleID;
}