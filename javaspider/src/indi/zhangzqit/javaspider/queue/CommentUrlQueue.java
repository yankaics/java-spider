package indi.zhangzqit.javaspider.queue;

import java.util.LinkedList;

/**
 * δ���ʵ�url����
 */
public class CommentUrlQueue {
	// �����Ӷ���
	public static LinkedList<String> commentUrlQueue = new LinkedList<String>();
	// �����ж�Ӧ���ĳ���������
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
