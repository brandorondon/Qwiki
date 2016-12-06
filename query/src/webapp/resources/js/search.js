$(document).ready(function() {
	var resBox = $("#results");
	var searchButton = $("#search-button");
	var queryTextBox = $("#query-input-box");
	
	function retrieveArticleIDs() {
		var articlesToRetrieve = [];
		for (var i = 0; i < (searchResults.length - numArticlesRetrieved) && i < 5; i++) {
			articlesToRetrieve.push(searchResults[i + numArticlesRetrieved]);
		}
		numArticlesRetrieved += 5;
		console.log(articlesToRetrieve);
	}
	
	function arrayToString(array) {
		var s = array[0];
		for (var i = 1; i < array.length; i++) {
			s += "**sep**" + array[i];
		}
		return s;
	}
	
	function retrieveArticlesAjax() {
		var articles = retrieveArticleIDs();
		if (articles.length > 0) {
			$.ajax({
				type : "POST",
				contentType : "application/json",
				url: './retrieve_wiki_articles',
				dataType: 'json',
				data : arrayToString(aritcles),
				success: function(json) {
					console.log(json);
					//$(resBox).append(json);
				}
			});
			
			//resBox.append("1<br>");
			//console.log("appending");
		}
	}
	
	// Each time the user scrolls
	resBox.scroll(function() {
		// End of the document reached?
		console.log("scroll");
		var diff = (resBox[0].scrollHeight - resBox.scrollTop()) - resBox.outerHeight();
	    if (5 > diff && diff > -5) {
			var articles = retrieveArticleIDs();
			if (articles.length > 0) {
				retriveArticlesAjax();
			}
	    }
	});
	
	searchButton.click(function(event) {
		event.preventDefault();
		var queryString = queryTextBox.val();
		var queryInfo = {};
		queryInfo["query"] = queryString;
		console.log(JSON.stringify(queryInfo));
		$.ajax({
			type : "POST",
			contentType : "application/json",
			url: './get_all_matching_articles',
			dataType: 'json',
			data : JSON.stringify(queryInfo),
			success: function(json) {
				window["searchResults"] = json.result;
				window["numArticlesRetrieved"] = 0;
				retriveArticlesAjax();
			},
			error : function(e) {
				console.log("ERROR: ", e);
				display(e);
			}
		});		
	});
});