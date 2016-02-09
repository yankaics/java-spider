package indi.zhangzqit.javaspider.queue;

import java.util.HashSet;

public class VisitedRepostUrlQueue {
	public static HashSet<String> visitedRepostUrlQueue = new HashSet<String>();
	public static int count = 0;
	
	public synchronized static void addElement(String url){
		count++;
	}
	
	public synchronized static boolean isContains(String url){
		return visitedRepostUrlQueue.contains(url);
	}
	
	public synchronized static int size(){
		return count;
	}
}
