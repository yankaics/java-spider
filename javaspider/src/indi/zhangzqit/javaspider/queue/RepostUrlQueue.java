package indi.zhangzqit.javaspider.queue;

import java.util.LinkedList;

public class RepostUrlQueue {
	public static LinkedList<String> repostUrlQueue = new LinkedList<String>();
	public static final int MAX_SIZE = 10000;
	
	public synchronized static void addElement(String url){
		repostUrlQueue.add(url);
	}
	
	public synchronized static void addFirstElement(String url){
		repostUrlQueue.addFirst(url);
	}
	
	public synchronized static String outElement(){
		return repostUrlQueue.removeFirst();
	}
	
	public synchronized static boolean isEmpty(){
		return repostUrlQueue.isEmpty();
	}
	
	public static int size(){
		return repostUrlQueue.size();
	}
	
	public static boolean isContains(String url){
		return repostUrlQueue.contains(url);
	}
}
