package indi.zhangzqit.javaspider.parser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import indi.zhangzqit.javaspider.parser.bean.Follow;
import indi.zhangzqit.javaspider.utils.Constants;
import indi.zhangzqit.javaspider.utils.DBConn;
import indi.zhangzqit.javaspider.utils.Utils;

public class FollowParser {
	private static final Logger Log = Logger.getLogger(FollowParser.class.getName());
	public static Connection conn = DBConn.getConnection();
	
	public static Document getPageDocument(String content){
		return Jsoup.parse(content);
	}
	
	public static List<Element> getGoalContent(Document doc) {
		
		return doc.getElementsByTag("table");
	}

	public static Follow parse(Element followEl, String followerID, int currentLevel){
		Follow follow = new Follow();
		try {
			int fansOfFollowee = 0;
			for(TextNode text: followEl.getElementsByTag("td").get(1).textNodes()){
				if(text.toString().startsWith("·ÛË¿")){
					int startIndex = "·ÛË¿".length();
					int endIndex = text.toString().indexOf("ÈË");
					fansOfFollowee = Integer.parseInt(text.toString().substring(startIndex, endIndex));
				}
			}
			
			if(fansOfFollowee > Constants.FANS_NO_MORE_THAN){
				Log.error("Too many followers: " + followEl);
				return null;
			}
			
			String followeeUrl = followEl.getElementsByTag("img").get(0).attr("src");
			String followeeID = Utils.getUserIdFromImgUrl(followeeUrl);

			follow.setFollower(followerID);
			follow.setFollowee(followeeID);
			follow.setLevel(currentLevel);
		}
		catch(Exception e){
			follow = null;
			Log.error("Not a valid follow item: " + followEl, e);
		}
		
		return follow;
	}
		
 	public static void createFile(List<Element> followeeItems, String urlPath, int currentLevel) {
		
 		String followerID = Utils.getUserIdFromFollowUrl(urlPath);
		int level = currentLevel + 1;
		
		PreparedStatement ps;
		for(int i = 0; i < followeeItems.size(); i++){
			try{
				ps = conn.prepareStatement("INSERT INTO follow (follower, followee, level) VALUES (?, ?, ?)");

				Follow follow = FollowParser.parse(followeeItems.get(i), followerID, level);
				
				if(follow != null){
					ps.setString(1, follow.getFollower());
					ps.setString(2, follow.getFollowee());
					ps.setInt(3, follow.getLevel());
					ps.execute();
				}			
				ps.close();
				
				if(follow != null && level < Constants.LEVEL){
					ps = conn.prepareStatement("INSERT INTO follower (follower, level) VALUES (?, ?)");
	
					if(follow != null){
						ps.setString(1, follow.getFollowee());
						ps.setInt(2, level);
						ps.execute();
					}	
					ps.close();
				}
			} 
			catch (SQLException e) {
				Log.error(e);
			}
			finally{

			}
		}	
	} 
}
