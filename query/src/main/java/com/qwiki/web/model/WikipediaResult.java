package com.qwiki.web.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.qwiki.web.jsonview.Views;

public class WikipediaResult {

	@JsonView(Views.Public.class)
	public String contentSample;
	
	@JsonView(Views.Public.class)
	public String articleTitle;
	
	@JsonView(Views.Public.class)
	public String articleID;
}