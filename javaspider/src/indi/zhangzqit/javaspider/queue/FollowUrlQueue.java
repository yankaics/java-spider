package indi.zhangzqit.javaspider.queue;

import java.util.LinkedList;

public class FollowUrlQueue {
	public static LinkedList<String> followUrlQueue = new LinkedList<String>();
	public static final int MAX_SIZE = 10000;
	
	public synchronized static void addElement(String url){
		followUrlQueue.add(url);
	}
	
	public synchronized static void addFirstElement(String url){
		followUrlQueue.addFirst(url);
	}
	
	public synchronized static String outElement(){
		return followUrlQueue.removeFirst();
	}
	
	public synchronized static boolean isEmpty(){
		return followUrlQueue.isEmpty();
	}
	
	public static int size(){
		return followUrlQueue.size();
	}
	
	public static boolean isContains(String url){
		return followUrlQueue.contains(url);
	}
}
