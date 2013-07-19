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
		Document doc = Jsoup.parse(html);

		// doc.getElementById("form").replaceWith(in)

		Elements elts = doc.select("form");

		for (Element element : elts) {
			if (element.attr("action").equals("/notice_search.do")) {
				element.attr("action", "JavaScript:modifyClick()");
			}
		}
		logger.debug(doc.select("form").attr("action").toString());

	}

}
