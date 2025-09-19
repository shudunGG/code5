package org.springblade.integrated.platform.common.utils.file;

import java.io.*;
import cn.hutool.core.io.FileUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.common.exception.file.InvalidExtensionException;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.common.utils.StringUtils;
import org.springblade.integrated.platform.common.utils.uuid.IdUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * 文件操作工具类
 *
 * @Author JG🧸
 * @Create 2022/4/15 15:50
 */
public class FileUploadUtils
{
	/**
	 * 默认大小 50M
	 */
	public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024;

	/**
	 * 默认的文件名最大长度 100
	 */
	public static final int DEFAULT_FILE_NAME_LENGTH = 100;


	/** 上传路径 */
	private static String profile;
	public static String getProfile()
	{
		System.out.println(profile);
		if(StringUtils.isEmpty(profile)){
			Properties props = System.getProperties();
			String os = props.getProperty("os.name");
			if (!os.contains("Windows")) {
				return "/home/ceshi/";
			}
			return  "D:/ZyFiles/";
		}
		return profile;
	}
	public void setProfile(String profile)
	{
		FileUploadUtils.profile = profile;
	}


	/**
	 * 默认上传的地址
	 */
	private static String defaultBaseDir = getProfile();//
	public static void setDefaultBaseDir(String defaultBaseDir)
	{
		FileUploadUtils.defaultBaseDir = defaultBaseDir;
	}
	public static String getDefaultBaseDir()
	{
		return defaultBaseDir;
	}
	/**
	 * 文件夹上传
	 * @param files  文件
	 * @param pathSrc 父目录
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public static void uploadFiles(List<MultipartFile> files,String pathSrc) throws Exception {
		if(files.size()==0) {
			throw new Exception("文件夹为空");
		}
		for(MultipartFile file:files) {
			boolean isOkFile = isOkFile(file);
			if (!isOkFile) {//不合格
				throw new Exception("您上传的文件格式有误,请检查!");
			}
			String  filePathSrc= pathSrc+file.getOriginalFilename();//上传文件全路径
			boolean flag = FileUtil.exist(filePathSrc);//判断文件是否存在
			if(!flag) {//不存在,创建
				FileUtil.mkParentDirs(filePathSrc);//创建目录
				//filePathSrc.substring(0, filePathSrc.lastIndexOf("/"));
				file.transferTo(FileUtil.file(filePathSrc));//创建并复制文件
			}
		}
	}


	/**
	 * 文件上传
	 * @param files  文件
	 * @param pathSrc 父目录
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public static void uploadFiles2(List<MultipartFile> files,String pathSrc) throws Exception {
		if(files.size()==0) {
			throw new Exception("文件夹为空");
		}
		for(MultipartFile file:files) {

			String  filePathSrc= pathSrc+file.getOriginalFilename();//上传文件全路径
			boolean flag = FileUtil.exist(filePathSrc);//判断文件是否存在
			if(!flag) {//不存在,创建
				FileUtil.mkParentDirs(filePathSrc);//创建目录
				//filePathSrc.substring(0, filePathSrc.lastIndexOf("/"));
				file.transferTo(FileUtil.file(filePathSrc));//创建并复制文件
			}
		}
	}


	/**
	 * 是否合格的文件
	 * @return
	 */
	public static boolean isOkFile(MultipartFile file){
		//获得文件后缀名
		String suffix = getExtension(file);
		if(StringUtils.isNotEmpty(suffix) &&
			(suffix.equals("doc")|| suffix.equals("docx") || suffix.equals("enc")
				||suffix.equals("png")||suffix.equals("jpg")||suffix.equals("gif")
				||suffix.equals("pdf")||suffix.equals("xls")||suffix.equals("xlsx")
				||suffix.equals("txt")||suffix.equals("rar")||suffix.equals("zip"))
		)
		{
			return true;
		}else {
			return  false;
		}

	}

