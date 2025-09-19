package com.om.common.cache;

public class Dict {
    public static final String All = "-99";
    public static String dbMap="_kingBase";
    /**
     * 系统模块
     */
    public static class FangCun{
        public static final String valid_url = "http://api.ifuncun.cn:8081/v1/proofread";
        public static final String errWordAdd_url = "http://crm.ifuncun.cn:8080/funcun/uploadDict_wrongWords";
        public static final String errWordModify_url = "http://crm.ifuncun.cn:8080/funcun/modifyDict_wrongWords";
        public static final String errWordDel_url = "http://crm.ifuncun.cn:8080/funcun/deleteDict_wrongWords";
        public static final String errWordQuery_url = "http://crm.ifuncun.cn:8080/funcun/getDict_wrongWords";

        public static final String rightWordAdd_url = "http://crm.ifuncun.cn:8080/funcun/uploadDict_rightWords";
        public static final String rightWordModify_url = "http://crm.ifuncun.cn:8080/funcun/modifyDict_rightWords";
        public static final String rightWordDel_url = "http://crm.ifuncun.cn:8080/funcun/deleteDict_rightWords";
        public static final String rightWordQuery_url = "http://crm.ifuncun.cn:8080/funcun/getDict_rightWords";

        public static final String specialWordAdd_url = "http://crm.ifuncun.cn:8080/funcun/uploadDict_specialWords";
        public static final String specialWordModify_url = "http://crm.ifuncun.cn:8080/funcun/modifyDict_specialWords";
        public static final String specialWordDel_url = "http://crm.ifuncun.cn:8080/funcun/deleteDict_specialWords";
        public static final String specialWordQuery_url = "http://crm.ifuncun.cn:8080/funcun/getDict_specialWords";

    }



    /**
     * 系统模块
     */
    public static class Module{
        public static final String login = "login";
        public static final String document = "document";
        public static final String docFile = "docFile";
        public static final String site = "site";
        public static final String channel = "channel";
        public static final String template = "template";
        public static final String mailBox = "mailBox";
        public static final String mailMsg = "mailMsg";
    }

    /**
     * 系统模块的具体功能
     */
    public static class ModuleType{
        public static final String login = "login";
        public static final String save = "new";
        public static final String del = "del";
        public static final String publish = "publish";
        public static final String cancel = "cancel";
        public static final String audit = "audit";
    }


    /**
     * 文档的状态
     */
    public static class DocSts{
        //0草稿 -1废弃 1新建待审 2待签 9待发布 99已经发布 -2审核未通过
        public static final int Draft = 0;
        public static final int Discard = -1;
        public static final int New = 1;
        public static final int toAudit = 1;
        public static final int AuditOk = 2;
        public static final int toSign = 2;
        public static final int toPublish = 9;
        public static final int PublishOk = 99;
        public static final int Auditfailed = -2;
        public static final int Signfailed = -3;
    }

    /**
     * 文档的状态
     */
    public static class ChannelSts{
        public static final int Draft = 0;
        public static final int Discard = -1;
        public static final int toPublish = 9;
        public static final int PublishOk = 99;
    }

    /**
     * 文档的状态
     */
    public static class SiteSts{
        public static final int Draft = 0;
        public static final int Discard = -1;
        public static final int toPublish = 9;
        public static final int PublishOk = 99;
    }

    /**
     * 部署的状态
     */
    public static class DeploySts{
        public static final int succ = 99;
        public static final int err = -1;
    }
    public static class DeployMode{
        public static final String file = "file";
        public static final String sftp = "sftp";
    }

}
