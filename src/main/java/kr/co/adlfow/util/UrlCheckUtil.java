package kr.co.adlfow.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class UrlCheckUtil {

	Map<Object, ArrayList<Object>> multiMap = new HashMap<Object, ArrayList<Object>>();
	ArrayList uriArr = new ArrayList();
	ArrayList queryStr = new ArrayList();

	public Map urlCheckTable(String result) throws Exception {

		result = result.replaceAll("\\p{Space}", "");
		System.out.println("------------------UrlCheckUtil Start------------------");
		System.out.println("UrlCheckUtil param:" + result);

		ObjectMapper om = new ObjectMapper();

		JsonNode rootNode = om.readTree(result);

		JsonNode uriNode = rootNode.path("uri");

		Iterator<JsonNode> it = uriNode.getElements();

		while (it.hasNext()) {
			JsonNode temp = it.next();
			System.out.println("JsonNode:" + temp.path("uri").getTextValue());
			uriArr.add(temp.path("uri").getTextValue());
			multiMap.put("uri", uriArr);
			JsonNode queryStrNode = null;
			Iterator<JsonNode> queryStrIT = temp.path("queryStr").iterator();

			while (queryStrIT.hasNext()) {
				queryStrNode = queryStrIT.next();
				System.out.println("JsonNode:" + queryStrNode.getTextValue());
				queryStr.add(queryStrNode.getTextValue());
				multiMap.put("queryStr", queryStr);

			}
		}
		System.out.println("------------------UrlCheckUtil END------------------");
		return multiMap;
	}
}
