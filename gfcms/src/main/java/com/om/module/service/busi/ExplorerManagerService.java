package com.om.module.service.busi;

import com.om.bo.base.FtpInfo;
import com.om.bo.base.FtpPathInfo;
import com.om.bo.busi.MyResFile;
import com.om.common.util.*;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.util.*;

@Service("ExplorerManagerService")
public class ExplorerManagerService extends CommonService {

    private static final String rt="..";
    private static final String rtEn="..";
    /**
     * 查看资源文件情况
     * @param param
     * @return
     * @throws Exception
     */
    public Map viewExplorerFiles(Map param, FtpInfo ftp) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");//目的是用找站点的，因为在前端这个SITE_PK比较容易获取，而SITE_CODE不容易获取
        String U_USER = (String)param.get("U_USER");//
        String SITE_CODE = (String)param.get("SITE_CODE");//SITE_CODE选传，如果能传入，而少一次数据库查询
        String MODULE = (String)param.get("MODULE");//目前有模板，文档，后续还能还有别的模块
        String CUR_CONTEXT = (String)param.get("CUR_CONTEXT");//当前所处的路径，当这个字段为空表，表示用户第一次进视图，进入默认路径
        String JUMP_TO_DIR = (String)param.get("JUMP_TO_DIR");//要操作进入的目录
        this.isNull("U_USER",U_USER);
        if(ObjectTools.isNull(CUR_CONTEXT) || ObjectTools.isNull(JUMP_TO_DIR)) {//为空时表示第一次进，需要知道是哪个站点下的，所以需要查询 ，如果非空则表示已经知道路径了
            if (SITE_CODE == null || "".equals(SITE_CODE)) {
                List list = this.baseService.getList("busiMapper.queryBusiSiteDef", param);
                if (list.size() > 0) {
                    Map rsMap = (Map) list.get(0);
                    SITE_CODE = (String) rsMap.get("SITE_CODE");
                }
            }
        }
        String rootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();

        CUR_CONTEXT = decodeString(CUR_CONTEXT);
        JUMP_TO_DIR = decodeString(JUMP_TO_DIR);

        logger.debug("JUMP_TO_DIR:"+JUMP_TO_DIR);
        //模认的路径就是ROOT/SITE_CODE/template(MODULE)
        String contextPath = SITE_CODE + sep+ MODULE;
        if(!ObjectTools.isNull(CUR_CONTEXT) && !ObjectTools.isNull(JUMP_TO_DIR)){
            contextPath = CUR_CONTEXT;
            if(rt.contentEquals(JUMP_TO_DIR) || rtEn.contentEquals(JUMP_TO_DIR)){//表示返回上一级
                if(contextPath.indexOf("/")>-1){
                    int idx = contextPath.lastIndexOf("/");
                    String subPath = contextPath.substring(0,idx);
                    contextPath = subPath;
                }else{
                    //contextPath=contextPath;//说明已经上一级到根了，没法再往上级了，如果用户的权限只是站点管理员，那么到站点一级，就不能走了。这个判断后面再加
                }
            }else{//表示进入下一级
                contextPath = contextPath+sep+JUMP_TO_DIR;
            }
        }

