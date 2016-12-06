$(document).ready(function() {
	window["allowInfiniteSearch"] = false;
	var resBox = $("#results");
	var searchButton = $("#search-button");
	var queryTextBox = $("#query-input-box");
	
	function retrieveArticleIDs() {
		var articlesToRetrieve = [];
		for (var i = 0; i < (searchResults.length - numArticlesRetrieved) && i < 10; i++) {
			articlesToRetrieve.push(searchResults[i + numArticlesRetrieved]);
		}
		numArticlesRetrieved += 10;
		return articlesToRetrieve;
	}
	
	function arrayToString(array) {
		var s = array[0];
		for (var i = 1; i < array.length; i++) {
			s += "," + array[i];
		}
		return s;
	}
	
	function jsonToHTML(json) {
		json = json.result;
		var html = "";
		for (var i = 0; i < json.length; i++) {
			var article = json[i];
			html += "<h2><a href='./article/" + article.articleID + "'>" + article.articleTitle + "</a></h2>";
			html += "<div class='panel panel-default'><div class='panel-body'><p>" + article.contentSample + "</p></div></div>"
		}
		return html;
	}
	
	
	function retrieveArticlesAjax() {
		var articles = retrieveArticleIDs();
		if (articles.length > 0) {
			$.ajax({
				type : "POST",
				contentType : "application/json",
				url: './retrieve_wiki_articles',
				dataType: 'json',
				data : arrayToString(articles),
				success: function(json) {
					$("#results").append(jsonToHTML(json));
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
	    if ((5 > diff && diff > -5) && allowInfiniteSearch) {
			var articles = retrieveArticleIDs();
			if (articles.length > 0) {
				retrieveArticlesAjax();
			}
	    }
	});
	
	searchButton.click(function(event) {
		event.preventDefault();
		var queryString = queryTextBox.val();
		$.ajax({
			type : "POST",
			contentType : "application/json",
			url: './get_all_matching_articles',
			data : queryString,
			dataType: 'json',
			success: function(json) {
				window["allowInfiniteSearch"] = false;
				$("#results").html("");
				window["searchResults"] = json.result;
				window["numArticlesRetrieved"] = 0;
				retrieveArticlesAjax();
				window["allowInfiniteSearch"] = true;
			},
			error : function(e) {
				console.log("ERROR: ", e);
				display(e);
			}
		});		
	});
});