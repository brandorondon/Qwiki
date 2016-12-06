package qwiki.gui;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;

@Controller
@RequestMapping("/")
public class WebInterfaceController {
    @RequestMapping(method = RequestMethod.GET)
    public String sayHello(ModelMap model) {
        model.addAttribute("greeting", "Hello World from Spring 4 MVC");
        return "welcome";
    }
    
    @RequestMapping(value = "/article/{articleID}", method = RequestMethod.GET)
    public @ResponseBody String getArticle(@PathVariable String articleID) {
    	try {
			ArticleFetcher fetcher = new ArticleFetcher();
			WikipediaPage page = fetcher.getPage(articleID);
			return page.getDisplayContent();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   	return "404";
		}
    }
}
