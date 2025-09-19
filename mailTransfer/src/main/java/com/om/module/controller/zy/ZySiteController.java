package com.om.module.controller.zy;

import com.om.bo.base.Const;
import com.om.module.controller.base.BaseCtrl;
import com.om.module.service.zy.ZySiteService;
import com.om.util.DateUtil;
import com.om.util.RequestUtl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/zySite")
public class ZySiteController extends BaseCtrl {
    protected Logger logger = LoggerFactory.getLogger(ZySiteController.class);
    @Resource(name = "ZySiteService")
    private ZySiteService service;

    @Autowired
    private Environment env;

    /**
     * 对收藏夹的增加
     * @return
     */
    @RequestMapping("/saveGfZyFavorite")
    public @ResponseBody Map<String,Object> saveGfZyFavorite() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            this.service.saveGfZyFavorite(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 对收藏夹的删除功能
     * @return
     */
    @RequestMapping("/deleteGfZyFavorite")
    public @ResponseBody Map<String,Object> deleteGfZyFavorite() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            this.service.deleteGfZyFavorite(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 对收藏夹的查询功能
     * @return
     */
    @RequestMapping("/queryGfZyFavoriteList")
    public @ResponseBody Map<String,Object> queryGfZyFavoriteList() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryGfZyFavoriteList(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.TOTAL,param.get("TOTAL"));
            rs.put(Const.RESP_DATA,list);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 一次性加载我的收藏夹的全部数据
     * @return
     */
    @RequestMapping("/queryMyAllFavoriteList")
    public @ResponseBody Map<String,Object> queryMyAllFavoriteList() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            Map resultMap = this.service.queryMyAllFavoriteList(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.putAll(resultMap);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 查询我的留言
     * @return
     */
    @RequestMapping("/queryIgiMyMessageList")
    public @ResponseBody Map<String,Object> queryIgiMyMessageList() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryIgiMyMessageList(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.TOTAL,param.get("TOTAL"));
            rs.put(Const.RESP_DATA,list);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 查询我的留言详情
     * @return
     */
    @RequestMapping("/queryMessageDetailById")
    public @ResponseBody Map<String,Object> queryMessageDetailById() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryMessageDetailById(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,list);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 查询我的办件的token
     * @return
     */
    @RequestMapping("/getBanjianToken")
    public @ResponseBody Map<String,Object> getBanjianToken() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String urlHead =   env.getProperty("zy_banjian_url_head");;
            Map map = this.service.getBanjianToken(param,urlHead);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,map);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 查询我的办件列表
     * @return
     */
    @RequestMapping("/getMyBanjianList")
    public @ResponseBody Map<String,Object> getMyBanjianList() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String urlHead =   env.getProperty("zy_banjian_url_head");;
            Map map = this.service.getMyBanjianList(param,urlHead);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,map);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }



    /**
     * 查询栏目的超时情况
     * @return
     */
    @RequestMapping("/calcReportDataBySiteId")
    public @ResponseBody Map<String,Object> calcReportDataBySiteId() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String ALLOW_LOAD_SITE_LIST =   env.getProperty("ALLOW_LOAD_SITE_LIST");
            param.put("ALLOW_LOAD_SITE_LIST",ALLOW_LOAD_SITE_LIST);

            String NOT_ALLOW_CHANNEL_LIST =   env.getProperty("NOT_ALLOW_CHANNEL_LIST");
            param.put("NOT_ALLOW_CHANNEL_LIST",NOT_ALLOW_CHANNEL_LIST);


            Map result = this.service.calcReportDataBySiteId(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 查询栏目的超时情况
     * @return
     */
    @RequestMapping("/queryChannelUpdateInfoList")
    public @ResponseBody Map<String,Object> queryChannelUpdateInfoList() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String ALLOW_LOAD_SITE_LIST =   env.getProperty("ALLOW_LOAD_SITE_LIST");
            param.put("ALLOW_LOAD_SITE_LIST",ALLOW_LOAD_SITE_LIST);

            String NOT_ALLOW_CHANNEL_LIST =   env.getProperty("NOT_ALLOW_CHANNEL_LIST");
            param.put("NOT_ALLOW_CHANNEL_LIST",NOT_ALLOW_CHANNEL_LIST);


            List list = this.service.queryChannelUpdateInfoList(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.TOTAL,param.get("TOTAL"));
            rs.put(Const.RESP_DATA,list);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 查询政策问答库
     * @return
     */
    @RequestMapping("/queryGfKnowlege12345List")
    public @ResponseBody Map<String,Object> queryGfKnowlege12345List() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryGfKnowlege12345List(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.TOTAL,param.get("TOTAL"));
            rs.put(Const.RESP_DATA,list);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 查询政策问答库
     * @return
     */
    @RequestMapping("/queryGfKnowlege12345Detail")
    public @ResponseBody Map<String,Object> queryGfKnowlege12345Detail() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryGfKnowlege12345Detail(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,list);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 导出栏目的统计文件
     * @return
     */
    @RequestMapping("/exportChannelUpdateExcel")
    public @ResponseBody Map<String,Object>  exportChannelUpdateExcel() {
        String rootPath = env.getProperty("channelUpdateExcelPath");
        HashMap rs = new HashMap();
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            HashMap<String,Object> params = com.om.common.util.RequestUtl.getRequestMap(request);
            Map p = new HashMap();
            String today = (String)params.get("today");
            String siteId = (String)params.get("siteId");
            String month = today.substring(0,6);
            String fullPath = rootPath+"/"+month+"/"+"栏目更新统计["+today+"_"+siteId+"].xlsx";

            logger.info("下载的文件路径是："+fullPath);
            File file = new File(fullPath);
            if(!file.exists()){

                rs.put(Const.RESP_CODE,Const.ErrCode);
                rs.put(Const.RESP_MSG,"文件还未生成，请重新选择统计日期");
                return rs;
            }
            String fileName = "栏目更新统计["+today+"].xlsx";
            response.setContentType("application/force-download");// 设置强制下载不打开
            response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8") );// 设置文件名

            byte[] buffer = new byte[1024];
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            logger.info("success");

            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,"文件还未生成，请重新选择统计日期");
            return rs;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            return rs;
        }finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
