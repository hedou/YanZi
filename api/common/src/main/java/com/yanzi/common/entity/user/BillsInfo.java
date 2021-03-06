package com.yanzi.common.entity.user;

import com.alibaba.fastjson.annotation.JSONField;

public class BillsInfo {
	@JSONField(serialize = false)
	public static final BillsInfo DEFAULT = new BillsInfo();
	private long userId;
	private long courseId;
	private long termId;
	private boolean state;
	private double number;
	public BillsInfo() {
    }

    public BillsInfo(long userId) {
        this.userId = userId;
    }

    public BillsInfo(long userId,boolean state,double number) {
        this.userId = userId;
        this.state = state;
        this.number = number;
    }
    
    public BillsInfo(long userId,long courseId,long termId,boolean state) {
        this.userId =userId;
        this.courseId=courseId;
        this.termId=termId;
        this.state = state;
    }
    
    public BillsInfo(long userId,long courseId,long termId,boolean state,double number) {
        this.userId =userId;
        this.courseId=courseId;
        this.termId=termId;
        this.state = state;
        this.number = number;
    }
    
    public long getUserId(){
        return userId;
    }

    public long setUserId(long userId) {
        return this.userId = userId;
    }
    
    public long getCourseId() {
        return courseId;
    }

    public long setCourseId(long courseId) {
        return this.courseId = courseId;
    }
    
    public long getTermId() {
        return termId;
    }

    public long setTermId(long termId) {
        return this.termId = termId;
    }
    
    public boolean getState() {
        return state;
    }

    public boolean setState(boolean state) {
        return this.state = state;
    }
    
    public double getNumber() {
        return number;
    }

    public double setNumber(double number) {
        return this.number = number;
    }
    
    
    
    
}