	public static File MultipartFileToFile(MultipartFile multiFile) {
		// 获取文件名
		String fileName = multiFile.getOriginalFilename();
		// 获取文件后缀
		String prefix = fileName.substring(fileName.lastIndexOf("."));
		// 若需要防止生成的临时文件重复,可以在文件名后添加随机码
		String name=fileName.substring(0,fileName.lastIndexOf("."));
		try {
			File file = File.createTempFile(name, prefix);
			multiFile.transferTo(file);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 文件下载
	 * @param parentPath  文件路径
	 * @param fileName  文件名称
	 * @param request
	 * @param response
	 */
	public static void downPrintFile(String parentPath,String fileName,HttpServletRequest request,HttpServletResponse response){
		//要下载的文件路径+文件名
		String filePath = parentPath + fileName;
		filePath=filePath.replace('\\', '/');
		System.out.println(">>>"+filePath);
		File file = new File(filePath);;

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			String aFileName = null;
			request.setCharacterEncoding("UTF-8");
			String agent = request.getHeader("User-Agent").toUpperCase();
			if ((agent.indexOf("MSIE") > 0)
				|| ((agent.indexOf("RV") != -1) && (agent
				.indexOf("FIREFOX") == -1))) {
				aFileName = URLEncoder.encode(fileName, "UTF-8");
			}else {
				aFileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
			}
			response.setContentType("application/octet-stream");//octet-stream为要下载文件是exe类型或看该文档http://www.w3school.com.cn/media/media_mimeref.asp
			response.setHeader("Content-disposition", "attachment; filename="
				+ aFileName);
			response.setHeader("Content-Length", String.valueOf(file.length()));
			bis = new BufferedInputStream(new FileInputStream(new File(filePath)));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
			System.out.println("success");
			bos.flush();
		} catch (Exception e) {
			System.out.println("失败！"+ e );
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
				if (bos != null) {
					bos.close();
				}
//				file.delete();
			} catch (Exception e) {
			}
		}
	}
	/**
	 * 以默认配置进行文件上传
	 *
	 * @param file 上传的文件
	 * @return 文件名称
	 * @throws Exception
	 */
	public static final String upload(MultipartFile file) throws IOException
	{
		try
		{
			boolean isOkFile = isOkFile(file);
			if (!isOkFile) {//不合格
				throw new Exception("文件格式不正确");
			}
			return upload(getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
		}
		catch (Exception e)
		{
			throw new IOException(e.getMessage(), e);
		}
	}

	/**
	 * 根据文件路径上传
	 *
	 * @param baseDir 相对应用的基目录
	 * @param file 上传的文件
	 * @return 文件名称
	 * @throws IOException
	 */
	public static final String upload(String baseDir, MultipartFile file) throws IOException
	{
		try
		{
			boolean isOkFile = isOkFile(file);

			if (!isOkFile) {//不合格
				throw new Exception("文件格式不正确" + getExtension(file));
			}
			return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
		}
		catch (Exception e)
		{
			throw new IOException(e.getMessage(), e);
		}
	}

	/**
	 * 文件上传
	 *
	 * @param baseDir 相对应用的基目录
	 * @param file 上传的文件
	 * @param allowedExtension 上传文件类型
	 * @return 返回上传成功的文件名
	 * @throws Exception 如果超出最大大小
	 * @throws Exception 文件名太长
	 * @throws IOException 比如读写文件出错时
	 * @throws Exception 文件校验异常
	 */
	public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension)
		throws Exception
	{
		boolean isOkFile = isOkFile(file);
		if (!isOkFile) {//不合格
			throw new Exception("文件格式不正确");
		}
		int fileNamelength = file.getOriginalFilename().length();
		if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH)
		{
			throw new Exception("超出文件大小："+FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
		}

		assertAllowed(file, allowedExtension);

		String fileName = extractFilename(file);

		File desc = getAbsoluteFile(baseDir, fileName);
		file.transferTo(desc);
		String pathFileName = getPathFileName(baseDir, fileName);
		return pathFileName;
	}

