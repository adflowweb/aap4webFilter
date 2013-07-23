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

		Elements formEl = doc.select("form");

		formEl.append("<input name=\"hash\" type=\"hidden\" id=\"hash\"/>");
		Elements aEl = doc.select("a");
		for (Element element : aEl) {
			if (element.attr("onclick").equals("searchScript();")) {
				logger.debug("onclick ..SearchScript!!!!!!!!!!!!!!!!!");
				element.attr("onclick", "modifyClick();");
			}
			// element.append("<input name=\"hash\" type=\"hidden\" id=\"hash\"/>");
		}
		logger.debug(doc.select("form").attr("action").toString());

		// doc.head()
		// .append("<script>function modifyClick() {"
		// +
		// "var str=\"<html>\" + document.documentElement.innerHTML + \"</html>\""
		// + "var hash = 0;" + "if (str.length == 0) return hash;"
		// + "for (i = 0; i < str.length; i++) " + "{"
		// + "char = str.charCodeAt(i);"
		// + "hash = ((hash<<5)-hash)+char;"
		// + "hash = hash & hash; "
		// + "// Convert to 32bit integer} console.log(hash);" +
		// "" + "return hash;" + "}"
		// + "</script>");
	
		
		doc.head()
				.append("<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js\"></script>");
		doc.head()
				.append("<script type=\"text/javascript\" src=\"https://hashmask.googlecode.com/svn-history/r2/trunk/jquery.sha1.js\"></script>");

		doc.head()
				.append("<script>"
					/*	+ "function lengthInUtf8Bytes(str) {"
						+ "var m = encodeURIComponent(str).match(/%[89ABab]/g);"
						+ "return str.length + (m ? m.length : 0);"
						+ "}"
						+*/

						+"function modifyClick(){"
					/*	+ "var str = $('html').html().replace(/[\\n\\r]/g, '').replace(/\\s+/g, '');"
						+ "var length = lengthInUtf8Bytes(str);"
						+ "var hex = '';"
						+ "console.log(str.length + \" characters, \" + length + \" bytes\");"
						+ "for(var i=0;i<length;i++) {"
						+ "    hex += str.charCodeAt(i).toString(16);"
						+ "}"
						+ "document.getElementById(\"hash\").value= 12334567677;"
						+ "document.searchForm.action=\"/notice_search.do\";"
						+ "var hashval = $('input[name=hash]').val();"
						+ "console.log('hidden hash value :',hashval);"
						+ "console.log('hex string : ', hex);"
						+ "console.log('hash : ',$.sha1(hex));"
						+ "console.log(testHex);"
						+ "document.searchForm.submit();" */
						+"var string=$.sha1(\"test\");"
						+ "}" 
						+"</script>");

		logger.debug("Script function TEst!!!!!!!!!!!!!!!!!!!!!!!");
		logger.debug("doc.html().toString():" + doc.head().toString());

		long stopTime = System.currentTimeMillis();

		long resultTime = stopTime - startime;
		logger.debug("ParsingTime:" + resultTime);
		return doc.html();
	}

}
