package com.qwiki.web.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.qwiki.web.jsonview.Views;

public class SearchResult {
	@JsonView(Views.Public.class)
	public List<String> result;
}