package indi.zhangzqit.javaspider.worker.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.fetcher.WeiboFetcher;
import indi.zhangzqit.javaspider.parser.WeiboParser;
import indi.zhangzqit.javaspider.parser.bean.Account;
import indi.zhangzqit.javaspider.queue.AbnormalAccountUrlQueue;
import indi.zhangzqit.javaspider.queue.AccountQueue;
import indi.zhangzqit.javaspider.queue.VisitedWeiboUrlQueue;
import indi.zhangzqit.javaspider.queue.WeiboUrlQueue;
import indi.zhangzqit.javaspider.utils.Utils;
import indi.zhangzqit.javaspider.worker.BasicWorker;

/**
 * 从UrlQueue中取出url，下载页面，分析url，保存已访问rul
 */
public class UrlAbnormalWeiboWorker extends BasicWorker implements Runnable {

	private static final Logger Log = Logger.getLogger(UrlAbnormalWeiboWorker.class.getName());

	// 下载对应页面并分析出页面对应URL，放置在未访问队列中
	protected String dataHandler(String url){
		Log.info("**************************************************");
		Log.info("collected: " + WeiboUrlQueue.size());
		Log.info("successful: " + VisitedWeiboUrlQueue.size());
		Log.info("abnormal: " + AbnormalAccountUrlQueue.size());
		Log.info("**************************************************");
		
		return WeiboFetcher.getContentFromUrl(url).getContent();
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
			// 若登录失败，则执行一轮切换账户的操作，如果还失败，则退出     
			if(gsid == null){
				gsid = switchAccount();
			}
		
			// 登录成功
			if(gsid != null) {
				while(!WeiboUrlQueue.isEmpty()) {
					result = dataHandler(WeiboUrlQueue.outElement() + "&" + gsid);
					
					gsid = process(result, gsid);

					if(WeiboUrlQueue.isEmpty()){
						Log.info(">> All abnormal weibos have been fetched...");
						break;
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
			WeiboParser.conn.close();
			Utils.conn.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		
		Log.info("Spider stop...");
	}
}
