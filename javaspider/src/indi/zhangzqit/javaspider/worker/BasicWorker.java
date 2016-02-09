package indi.zhangzqit.javaspider.worker;

import java.io.IOException;
import java.util.Date;

import org.apache.http.client.CookieStore;
import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.parser.bean.Account;
import indi.zhangzqit.javaspider.queue.AccountQueue;
import indi.zhangzqit.javaspider.utils.Constants;
import indi.zhangzqit.javaspider.utils.LogType;
import indi.zhangzqit.javaspider.utils.Utils;

public abstract class BasicWorker {
	private static final Logger Log = Logger.getLogger(BasicWorker.class
			.getName());

	protected String username = null;
	protected String password = null;

	// ����ֵ�������˺�/ϵͳ��æ/OK
	protected abstract String dataHandler(String url);

	protected String process(String result, String gsid)
			throws InterruptedException, IOException {
		// ��ֹƵ�����ʱ����ٶ����˺�
		if (result.equals(Constants.OK)) {
			Thread.sleep(200);
		} else if (result.equals(Constants.SYSTEM_BUSY)) {
			Log.info(">> System busy, retry after 5s...");
			Thread.sleep(5 * 1000);
		} else if (result.equals(Constants.ACCOUNT_FORBIDDEN)) {
			Log.info(">> " + (new Date()).toString() + ": " + username
					+ " account has been frozen!");
			gsid = switchAccount();
			while (gsid == null) {
				Thread.sleep(5 * 60 * 1000);
				gsid = switchAccount();
			}
		}

		return gsid;
	}

	protected CookieStore process(String result, CookieStore cookie)
			throws InterruptedException, IOException {
		if (result.equals(Constants.OK)) {
			Thread.sleep(200);
		} else if (result.equals(Constants.SYSTEM_BUSY)) {
			Log.info(">> System busy, retry after 5s...");
			Thread.sleep(5 * 1000);
		} else if (result.equals(Constants.ACCOUNT_FORBIDDEN)) {
			Log.info(">> " + (new Date()).toString() + ": " + username
					+ " account has been frozen!");
			cookie = switchAccountForCookie();
			while (cookie == null) {
				Thread.sleep(5 * 60 * 1000);
				cookie = switchAccountForCookie();
			}
		}

		return cookie;
	}

	// �����û����������¼΢���ֻ��棬������ά���Ự��gsid ��¼ʧ��ʱ����null
	public String login(String username, String password) {
		return (new LoginWeibo()).loginAndGetGsid(username, password);
	}

	// �����û����������¼΢���ֻ��棬������ά���Ự��cookie ��¼ʧ��ʱ����null
	public CookieStore loginForCookie(String username, String password) {
		return (new LoginWeibo()).loginAndGetCookie(username, password);
	}

	public String switchAccount() throws IOException {
		Account account = null;
		String gsid = null;
		do {
			account = AccountQueue.outElement();
			AccountQueue.addElement(account);
			gsid = login(account.getUsername(), account.getPassword());
			if (gsid != null) {
				this.username = account.getUsername();
				this.password = account.getPassword();
				String logStr = "Switch to account: " + account.getUsername()
						+ " success!";
				Utils.writeLog(LogType.SWITCH_ACCOUNT_LOG, logStr);
				break;
			}
			String logStr = "Switch to account: " + account.getUsername()
					+ " failed!";
			Utils.writeLog(LogType.SWITCH_ACCOUNT_LOG, logStr);
		} while (!account.getUsername().equals(username));

		return gsid;
	}

	// �л��˻�����¼
	public CookieStore switchAccountForCookie() throws IOException {
		Account account = null;
		CookieStore cookie = null;
		do {
			account = AccountQueue.outElement();
			AccountQueue.addElement(account);
			cookie = loginForCookie(account.getUsername(), account
					.getPassword());
			if (cookie != null) {
				this.username = account.getUsername();
				this.password = account.getPassword();
				String logStr = "Switch to account: " + account.getUsername()
						+ " success!";
				Utils.writeLog(LogType.SWITCH_ACCOUNT_LOG, logStr);
				break;
			}
			String logStr = "Switch to account: " + account.getUsername()
					+ " failed!";
			Utils.writeLog(LogType.SWITCH_ACCOUNT_LOG, logStr);
		} while (!account.getUsername().equals(username));

		return cookie;
	}
}
