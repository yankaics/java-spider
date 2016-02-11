package indi.zhangzqit.javaspider.worker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import indi.zhangzqit.javaspider.utils.Constants;

public class LoginWeiboBackup {
	private static final Logger Log = Logger.getLogger(LoginWeiboBackup.class
			.getName());
	public static final String DOMAIN = "http://weibo.cn";
	public static final String URL = DOMAIN + "/dpool/ttt/login.php";
	private AbstractHttpClient client = new DefaultHttpClient();

	/**
	 * �����ڵ�¼�ֻ���΢��ʱ�������һ���ض���Ĺ��̣���˵�¼���̷�ΪPOST��GET��������
	 * ��һ�Σ��ύ��¼��Ϣ�����ݷ���ͷLocation��ȡgid �ڶ��Σ���gidƴ�ӵ�weibo.cn֮�󣬷�����ҳ����ȡcookie
	 */
	public String loginAndGetCookie(String username, String password) {
		String content = null;
		String gsid = null;

		try {
			HttpPost httpPost = new HttpPost(URL);

			httpPost
					.setHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
			httpPost
					.setHeader("Referer", "http://weibo.cn/dpool/ttt/login.php");
			httpPost.setHeader("Content-Type",
					"application/x-www-form-urlencoded");

			StringEntity reqEntity = new StringEntity("uname="
					+ encodeAccount(username) + "&pwd=" + password
					+ "&l=&scookie=on");
			httpPost.setEntity(reqEntity);

			HttpResponse response = client.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				entity.consumeContent();
			}

			// ΢����¼��ᱻ�ض��򣬻�ȡ�ض���URL
			String redirectStr = response.getLastHeader("Location").getValue();
			gsid = redirectStr.substring(redirectStr.indexOf("?") + 1);
			String redirectUrl = DOMAIN + "?" + gsid;

			HttpGet httpGet = new HttpGet(redirectUrl);

			httpGet
					.setHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; rv:16.0) Gecko/20100101 Firefox/16.0");
			httpGet.setHeader("Referer", "http://weibo.cn/dpool/ttt/login.php");

			response = client.execute(httpGet);
			entity = response.getEntity();
			if (entity != null) {
				// ת��Ϊ�ı���Ϣ, ������ȡ��ҳ���ַ�������ֹ����
				content = EntityUtils.toString(entity, "UTF-8");
			}

			Log.info(">> login response content: \n" + content);
			Log.info(">> login response cookies: \n"
					+ client.getCookieStore().getCookies().toString());
		} catch (ClientProtocolException e) {
			Log.error(e);
			client.getConnectionManager().shutdown();
		} catch (UnsupportedEncodingException e) {
			Log.error(e);
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			Log.error(e);
			client.getConnectionManager().shutdown();
		}

		if (content == null
				|| content.contains(Constants.FORBIDDEN_PAGE_TITILE)) {
			return null;
		}

		return gsid;
	}

	private static String encodeAccount(String account) {
		String userName = "";
		try {
			userName = URLEncoder.encode(account, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.error(e);
		}
		return userName;
	}
}
