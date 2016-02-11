package indi.zhangzqit.javaspider.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.parser.bean.Account;
import indi.zhangzqit.javaspider.queue.AbnormalAccountUrlQueue;
import indi.zhangzqit.javaspider.queue.AccountQueue;
import indi.zhangzqit.javaspider.queue.CommentUrlQueue;
import indi.zhangzqit.javaspider.queue.FollowUrlQueue;
import indi.zhangzqit.javaspider.queue.RepostUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedCommentUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedFollowUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedRepostUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedWeiboUrlQueue;
import indi.zhangzqit.javaspider.queue.WeiboUrlQueue;

public class Utils {
	private static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static final Logger Log = Logger.getLogger(Utils.class.getName());
	public static Connection conn = DBConn.getConnection();

	public static boolean isEmptyStr(String str) {
		if (str == null || str.trim().length() == 0) {
			return true;
		}

		return false;
	}

	/**
	 * ��ʱ��ת����yyyyMMddHHmmSS
	 */
	public static String parseDate(String weiboTimeStr) {

		Calendar currentTime = Calendar.getInstance();// ʹ��Ĭ��ʱ�������Ի������һ��������

		if (weiboTimeStr.contains("����ǰ")) {
			int minutes = Integer.parseInt(weiboTimeStr.split("����ǰ")[0]);

			currentTime.add(Calendar.MINUTE, -minutes);

			return simpleDateTimeFormat.format(currentTime.getTime());
		} else if (weiboTimeStr.startsWith("����")) {
			String[] time = weiboTimeStr.split("��")[1].split(":");
			int hour = Integer.parseInt(time[0].substring(1));
			int minute = Integer.parseInt(time[1].substring(0, 2));

			currentTime.set(Calendar.HOUR_OF_DAY, hour);
			currentTime.set(Calendar.MINUTE, minute);

			return simpleDateTimeFormat.format(currentTime.getTime());
		} else if (weiboTimeStr.contains("��")) {
			String[] time = weiboTimeStr.split("��")[1].split(":");
			int dayIndex = weiboTimeStr.indexOf("��") - 2;
			int month = Integer.parseInt(weiboTimeStr.substring(0, 2));
			int day = Integer.parseInt(weiboTimeStr.substring(dayIndex,
					dayIndex + 2));
			int hour = Integer.parseInt(time[0].substring(1));
			int minute = Integer.parseInt(time[1].substring(0, 2));

			currentTime.set(Calendar.MONTH, month - 1);
			currentTime.set(Calendar.DAY_OF_MONTH, day);
			currentTime.set(Calendar.HOUR_OF_DAY, hour);
			currentTime.set(Calendar.MINUTE, minute);

			return simpleDateTimeFormat.format(currentTime.getTime());
		} else if (weiboTimeStr.contains("-")) {
			return weiboTimeStr.replace("-", "").replace(":", "").replace(" ",
					"").substring(0, 14);
		} else {
			Log.info(">> Error: Unknown time format - " + weiboTimeStr);
		}

		return null;
	}

	/**
	 * ����logType����־д����Ӧ���ļ�
	 */
	public static void writeLog(int logType, String logStr) {
		// ѡȡlog����
		String filePath = null;
		switch (logType) {
		case LogType.SWITCH_ACCOUNT_LOG:
			filePath = Constants.SWITCH_ACCOUNT_LOG_PATH;
			break;
		case LogType.COMMENT_LOG:
			filePath = Constants.COMMENT_LOG_PATH;
			break;
		case LogType.REPOST_LOG:
			filePath = Constants.REPOST_LOG_PATH;
			break;
		case LogType.WEIBO_LOG:
			filePath = Constants.ABNORMAL_WEIBO_PATH;
			break;
		default:
			return;
		}

		// д����־
		try {
			FileWriter fileWriter = new FileWriter(filePath, true);
			if (logType == LogType.WEIBO_LOG) {
				fileWriter.write(logStr + "\r\n");
			} else {
				fileWriter.write((new Date()).toString() + ": " + logStr
						+ "\r\n");
			}
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			Log.error(e);
		}
	}

