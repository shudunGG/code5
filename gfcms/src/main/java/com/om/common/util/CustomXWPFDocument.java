package com.om.common.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
  
/**
 * 自定义 XWPFDocument，并重写 createPicture()方法
 */
public class CustomXWPFDocument extends XWPFDocument {  
    public CustomXWPFDocument(InputStream in) throws IOException {  
        super(in);  
    }  
  
    public CustomXWPFDocument() {  
        super();  
    }  
  
    public CustomXWPFDocument(OPCPackage pkg) throws IOException {  
        super(pkg);  
    }  
  
    /**
     * @param id
     * @param width 宽
     * @param height 高
     * @param paragraph  段落
     */
    public void createPicture(int id, int width, int height,XWPFParagraph paragraph) {  

    }  
}  
