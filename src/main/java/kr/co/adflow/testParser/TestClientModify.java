package kr.co.adflow.testParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClientModify {
		
	Logger logger= LoggerFactory.getLogger(TestClientModify.class);
	
	public void jsoupModify(String html){
		
		logger.debug(html);
		
		Document doc= Jsoup.parse(html);
		
		String result=doc.attr("action");
		
		logger.debug("action Result:"+result);
		
	}

}
