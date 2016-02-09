package indi.zhangzqit.javaspider.worker.impl;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.fetcher.RepostFetcher;
import indi.zhangzqit.javaspider.handler.NextUrlHandler;
import indi.zhangzqit.javaspider.parser.RepostParser;
import indi.zhangzqit.javaspider.parser.bean.Account;
import indi.zhangzqit.javaspider.queue.AccountQueue;
import indi.zhangzqit.javaspider.queue.RepostUrlQueue;
import indi.zhangzqit.javaspider.utils.Utils;
import indi.zhangzqit.javaspider.worker.BasicWorker;

public class UrlRepostWorker extends BasicWorker implements Runnable {
	private static final Logger Log = Logger.getLogger(UrlRepostWorker.class.getName());
	protected String dataHandler(String url){
		return NextUrlHandler.addNextRepostUrl(RepostFetcher.getContentFromUrl(url));
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
				while(!RepostUrlQueue.isEmpty()) {
					result = dataHandler(RepostUrlQueue.outElement() + "&" + gsid);
					
					gsid = process(result, gsid);

					if(RepostUrlQueue.isEmpty()){
						if(RepostUrlQueue.isEmpty()){
							Log.info(">> Add new repost Url...");
							Utils.initializeRepostUrl();
							
							if(RepostUrlQueue.isEmpty()){
								Log.info(">> All reposts of all weibos have been fetched...");
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
			RepostParser.conn.close();
			Utils.conn.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		
		Log.info("***************Stop***************");
	}
}
