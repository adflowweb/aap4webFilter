package kr.co.adflow.testParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClientModify {

	Logger logger = LoggerFactory.getLogger(TestClientModify.class);

	public void jsoupModify(String html) {

		logger.debug(html);

		Document doc = Jsoup.parse(html);
		

		if (doc.select("form").attr("action").equals("/notice_search.do")) {
			logger.debug("Notice_Search DO Select");

			String result = doc.select("form").attr("action");
			logger.debug("result:" + result);

			result = doc.select("form").attr("action")
					.replaceAll("/notice_search.do", "JavaScript:modifyClick()");
			logger.debug("replaceResult:" + result);
			
		
			Element el=doc.select("form").first();
			
			el.removeAttr("action");
			
			
			logger.debug("el.html():"+el.html());

		}
		
	    String testResult=doc.select("form").attr("action");
	    logger.debug("testResult:"+testResult);
	}

}
