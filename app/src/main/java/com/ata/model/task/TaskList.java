package com.ata.model.task;

import android.util.Log;

import com.ata.model.task.test.VirtualPiece;
import com.ata.provider.task.TaskPartition;
import com.ata.provider.task.TaskSet;
import com.ata.util.FileOperation;


import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by raven on 2015/8/28.
 */
public class TaskList implements TaskSet {
    private static String TAG ="TaskList";
    public static final int MINSTART=0;
    public static final int MAXEND=Integer.MAX_VALUE;
    private List<TaskPartition> partitions;
    private String taskName;
    public TaskList(String task){
        taskName=task;
        partitions =new ArrayList<>();
    }
    /*
        将等待完成(注意和运行线程同步)的任务段按照给定的比例数组分割，要求比例数组和为1，完成后移除被分割的任务段。

     */
    @Override
    public synchronized TaskSet[] RemoveAndDivide(float[] percentages) {
//        Log.d(TAG,"current set is "+toString());
        assert TestSum(percentages);
        int length =percentages.length;
        int []size =getSizeOfNewSets(percentages);
        //testing
       // System.out.println("size is :"+Arrays.toString(size));
        if(size==null) return null;
        int point =0;
        TaskSet[] res =new TaskList[length];
        for(int i=0;i<length;i++){
            res[i] =new TaskList(taskName);
            point=getFitSet(res[i],point,size[i]);
        }
        return res;
    }
    private int[] getSizeOfNewSets(float[] percentages){
        int []size=new int[percentages.length];
        int totalLength=getTotalUnFLength();
        if(totalLength==0){
            System.out.println("no unFinished part!");
            return null;
        }
        int extraLength=totalLength;
        for(int i=0;i<percentages.length-1;i++) {
            int length= (int) (percentages[i] * totalLength);
            extraLength-=length;
            size[i] =length;
        }
        if(extraLength<0)extraLength=0;
        size[percentages.length-1]=extraLength;
        return size;
    }
    private int getNextAllocatedTaskPar(int point){
        while (!allocatable(partitions.get(point))){

            point++;
            if(point>=partitions.size()){
                System.out.println("error! size is"+partitions.size()+"point is "+point);
                return -1;
            }
        }
        return point;
    }
    /*
    params:新的任务集，可用任务段的序号
    description:对给定的大小给出 合适的任务集，并返回下一个可用的任务段的序号
     */
    private int getFitSet(TaskSet newSet,int point,int size){
        assert size>0;
        point=getNextAllocatedTaskPar(point);

        int lengthCount=0;
        for(int i=point;i<partitions.size();i=getNextAllocatedTaskPar(i+1)){
            if(i==-1){
                System.out.println("error index!");
            }
            TaskPartition p =partitions.get(i);
            lengthCount+=p.getEnd()-p.getStart()+1;

            if(lengthCount<size){

                TaskPartition pRmov =partitions.remove(i);
                pRmov.setStatus(TaskPiece.ADDED);
                newSet.add(pRmov);

                i--;
            }else if(lengthCount>size) {
                    TaskPartition toBeCut = partitions.get(i);
                    int end = toBeCut.getEnd();
                    int start = toBeCut.getStart();
                    int lengthLeft = lengthCount - size;
                    int lengthRemov = end - start + 1 - lengthLeft;
                    toBeCut.reSet(end - lengthLeft + 1, end);
                    newSet.add(new TaskPiece(start, start + lengthRemov - 1, taskName));
                    //test
                    //newSet.add(new VirtualPiece(start,start+lengthRemov-1,false));
                    point = i;
                    break;
                }else {
                    TaskPartition pRmov =partitions.remove(i);
                    pRmov.setStatus(TaskPiece.ADDED);
                    newSet.add(pRmov);
                    point=i;
                    break;
                }


        }
        return point;
    }
    private boolean allocatable(TaskPartition p){
        return p.getStatus()==TaskPiece.ADDED||p.getStatus()==TaskPiece.INDIS||p.getStatus()==TaskPiece.PREDIS;
    }
    private boolean runnable(TaskPartition p){
        return p.getStatus()== TaskPiece.ADDED;
    }
    private int getTotalUnFLength(){
        int length=0;
        for(TaskPartition p:partitions){
            if(allocatable(p)){
                length+=p.getEnd()-p.getStart()+1;
                p.setStatus(TaskPiece.INDIS);
            }
        }
        return length;
    }
    private boolean TestSum(float[] pert){
        float sum=0;
        for(float f:pert){
            sum+=f;
        }
        if(Math.abs((sum-1))<0.05){
            return true;
        }else
            return false;
    }
    /*
        和另外一个任务集合并，同时处理重复的段落
     */
    @Override
    public synchronized boolean merge(TaskSet s) {
        List<TaskPartition> list= s.toList();
        for(int i=0;i<list.size();i++){
            if(list.get(i).getStart()<=list.get(i).getEnd())
            add(list.get(i));
        }
        return true;
    }
    /*
        添加一个任务段，同时处理重复的段落
     */
    @Override
    public synchronized boolean add(TaskPartition p) {
        assert p.getEnd()>=p.getStart();
        for(int i=0;i<partitions.size();i++){
            TaskPartition pari=partitions.get(i);
            if(intersect(p,pari)){
                if((p.isFinished()&&pari.isFinished())||(!p.isFinished()&&!pari.isFinished())){
                    if(coverd(p,pari)){
                        partitions.remove(i);
                        i--;
                    }else if(coverd(pari, p)){
                        return false;
                    }else {
                        if(p.getStart()<=pari.getStart())
                            pari.reSet(p.getEnd()+1,pari.getEnd());
                        else
                            pari.reSet(pari.getStart(),p.getStart()-1);
                    }
                }
                else if(p.isFinished()){
                    if(coverd(p, pari)){
                        partitions.remove(i);
                        i--;
                    }else if(coverd(pari,p)) {
                        partitions.add(new TaskPiece(p.getEnd() + 1, pari.getEnd(), taskName));
                        pari.reSet(pari.getStart(), p.getStart() - 1);

                    }else {
                        if(p.getStart()<=pari.getStart())
                            pari.reSet(p.getEnd()+1,pari.getEnd());
                        else
                            pari.reSet(pari.getStart(),p.getStart()-1);
                    }

                }else if(pari.isFinished()){
                    if(coverd(p, pari)){
                        add(new TaskPiece(pari.getEnd() + 1, p.getEnd(), taskName));
                        p.reSet(p.getStart(), pari.getStart() - 1);
                    }else if(coverd(pari,p)) {
                       return false;

                    }else
                    if(p.getStart()<=pari.getStart())
                        p.reSet(p.getStart(),pari.getStart()-1);
                    else
                        p.reSet(pari.getEnd()+1,p.getEnd());
                }
            }
        }
        partitions.add(p);
        return true;
    }
    private boolean intersect(TaskPartition a,TaskPartition b){
        return (a.getEnd()+1>b.getStart()&&b.getEnd()>a.getStart())||
                (b.getEnd()+1>a.getStart()&&a.getEnd()>b.getStart());
    }
    private boolean coverd(TaskPartition a,TaskPartition b){
        return (a.getStart()<=b.getStart()&&a.getEnd()>=b.getEnd());
    }
    /*
        将任务集转成数组，用于遍历，使用此接口不应修改数组内容。为同之前的代码兼容。可考虑改成迭代器
     */
    @Override
    public synchronized List<TaskPartition> toList() {
        return partitions;
    }
    /*
        判断任务集完成的部分是否覆盖了给定的任务段数组
     */
    @Override
    public synchronized boolean covered(List<TaskPartition> list,boolean onlyResult) {
        List<TaskPartition> unF;
            unF = getUnFinishedParts(!onlyResult);
        Log.d(TAG,"unF is"+Arrays.asList(unF));
        for(TaskPartition p:list)
            for (TaskPartition uFPar:unF){
                if(intersect(p,uFPar)){
                    return false;
                }
            }

        return true;
    }
    /*
    获取未完成的任务段，注意这里假定了任务集为(MINSTART,MAXEND)，为了简化覆盖的判断
     */
    private List<TaskPartition> getUnFinishedParts(boolean containDealingPart){
        SortPieces();
        List<TaskPartition> unF =new ArrayList<>();
        if(partitions!=null&&partitions.size()>0) {
            TaskPartition s = partitions.get(0);
            if (!s.isFinished()&&!containDealingPart)
                unF.add(s);
            if (s.getStart() > MINSTART) {

                unF.add(new VirtualPiece(MINSTART, s.getStart() - 1, false));
            }
            TaskPartition e = partitions.get(partitions.size() - 1);
            unF.add(new VirtualPiece(e.getEnd() + 1, MAXEND, false));

            for (int i = 0; i < partitions.size() - 1; i++) {

                TaskPartition pre = partitions.get(i);
                TaskPartition aft = partitions.get(i + 1);
                if (!aft.isFinished()) {
                    unF.add(aft);
                }
                if (pre.getEnd() < aft.getStart() - 1) {
                    unF.add(new VirtualPiece(pre.getEnd() + 1, aft.getStart() - 1, false));

                }
            }
        }else {
            unF.add(new VirtualPiece(MINSTART,MAXEND,false));
        }
        return unF;
    }
    private void SortPieces(){
        Collections.sort(partitions, new Comparator<TaskPartition>() {
            @Override
            public int compare(TaskPartition lhs, TaskPartition another) {
                if (lhs.getStart() < another.getStart()) {
                    return -1;
                } else if (lhs.getStart() == another.getStart()) {
                    if (lhs.getEnd() < another.getStart()) {
                        return 1;
                    } else if (lhs.getEnd() == another.getEnd())
                        return 0;
                    else
                        return -1;
                } else {
                    return 1;
                }
            }
        });
    }
    /*
        将任务集按照给定大小分割
     */
    @Override
    public synchronized void cutToMinPieces(int MinPieceSize) {
        List<TaskPartition> extraPieces =new ArrayList<>();
        for(TaskPartition p:partitions){
            if(!p.isFinished()&&p.getEnd()-p.getStart()+1>MinPieceSize){
                TaskPartition[] extra =cutToMinPiece(p,MinPieceSize);

                for(TaskPartition pExtra:extra)
                    extraPieces.add(pExtra);
            }
        }
        partitions.addAll(extraPieces);
        SortPieces();
    }
    private TaskPartition[] cutToMinPiece(TaskPartition p,int MinPieceSize){
        assert !p.isFinished();
        int start=p.getStart();
        int end =p.getEnd();
        int size =p.getEnd()-p.getStart()+1;
        int NumOfPiece =(size+MinPieceSize-1)/MinPieceSize-1;
        TaskPartition[] extra= new TaskPartition[NumOfPiece];
        for(int i=0;i<NumOfPiece;i++){
            extra[i]=new TaskPiece(start+MinPieceSize*i,start+MinPieceSize*(i+1)-1,taskName);
          //testing
          //   extra[i]=new VirtualPiece(start+MinPieceSize*i,start+MinPieceSize*(i+1)-1,false);

        }
        p.reSet(start + MinPieceSize * NumOfPiece, end);
        return extra;
    }
    @Override
    public synchronized void MergePieces() {
        SortPieces();
        int point =0;
        while(partitions.size()>=2&&point<partitions.size()-1){
            TaskPartition pre =partitions.get(point);
            TaskPartition aft =partitions.get(point+1);
            if(pre.getEnd()+1==aft.getStart()&&
                    ((pre.isFinished()&&aft.isFinished())||(!pre.isFinished()&&!aft.isFinished()))){
                partitions.remove(point+1);
                Merge(pre, aft);
            }else {
                point++;
            }
        }
    }
    /*
    将第二个任务段合并到第一个
     */
    private void Merge(TaskPartition dst,TaskPartition src){
        dst.Merge(src);
    }

