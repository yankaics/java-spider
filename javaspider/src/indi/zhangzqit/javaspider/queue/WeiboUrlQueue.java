package indi.zhangzqit.javaspider.queue;

import java.util.LinkedList;

public class WeiboUrlQueue {
	public static LinkedList<String> urlQueue = new LinkedList<String>();
	public static final int MAX_SIZE = 10000;
	
	public synchronized static void addElement(String url){
		urlQueue.add(url);
	}
	
	public synchronized static void addFirstElement(String url){
		urlQueue.addFirst(url);
	}
	
	public synchronized static String outElement(){
		return urlQueue.removeFirst();
	}
	
	public synchronized static boolean isEmpty(){
		return urlQueue.isEmpty();
	}
	
	public static int size(){
		return urlQueue.size();
	}
	
	public static boolean isContains(String url){
		return urlQueue.contains(url);
	}
}

