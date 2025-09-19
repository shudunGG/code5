package org.springblade.integrated.platform.common.utils.file;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

/**
 * 文件处理工具类
 *
 * @Author JG🧸
 * @Create 2022/4/15 15:50
 */
public class FileUtils extends org.apache.commons.io.FileUtils
{
    public static String FILENAME_PATTERN = "[a-zA-Z0-9_\\-\\|\\.\\u4e00-\\u9fa5]+";
    /**
	 * 打开或下载 pdf
	 * @param filePath 文件路径
	 * @param response
	 * @param isOnLine true-在线打开,false-在线下载
	 */
	public static void downLoad(String filePath,HttpServletResponse response, boolean isOnLine){
        filePath=FileUploadUtils.getDefaultBaseDir()+filePath;
		File f = new File(filePath);
		BufferedInputStream br=null;
		OutputStream out=null;
		try {

			if (!f.exists()) {
				response.sendError(404, "File not found!");
				return;
			}

			br = new BufferedInputStream(new FileInputStream(f));
			byte[] buf = new byte[1024];
			int len = 0;

			response.reset(); // 非常重要
			if (isOnLine) { // 在线打开方式
				//URL u = new URL("file:///" + filePath);
				response.setCharacterEncoding("utf-8");
				response.setContentType("application/pdf");
				String filename = URLEncoder.encode(f.getName(), "utf-8");
				response.setHeader("Content-Disposition", "inline; filename=" + filename);
				// 文件名应该编码成UTF-8
			} else { // 纯下载方式
				response.setCharacterEncoding("utf-8");
				response.setContentType("application/x-msdownload");
				String filename = URLEncoder.encode(f.getName(), "utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			}
			out = response.getOutputStream();
			while ((len = br.read(buf)) > 0)
				out.write(buf, 0, len);
			//br.close();
			//out.close();

		} catch (Exception e) {
			e.printStackTrace();

		}finally {
			try {
				if(br!=null)
					br.close();
				if(out!=null)
					out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

    /**
     * 输出指定文件的byte数组
     *
     * @param filePath 文件路径
     * @param os 输出流
     * @return
     */
    public static void writeBytes(String filePath, OutputStream os) throws IOException
    {
        filePath=FileUploadUtils.getDefaultBaseDir()+filePath;
        FileInputStream fis = null;
        try
        {
            File file = new File(filePath);
            if (!file.exists())
            {
                throw new FileNotFoundException(filePath);
            }
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int length;
            while ((length = fis.read(b)) > 0)
            {
                os.write(b, 0, length);
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件
     * @return
     */
    public static boolean deleteFile(String filePath)
    {
        filePath=FileUploadUtils.getDefaultBaseDir()+filePath;
        boolean flag = false;
        File file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists())
        {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 文件名称验证
     *
     * @param filename 文件名称
     * @return true 正常 false 非法
     */
    public static boolean isValidFilename(String filename)
    {
        return filename.matches(FILENAME_PATTERN);
    }

    /**
     * 下载文件名重新编码
     *
     * @param request 请求对象
     * @param fileName 文件名
     * @return 编码后的文件名
     */
    public static String setFileDownloadHeader(HttpServletRequest request, String fileName) throws UnsupportedEncodingException
    {
        final String agent = request.getHeader("USER-AGENT");
        String filename = fileName;
        if (agent.contains("MSIE"))
        {
            // IE浏览器
            filename = URLEncoder.encode(filename, "utf-8");
            filename = filename.replace("+", " ");
        }
        else if (agent.contains("Firefox"))
        {
            // 火狐浏览器
            filename = new String(fileName.getBytes(), "ISO8859-1");
        }
        else if (agent.contains("Chrome"))
        {
            // google浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        else
        {
            // 其它浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        return filename;
    }

    /**
     * 下载文件名重新编码
     *
     * @param response 响应对象
     * @param realFileName 真实文件名
     * @return
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException
    {
        String percentEncodedFileName = percentEncode(realFileName);

        StringBuilder contentDispositionValue = new StringBuilder();
        contentDispositionValue.append("attachment; filename=")
                .append(percentEncodedFileName)
                .append(";")
                .append("filename*=")
                .append("utf-8''")
                .append(percentEncodedFileName);

        response.setHeader("Content-disposition", contentDispositionValue.toString());
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) throws UnsupportedEncodingException
    {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }
/**
 * 图片在线查看
 * @param path  图片路径
 * @param response
 */
	public static void openImg(String path, HttpServletResponse response) {
	    path=FileUploadUtils.getDefaultBaseDir()+path;
		  File imgFile = new File(path.replace("'", ""));
		  FileInputStream fin = null;
		  OutputStream output = null;
		  try {
		   output = response.getOutputStream();
		   fin = new FileInputStream(imgFile);
		   byte[] arr = new byte[1024 * 10];
		   int n;
		   while ((n = fin.read(arr)) != -1) {
		    output.write(arr, 0, n);
		   }
		   output.flush();
		   output.close();
		   fin.close();
		  } catch (IOException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
		  }finally {
		   if(fin!=null)
		    try {
		     fin.close();
		    } catch (IOException e) {
		     // TODO Auto-generated catch block
		     e.printStackTrace();
		    }
		   if(output!=null)
		    try {
		     output.flush();
		     output.close();
		    } catch (IOException e) {
		     // TODO Auto-generated catch block
		     e.printStackTrace();
		    }
		  }
	}


	/**
	 * 图片大小转换
	 * @param size 文件大小
	 */
	public static String getNetFileSizeDescription(long size) {
		StringBuffer bytes = new StringBuffer();
		DecimalFormat format = new DecimalFormat("###.0");
		if (size >= 1024 * 1024 * 1024) {
			double i = (size / (1024.0 * 1024.0 * 1024.0));
			bytes.append(format.format(i)).append("GB");
		}
		else if (size >= 1024 * 1024) {
			double i = (size / (1024.0 * 1024.0));
			bytes.append(format.format(i)).append("MB");
		}
		else if (size >= 1024) {
			double i = (size / (1024.0));
			bytes.append(format.format(i)).append("KB");
		}
		else if (size < 1024) {
			if (size <= 0) {
				bytes.append("0B");
			}
			else {
				bytes.append((int) size).append("B");
			}
		}
		return bytes.toString();
	}

}
