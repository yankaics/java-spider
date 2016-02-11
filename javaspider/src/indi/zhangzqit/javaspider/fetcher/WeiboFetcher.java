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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import indi.zhangzqit.javaspider.parser.WeiboParser;
import indi.zhangzqit.javaspider.parser.bean.Page;
import indi.zhangzqit.javaspider.queue.VisitedWeiboUrlQueue;
import indi.zhangzqit.javaspider.queue.WeiboUrlQueue;
import indi.zhangzqit.javaspider.utils.Constants;
import indi.zhangzqit.javaspider.utils.FetcherType;
import indi.zhangzqit.javaspider.utils.Utils;

public class WeiboFetcher {
	private static final Logger Log = Logger.getLogger(WeiboFetcher.class
			.getName());

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
				
				if((content.contains("<div class=\"c\">他还没发过微博.</div>") || content.contains("<div class=\"c\">她还没发过微博.</div>")) && (!url.contains("page=1&")) ){	
					url = url.split("&gsid")[0];
					Utils.handleAbnormalWeibo(content, url);
					return new Page(Constants.SYSTEM_EMPTY + "|" + url, null);
				}
				else{
					String returnMsg = Utils.checkContent(content, url, FetcherType.WEIBO);
					if(returnMsg != null){
						return new Page(returnMsg, null);
					}
				}

				contentDoc = WeiboParser.getPageDocument(content);
				// 判断是否符合下载网页源代码到本地的条件
				List<Element> weiboItems = WeiboParser.getGoalContent(contentDoc);
				
				// 微博数量超过限制，过滤掉，使其拿不到后续链接自动结束
				if(weiboItems == null){
					contentDoc = new Document("");
				}
				
				if(weiboItems != null && weiboItems.size() > 0){
					WeiboParser.createFile(weiboItems, url);
				}				
			}
		}
		catch(Exception e){
			Log.error(e);
			
			// 处理超时，和请求忙相同
			url = url.split("&gsid")[0];
			Log.info(">> Put back url: " + url);
			WeiboUrlQueue.addFirstElement(url);
			return new Page(Constants.SYSTEM_BUSY, null);
		}
		
		VisitedWeiboUrlQueue.addElement(url);
		return new Page(content, contentDoc);
	}
}
