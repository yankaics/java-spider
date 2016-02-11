package indi.zhangzqit.javaspider.handler;

import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import indi.zhangzqit.javaspider.parser.bean.Page;
import indi.zhangzqit.javaspider.queue.AbnormalAccountUrlQueue;
import indi.zhangzqit.javaspider.queue.CommentUrlQueue;
import indi.zhangzqit.javaspider.queue.FollowUrlQueue;
import indi.zhangzqit.javaspider.queue.RepostUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedCommentUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedFollowUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedRepostUrlQueue;
import indi.zhangzqit.javaspider.queue.VisitedWeiboUrlQueue;
import indi.zhangzqit.javaspider.queue.WeiboUrlQueue;
import indi.zhangzqit.javaspider.utils.Constants;

public class NextUrlHandler {

	public static final Logger Log = Logger.getLogger(NextUrlHandler.class
			.getName());

	/**
	 * ��ץȡҳ���content HTML�н�������һҳ��URL���������UrlQueue
	 */
	public static String addNextWeiboUrl(Page page){
		String content = page.getContent();
		Document doc = page.getContentDoc();
		
		if(content == null){
			return Constants.ACCOUNT_FORBIDDEN;
		}
		// ϵͳ��æ���˺ű���
		if(content.equals(Constants.ACCOUNT_FORBIDDEN) || content.equals(Constants.SYSTEM_BUSY)){
			return content;
		}
		// ΢��ҳ���쳣����ʾ��û�з���΢��������ת����һҳ��������
		else if(content.startsWith(Constants.SYSTEM_EMPTY)){
			Log.info(">> ��ǰҳ����ʾ��û�з���΢������" + content);
		}
		// ����
		else{
			
			Element pageEl = doc.getElementById("pagelist");
			
			if(pageEl != null){
				List<Element> hrefEls = pageEl.getElementsByTag("a");
				for(Element el: hrefEls){
					if(el.toString().contains("��ҳ")){
						// ��href�н�����page��ҳ�룬ǣ�浽gsid���Զ����ϵ����⣬���Ըɾ��Ľ�����ҳ��
						WeiboUrlQueue.addElement("http://weibo.cn" + el.attr("href").split("&gsid=")[0]); 
						break;
					}
				}
				Log.info(">> progress of current user: " + pageEl.text());
			}
		}
		Log.info("***************************************");
		Log.info("collected: " + WeiboUrlQueue.size());
		Log.info("successful: " + VisitedWeiboUrlQueue.size());
		Log.info("abnormal: " + AbnormalAccountUrlQueue.size());
		Log.info("***************************************");
		
		return Constants.OK;
	}

	public static String addNextCommentUrl(Page page){
		String content = page.getContent();
		Document doc = page.getContentDoc();
		
		if(content == null){
			return Constants.ACCOUNT_FORBIDDEN;
		}
		if(content.equals(Constants.ACCOUNT_FORBIDDEN) || content.equals(Constants.SYSTEM_BUSY)){
			return content;
		}
		
		Element pageEl = doc.getElementById("pagelist");
		if(pageEl != null){
			List<Element> hrefEls = pageEl.getElementsByTag("a");
			for(Element el: hrefEls){
				if(el.toString().contains("��ҳ")){
					// ��href�н�����page��ҳ�룬ǣ�浽gsid���Զ����ϵ����⣬���Ըɾ��Ľ�����ҳ��
					String[] hrefParts = el.attr("href").split("\\?");
					String pageNum = null;
					for(int i = 0; i < hrefParts.length; i++){
						if(hrefParts[i].contains("page=")){
							String[] params = hrefParts[i].split("&");
							for(int j = 0; j < params.length; j++){
								if(params[j].contains("page=")){
									pageNum = params[j].substring(5);
									break;
								}
							}
							break;
						}
					}
					String nextUrl = "http://weibo.cn" + hrefParts[0] + "?page=" + pageNum;
					CommentUrlQueue.addElement(nextUrl); 
					Log.info(">> Add next page: " + nextUrl);
					break;
				}
			}
		}
		
		Log.info("***************************************");
		Log.info("collected: " + CommentUrlQueue.size());
		Log.info("successful: " + VisitedCommentUrlQueue.size());
		Log.info("abnormal: " + AbnormalAccountUrlQueue.size());
		Log.info("***************************************");
		
		return Constants.OK;
	}

	public static String addNextRepostUrl(Page page){
		String content = page.getContent();
		Document doc = page.getContentDoc();
		
		if(content == null){
			return Constants.ACCOUNT_FORBIDDEN;
		}
		if(content.equals(Constants.ACCOUNT_FORBIDDEN) || content.equals(Constants.SYSTEM_BUSY)){
			return content;
		}
		
		Element pageEl = doc.getElementById("pagelist");	
		if(pageEl != null){
			List<Element> hrefEls = pageEl.getElementsByTag("a");
			for(Element el: hrefEls){
				if(el.toString().contains("��ҳ")){
					String[] hrefParts = el.attr("href").split("\\?");
					String pageNum = null;
					for(int i = 0; i < hrefParts.length; i++){
						if(hrefParts[i].contains("page=")){
							String[] params = hrefParts[i].split("&");
							for(int j = 0; j < params.length; j++){
								if(params[j].contains("page=")){
									pageNum = params[j].substring(5);
									break;
								}
							}
							break;
						}
					}
					String nextUrl = "http://weibo.cn" + hrefParts[0] + "?page=" + pageNum;
					RepostUrlQueue.addElement(nextUrl); 
					Log.info(">> Add next page: " + nextUrl);
					break;
				}
			}
		}
		
		Log.info("***************************************");
		Log.info("collected: " + RepostUrlQueue.size());
		Log.info("successful: " + VisitedRepostUrlQueue.size());
		Log.info("abnormal: " + AbnormalAccountUrlQueue.size());
		Log.info("***************************************");
		
		return Constants.OK;
	}

	public static String addNextFollowUrl(Page page){
		String content = page.getContent();
		Document doc = page.getContentDoc();
		
		if(content == null){
			return Constants.ACCOUNT_FORBIDDEN;
		}
		if(content.equals(Constants.ACCOUNT_FORBIDDEN) || content.equals(Constants.SYSTEM_BUSY)){
			return content;
		}
		
		if(doc != null){
			Element pageEl = doc.getElementById("pagelist");	
			if(pageEl != null){
				List<Element> hrefEls = pageEl.getElementsByTag("a");
				for(Element el: hrefEls){
					if(el.toString().contains("��ҳ")){
						String[] hrefParts = el.attr("href").split("\\?");
						String pageNum = null;
						for(int i = 0; i < hrefParts.length; i++){
							if(hrefParts[i].contains("page=")){
								String[] params = hrefParts[i].split("&");
								for(int j = 0; j < params.length; j++){
									if(params[j].contains("page=")){
										pageNum = params[j].substring(5);
										break;
									}
								}
								break;
							}
						}
						String nextUrl = "http://weibo.cn" + hrefParts[0] + "?page=" + pageNum;
						FollowUrlQueue.addElement(nextUrl); 
						Log.info(">> Add next page: " + nextUrl);
						break;
					}
				}
			}
		}
		Log.info("***************************************");
		Log.info("collected: " + FollowUrlQueue.size());
		Log.info("successful: " + VisitedFollowUrlQueue.size());
		Log.info("abnormal: " + AbnormalAccountUrlQueue.size());
		Log.info("***************************************");
		
		return Constants.OK;
	}
}