        String currDir = rootPath + contextPath + sep;
        logger.info("文件浏览的路径为[file view path]:" + currDir);
        File file = new File(currDir);
        List<MyResFile> list = new ArrayList<MyResFile>();
        if (!file.exists() ){
            MyResFile res  = new MyResFile();
            res.setName(rt);
            res.setIsDir(true);
            res.setFileSizeLong(0);
            res.setModifyTimeLong(new Date().getTime());
            list.add(res);
            logger.info("文件不存在[file path not exist]:" + currDir);
        }else{
            MyResFile res = null;
            //增加返回上一级
            if(!"".equals(contextPath) && contextPath.indexOf("/")>-1) {  //contextPath.indexOf("/")==-1表示已经到站点的根了，不能再返回上一级了
                //说明已经上一级到根了，没法再往上级了，如果用户的权限只是站点管理员，那么到站点一级，就不能走了。这个判断后面再加
                res = new MyResFile();
                res.setName(rt);
                res.setIsDir(true);
                res.setFileSizeLong(0);
                res.setModifyTimeLong(new Date().getTime());
                list.add(res);
            }

            File[] files = file.listFiles();

            //这样写两遍的目录是为了把目录和文件分开，目录放前面，文件放后面
            for(File f:files){
                logger.debug("file:"+f.getName()+"       dir:"+f.isDirectory());
                if(f.isDirectory()) {
                    res = new MyResFile();
                    res.setName(f.getName());
                    res.setIsDir(f.isDirectory());
                    res.setFileSizeLong(f.length());
                    res.setModifyTimeLong(f.lastModified());
                    list.add(res);
                }
            }

            for(File f:files){
                logger.debug("file:"+f.getName()+"       dir:"+f.isDirectory());
                if(!f.isDirectory()) {
                    res = new MyResFile();
                    res.setName(f.getName());
                    res.setIsDir(f.isDirectory());
                    res.setFileSizeLong(f.length());
                    res.setModifyTimeLong(f.lastModified());
                    list.add(res);
                }

            }
        }
        contextPath = decodeString(contextPath);
        Map resultMap = new HashMap();
        resultMap.put("CUR_CONTEXT",contextPath);
        resultMap.put("FILE_LIST",list);
        resultMap.put("SITE_CODE",SITE_CODE);
        return resultMap;
    }


    /**
     * 上传资源文件，并检查文件是否存在
     * @param param
     * @param mf
     * @return
     * @throws Exception
     */
    public List<String> checkExistsExplorerFiles(Map param, MultipartFile mf,  FtpInfo ftp) throws Exception {
        String CUR_CONTEXT = (String)param.get("CUR_CONTEXT");
        String U_USER = (String)param.get("U_USER");//
        String IS_COVER = (String)param.get("IS_COVER");//
        this.isNull("CUR_CONTEXT",CUR_CONTEXT);

        List<String> existList = new ArrayList<String>();
        String rootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();
        String contextPath = CUR_CONTEXT;
        String currDir = rootPath + contextPath + sep;
        logger.info("文件检查的路径为[file upload check path]:" + currDir);

        String file_init = mf.getOriginalFilename(); //得到文件名称
        String fix = file_init.substring(file_init.lastIndexOf(".")+1, file_init.length());
        String dstPathAndFile = currDir + sep + file_init;
        File file1 = new File(dstPathAndFile); // 新建一个文件
        if(file1.exists()){
            //表示这个文件已经存在；可以返回结果了
            existList.add(file_init);
        }

        /*else{ 这一段得注释掉，不然两次transferTo，第二次就会报文件不存在，因为文件已经移动走了
            mf.transferTo(file1);

            if("zip".equals(fix.toLowerCase())){
                existList = UZipFile.checkFileExist(file1, currDir + sep);
            }
        }*/
        if("1".equals(IS_COVER)){//这里的逻辑是，如果参数是强制（覆盖）上传，则忽略文件存在列表，直接上传文件并解压；否则是视文件存在列表非空，才直接上传文件并解压，如果文件存在列表不空，则在界面上显示这些文件
            existList = new ArrayList<String>();
            this.saveExplorerFiles(param,      mf,   ftp);
        }else{
            if(existList.size() == 0){
                this.saveExplorerFiles(param,      mf,   ftp);
            }
        }
        /*if(existList.size() == 0){
            this.saveExplorerFiles(param,   rootPath,   mf,   appRoot);
        }*/
        return existList;
    }

    public void syncResFileToDeploy(Map param,  FtpInfo ftp) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");//目的是用找站点的，因为在前端这个SITE_PK比较容易获取，而SITE_CODE不容易获取
        String U_USER = (String)param.get("U_USER");//
        String SITE_CODE = (String)param.get("SITE_CODE");//SITE_CODE选传，如果能传入，而少一次数据库查询
        String CUR_CONTEXT = (String)param.get("CUR_CONTEXT");//当前所处的路径，当这个字段为空表，表示用户第一次进视图，进入默认路径
        String FILE_NAME = (String)param.get("FILE_NAME");//要操作进入的目录

        this.isNull("SITE_PK",SITE_PK);
        this.isNull("U_USER",U_USER);
        this.isNull("CUR_CONTEXT",CUR_CONTEXT);
        this.isNull("FILE_NAME",FILE_NAME);

        if (SITE_CODE == null || "".equals(SITE_CODE)) {
            List list = this.baseService.getList("busiMapper.queryBusiSiteDef", param);
            if (list.size() > 0) {
                Map rsMap = (Map) list.get(0);
                SITE_CODE = (String) rsMap.get("SITE_CODE");
            }
        }

        String rootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();

        CUR_CONTEXT = decodeString(CUR_CONTEXT);


        logger.debug("CUR_CONTEXT:"+CUR_CONTEXT);
        //模认的路径就是ROOT/SITE_CODE/template(MODULE)
        String contextPath = CUR_CONTEXT.replaceAll("template/","");;


        String fullDir = rootPath + contextPath + sep +FILE_NAME;
        logger.info("同步的文件或者文件夹的路径为[file view path]:" + fullDir);
        File file = new File(fullDir);
        List<MyResFile> list = new ArrayList<MyResFile>();
        if (!file.exists() ){
            logger.error("文件不存在,请检查["+fullDir+"]:");
            throw new Exception("文件不存在,请检查["+fullDir+"]:");
        }else{
            if(file.isFile()){
                //单个文件部署
                this.deployFile(rootPath+CUR_CONTEXT, ftp.getTargetRoot()+contextPath, "", FILE_NAME, ftp);
            }
            if(file.isDirectory()){
                List<File> fileList = new ArrayList<File>();
                FileUtils.listFiles(fileList,file);

                List<FtpPathInfo> pathInfoList = new ArrayList<FtpPathInfo>();
                FtpPathInfo bo = null;
                for(File f:fileList){
                    logger.debug("saveExplorerFiles_zipFilePath:"+f.getAbsolutePath());
                    int start = f.getAbsolutePath().lastIndexOf("/");
                    String ftpPath = contextPath;
                    if(start>0){
                        String fPath = f.getAbsolutePath().substring(0,start);
                        ftpPath = fPath.replaceAll(rootPath,"");
                    }

                    String sourcePath = f.getAbsolutePath();
                    File sourcefile = new File(sourcePath);
                    String targetPath = ftp.getTargetRoot() + ftpPath;
                    File targetfile = new File(targetPath);
                    bo = new FtpPathInfo(ftpPath,sourcefile,targetfile);
                    pathInfoList.add(bo);
                }

                //多个文件部署
                this.deployFile(pathInfoList,ftp);
            }

        }
    }

    public String saveExplorerFiles(Map param, MultipartFile mf, FtpInfo ftp) throws Exception {
        String CUR_CONTEXT = (String)param.get("CUR_CONTEXT");
        String U_USER = (String)param.get("U_USER");//
        this.isNull("CUR_CONTEXT",CUR_CONTEXT);
        String rootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();
        String contextPath = CUR_CONTEXT;//
        contextPath = decodeString(contextPath);
        String currDir = rootPath + contextPath + sep;
        logger.info("文件上传路径为[file upload path]:" + currDir);
        File file = new File(currDir);
        if (!file.exists()) file.mkdirs();

        String file_init = mf.getOriginalFilename(); //得到文件名称
        String fix = file_init.substring(file_init.lastIndexOf(".")+1, file_init.length());
        //String file_new = System.currentTimeMillis() +"."+ fix; //生成一个新的文件名称

        String dstPathAndFile = currDir + sep + file_init;
        File file1 = new File(dstPathAndFile); // 新建一个文件
        try {
            mf.transferTo(file1);
            logger.info("=======文件上传成功file upload success====" + dstPathAndFile);
            if("zip".equals(fix.toLowerCase())){
                List<String> fileList = UZipFile.unZipFiles(file1, currDir + sep);

                List<FtpPathInfo> pathInfoList = new ArrayList<FtpPathInfo>();
                FtpPathInfo bo = null;
                for(String zipFilePath:fileList){
                    logger.debug("saveExplorerFiles_zipFilePath:"+zipFilePath);
                    int start = zipFilePath.lastIndexOf("/");
                    String ftpPath = contextPath;
                    if(start>0){
                        String fPath = zipFilePath.substring(0,start);
                        ftpPath = contextPath+sep+fPath;
                    }

                    String sourcePath = currDir + zipFilePath;
                    File sourcefile = new File(sourcePath);
                    String targetPath = ftp.getTargetRoot() + contextPath + sep + zipFilePath;
                    File targetfile = new File(targetPath);
                    bo = new FtpPathInfo(ftpPath,sourcefile,targetfile);
                    pathInfoList.add(bo);
                }
                //多个文件部署
                this.deployFile(pathInfoList,ftp);
            }else{
                //单个文件部署
                this.deployFile(rootPath, ftp.getTargetRoot(), contextPath, file_init, ftp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        param.put("FILE_PATH_ROOT", rootPath);
        param.put("FILE_PATH_CONTEXT", contextPath + sep);
        param.put("FILE_NAME_NEW", file_init);
        param.put("FILE_NAME_INIT", file_init);
        param.put("FILE_NAME_EXT", fix);
        param.put("FILE_LENGTH", mf.getSize());
        param.put("FILE_PATH_URL", appRoot+sep+contextPath);
        String uuid = Pk.getId("F");
        param.put("FILE_PK",uuid);
        //这里保存数据库的先屏蔽了，考虑这样一个问题，如果用户上传的ZIP包里文件特别多，比如几千个小文件，如果加上入库，这个请求就会越时
        //this.baseService.insert("busiMapper.saveBusiTemplateFile",param);
        return uuid;
    }


    public boolean deleteExplorerFiles(Map param, FtpInfo ftp) throws Exception {
        String CUR_CONTEXT = (String)param.get("CUR_CONTEXT");
        String DEL_FILE = (String)param.get("DEL_FILE");//要删除的目录或者文件
        this.isNull("CUR_CONTEXT",CUR_CONTEXT);
        this.isNull("DEL_FILE",DEL_FILE);
        String rootPath = ftp.getRootPath();
        if(rt.equals(DEL_FILE) || rtEn.equals(DEL_FILE) || DEL_FILE.indexOf("*")>-1|| DEL_FILE.indexOf("/")>-1){
            throw new Exception("选中的目录名["+DEL_FILE+"]值非法");
        }
        CUR_CONTEXT = decodeString(CUR_CONTEXT);
        DEL_FILE = decodeString(DEL_FILE);

        String contextPath = CUR_CONTEXT;//
        String currDir = rootPath + contextPath + sep + DEL_FILE;
        logger.info("文件删除的路径为[file delete path]:" + currDir);
        File file = new File(currDir);
        if(file.exists()){
            boolean isDel = file.delete();
            logger.debug("isDel:"+isDel);
            return isDel;
        }
        return false;
    }

    public String openExplorerFiles(Map param, FtpInfo ftp) throws Exception {
        String CUR_CONTEXT = (String)param.get("CUR_CONTEXT");
        String OPEN_FILE = (String)param.get("OPEN_FILE");//要DOWN的文件
        this.isNull("CUR_CONTEXT",CUR_CONTEXT);
        this.isNull("OPEN_FILE",OPEN_FILE);
        if(rt.equals(OPEN_FILE) || rtEn.equals(OPEN_FILE) || OPEN_FILE.indexOf("*")>-1|| OPEN_FILE.indexOf("/")>-1){
            throw new Exception("选中的目录名["+OPEN_FILE+"]值非法");
        }
        CUR_CONTEXT = decodeString(CUR_CONTEXT);
        OPEN_FILE = decodeString(OPEN_FILE);

        String rootPath = ftp.getRootPath();
        String contextPath = CUR_CONTEXT;//
        String currDir = rootPath + contextPath + sep + OPEN_FILE;
        logger.info("文件路径为[file delete path]:" + currDir);
        File file = new File(currDir);
        if(!file.exists()){
            throw new Exception("文件不存在【FILE NOT EXISTS】"+ currDir);
        }
        if(file.isDirectory()){
            throw new Exception("目录无法打开【FILE NOT EXISTS】"+ currDir);
        }
        String fix = OPEN_FILE.substring(OPEN_FILE.lastIndexOf(".")+1, OPEN_FILE.length()).toLowerCase();
        if("html".equals(fix) || "xml".equals(fix) || "txt".equals(fix) || "log".equals(fix) || "js".equals(fix) || "css".equals(fix) || "json".equals(fix) || "properties".equals(fix) ){
            return TxtFileUtils.readTxtToString(currDir).toString();
        }else{
            throw new Exception("不支持此类文件后缀的打开【FILE FORMAT WAS NOT SUPPORT 】"+ currDir);
        }
    }

    public void writeExplorerFiles(Map param, FtpInfo ftp) throws Exception {
        String CUR_CONTEXT = (String)param.get("CUR_CONTEXT");
        String WRITE_FILE = (String)param.get("WRITE_FILE");//要write的文件
        String FILE_CONTENT = (String)param.get("FILE_CONTENT");
        this.isNull("CUR_CONTEXT",CUR_CONTEXT);
        this.isNull("WRITE_FILE",WRITE_FILE);
        this.isNull("FILE_CONTENT",FILE_CONTENT);
        if(rt.equals(WRITE_FILE) || rtEn.equals(WRITE_FILE) || WRITE_FILE.indexOf("*")>-1|| WRITE_FILE.indexOf("/")>-1){
            throw new Exception("选中的目录名["+WRITE_FILE+"]值非法");
        }

        CUR_CONTEXT = decodeString(CUR_CONTEXT);
        WRITE_FILE = decodeString(WRITE_FILE);


        String rootPath = ftp.getRootPath();
        String contextPath = CUR_CONTEXT;//
        String currDir = rootPath + contextPath + sep + WRITE_FILE;
        logger.info("文件路径为[file delete path]:" + currDir);
        File file = new File(currDir);
        if(!file.exists()){
            throw new Exception("文件不存在【FILE NOT EXISTS】"+ currDir);
        }
        if(file.isDirectory()){
            throw new Exception("目录无法写入【FILE NOT EXISTS】"+ currDir);
        }
        String fix = WRITE_FILE.substring(WRITE_FILE.lastIndexOf(".")+1, WRITE_FILE.length()).toLowerCase();
        if("html".equals(fix) || "xml".equals(fix) || "txt".equals(fix) || "log".equals(fix) || "js".equals(fix) || "css".equals(fix) || "json".equals(fix) || "properties".equals(fix) ){
            TxtFileUtils.writeStrtoTxtFile(rootPath + contextPath,WRITE_FILE,FILE_CONTENT);
        }else{
            throw new Exception("不支持此类文件后缀的写入【FILE FORMAT WAS NOT SUPPORT 】"+ currDir);
        }
    }

}
