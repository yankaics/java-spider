package indi.zhangzqit.javaspider.worker.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.fetcher.CommentFetcher;
import indi.zhangzqit.javaspider.handler.NextUrlHandler;
import indi.zhangzqit.javaspider.parser.CommentParser;
import indi.zhangzqit.javaspider.parser.bean.Account;
import indi.zhangzqit.javaspider.queue.AccountQueue;
import indi.zhangzqit.javaspider.queue.CommentUrlQueue;
import indi.zhangzqit.javaspider.utils.Utils;
import indi.zhangzqit.javaspider.worker.BasicWorker;

/**
 * 从UrlQueue中取出url，下载页面，分析url，保存已访问rul
 */       
public class UrlCommentWorker extends BasicWorker implements Runnable {
	private static final Logger Log = Logger.getLogger(UrlCommentWorker.class.getName());
	
	protected String dataHandler(String url){
		return NextUrlHandler.addNextCommentUrl(CommentFetcher.getContentFromUrl(url));
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
				while(!CommentUrlQueue.isEmpty()) {
					result = dataHandler(CommentUrlQueue.outElement() + "&" + gsid);
					
					gsid = process(result, gsid);

					// 拿完还是空，退出爬虫
					if(CommentUrlQueue.isEmpty()){
						if(CommentUrlQueue.isEmpty()){
							Log.info(">> Add new comment Url...");
							Utils.initializeCommentUrl();
							
							if(CommentUrlQueue.isEmpty()){
								Log.info(">> All comments of all weibos have been fetched...");
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
			CommentParser.conn.close();
			Utils.conn.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		
		Log.info("Spider stop...");
	}
}
