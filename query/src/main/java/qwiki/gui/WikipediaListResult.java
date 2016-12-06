package qwiki.gui;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;


public class WikipediaListResult {
	@JsonView(Views.Public.class)
	List<WikipediaResult> result;
}