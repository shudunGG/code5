package com.om.bo.busi;

public class MyLabelBo {
    private String label = null;
   // private String classPath = null;
    private String startLabel = null;
    private String endLabel = null;
    public MyLabelBo(String label){
        this.label = label;
        this.startLabel = "<"+label+" ";
        this.endLabel = "</"+label+">";
    }
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

//    public String getClassPath() {
//        return classPath;
//    }
//
//    public void setClassPath(String classPath) {
//        this.classPath = classPath;
//    }

    public String getStartLabel() {
        return startLabel;
    }

    public String getEndLabel() {
        return endLabel;
    }
}
