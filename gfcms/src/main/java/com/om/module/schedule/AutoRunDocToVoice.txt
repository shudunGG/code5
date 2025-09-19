package com.om.module.schedule;


import com.iflytek.cloud.speech.*;
import com.om.bo.base.FtpInfo;
import com.om.bo.base.Params;
import com.om.common.util.DateUtil;
import com.om.common.util.NetState;
import com.om.common.util.ObjectTools;
import com.om.common.util.PcmToMp3;
import com.om.module.controller.base.BaseController;
import com.om.module.core.base.service.BaseService;
import com.om.module.service.busi.AutoPublishService;
import com.om.module.service.busi.DocumentManagerService;
import com.om.module.service.busi.WxbService;
import com.om.module.service.sys.DeployService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:用于后台自动处理检查任务
 * User: Liuxj
 * Date: 2022-4-10
 * Time: 09:59
 */
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class AutoRunDocToVoice {
    private static Logger logger = LoggerFactory.getLogger(AutoRunDocToVoice.class);

    @Autowired
    private Environment env;

    @Resource(name="baseService")
    public BaseService baseService;

    @Resource(name = "DocumentManagerService")
    private DocumentManagerService docService;

    @Resource(name = "DeployService")
    private DeployService deployService;




    private String nodeDesc = null;
    private String pathPcm = "";
    private String pathMp3 = "";

    // 后台实体生成定时任务，间隔3600秒运行一次，请勿屏蔽
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，例如：3600秒
    @Scheduled(fixedRate=3600000)
    private void configureTasks() {
        //这个是用来把文档转换成语音的，这个功能去掉了
        if(2>1) return;
        String macs = Params.allowAutoGenerateMac;
        String localMac = NetState.getMac().toUpperCase();
        String os = System.getProperty("os.name");
        logger.info("AutoRunDocToVoice:-----------localMac:" + localMac + " os:" + os + "-------------");
        boolean allowRun = false;
        for (String mac : macs.split(",")) {
            if (localMac != null && localMac.contains(mac)) {
                allowRun = true;
                break;
            }
        }
        if (!allowRun) return;


        try {
            logger.info("prepare start switch voice!");
            BaseController base = new BaseController();

            //这个是用来控制哪些站点需要自动发布，
            String appId =   env.getProperty("ifly_appId");
            String voiceName =   env.getProperty("ifly_voiceName");
            String bat_proc_switch_voice =  env.getProperty("bat_proc_switch_voice");
            int limitNum=10;
            try{
                limitNum = Integer.parseInt(bat_proc_switch_voice);
            }catch (Exception e){

            }



            List list = docService.queryDocumentToSwitchVoice(limitNum);
            for(int i=0;i<list.size();i++){
                Map m = (Map)list.get(i);

                String content = m.get("CONTENT").toString();
                //根据m中的site_pk取到部署的配置信息
                Map deployConfMap = deployService.querySysConfDeployBySitePk(m);
                FtpInfo ftp = base.getDeployInfo(env,deployConfMap);
                String sep = "/";
                String path = m.get("SERVER_PATH").toString();
                if(path.indexOf(".html")>-1){
                    pathPcm = path.replaceAll(".html",".pcm");
                    pathMp3 = path.replaceAll(".html",".mp3");
                }else{
                    continue;
                }

                if(voiceName==null){
                    voiceName="xiaoyan";
                }
                SpeechUtility.createUtility("appid=" + appId);
                SpeechSynthesizer speechSynthesizer = SpeechSynthesizer
                        .createSynthesizer();
                // 设置发音人
                speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, voiceName);

                //启用合成音频流事件，不需要时，不用设置此参数
                speechSynthesizer.setParameter( SpeechConstant.TTS_BUFFER_EVENT, "1" );
                // 设置合成音频保存位置（可自定义保存位置），默认不保存
                speechSynthesizer.synthesizeToUri(content, pathPcm,synthesizeToUriListener);

                synchronized(this){
                    this.wait();
                }
                logger.info("mp3转换完成，开始删除pcm");
                File file = new File(pathPcm);
                boolean isDel = file.delete();
                logger.info("删除是否成功："+isDel+"   "+pathPcm);
                String context = path.replaceAll(ftp.getRootPath(), "");
                int index = context.lastIndexOf("/");
                String fileName = context.substring(index+1, context.length());
                fileName = fileName.replaceAll(".html",".mp3");
                context = context.substring(0, index);
                boolean isDeploy = this.docService.deployFile(ftp.getRootPath(), ftp.getTargetRoot(), context, fileName, ftp);
                if(isDeploy){
                    //update column
                    Map mm = new HashMap();
                    mm.put("DOC_PK",m.get("DOC_PK"));
                    mm.put("VOICE_URL",ftp.getAppRootPath() + sep + context + sep + fileName);
                    this.baseService.insert("busiMapper.updateBusiDocumentDef",mm);
                    logger.info("音频地址保存成功");
                }
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }


    /**
     * 合成监听器
     */
    SynthesizeToUriListener synthesizeToUriListener = new SynthesizeToUriListener() {

        public void onBufferProgress(int progress) {
            logger.debug("*************合成进度*************" + progress);
        }

        public void onSynthesizeCompleted(String uri, SpeechError error) {
            if (error == null) {
                logger.info("*************合成成功*************"+uri);
                String uri2 = uri.replaceAll(".pcm", ".mp3");//这里出来的地址和pathPcm,pathMp3是一致的，耦合的
                try {
                    PcmToMp3.convertAudioFiles(uri, uri2);
                    logger.info("mp3转换完成");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                logger.error("*************" + error.getErrorCode() + "*************");
            }
            waitupLoop();
        }


        @Override
        public void onEvent(int eventType, int arg1, int arg2, int arg3, Object obj1, Object obj2) {
            if( SpeechEvent.EVENT_TTS_BUFFER == eventType ){
                logger.debug( "onEvent: type="+eventType
                        +", arg1="+arg1
                        +", arg2="+arg2
                        +", arg3="+arg3
                        +", obj2="+(String)obj2 );
                ArrayList<?> bufs = null;
                if( obj1 instanceof ArrayList<?> ){
                    bufs = (ArrayList<?>) obj1;
                }else{
                    logger.error( "onEvent error obj1 is not ArrayList !" );
                }//end of if-else instance of ArrayList

                if( null != bufs ){
                    for( final Object obj : bufs ){
                        if( obj instanceof byte[] ){
                            final byte[] buf = (byte[]) obj;
                            logger.debug( "onEvent buf length: "+buf.length );
                        }else{
                            logger.error( "onEvent error element is not byte[] !" );
                        }
                    }//end of for
                }//end of if bufs not null
            }//end of if tts buffer event
        }

    };


    private void waitupLoop(){
        synchronized(this){
            AutoRunDocToVoice.this.notify();
        }
    }


}