	/**
	 * 编码文件名
	 */
	public static final String extractFilename(MultipartFile file)
	{
		String fileName = file.getOriginalFilename();
		String extension = getExtension(file);
		fileName = DateUtils.datePath() + "/" + IdUtils.fastUUID() + "." + extension;
		return fileName;
	}

	private static final File getAbsoluteFile(String uploadDir, String fileName) throws IOException
	{
		File desc = new File(uploadDir + File.separator + fileName);

		if (!desc.getParentFile().exists())
		{
			desc.getParentFile().mkdirs();
		}
		if (!desc.exists())
		{
			desc.createNewFile();
		}
		return desc;
	}

	private static final String getPathFileName(String uploadDir, String fileName) throws IOException
	{
		int dirLastIndex = getProfile().length() + 1;
		String currentDir = StringUtils.substring(uploadDir, dirLastIndex);
		String pathFileName = Constants.RESOURCE_PREFIX + "/" + currentDir + "/" + fileName;
		return pathFileName;
	}

	/**
	 * 文件大小校验
	 *
	 * @param file 上传的文件
	 * @return
	 * @throws Exception 如果超出最大大小
	 * @throws Exception
	 */
	public static final void assertAllowed(MultipartFile file, String[] allowedExtension)throws Exception
	{
		long size = file.getSize();
		if (DEFAULT_MAX_SIZE != -1 && size > DEFAULT_MAX_SIZE)
		{
			throw new Exception("超出文件大小："+DEFAULT_MAX_SIZE / 1024 / 1024);
		}

		String fileName = file.getOriginalFilename();
		String extension = getExtension(file);
		if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension))
		{
			if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION)
			{
				throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
					fileName);
			}
			else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION)
			{
				throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
					fileName);
			}
			else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION)
			{
				throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
					fileName);
			}
			else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION)
			{
				throw new InvalidExtensionException.InvalidVideoExtensionException(allowedExtension, extension,
					fileName);
			}
			else
			{
				throw new InvalidExtensionException(allowedExtension, extension, fileName);
			}
		}

	}

	/**
	 * 判断MIME类型是否是允许的MIME类型
	 *
	 * @param extension
	 * @param allowedExtension
	 * @return
	 */
	public static final boolean isAllowedExtension(String extension, String[] allowedExtension){
		for (String str : allowedExtension)
		{
			if (str.equalsIgnoreCase(extension))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取文件名的后缀
	 *
	 * @param file 表单文件
	 * @return 后缀名
	 */
	public static final String getExtension(MultipartFile file){
		try {
			Tika tika = new Tika();
			String contentType = tika.detect(file.getInputStream());
			MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
			MimeType mime = allTypes.forName(contentType);
			String extension = mime.getExtension();
			System.out.println("后缀名是：" + extension.substring(1));
			String extension1 = FilenameUtils.getExtension(file.getOriginalFilename());
			System.out.println("==========后缀名是：" + extension1.substring(0));

			//if (StringUtils.isNotEmpty(extension) && StringUtils.isNotEmpty(extension1) && extension.equals("."+extension1)){
			if (StringUtils.isNotEmpty(extension1)){
				return extension1.substring(0);
			}else {
				if(mime != null){
					return extension.substring(1);
				}else {
					return "错误";
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "错误";
		}
	}


	/**
	 * @param filename
	 * @param response
	 * @throws IOException
	 */
	public static void downloadFile(String uploadPath, String filename, HttpServletResponse response) throws IOException {
		response.setHeader("Content-type", "application/zip");
		String downloadFilename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
		response.setHeader("Content-Disposition", "attachment;filename=" + downloadFilename);
		OutputStream outputStream = response.getOutputStream();
		byte[] buff = new byte[1024];
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(uploadPath + File.separator + filename)));
		int i = bis.read(buff);
		while (i != -1) {
			outputStream.write(buff, 0, buff.length);
			outputStream.flush();
			i = bis.read(buff);
		}
	}

}
