package kr.co.adflow.testParser;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClientModify {

	Logger logger = LoggerFactory.getLogger(TestClientModify.class);

	public String jsoupModify(String html) {

		long startime = System.currentTimeMillis();
		Document doc = Jsoup.parse(html);
		String headToString = doc.head().toString();
		if (!(headToString.contains("ajax.googleapis.com/ajax/libs/jquery") && headToString
				.contains("jquery.min.js"))) {
			logger.debug("If..Jquery .......!!");
			/*
			 * doc.head() .append(
			 * "<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js\"></script>"
			 * ); logger.debug("doc.head().toString():"+doc.head().toString());
			 */
		}

		//uuid create
		UUID uuid = UUID.randomUUID();
		RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
		//split
		String temp = rmxb.getName();
		String[] arrTemp = temp.split("@");
		String pid = arrTemp[0];
		logger.debug("PID:" + pid);
		//txid+uuid
		String txid = uuid.toString();
		txid = pid + "-" + txid;
		//policy
		String policy = "N";

		//add head txid,uPolicy
		doc.head().append(
				"<script> var jsontext = {\"TXID\": \"" + txid
						+ "\", \"uPolicy\": \"" + policy + "\" };</script>");
	
		return doc.html();
	}

}