	/**
	 * ���쳣�˺�д���ļ�
	 */
	public static void writeAbnormalAccount(String account) throws IOException {
		FileWriter fileWriter = new FileWriter(Constants.ABNORMAL_ACCOUNT_PATH,
				true);
		fileWriter.write(account + "\r\n");
		fileWriter.flush();
		fileWriter.close();
	}

	// ��url�н�������ǰ�û���ID
	public static String getUserIdFromUrl(String url) {
		int startIndex = url.lastIndexOf("/");
		int endIndex = url.indexOf("?");

		if (endIndex == -1) {
			return url.substring(startIndex + 1);
		}
		return url.substring(startIndex + 1, endIndex);
	}

	// ��follow url�н�������ǰ�û���ID
	public static String getUserIdFromFollowUrl(String url) {
		int startIndex = 16;
		int endIndex = url.indexOf("/follow");

		return url.substring(startIndex, endIndex);
	}

	public static String getUserIdFromImgUrl(String url) {
		int startIndex = url.indexOf("sinaimg.cn/") + "sinaimg.cn/".length();
		String subStr = url.substring(startIndex);

		return subStr.substring(0, subStr.indexOf("/"));
	}

	/**
	 * ��ʽΪ��account----email----password
	 */
	public static void readAccountFromFile() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					Constants.LOGIN_ACCOUNT_PATH));
			String accountLine = null;
			while (((accountLine = reader.readLine()) != null)) {
				String[] account = accountLine.split("----");
				AccountQueue.addElement(new Account(account[0], account[2]));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			Log.error(e);
		} catch (IOException e) {
			Log.error(e);
		}
	}

	/**
	 * ���ݿ��ж�ȡ�û��˺ţ������ɵ�һҳ΢����url������WeiboUrlQueue
	 */
	public static synchronized void initializeWeiboUrl() {
		String querySql = "SELECT accountID FROM USER WHERE isFetched = 0 ORDER BY id LIMIT 1";
		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs = null;
		String accountID = null;

		try {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			st = conn.createStatement();
			rs = st.executeQuery(querySql);
			if (rs.next()) {
				accountID = rs.getString("accountID");
				ps = conn
						.prepareStatement("UPDATE USER SET isFetched = 1 WHERE accountID = ?");
				ps.setString(1, accountID);
				ps.execute();
				ps.close();
			}
			rs.close();
			st.close();

			conn.commit();
			if (accountID != null) {
				// �ύ�ɹ����ٷ������
				WeiboUrlQueue.addElement(Constants.WEIBO_BASE_STR + accountID
						+ "?page=1");
			}
		} catch (SQLException e) {
			Log.error(e);
			// �ύʧ�� roll back������������е�URL�ó���
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Log.error(e1);
			}
		} finally {
		}
	}

	/**
	 * ���ݿ��ж�ȡ��΢���˺ţ������ɵ�һҳ���۵�url������CommentUrlQueue
	 */
	public static synchronized void initializeCommentUrl() {
		String querySql = "SELECT weiboID FROM weibo WHERE isCommentFetched = 0 LIMIT 1";
		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs = null;
		String weiboID = null;

		try {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			st = conn.createStatement();
			rs = st.executeQuery(querySql);
			if (rs.next()) {
				weiboID = rs.getString("weiboID");
				ps = conn
						.prepareStatement("UPDATE weibo SET isCommentFetched = 1 WHERE weiboID = ?");
				ps.setString(1, weiboID);
				ps.execute();
				ps.close();
			}
			rs.close();
			st.close();

			conn.commit();
			if (weiboID != null) {
				// �ύ�ɹ����ٷ������
				CommentUrlQueue.addElement(Constants.COMMENT_BASE_STR + weiboID
						+ "?page=1");
			}
		} catch (SQLException e) {
			Log.error(e);
			// �ύʧ�� roll back������������е�URL�ó���
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Log.error(e1);
			}
		} finally {
		}
	}

	/**
	 * ���ݿ��ж�ȡ΢���˺ţ������ɵ�һҳת����url������WeiboUrlQueue
	 */
	public static synchronized void initializeRepostUrl() {
		String querySql = "SELECT weiboID FROM weibo WHERE isRepostFetched = 0 LIMIT 1";
		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs = null;
		String weiboID = null;

		try {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			st = conn.createStatement();
			rs = st.executeQuery(querySql);
			if (rs.next()) {
				weiboID = rs.getString("weiboID");
				ps = conn
						.prepareStatement("UPDATE weibo SET isRepostFetched = 1 WHERE weiboID = ?");
				ps.setString(1, weiboID);
				ps.execute();
				ps.close();
			}
			rs.close();
			st.close();

			conn.commit();
			if (weiboID != null) {
				RepostUrlQueue.addElement(Constants.REPOST_BASE_STR + weiboID
						+ "?page=1");
			}
		} catch (SQLException e) {
			Log.error(e);
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Log.error(e1);
			}
		} finally {
		}
	}

	/**
	 * ��account.txt�ж�ȡ�û��˺ţ��������û���ҳ��url������AccountInfoUrlQueue
	 */
	public static void initializeAbnormalWeiboUrl() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(Constants.ABNORMAL_WEIBO_CLEANED_PATH),
					"utf-8"));

			String accountLine = null;
			while ((accountLine = reader.readLine()) != null) {
				WeiboUrlQueue.addElement(accountLine);
			}
			reader.close();
		} catch (IOException e) {
			Log.error(e);
		}
	}

	public static int initializeFollowUrl() {
		String querySql = "SELECT follower, LEVEL FROM follower WHERE isFetched = 0 ORDER BY LEVEL ASC LIMIT 1";
		PreparedStatement ps = null;
		Statement st = null;
		ResultSet rs = null;
		String followerID = null;
		int level = Integer.MAX_VALUE;

		try {
			// ��ȡ����follower��level
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			st = conn.createStatement();
			rs = st.executeQuery(querySql);
			if (rs.next()) {
				followerID = rs.getString("follower");
				level = rs.getInt("level");
				ps = conn
						.prepareStatement("UPDATE follower SET isFetched = 1 WHERE follower = ?");
				ps.setString(1, followerID);
				ps.execute();
				ps.close();
			}
			rs.close();
			st.close();

			conn.commit();

			// ������level < Constants.LEVEL������Ӷ���URL
			if (level < Constants.LEVEL) {
				FollowUrlQueue.addElement("http://weibo.cn/" + followerID
						+ "/follow");
			}
		} catch (SQLException e) {
			Log.error(e);

			try {
				conn.rollback();
			} catch (SQLException e1) {
				Log.error(e1);
			}
		} finally {

		}

		return level;
	}

	public static synchronized void addNextLevelFollower(int currentLevel) {

		String querySql = "SELECT DISTINCT followee FROM follow WHERE LEVEL = ? AND followee NOT IN(SELECT DISTINCT follower FROM follow )";
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(querySql);
			ps.setInt(1, currentLevel);
			rs = ps.executeQuery();
			while (rs.next()) {
				FollowUrlQueue.addElement("http://weibo.cn/"
						+ rs.getString("followee") + "/follow");
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			Log.error(e);
		} finally {

		}
	}

	public static String checkContent(String content, String url,
			int fetcherType) throws IOException {
		String returnMsg = null;
		// ��⵱ǰ���ʵ��û��Ƿ�Ϊ�쳣�˺�
		if (content.contains("<div class=\"me\">��Ǹ������ǰ���ʵ��û�״̬�쳣����ʱ�޷����ʡ�</div>")
				|| content.contains("<div class=\"me\">�û�������Ŷ!</div>")
				|| content.contains("<div class=\"me\">����ҳ������")) {

			AbnormalAccountUrlQueue.addElement(url);

			Log.info(">> ��ǰ���ʵ��û����쳣�˺�: " + content);
			Log.info("-----------------------------------");

			if (fetcherType == FetcherType.COMMENT) {
				Utils.writeAbnormalAccount(Utils.getUserIdFromUrl(url));
				Log.info("ץȡ������������" + CommentUrlQueue.size());
				Log.info("�Ѵ����ҳ������" + VisitedCommentUrlQueue.size());
			} else if (fetcherType == FetcherType.REPOST) {
				Utils.writeAbnormalAccount(Utils.getUserIdFromUrl(url));
				Log.info("ץȡ������������" + RepostUrlQueue.size());
				Log.info("�Ѵ����ҳ������" + VisitedRepostUrlQueue.size());
			} else if (fetcherType == FetcherType.WEIBO) {
				Utils.writeAbnormalAccount(Utils.getUserIdFromUrl(url));
				Log.info("ץȡ������������" + WeiboUrlQueue.size());
				Log.info("�Ѵ����ҳ������" + VisitedWeiboUrlQueue.size());
			} else if (fetcherType == FetcherType.FOLLOW) {
				Utils.writeAbnormalAccount(Utils.getUserIdFromFollowUrl(url));
				Log.info("ץȡ������������" + FollowUrlQueue.size());
				Log.info("�Ѵ����ҳ������" + VisitedFollowUrlQueue.size());
			}

			Log.info("�쳣�˺���         ��" + AbnormalAccountUrlQueue.size());
			Log.info("----------------------------------");
			returnMsg = Constants.OK;
			// ����˺��Ƿ񱻶���
		} else if (content.contains(Constants.FORBIDDEN_PAGE_TITILE)
				|| content
						.contains("<div class=\"c\">���΢���˺ų����쳣����ʱ����!<br/>������²������ɼ������΢����<br/></div>")
				|| content
						.contains("<div class=\"c\">��Ǹ������ʺŴ����쳣����ʱ�޷����ʡ�<br/>")
				|| content.contains("<div class=\"c\">�����ʺŴ����쳣����ʱ�޷����ʡ�<br/>")
				|| content.contains("<div class=\"c\">����΢���ʺų����쳣����ʱ���ᡣ<br/>")
				|| content.contains("<div class=\"c\">�����֤�󼴿ɿ�ʼ΢��֮�ã�</div>")) {
			// ����ʱ�����˺��ˣ���ǰurlû�д����Ƴ�ԭgsid�����ض���ͷ����������forbidden
			url = url.split("&gsid")[0];
			Log.info(">> Put back url: " + url);
			Log.info(">> ��ǰ�˺ű�����: " + content);

			if (fetcherType == FetcherType.COMMENT) {
				CommentUrlQueue.addFirstElement(url);
			} else if (fetcherType == FetcherType.REPOST) {
				RepostUrlQueue.addFirstElement(url);
			} else if (fetcherType == FetcherType.WEIBO) {
				WeiboUrlQueue.addFirstElement(url);
			} else if (fetcherType == FetcherType.FOLLOW) {
				FollowUrlQueue.addFirstElement(url);
			}

			returnMsg = Constants.ACCOUNT_FORBIDDEN;
		} else if (content.contains("<div class=\"me\">ϵͳ��æ,���Ժ�����!</div>")) {
			// ϵͳ��æ����ǰurlû�д����Ƴ�ԭgsid�����ض���ͷ����������busy
			url = url.split("&gsid")[0];
			Log.info(">> Put back url: " + url);
			Log.info(">> ϵͳ��æ: " + content);

			if (fetcherType == FetcherType.COMMENT) {
				CommentUrlQueue.addFirstElement(url);
			} else if (fetcherType == FetcherType.REPOST) {
				RepostUrlQueue.addFirstElement(url);
			} else if (fetcherType == FetcherType.WEIBO) {
				WeiboUrlQueue.addFirstElement(url);
			}

			returnMsg = Constants.SYSTEM_BUSY;
		}

		return returnMsg;
	}

	public static void handleAbnormalWeibo(String content, String url) {
		String[] urlParts = url.split("page=");
		int page = Integer.parseInt(urlParts[1]);
		int weiboNum = Integer.parseInt(content
				.split("<div class=\"tip2\"><span class=\"tc\">΢��\\[")[1]
				.split("\\]")[0]);

		if (page * 10 >= weiboNum) {
			return;
		} else {
			Utils.writeLog(LogType.WEIBO_LOG, url);
			String nextUrl = urlParts[0] + (page + 1);
			WeiboUrlQueue.addElement(nextUrl);
		}

	}
}
