package indi.zhangzqit.javaspider.fetcher;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import indi.zhangzqit.javaspider.parser.RepostParser;
import indi.zhangzqit.javaspider.parser.bean.Page;
import indi.zhangzqit.javaspider.queue.RepostUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedRepostUrlQueue;
import indi.zhangzqit.javaspider.utils.Constants;
import indi.zhangzqit.javaspider.utils.FetcherType;
import indi.zhangzqit.javaspider.utils.Utils;

public class RepostFetcher {
	private static final Logger Log = Logger.getLogger(RepostFetcher.class
			.getName());

	public static Document getPageDocument(String content) {
		return Jsoup.parse(content);
	}

	public static Page getContentFromUrl(String url){
		String content = null;
		Document contentDoc = null;
		
		HttpParams params = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
	    HttpConnectionParams.setSoTimeout(params, 10 * 1000);	    
		AbstractHttpClient httpClient = new DefaultHttpClient(params);
		HttpGet getHttp = new HttpGet(url);	
		getHttp.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
		HttpResponse response;
		
		try{
			response = httpClient.execute(getHttp);
			HttpEntity entity = response.getEntity();				  
			
			if(entity != null){
				content = EntityUtils.toString(entity, "UTF-8");
				
				String returnMsg = Utils.checkContent(content, url, FetcherType.REPOST);
				if(returnMsg != null){
					return new Page(returnMsg, null);
				}
				
				contentDoc = RepostParser.getPageDocument(content);
				List<Element> repostItems = RepostParser.getGoalContent(contentDoc);
				if(repostItems != null && repostItems.size() > 0){
					RepostParser.createFile(repostItems, url);
				}				
			}
		}
		catch(Exception e){
			Log.error(e);
			
			url = url.split("&gsid")[0];
			Log.info(">> Put back url: " + url);
			RepostUrlQueue.addFirstElement(url);
			return new Page(Constants.SYSTEM_BUSY, null);
		}
		
		VisitedRepostUrlQueue.addElement(url);

		return new Page(content, contentDoc);
	}
}
