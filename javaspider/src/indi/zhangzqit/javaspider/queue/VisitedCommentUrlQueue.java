package indi.zhangzqit.javaspider.queue;

import java.util.HashSet;

/**
 * �ѷ���url����
 */
public class VisitedCommentUrlQueue {
	public static HashSet<String> visitedCommentUrlQueue = new HashSet<String>();
	public static int count = 0;
	
	public synchronized static void addElement(String url){
		count++;
	}
	
	public synchronized static boolean isContains(String url){
		return visitedCommentUrlQueue.contains(url);
	}
	
	public synchronized static int size(){
		return count;
	}
}

