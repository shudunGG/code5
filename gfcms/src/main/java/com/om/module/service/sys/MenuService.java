package com.om.module.service.sys;
import com.om.bo.menu.MenuBo;
import com.om.common.cache.Dict;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("MenuService")
public class MenuService extends CommonService {

    /**
     * 返回菜单数据
     * @param param
     * @return
     * @throws Exception
     */
    public List queryMenuList(Map param) throws Exception {
        String SUB_SYS = (String)param.get("SUB_SYS");
        this.isNull("SUB_SYS",SUB_SYS);

        List returnList = new ArrayList();
        MenuBo bo = null;
        List<Map> rootMenuList = new ArrayList<Map>();//所有的根节点
        Map<String,List<String>> pId2sonIdMap = new HashMap<String,List<String>>();//父ID:子ID串
        Map menuDb = new HashMap();//以menu_id为key,menu对象为value
        param.put("SUB_SYS",SUB_SYS);
        List<Map> menuList = this.baseService.getList("sysMapper.querySysMenuConf",param);
        for(Map menuMap:menuList){
            String P_MENU_ID = menuMap.get("P_MENU_ID").toString();
            String MENU_ID = menuMap.get("MENU_ID").toString();
            menuDb.put(MENU_ID,menuMap);
            if("-1".equals(P_MENU_ID)){
                rootMenuList.add(menuMap);
            }else{
                List sonList = pId2sonIdMap.get(P_MENU_ID);
                if(sonList == null){
                    sonList = new ArrayList<String>();
                    pId2sonIdMap.put(P_MENU_ID,sonList);
                }
                sonList.add(MENU_ID);
            }
        }

        for(Map menuMap:menuList){
            String P_MENU_ID = menuMap.get("P_MENU_ID").toString();
            String MENU_ID = menuMap.get("MENU_ID").toString();
            String MENU_NAME = menuMap.get("MENU_NAME").toString();
            String URL = menuMap.get("URL").toString();
            String ICON = menuMap.get("ICON").toString();

            bo = new MenuBo(MENU_NAME,URL,ICON);
            bo.setValue(MENU_ID);
            List<String> sonList = pId2sonIdMap.get(MENU_ID);
            List<MenuBo> menuSonList = new ArrayList<MenuBo>();
            if(sonList!=null) {
                for (String sonMenuId : sonList) {
                    Map sonMenuMap = (Map) menuDb.get(sonMenuId);
                    if (sonMenuMap != null) {
                        P_MENU_ID = sonMenuMap.get("P_MENU_ID").toString();
                        MENU_ID = sonMenuMap.get("MENU_ID").toString();
                        MENU_NAME = sonMenuMap.get("MENU_NAME").toString();
                        URL = sonMenuMap.get("URL").toString();
                        ICON = sonMenuMap.get("ICON").toString();

                        MenuBo sbo = new MenuBo(MENU_NAME, URL, ICON);
                        sbo.setValue(MENU_ID);
                        menuSonList.add(sbo);
                    }
                }
                if(menuSonList.size()>0){
                    bo.setChildren(menuSonList);
                }
            }

            returnList.add(bo);
        }

        return returnList;
    }


}
