package indi.zhangzqit.javaspider.worker.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.http.client.CookieStore;
import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.fetcher.FolloweeFetcher;
import indi.zhangzqit.javaspider.handler.NextUrlHandler;
import indi.zhangzqit.javaspider.parser.FollowParser;
import indi.zhangzqit.javaspider.parser.bean.Account;
import indi.zhangzqit.javaspider.queue.AccountQueue;
import indi.zhangzqit.javaspider.queue.FollowUrlQueue;
import indi.zhangzqit.javaspider.utils.Utils;
import indi.zhangzqit.javaspider.worker.BasicWorker;

/**
 * 从UrlQueue中取出url，下载页面，分析url，保存已访问url
 */
public class UrlFollowWorker extends BasicWorker implements Runnable {
	private static final Logger Log = Logger.getLogger(UrlFollowWorker.class.getName());
	public static int CURRENT_LEVEL = 0;
	
	// 返回值：被封账号/系统繁忙/OK
	protected String dataHandler(String url){
		return null;
	}
	
	private String dataHandler(String url, CookieStore cookie, int currentLevel){
		return NextUrlHandler.addNextFollowUrl(FolloweeFetcher.getContentFromUrl(url, cookie, currentLevel));
	}
	
	@Override
	public void run() {
		Account account = AccountQueue.outElement();
		AccountQueue.addElement(account);
		this.username = account.getUsername();
		this.password = account.getPassword();
		
		CookieStore cookie = loginForCookie(username, password);
		String result = null;
		
		try {
			if(cookie == null){
				cookie = switchAccountForCookie();
			}
			
			if(cookie != null) {
				while(!FollowUrlQueue.isEmpty()) {
					
					String followUrl = FollowUrlQueue.outElement();
					
					result = dataHandler(followUrl, cookie, CURRENT_LEVEL);
					
					// OK, SYSTEM_BUSY, ACCOUNT_FORBIDDEN
					cookie = process(result, cookie);
					
					if(FollowUrlQueue.isEmpty()){
						
						// 仍为空，从数据库中取
						if(FollowUrlQueue.isEmpty()){
							Log.info(">> Add new follow Url...");
							CURRENT_LEVEL = Utils.initializeFollowUrl();
							
							// 拿完还是空，退出爬虫
							if(FollowUrlQueue.isEmpty()){
								Log.info(">> All followees of all followers have been fetched...");
								break;
							}
						}
					}
				}
			}
			else{
				Log.info(">> " + username + " login failed!");
			}
		}
		catch (InterruptedException e) {
			Log.error(e);
		}
		catch (IOException e) {
			Log.error(e);
		}
		
		// 关闭数据库连接
		try {
			FollowParser.conn.close();
			Utils.conn.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		
		Log.info("Spider stop...");
	}
}
