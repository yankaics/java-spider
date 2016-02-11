package indi.zhangzqit.javaspider.worker.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.fetcher.WeiboFetcher;
import indi.zhangzqit.javaspider.handler.NextUrlHandler;
import indi.zhangzqit.javaspider.parser.WeiboParser;
import indi.zhangzqit.javaspider.parser.bean.Account;
import indi.zhangzqit.javaspider.queue.AccountQueue;
import indi.zhangzqit.javaspider.queue.WeiboUrlQueue;
import indi.zhangzqit.javaspider.utils.Utils;
import indi.zhangzqit.javaspider.worker.BasicWorker;

/**
 * 从UrlQueue中取出url，下载页面，分析url，保存已访问rul
 */
public class UrlWeiboWorker extends BasicWorker implements Runnable {
	private static final Logger Log = Logger.getLogger(UrlWeiboWorker.class.getName());
	protected String dataHandler(String url){
		return NextUrlHandler.addNextWeiboUrl(WeiboFetcher.getContentFromUrl(url));
	}
	
	@Override
	public void run() {
		Account account = AccountQueue.outElement();
		AccountQueue.addElement(account);
		this.username = account.getUsername();
		this.password = account.getPassword();
		
		String gsid = login(username, password);
		String result = null;
		try {
			if(gsid == null){
				gsid = switchAccount();
			}
		
			if(gsid != null) {
				while(!WeiboUrlQueue.isEmpty()) {
					result = dataHandler(WeiboUrlQueue.outElement() + "&" + gsid);
					
					gsid = process(result, gsid);

					if(WeiboUrlQueue.isEmpty()){
						if(WeiboUrlQueue.isEmpty()){
							Log.info(">> Add new weibo Url...");
							Utils.initializeWeiboUrl();
							
							if(WeiboUrlQueue.isEmpty()){
								Log.info(">> All users have been fetched...");
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
		
		try {
			WeiboParser.conn.close();
			Utils.conn.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		
		Log.info("*****************Stop*****************");
	}
}
