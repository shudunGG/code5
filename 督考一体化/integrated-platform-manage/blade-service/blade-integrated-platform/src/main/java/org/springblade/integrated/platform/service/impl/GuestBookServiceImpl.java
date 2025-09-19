package org.springblade.integrated.platform.service.impl;

import com.vingsoft.entity.GuestBook;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.GuestBookMapper;
import org.springblade.integrated.platform.service.IGuestBookService;
import org.springframework.stereotype.Service;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/29 19:40
 */
@Slf4j
@Service
public class GuestBookServiceImpl extends BaseServiceImpl<GuestBookMapper,GuestBook> implements IGuestBookService {
}
