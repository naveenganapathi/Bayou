package com.bayou.common;

import java.util.LinkedList;

public class Queue<T> {
	public LinkedList<T> list = new LinkedList<T>();
	public synchronized void enqueue(T object) {
		list.add(object);
		notify();
	}

	public synchronized T dequeue() {
		while(list.size()==0) {
			try{
				wait();
			}catch(InterruptedException e){
				e.printStackTrace();        
			}
		}
		return list.removeFirst();
	}
}