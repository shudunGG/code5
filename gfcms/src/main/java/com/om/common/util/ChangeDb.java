package com.om.common.util;

import com.om.config.mybatis.core.DataSourcesName;
import com.om.config.mybatis.core.DynamicDataSource;

/**
 * 切换数据源的工具类
 */
public class ChangeDb {

    public static void changeDb(String str){
        if("DEFAULTDB".equals(str)){
            DynamicDataSource.setDataSource(DataSourcesName.DEFAULTDB);
        }else if("CRMPBDBPROD".equals(str)){
            DynamicDataSource.setDataSource(DataSourcesName.CRMPBDBPROD);
        }else if("CRMYYDBINS1".equals(str)){
            DynamicDataSource.setDataSource(DataSourcesName.CRMYYDBINS1);
        }else if("UNIONKT".equals(str)){
            DynamicDataSource.setDataSource(DataSourcesName.UNIONKT);
        }else if("CZXXDBOB60".equals(str)){
            DynamicDataSource.setDataSource(DataSourcesName.CZXXDBOB60);
        }

//      ///  权限测试所需的数据源
//        else if("MT".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.DEFAULTDB);
//        }else if("RA".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CRMPBDBPROD);
//        }else if("CRMYY".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CRMYYDBINS1);
//        }else if("CRMPB".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.UNIONKT);
//        }else if("CZXXDB".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CZXXDBOB60);
//        }else if("ZGDB".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CRMPBDBPROD);
//        }else if("JFYHDB".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CRMYYDBINS1);
//        }else if("KT".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.UNIONKT);
//        }else if("BOMC".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CZXXDBOB60);
//        }else if("CRMTSEST1".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CRMPBDBPROD);
//        }else if("CRMTSEST2".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CRMYYDBINS1);
//        }else if("CRMTSEST3".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.UNIONKT);
//        }else if("BILL01DB".equals(str)){
//            DynamicDataSource.setDataSource(DataSourcesName.CZXXDBOB60);
//        }


    }
}
