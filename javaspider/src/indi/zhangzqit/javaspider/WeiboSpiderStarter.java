package indi.zhangzqit.javaspider;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.utils.Constants;
import indi.zhangzqit.javaspider.utils.DBConn;
import indi.zhangzqit.javaspider.utils.Utils;
import indi.zhangzqit.javaspider.worker.impl.UrlAbnormalWeiboWorker;
import indi.zhangzqit.javaspider.worker.impl.UrlCommentWorker;
import indi.zhangzqit.javaspider.worker.impl.UrlFollowWorker;
import indi.zhangzqit.javaspider.worker.impl.UrlRepostWorker;
import indi.zhangzqit.javaspider.worker.impl.UrlWeiboWorker;

public class WeiboSpiderStarter {
	private static final Logger Log = Logger.getLogger(WeiboSpiderStarter.class
			.getName());
	private static int WORKER_NUM = 1;
	private static String TYPE;

	public static void main(String[] args) {
		initializeParams();

		// 判断任务类型
		if (TYPE.equals("weibo")) {
			fetchWeibo();
		} else if (TYPE.equals("comment")) {
			fetchComment();
		} else if (TYPE.equals("repost")) {
			fetchRepost();
		} else if (TYPE.equals("abnormal")) {
			fetchAbnormalWeibo();
		} else if (TYPE.equals("follow")) {
			fetchFollowee();
		} else {
			Log.error("Unknown crawl type: " + TYPE + ".\n Exit...");
		}
	}

	/**
	 * 从配置文件中读取配置信息：数据库连接、相关文件根目录、爬虫任务类型
	 */
	private static void initializeParams() {
		InputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(
					"conf/spider.properties"));
			Properties properties = new Properties();
			properties.load(in);

			DBConn.CONN_URL = properties.getProperty("DB.connUrl");
			DBConn.USERNAME = properties.getProperty("DB.username");
			DBConn.PASSWORD = properties.getProperty("DB.password");

			Constants.ROOT_DISK = properties.getProperty("spider.rootDisk");
			Constants.REPOST_LOG_PATH = Constants.ROOT_DISK + "repost_log.txt";
			Constants.COMMENT_LOG_PATH = Constants.ROOT_DISK
					+ "comment_log.txt";
			Constants.SWITCH_ACCOUNT_LOG_PATH = Constants.ROOT_DISK
					+ "switch_account_log.txt";
			Constants.ACCOUNT_PATH = Constants.ROOT_DISK + "account.txt";
			Constants.ACCOUNT_RESULT_PATH = Constants.ROOT_DISK
					+ "account_result.txt";
			Constants.LOGIN_ACCOUNT_PATH = Constants.ROOT_DISK
					+ "login_account.txt";
			Constants.ABNORMAL_ACCOUNT_PATH = Constants.ROOT_DISK
					+ "abnormal_account.txt";
			Constants.ABNORMAL_WEIBO_PATH = Constants.ROOT_DISK
					+ "abnormal_weibo.txt";
			Constants.ABNORMAL_WEIBO_CLEANED_PATH = Constants.ROOT_DISK
					+ "abnormal_weibo_cleaned.txt";

			WeiboSpiderStarter.TYPE = properties.getProperty("spider.type");

			if (TYPE.equals("follow")) {
				Constants.LEVEL = Integer.parseInt(properties
						.getProperty("follow.level"));
				Constants.FANS_NO_MORE_THAN = Integer.parseInt(properties
						.getProperty("follow.maxFansNum"));
			}

			Constants.CHECK_WEIBO_NUM = Boolean.parseBoolean(properties
					.getProperty("weibo.checkWeiboNum", "false"));
			if (Constants.CHECK_WEIBO_NUM) {
				Constants.WEIBO_NO_MORE_THAN = Integer.parseInt(properties
						.getProperty("weibo.maxWeiboNum"));
			}

			in.close();
		} catch (FileNotFoundException e) {
			Log.error(e);
		} catch (IOException e) {
			Log.error(e);
		}
	}

	private static void fetchWeibo() {
		Log
				.info("\n\n\n**********************************************************[Fetch Weibo]\n");
		// 初始化账号队列
		Utils.readAccountFromFile();

		// 初始化链接
		Utils.initializeWeiboUrl();

		// 启动worker线程
		for (int i = 0; i < WORKER_NUM; i++) {
			new Thread(new UrlWeiboWorker()).start();
		}
	}

	private static void fetchAbnormalWeibo() {
		Log
				.info("\n\n\n**********************************************************[Abnormal Weibo]\n");
		Utils.readAccountFromFile();

		Utils.initializeAbnormalWeiboUrl();

		for (int i = 0; i < WORKER_NUM; i++) {
			new Thread(new UrlAbnormalWeiboWorker()).start();
		}
	}

	private static void fetchComment() {
		Log
				.info("\n\n\n**********************************************************[Fetch Comment]\n");
		Utils.readAccountFromFile();

		Utils.initializeCommentUrl();

		for (int i = 0; i < WORKER_NUM; i++) {
			new Thread(new UrlCommentWorker()).start();
		}
	}

	private static void fetchRepost() {
		Log
				.info("\n\n\n**********************************************************[Fetch Repost]\n");
		Utils.readAccountFromFile();

		Utils.initializeRepostUrl();

		for (int i = 0; i < WORKER_NUM; i++) {
			new Thread(new UrlRepostWorker()).start();
		}
	}

	private static void fetchFollowee() {
		Log
				.info("\n\n\n**********************************************************[Fetch Followee]\n");
		Utils.readAccountFromFile();

		UrlFollowWorker.CURRENT_LEVEL = Utils.initializeFollowUrl();

		for (int i = 0; i < WORKER_NUM; i++) {
			new Thread(new UrlFollowWorker()).start();
		}
	}
}
