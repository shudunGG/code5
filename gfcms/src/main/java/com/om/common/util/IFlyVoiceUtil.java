package com.om.common.util;

import java.io.IOException;
import java.util.ArrayList;

import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechEvent;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUtility;
import com.iflytek.cloud.speech.SynthesizeToUriListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IFlyVoiceUtil {
	private static Logger logger = LoggerFactory.getLogger(IFlyVoiceUtil.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s = "针对俄乌冲突引发的危机，世界各国都在担心会引发三战，毕竟俄乌之间的冲突并不是根本问题，美国的参与才是该冲突一直久悬未决的真正原因，所以作为两大军事强国之间的对决，世界各国都在担心，一旦爆发核武，对全球来说，将会是毁灭性的打击。";
		String path="d:\\test1.pcm";
		String appId = "2b01405b";
		String voiceName="xiaoyan";
		IFlyVoiceUtil u = new IFlyVoiceUtil();
		u.Synthesize(s, path,appId,voiceName);
		
	}

	
	/**
	 * 合成
	 */
	private void Synthesize(String s,String path,String appId,String voiceName) {
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
		speechSynthesizer.synthesizeToUri(s, path,synthesizeToUriListener);
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
				String uri2 = uri.replaceAll(".pcm", ".mp3");
				try {
					PcmToMp3.convertAudioFiles(uri, uri2);
					System.out.println("mp3转换完成");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else
				logger.error("*************" + error.getErrorCode()	+ "*************");
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
			IFlyVoiceUtil.this.notify();
		}
	}
}
