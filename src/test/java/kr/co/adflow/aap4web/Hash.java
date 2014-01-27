package kr.co.adflow.aap4web;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

public class Hash {
/*
	@Test
	public void hashTest() {

		String val = makeHash("<html><script>functionuf_submit(form1){	alert(\"before\");	alert(document.aap4web.getSID_AAPlus4Web());	document.aap4web.submitForm(form1);	alert(\"after\");}<meta><meta><meta><script><script><script><script>	functionaaplus4Web_goInstallPage(){		if(window.top==window.self||AAPBrowserDetect.version<=7){				document.location.href=\"/aap4web/setup/aap4wp_install.jsp?prv=\"				+encodeURIComponent(document.location.href);		}		else{			url=\"/aap4web/setup/aap4wp_install.jsp?close=true\";			varsizeW=822;												varsizeH=640;			varnLeft=screen.width/2-sizeW/2;			varnTop=screen.height/2-sizeH/2;			varopt=\",toolbar=no,menubar=no,location=no,scrollbars=\"+scroll+\",status=no\";			window.open(url,\"_blank\",\"left=\"+nLeft+\",top=\"+nTop+\",width=\"+sizeW+\",height=\"+sizeH+opt);			document.location.href=\"/aap4web/setup/aap4wp_install.jsp?prv=\"				+encodeURIComponent(document.location.href)				+\"&manual=true\";		}	}	if(!AAP4WPInstall.isInstalled())		aaplus4Web_goInstallPage();AAP4WPInstall.printObject('0dNl9DJ4A8BtebC6Gx4tf1Bg3Z6ShurFGsYxbzO85oMKprQMKpis4rDcfgIEEDMR',\"aap4web\",\"/aap4web/setup/\",true);varv_aap4web_url=\"/v_aap4web.jsp\";var_aaweb_=\"PSYwolt/1pqP0Qcu0KSrqr1unisFgMKxbhhhO+DWNF+2nQCkq1llIp7SKRs2wrHJ\";varv_aap4web_err_url=\"/jsp/common/comm_info.jsp\";	varv_aap4web_err_target=\"mainframe\";if(window.top==window.self){v_aap4web_err_url=\"/jsp/common/pop_error_info.jsp\";v_aap4web_err_target=\"_self\";}	elseif(window.top==window.parent){		v_aap4web_err_target=\"_self\";	}	else	{		try{			if(window.parent.parent==window.top)				v_aap4web_err_target=\"_parent\";		}		catch(e){			varv_aap4web_err_target=\"mainframe\";		}	}					varAAPlus4WebAPI={		submitForm:function(frm){			document.aap4web.submitForm(frm);		},		submitForm2:function(success_cb,frm){			document.aap4web.submitForm2(frm,success_cb);		},		openWindow:function(targetURL,target,feature){			document.aap4web.openWindow(targetURL,target,feature);		},		openWindow2:function(success_cb,targetURL,target,feature){			document.aap4web.openWindow2(targetURL,success_cb,target,feature);		},		sendAAPlus:function(targetURL,form){			varparam=$(form).serialize();			document.aap4web.sendAAPlus(targetURL,param,this.documentWrite_);		},		documentWrite_:function(success,html){						document.open();			document.wite(html);			document.close();		},		//��������.		submit:function(targetURL,frm,callbackFunc){			document.aap4web.submitForm(frm);		},		getSID:function(){			try{				returndocument.aap4web.getSID_AAPlus4Web();			}			catch(e){				return\"\";			}		}	};	//��������functionsendAAPlus4Web(targetURL,frm,callbackFunc){		alert(\"sendAAPlus4Web�޼���»����ɿ����Դϴ�.\");		AAPlus4WebAPI.submitForm(frm);	}functiongetSID_AAPlus4Web(){			returndocument.aap4web.getSID_AAPlus4Web();}<form><input><input>");
		System.out.println("val : " + val);

	}

	@Test
	public void checkHashElapsedTimeTest() {
		int ITERATION = 1000;

		long sum = 0;

		for (int i = 0; i < ITERATION; i++) {
			long start = System.currentTimeMillis();
			String val = makeHash("<html><script>functionuf_submit(form1){	alert(\"before\");	alert(document.aap4web.getSID_AAPlus4Web());	document.aap4web.submitForm(form1);	alert(\"after\");}<meta><meta><meta><script><script><script><script>	functionaaplus4Web_goInstallPage(){		if(window.top==window.self||AAPBrowserDetect.version<=7){				document.location.href=\"/aap4web/setup/aap4wp_install.jsp?prv=\"				+encodeURIComponent(document.location.href);		}		else{			url=\"/aap4web/setup/aap4wp_install.jsp?close=true\";			varsizeW=822;												varsizeH=640;			varnLeft=screen.width/2-sizeW/2;			varnTop=screen.height/2-sizeH/2;			varopt=\",toolbar=no,menubar=no,location=no,scrollbars=\"+scroll+\",status=no\";			window.open(url,\"_blank\",\"left=\"+nLeft+\",top=\"+nTop+\",width=\"+sizeW+\",height=\"+sizeH+opt);			document.location.href=\"/aap4web/setup/aap4wp_install.jsp?prv=\"				+encodeURIComponent(document.location.href)				+\"&manual=true\";		}	}	if(!AAP4WPInstall.isInstalled())		aaplus4Web_goInstallPage();AAP4WPInstall.printObject('0dNl9DJ4A8BtebC6Gx4tf1Bg3Z6ShurFGsYxbzO85oMKprQMKpis4rDcfgIEEDMR',\"aap4web\",\"/aap4web/setup/\",true);varv_aap4web_url=\"/v_aap4web.jsp\";var_aaweb_=\"PSYwolt/1pqP0Qcu0KSrqr1unisFgMKxbhhhO+DWNF+2nQCkq1llIp7SKRs2wrHJ\";varv_aap4web_err_url=\"/jsp/common/comm_info.jsp\";	varv_aap4web_err_target=\"mainframe\";if(window.top==window.self){v_aap4web_err_url=\"/jsp/common/pop_error_info.jsp\";v_aap4web_err_target=\"_self\";}	elseif(window.top==window.parent){		v_aap4web_err_target=\"_self\";	}	else	{		try{			if(window.parent.parent==window.top)				v_aap4web_err_target=\"_parent\";		}		catch(e){			varv_aap4web_err_target=\"mainframe\";		}	}					varAAPlus4WebAPI={		submitForm:function(frm){			document.aap4web.submitForm(frm);		},		submitForm2:function(success_cb,frm){			document.aap4web.submitForm2(frm,success_cb);		},		openWindow:function(targetURL,target,feature){			document.aap4web.openWindow(targetURL,target,feature);		},		openWindow2:function(success_cb,targetURL,target,feature){			document.aap4web.openWindow2(targetURL,success_cb,target,feature);		},		sendAAPlus:function(targetURL,form){			varparam=$(form).serialize();			document.aap4web.sendAAPlus(targetURL,param,this.documentWrite_);		},		documentWrite_:function(success,html){						document.open();			document.wite(html);			document.close();		},		//��������.		submit:function(targetURL,frm,callbackFunc){			document.aap4web.submitForm(frm);		},		getSID:function(){			try{				returndocument.aap4web.getSID_AAPlus4Web();			}			catch(e){				return\"\";			}		}	};	//��������functionsendAAPlus4Web(targetURL,frm,callbackFunc){		alert(\"sendAAPlus4Web�޼���»����ɿ����Դϴ�.\");		AAPlus4WebAPI.submitForm(frm);	}functiongetSID_AAPlus4Web(){			returndocument.aap4web.getSID_AAPlus4Web();}<form><input><input>");
			// System.out.println("val : " + val);
			// System.out.println("elapsedTime : "
			// + (System.currentTimeMillis() - start));
			sum += System.currentTimeMillis() - start;
		}
		// System.out.println("hash : " + val);
		System.out.println("average elapsedTime : " + sum / ITERATION + " ms ");

	}

	public static String makeHash(String value) {
		StringBuffer hashBuf = new StringBuffer();

		MessageDigest sha = null;
		try {
			sha = MessageDigest.getInstance("SHA-256");
			sha.update(value.getBytes("utf-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] digest = sha.digest();
		String hexString = "";
		for (int i = 0; i < digest.length; i++) {
			// hexString = Integer.toHexString(digest[i] & 0xFF);
			hexString = String.format("%02x", (char) digest[i] & 0xFF);
			hexString = hexString.toUpperCase();
			hashBuf.append(hexString);
		}

		return hashBuf.toString();
	}*/
}
