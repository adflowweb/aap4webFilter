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
		
		long startime=System.currentTimeMillis();
		Document doc = Jsoup.parse(html);


		

		
		
		/*  <script> 
			function modifyClick() {
			var str="<html>" + document.documentElement.innerHTML + "</html>"
			    var hash = 0;
		    if (str.length == 0) return hash;
		    for (i = 0; i < str.length; i++) {
		        char = str.charCodeAt(i);
		        hash = ((hash<<5)-hash)+char;
		        hash = hash & hash; // Convert to 32bit integer
		    }
		    return hash;
			
			}
		</script>*/
		
		
		Elements elts = doc.select("form");

		for (Element element : elts) {
			if (element.attr("action").equals("/notice_search.do")) {
				element.attr("action", "JavaScript:modifyClick()");
			}
		}
		logger.debug(doc.select("form").attr("action").toString());
		
			doc.head().append("<script>function modifyClick() {" +
					"var str=\"<html>\" + document.documentElement.innerHTML + \"</html>\"" +
					"var hash = 0;" +
					"if (str.length == 0) return hash;" +
					"for (i = 0; i < str.length; i++) " +
					"{" +
					"char = str.charCodeAt(i);" +
					"hash = ((hash<<5)-hash)+char;" +
					"hash = hash & hash; " +
					"// Convert to 32bit integer}" +
					"return hash;" +
					"}" +
					"</script>");
		
//				doc.head().appendElement("script").append("function modifyClick() {" +
//				"var str=\"<html>\" + document.documentElement.innerHTML + \"</html>\"" +
//				"var hash = 0;" +
//				"if (str.length == 0) return hash;" +
//				"for (i = 0; i < str.length; i++) " +
//				"{" +
//				"char = str.charCodeAt(i);" +
//				"hash = ((hash<<5)-hash)+char;" +
//				"hash = hash & hash; " +
//				"// Convert to 32bit integer}" +
//				"return hash;" +
//				"}");

	logger.debug("Script function TEst!!!!!!!!!!!!!!!!!!!!!!!");
	logger.debug("doc.html().toString():"+doc.head().toString());

		long stopTime=System.currentTimeMillis();
		
		long resultTime=stopTime-startime;
		logger.debug("ParsingTime:"+resultTime);
	}
	

}