    @Override
    public synchronized String toString(){
        String result="";
        for(TaskPartition p:partitions){
            result+=p.toString()+" , ";
        }
        return result;
    }

    @Override
    public void CopyResultTo(File dir) {
        for(TaskPartition p:partitions){
            String respath =p.getResultFilePath();
            File resul =new File(respath);
            String name =resul.getName();
            File outdir =new File(dir.getAbsolutePath()+File.separator+name);
            File par =outdir.getParentFile();
            if(!par.exists())par.mkdirs();
            FileOperation.copyFile(resul,outdir);
        }
    }

    @Override
    public int getUnfinishedLength() {
        int length=0;
        for(TaskPartition p:partitions){
            if(allocatable(p)){
              //  p.setStatus(TaskPiece.PREDIS);
                length+=p.getEnd()-p.getStart()+1;
            }
        }
        return length;    }

    /*
    当有任务片从等待分发状态解锁时，需要通知任务管理者继续运行
     */
    @Override
    public boolean cancelDis() {
        boolean unlockedOnePiece=false;
        for(TaskPartition p:partitions){
            if(p.getStatus()==TaskPiece.PREDIS||p.getStatus()==TaskPiece.INDIS){
                p.setStatus(TaskPiece.ADDED);
                unlockedOnePiece=true;
            }
        }
        return unlockedOnePiece;
    }
    /*
    此函数同样返回正在被分发线程占用的任务段
     */
    @Override
    public synchronized TaskPartition getAnUnFinishedPiece() {
        for(TaskPartition p:partitions){
            if(runnable(p)){
                p.setStatus(TaskPiece.RUNNING);
                return  p;
            }
        }
        for(TaskPartition p:partitions){
            if(p.getStatus()==TaskPiece.PREDIS){
                return  p;
            }
        }
        return null;
    }

