package com.ata.model;

import java.io.Serializable;

/**
 * Created by raven on 2015/5/24.
 */
public class AtaPacket implements Serializable{

    //定义的种类
    public static final int DEVICEINFO=0;
    public static final int RESULT=1;
    public static final int TASKINFO=2;
    public static final int RESREQUEST=3;
    public static final int TRANSFERACK=4;
    public static final int REQUEST=5;
    public static final int ACK=6;
    public static final int REPLY=7;
    private int mType;
    private String mMessage;
    private boolean isEnd=true;//是否是一次传输的结束包
    private Node source;
    private Node dest;
    private Object mContent;

    /**
     *
     */
    public AtaPacket(int type,Object object,String str) {
        mType =type;
        mMessage =str;
        mContent=object;
        source=null;
        dest=null;

    }
    public boolean isEnd() {
        return isEnd;
    }
    public void setEndFlag(boolean isEnd) {
        this.isEnd = isEnd;
    }


    public String getmMessage() {
        return mMessage;
    }

    public Object getmContent() {
        return mContent;
    }
    public void setSource(String sourceName,String sourceAddress) {
        if(source==null)
            this.source = new Node(sourceName, sourceAddress);
    }
    public String getDestName() {
        return dest.NodeName;
    }
    public String getDestAddress(){return  dest.Address;}
    public String getSourceName() {
        return source.NodeName;
    }
    public String getSourceAddress(){return source.Address;}
    public void setDest(String destName,String destAddress) {
        if(dest==null)
            this.dest = new Node(destName,destAddress);
    }
    public int getmType() {
        return mType;
    }
    //如果再对节点进行封装的话层次就过于复杂了……
    private class Node implements Serializable{

    public String NodeName;
    public String Address;
    public Node(String name,String addr){
        NodeName=name;
        Address=addr;
    }
}

}
