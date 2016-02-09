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

import indi.zhangzqit.javaspider.bean.Comment;
import indi.zhangzqit.javaspider.utils.DBConn;
import indi.zhangzqit.javaspider.utils.Utils;

public class CommentParser {	
	private static final Logger Log = Logger.getLogger(CommentParser.class.getName());
	public static Connection conn = DBConn.getConnection();
	
	public static Document getPageDocument(String content){
		return Jsoup.parse(content);
	}
	
	public static List<Element> getGoalContent(Document doc) {
		List<Element> commentItems = new ArrayList<Element>();
		
		Elements elements = doc.getElementsByClass("c");
		for(int i = 0; i < elements.size(); i++){
			if(elements.get(i).id().startsWith("C_")){
				commentItems.add(elements.get(i));
			}
		}
		
		return commentItems;
	}

	public static Comment parse(Element commentEl, String weiboID){
		Comment comment = new Comment();
		
		try {
			comment.setId(commentEl.attr("id").substring(2));
			
			//一部分人的ID并不是数字串，而是个性域名，这部分也尚待处理
			String tempAuthor = commentEl.getElementsByAttribute("href").get(0).attr("href");			
			comment.setAuthor(tempAuthor.substring(tempAuthor.lastIndexOf("/") + 1,tempAuthor.lastIndexOf("?")));
			
			String tempContent = commentEl.getElementsByClass("ctt").get(0).toString();
			comment.setContent(tempContent.substring(18, tempContent.length() - 7));
			comment.setTime(Utils.parseDate(commentEl.getElementsByClass("ct").get(0).text().split("来自")[0]));

		}
		catch(Exception e){
			comment = null;
			Log.error("Not a valid comment item: " + commentEl);
		}
		
		return comment;
	}
	
 	public static void createFile(List<Element> commentItems, String urlPath) {
		String weiboID = Utils.getUserIdFromUrl(urlPath);
		
		PreparedStatement ps;
		try{
			ps = conn.prepareStatement("INSERT INTO comment (weiboID, poster, content, postTime) VALUES (?, ?, ?, ?)");
			for(int i = 0; i < commentItems.size(); i++){
				Comment comment = CommentParser.parse(commentItems.get(i), weiboID);
				if(comment != null){
					ps.setString(1, weiboID);
					ps.setString(2, comment.getAuthor());
					ps.setString(3, comment.getContent());
					ps.setString(4, comment.getTime());
					ps.execute();
					Log.info("Succesfully Import One Comment:" + comment.getContent());
				}
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
