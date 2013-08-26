package kr.co.adflow.testParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClientModify {

	Logger logger = LoggerFactory.getLogger(TestClientModify.class);

	public String jsoupModify(String html) {

		long startime = System.currentTimeMillis();
		Document doc = Jsoup.parse(html);
		
		logger.debug("JSoup Head Data Log");
		logger.debug("doc.head().data():"+doc.head().data());
		logger.debug("doc.head().html():"+doc.head().html());
		logger.debug("doc.head().text():"+doc.head().text());
		logger.debug("doc.head().toString():"+doc.head().toString());

	/*	Elements formEl = doc.select("form");

		formEl.append("<input name=\"hash\" type=\"hidden\" id=\"hash\"/>");
		Elements aEl = doc.select("a");
		for (Element element : aEl) {
			if (element.attr("onclick").equals("searchScript();")) {
				logger.debug("onclick ..SearchScript!!!!!!!!!!!!!!!!!");
				element.attr("onclick", "return modifyClick();");
			}
		}
		logger.debug(doc.select("form").attr("action").toString());

		doc.head()
				.append("<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js\"></script>");
		doc.head()
				.append("<script src=\"http://crypto-js.googlecode.com/svn/tags/3.1.2/build/rollups/sha1.js\"></script>");
		doc.head()
				.append("<script>"
						+ "function lengthInUtf8Bytes(str) {"
						+ "var m = encodeURIComponent(str).match(/%[89ABab]/g);"
						+ "return str.length + (m ? m.length : 0);"
						+ "}"	
						+"function modifyClick(){"
						+"console.log(\"test\");"
						+"console.log('test:',$);"
						+ "var str = $('html').html().replace(/[\\n\\r]/g, '').replace(/\\s+/g, '');"
						+ "var length = lengthInUtf8Bytes(str);"
						+ "var hex = '';"
						+ "console.log(str.length + \" characters, \" + length + \" bytes\");"
						+ "for(var i=0;i<length;i++) {"
						+ "    hex += str.charCodeAt(i).toString(16);"
						+ "}"
						+"console.log('hex string : ', hex);"
						+"var hash = CryptoJS.SHA1(hex);"
						+"console.log('hash:',hash.toString(CryptoJS.enc.Hex));"
						+"document.getElementById(\"hash\").value=hash.toString(CryptoJS.enc.Hex);"
						+ "var hashval = $('input[name=hash]').val();"
						+ "console.log('hidden hash value :',hashval);"						
						+ "document.searchForm.action=\"/notice_search.do\";"
						+ "document.searchForm.submit();" 
						+ "}" 
						+"</script>");

		logger.debug("Script function TEst!!!!!!!!!!!!!!!!!!!!!!!");
		logger.debug("doc.html().toString():" + doc.head().toString());

		long stopTime = System.currentTimeMillis();

		long resultTime = stopTime - startime;
		logger.debug("ParsingTime:" + resultTime);*/
		return doc.html();
	}

}
