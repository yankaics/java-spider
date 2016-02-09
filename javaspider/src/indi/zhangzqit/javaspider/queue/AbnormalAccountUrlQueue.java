package indi.zhangzqit.javaspider.queue;

import java.util.HashSet;

/**
 * �ѷ���url����
 */
public class AbnormalAccountUrlQueue {
	public static HashSet<String> visitedUrlQueue = new HashSet<String>();
	
	public synchronized static void addElement(String url){
		visitedUrlQueue.add(url);
	}
	
	public synchronized static boolean isContains(String url){
		return visitedUrlQueue.contains(url);
	}
	
	public synchronized static int size(){
		return visitedUrlQueue.size();
	}
}