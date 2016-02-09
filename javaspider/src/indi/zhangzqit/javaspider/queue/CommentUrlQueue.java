package indi.zhangzqit.javaspider.queue;

import java.util.LinkedList;

/**
 * 未访问的url队列
 */
public class CommentUrlQueue {
	// 超链接队列
	public static LinkedList<String> commentUrlQueue = new LinkedList<String>();
	public static final int MAX_SIZE = 10000;
	
	public synchronized static void addElement(String url){
		commentUrlQueue.add(url);
	}
	
	public synchronized static void addFirstElement(String url){
		commentUrlQueue.addFirst(url);
	}
	
	public synchronized static String outElement(){
		return commentUrlQueue.removeFirst();
	}
	
	public synchronized static boolean isEmpty(){
		return commentUrlQueue.isEmpty();
	}
	
	public static int size(){
		return commentUrlQueue.size();
	}
	
	public static boolean isContains(String url){
		return commentUrlQueue.contains(url);
	}
}
