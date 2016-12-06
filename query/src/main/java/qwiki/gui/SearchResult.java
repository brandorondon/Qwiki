package qwiki.gui;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonView;

public class SearchResult {
	@JsonView(Views.Public.class)
	List<String> result;
}