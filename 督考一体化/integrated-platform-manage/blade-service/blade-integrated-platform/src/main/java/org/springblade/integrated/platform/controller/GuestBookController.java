package org.springblade.integrated.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.vingsoft.entity.GuestBook;
import com.vingsoft.vo.GuestBookVO;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.HttpSafeConstant;
import org.springblade.common.utils.VSTool;
import org.springblade.core.log.logger.BladeLogger;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.integrated.platform.service.IGuestBookService;
import org.springblade.integrated.platform.wrapper.AppTaskWrapper;
import org.springblade.integrated.platform.wrapper.GuestBookWrapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


/**
 * @author mrtang
 * @version 1.0
 * @description: 意见反馈——留言簿
 * @date 2022/4/29 18:10
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/guestBook")
public class GuestBookController {

	private IGuestBookService guestBookService;
	private final BladeLogger bladeLogger;
	@Resource
	private HttpSafeConstant httpSafeConstant;
	private ObjectMapper objectMapper;

	/**
     * 获取列表数据
	 * @param guestbook
     * @param query
     * @return
     */
	@GetMapping("/list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取列表数据")
	public R<IPage<GuestBookVO>> list(@RequestParam Map<String,Object> guestbook, Query query){
		QueryWrapper<GuestBook> queryWrapper = Condition.getQueryWrapper(guestbook,GuestBook.class);
		IPage<GuestBook> page = this.guestBookService.page(Condition.getPage(query),queryWrapper);
		return R.data(GuestBookWrapper.build().pageVO(page));
	}

	/**
	 * 获取列表数据
	 * @return
	 */
	@PostMapping("/listApp")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "获取列表数据")
	public R listApp(@RequestBody Map<String, Object> map){
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("guestBook-listApp",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			Query query = new Query();
			query.setCurrent(jsonParams.getInteger("current"));
			query.setSize(jsonParams.getInteger("size"));

			Map<String, Object> guestbook = new HashMap<>(jsonParams);
			QueryWrapper<GuestBook> queryWrapper = Condition.getQueryWrapper(guestbook,GuestBook.class);
			IPage<GuestBook> page = this.guestBookService.page(Condition.getPage(query),queryWrapper);
			JSONObject pageJson = objectMapper.convertValue(GuestBookWrapper.build().pageVO(page), JSONObject.class);
			return R.data(VSTool.encrypt(encryptSign, pageJson.toJSONString(), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 查询单条
	 */
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "查看详情", notes = "传入id")
	@GetMapping("/detail")
	public R<GuestBookVO> detail(GuestBook guestbook) {
		GuestBook detail = this.guestBookService.getOne(Condition.getQueryWrapper(guestbook));
		return R.data(GuestBookWrapper.build().entityVO(detail));
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "新增", notes = "传入GuestBook")
	public R save(@Valid @RequestBody GuestBookVO guestbook) {
		if(guestbook.getFilesJson()!=null && guestbook.getFilesJson().size()>0){
			guestbook.setFiles(guestbook.getFilesJson().toJSONString());
		}
		return R.status(this.guestBookService.save(guestbook));
	}

	/**
	 * 新增-app
	 */
	@PostMapping("/saveApp")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "新增", notes = "传入GuestBook")
	public R saveApp(@RequestBody Map<String, Object> map) {
		//参数解密
		String params = map.get("params").toString();
		//1、日志记录
		bladeLogger.info("guestBook-saveApp",params);
		//2、参数解密
		Map<String, Object> dataMap = VSTool.decrypt(httpSafeConstant.getPrivateKey(), params, VSTool.CHN);
		if (dataMap.get("extra") != null) {
			String encryptSign = dataMap.get("sign").toString();
			JSONObject jsonParams = JSONObject.parseObject(dataMap.get("extra").toString());
			GuestBookVO guestbook = objectMapper.convertValue(jsonParams, GuestBookVO.class);

			if(guestbook.getFilesJson()!=null && guestbook.getFilesJson().size()>0){
				guestbook.setFiles(guestbook.getFilesJson().toJSONString());
			}
			return R.data(VSTool.encrypt(encryptSign, String.valueOf(this.guestBookService.save(guestbook)), VSTool.CHN));
		}else {
			return R.fail("加密解析错误");
		}
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "修改", notes = "传入GuestBook")
	public R update(@Valid @RequestBody GuestBookVO guestbook) {
		if(guestbook.getFilesJson()!=null && guestbook.getFilesJson().size()>0){
			guestbook.setFiles(guestbook.getFilesJson().toJSONString());
		}
		return R.status(this.guestBookService.updateById(guestbook));
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "删除", notes = "传入id集合")
	public R remove(@RequestParam String ids) {
		return R.status(this.guestBookService.deleteLogic(Func.toLongList(ids)));
	}
}