    private static TaskList createTaskSet(){
        String task ="testTaskList";
        Random r =new Random();

        TaskList set =new TaskList(task);
        TaskPartition p0 =new VirtualPiece(100+r.nextInt(100),400+r.nextInt(100),r.nextBoolean());
        TaskPartition p1 =new VirtualPiece(250+r.nextInt(100),500+r.nextInt(100),r.nextBoolean());
        TaskPartition p2 =new VirtualPiece(50+r.nextInt(100),390+r.nextInt(100),r.nextBoolean());
        TaskPartition p3 =new VirtualPiece(0+r.nextInt(100),150+r.nextInt(100),r.nextBoolean());
        TaskPartition p4 =new VirtualPiece(200+r.nextInt(400),600+r.nextInt(10),r.nextBoolean());
        set.add(p0);
        set.SortPieces();
        System.out.println("set is" + set.toString());

        set.add(p1);
        set.SortPieces();
        System.out.println("set is" + set.toString());

        set.add(p2);
        set.SortPieces();
        System.out.println("set is" + set.toString());

        set.add(p3);
        set.SortPieces();
        System.out.println("set is" + set.toString());
        set.add(p4);
        set.SortPieces();
        System.out.println("set is" + set.toString());


        return set;
    }
    public static void main(String[] args) {
        /*
        //test add task piece and merge with other set
       TaskList set1 =createTaskSet();
        System.out.println("set1 is" + set1);

        TaskList set2 =createTaskSet();
        System.out.println("set2 is" + set2);

        set1.merge(set2);
        System.out.println("set is" + set1);
        set1.MergePieces();
        System.out.println("after merge:"+set1);
        */
        /*
        //test cut to min pieces
        TaskList list=new TaskList("TestTask");
        list.add(new VirtualPiece(0,901,false));
        list.cutToMinPieces(100);
        System.out.println(list);
        */
        /*
        //test unFinished part
        TaskList list2 =new TaskList("Test");
        list2.add(new VirtualPiece(1,20,true));
        list2.add(new VirtualPiece(30,50,true));
        list2.add(new VirtualPiece(40,70,false));
        list2.add(new VirtualPiece(60,200,true));
        List<TaskPartition> unF= list2.getUnFinishedParts();
        //testing
        System.out.println("unFinished part is");
        for(TaskPartition p:unF)
            System.out.println(p);
        */
        //test divde
        TaskList list3 =new TaskList("Test");

        list3.add(new VirtualPiece(0,99,true));
        list3.add(new VirtualPiece(100,199,false));
        list3.add(new VirtualPiece(200,200,false));
        int n=2;
        float[] percentages =new float[n];
        for(int i=0;i<n;i++){
            percentages[i]=1.0f/n;
        }
        TaskSet[]sets=list3.RemoveAndDivide(percentages);
        for(TaskSet s:sets){
            System.out.println(s.toString());
        }
    }

}
