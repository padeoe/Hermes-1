package com.ata.provider.task;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by raven on 2015/8/28.
 */
public interface TaskSet extends Serializable{
    TaskSet[] RemoveAndDivide(float[] percentages);
    boolean merge(TaskSet set);
    boolean add(TaskPartition p);
    List<TaskPartition >toList();
    /*
    返回完成的任务集是否覆盖了list
     */
    boolean covered(List<TaskPartition> list,boolean onlyResult);
    /*
    用于切割未完成的任务为小块，方便分发
     */
    void cutToMinPieces(int MinPieceSize);
    void MergePieces();
    String toString();
    void CopyResultTo(File dir);
    int getUnfinishedLength();
    boolean cancelDis();
    TaskPartition getAnUnFinishedPiece();
}
