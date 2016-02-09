package indi.zhangzqit.javaspider.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import indi.zhangzqit.javaspider.parser.bean.Weibo;
import indi.zhangzqit.javaspider.utils.Constants;
import indi.zhangzqit.javaspider.utils.DBConn;
import indi.zhangzqit.javaspider.utils.Utils;

public class WeiboParser {
	private static final int BACK_NODES_NUM_IN_REPOST_DIV = 9;
	private static final Logger Log = Logger.getLogger(WeiboParser.class.getName());
	public static Connection conn = DBConn.getConnection();
	
	public static Document getPageDocument(String content){
		return Jsoup.parse(content);
	}
	
	public static List<Element> getGoalContent(Document doc) {
		List<Element> weiboItems = new ArrayList<Element>();
		
		if(Constants.CHECK_WEIBO_NUM){
			Element element = doc.getElementsByClass("tc").get(0);
			int startIndex = element.text().indexOf("[");
			int endIndex = element.text().indexOf("]");
			int weiboNum = Integer.parseInt(element.text().substring(startIndex + 1, endIndex));
			if(weiboNum > Constants.WEIBO_NO_MORE_THAN){
				Log.info("Number of weibos is too large: " + weiboNum);
				return null;
			}
		}
		
		Elements elements = doc.getElementsByClass("c");
		for(Element el: elements){
			if(el.id().startsWith("M_")){
				weiboItems.add(el);
			}
		}
		
		return weiboItems;
	}

	public static Weibo parse(Element weiboEl, String poster){
		Weibo weibo = new Weibo();
		List<Element> subDivs =  weiboEl.children();
		
		try {
			int subDivsSize = subDivs.size();
			weibo.setId(weiboEl.attr("id").substring(2));
			weibo.setPoster(poster);
			weibo.setPostTime(Utils.parseDate(weiboEl.getElementsByClass("ct").get(0).text().split("来自")[0]));
			
			if(subDivsSize == 1){
				weibo.setRepost(false);
				weibo.setHasPic(false);
				weibo.setContent(weiboEl.getElementsByClass("ctt").get(0).text());
			}
			else if(subDivsSize == 2){
				if(subDivs.get(0).toString().contains("<span class=\"cmt\">原文转发")){
					weibo.setRepost(true);
					weibo.setHasPic(false);
					weibo.setContent(getRepostReason(subDivs.get(1)));
				}
				else{
					weibo.setRepost(false);
					weibo.setHasPic(true);
					weibo.setContent(weiboEl.getElementsByClass("ctt").get(0).text());
				}
			}
			else if(subDivsSize == 3){
				weibo.setRepost(true);
				weibo.setHasPic(true);
				weibo.setContent(getRepostReason(subDivs.get(2)));
			}
			else{
				throw new Exception();
			}
		}
		catch(Exception e){
			weibo = null;
			Log.error(e);
			Log.error("Not a valid weibo item: " + weiboEl);
		}
		
		return weibo;
	}
	
	private static String getRepostReason(Element processEl){
		StringBuilder repostReason = new StringBuilder();
		int endIndex = processEl.childNodes().size() - BACK_NODES_NUM_IN_REPOST_DIV;
		
		for(int i = 1; i < endIndex; i++){
			repostReason.append(processEl.childNode(i).toString());
		}
		
		return repostReason.toString();
	}
	
 	public static void createFile(List<Element> weiboItems, String urlPath) {
		String userID = Utils.getUserIdFromUrl(urlPath);
	
		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO weibo (accountID, weiboID, content, postTime) VALUES (?, ?, ?, ?)");
			for(Element el: weiboItems){				
				Weibo weibo = WeiboParser.parse(el, userID);
				ps.setString(1, userID);
				ps.setString(2, weibo.getId());
				ps.setString(3, weibo.getContent());
				ps.setString(4, weibo.getPostTime());
				ps.executeUpdate();
			}
			ps.close();
		} 
		catch (SQLException e) {
			Log.error(e);
		}
		finally{
		}
	}
}
