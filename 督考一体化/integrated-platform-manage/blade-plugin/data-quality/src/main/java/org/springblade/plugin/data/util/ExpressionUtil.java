package org.springblade.plugin.data.util;

import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.data.entity.FunctionComparison;
import org.springblade.plugin.data.entity.FunctionParameter;
import org.springblade.plugin.data.service.IFunctionComparisonService;
import org.springblade.plugin.data.service.IFunctionParameterService;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 10:36 2021/11/12 0012
 * @ Description：表达式工具类用于 翻译、校验用户输入的表达式
 */
@Component
@AllArgsConstructor
public class ExpressionUtil {
	private IFunctionComparisonService comparisonService;
	private IFunctionParameterService parameterService;
	private static final String INSPECTION_NAME = "inspectionName";

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 校验表达式 规则：
	 * 1.括号()、引号'、"必须成对出现，
	 * 2.表达式参数必须符合函数参数定义的要求，
	 * 3.操作符连接符不能放在开头或结尾
	 * @Date 上午 10:45 2021/11/12 0012
	 * @Param [e]
	 **/
	public R validate(String e) {
		e = HtmlUtils.htmlUnescape(e).trim();
		//先看连接符是否在开头或结尾处
		if (StringUtil.isBlank(e) || this.connector(e.charAt(0)) || this.connector(e.charAt(e.length() - 1))) {
			return R.fail("连接符不能出现在开头或结尾处");
		}
		//找到所有单双引号
		List<QuotesPosition> quotesPositions = getActualQuotesPosition(e);
		//引号校验
		if (CollectionUtil.isNotEmpty(quotesPositions)) {
			if (quotesPositions.get(quotesPositions.size() - 1).getEnd() == 0) {
				return R.fail("请检查引号！");
			}
		}
		//表达式、括号校验
		Stack<Integer> lb = new Stack<>();//非常量内的左括号位置
		List<HashMap<String, Object>> functionAndParams = getFunctionAndParam(e, quotesPositions, lb);
		//最后左括号内不能剩括号，否则说明不成对
		if (!lb.empty()) {
			return R.fail("请检查表达式内的括号！");
		}
		if (CollectionUtil.isNotEmpty(functionAndParams)) {
			for (int i = 0; i < functionAndParams.size(); i++) {
				HashMap<String, Object> functionMap = functionAndParams.get(i);
				String name = functionMap.get(INSPECTION_NAME).toString();
				FunctionComparison functionComparison = comparisonService.getByInspectionName(name);
				if (Func.isEmpty(functionComparison)) {
					return R.fail("函数" + name + "不存在！");
				}
				if (functionMap.size() != functionComparison.getParameterCount() + 1) {
					return R.fail("函数" + name + "参数数量不正确！");
				}
				List<FunctionParameter> parameters = parameterService.getByFunctionName(name);
				if (CollectionUtil.isNotEmpty(parameters)) {
					for (int j = 0; j < parameters.size(); j++) {
						//
						FunctionParameter functionParameter = parameters.get(j);
						if (functionParameter.getIfLimitValue().equals(1)) {
							String[] options = functionParameter.getSelectValueRange().split(",");
							List<String> optionList = Arrays.asList(options);
							String pn = functionMap.get(functionParameter.getParameterName()).toString().toUpperCase();
							if (!optionList.contains(pn)) {
								return R.fail("函数" + name + "参数不在可选择范围内！");
							}
						}
					}
				}
			}
		}
		ArrayList<Boolean> booleans = new ArrayList<>();
		//连接符判断
		for (int i = 0; i < e.length(); i++) {
			String temp = "";
			if (judgeChar(e.charAt(i)) && !judgePosition(i, quotesPositions)) {
				temp = temp + e.charAt(i);
				i = i + 1;
				while (i < e.length() && judgeChar(e.charAt(i))) {
					temp = temp + e.charAt(i);
					i++;
				}
				if (!judgeConjunction(temp)) {
					booleans.add(true);
				} else {
					if (StringUtil.equalsIgnoreCase("in", temp)) {
						continue;
					} else {
						booleans.add(false);
					}
				}
				i = i - 1;
			} else if (e.charAt(i) == '\'' || e.charAt(i) == '"') {
				char c = e.charAt(i);
				i = i + 1;
				while (e.charAt(i) != c) {//跳到字符串常量结束
					i = i + 1;
				}
				booleans.add(true);//常量和变量是true
			} else if (judgeSymbol(e.charAt(i))) {
				booleans.add(false);
				if (e.charAt(i) == '(') {
					i = i + 1;
					while (e.length() > i && judgeWhiteSpace(e.charAt(i))) {
						i++;
					}
					if (e.charAt(i) == ')') {
						booleans.add(true);
					} else {
						i--;
					}
				}
			}
		}
		if (CollectionUtil.isNotEmpty(booleans)) {
			if (!booleans.get(booleans.size() - 1)) {
				return R.fail("请检查连接符！");
			}
			for (int k = 0; k < booleans.size() - 1; k++) {
				if (booleans.get(k) == false && booleans.get(k + 1) == false) {
					return R.fail("请检查连接符！");
				}
			}
		}
		return R.success("表达式格式正确！");
	}

	/**
	 * @return java.lang.String
	 * @Author MaQY
	 * @Description 翻译表达式中的函数（默认表达式正确）
	 * @Date 上午 10:18 2021/11/16 0016
	 * @Param [e, driver]
	 **/
	public String translate(String e, String driver) {
		if (StringUtil.isBlank(e)) {
			return e;
		}
		e=HtmlUtils.htmlUnescape(e);
		//找到所有单双引号
		List<QuotesPosition> quotesPositions = getActualQuotesPosition(e);
		Stack<Integer> cP = new Stack<>();//非常量内的逗号位置
		Stack<Integer> lb = new Stack<>();//非常量内的左括号位置
		Stack<String> ori = new Stack<>();//原文中要被替换的字段
		Stack<String> rep = new Stack<>();//要替换原文的字段
		for (int i = 0; i < e.length(); i++) {
			if (e.charAt(i) == '(' && !judgePosition(i, quotesPositions)) {
				//左括号，并且不在引号内
				lb.push(i);
			} else if (e.charAt(i) == ',' && !judgePosition(i, quotesPositions)) {
				//逗号，并且不在引号内
				cP.push(i);
			} else if (e.charAt(i) == ')' && !judgePosition(i, quotesPositions)) {
				//右括号，并且不在引号内，开始组装函数和参数
				Integer lbPosition = lb.pop();//左括号位置
				//根据左括号位置往前找函数名，没有就算了
				String f = "";
				int j = lbPosition - 1;
				if (lbPosition > 0) {
					while (j >= 0 && judgeWhiteSpace(e.charAt(j))) {
						j--;
					}
					while (j >= 0 && judgeChar(e.charAt(j))) {
						f = e.charAt(j) + f;
						j--;
					}
				}
				if (StringUtil.isNotBlank(f) && !StringUtil.equalsIgnoreCase(f, "in")) {
					//原文被替代位置是j到i
					String o = e.substring(j + 1, i + 1);
					ori.push(o);
					//有函数名
					FunctionComparison comparison = comparisonService.getByInspectionName(f);
					//参数 根据左右括号位置和逗号位置切割
					ArrayList<Integer> tc = new ArrayList<>();//当前的逗号和括号位置
					//把右括号的位置加上去
					tc.add(i);
					if (!cP.empty()) {
						Integer c = cP.pop();
						if (c < lbPosition) {
							cP.push(c);//在左括号之外，直接再放回去
						} else {
							while (c > lbPosition) {
								tc.add(c);
								if (cP.empty()) {
									break;
								}
								c = cP.pop();
							}
							if (c < lbPosition) {
								cP.push(c);
							}
						}
					}
					//把左括号的位置加上去
					tc.add(lbPosition);
					//从小到大排序
					Collections.sort(tc);
					//组装参数
					String r = StringUtil.equalsIgnoreCase("mysql", driver) ? comparison.getMysqlFunction() : comparison.getOracleFunction();
					int order = 1;
					String prefix = "fP_";
					for (int p = 0; p < tc.size() - 1; p++) {
						r = r.replace(prefix + order, e.substring(tc.get(p) + 1, tc.get(p + 1)).trim());
						order++;
					}
					rep.push(r);
				}
			}
		}
		while (!ori.empty()) {
			String o = ori.pop();
			String r = rep.pop();
			e = e.replace(o, r);
		}
		return e;
	}

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 是否是连接词
	 * @Date 下午 1:23 2021/11/15 0015
	 * @Param [str]
	 **/
	private boolean judgeConjunction(String str) {
		str = str.toLowerCase();
		switch (str) {
			case "like":
			case "and":
			case "in":
			case "or":
			case "div":
			case "mod":
				return true;
		}
		return false;
	}

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 判断是否是连接符号（不能两个连着写，不能在句尾）
	 * @Date 上午 10:16 2021/11/16 0016
	 * @Param [c]
	 **/
	private boolean judgeSymbol(char c) {
		switch (c) {
			case '(':
			case '+':
			case ',':
			case '|':
			case '=':
			case '.': // 小数点
			case '%':
			case '/':
				return true;
		}
		return false;
	}

	/**
	 * @return java.util.List<java.util.HashMap < java.lang.String, java.lang.Object>>
	 * @Author MaQY
	 * @Description 提取函数和参数
	 * @Date 下午 1:14 2021/11/15 0015
	 * @Param [e, quotesPositions]
	 **/
	private List<HashMap<String, Object>> getFunctionAndParam(String e, List<QuotesPosition> quotesPositions, Stack<Integer> lb) {
		Stack<Integer> cP = new Stack<>();//非常量内的逗号位置
		List<HashMap<String, Object>> functionAndParams = new ArrayList<>();
		for (int i = 0; i < e.length(); i++) {
			if (e.charAt(i) == '(' && !judgePosition(i, quotesPositions)) {
				//左括号，并且不在引号内
				lb.push(i);
			} else if (e.charAt(i) == ',' && !judgePosition(i, quotesPositions)) {
				//逗号，并且不在引号内
				cP.push(i);
			} else if (e.charAt(i) == ')' && !judgePosition(i, quotesPositions)) {
				//右括号，并且不在引号内，开始组装函数和参数
				Integer lbPosition = lb.pop();//左括号位置
				//根据左括号位置往前找函数名，没有就算了
				String f = "";
				if (lbPosition > 0) {
					int j = lbPosition - 1;
					while (j >= 0 && judgeWhiteSpace(e.charAt(j))) {
						j--;
					}
					while (j >= 0 && judgeChar(e.charAt(j))) {
						f = e.charAt(j) + f;
						j--;
					}
				}
				if (StringUtil.isNotBlank(f) && !StringUtil.equalsIgnoreCase(f, "in")) {
					//有函数名
					HashMap<String, Object> functionAndParam = new HashMap<>();
					functionAndParam.put(INSPECTION_NAME, f);
					//参数 根据左右括号位置和逗号位置切割
					ArrayList<Integer> tc = new ArrayList<>();//当前的逗号和括号位置
					tc.add(i);
					tc.add(lbPosition);
					if (!cP.empty()) {
						Integer c = cP.pop();
						if (c < lbPosition) {
							cP.push(c);//在左括号之外，直接再放回去
						} else {
							while (c > lbPosition) {
								tc.add(c);
								if (cP.empty()) {
									break;
								} else {
									c = cP.pop();
								}
							}
							//把最后这个不符合的还Push进去根据左括号的位置
							if (c < lbPosition) {
								cP.push(c);
							}
						}
					}
					//从小到大排序
					Collections.sort(tc);
					//组装参数
					int order = 1;
					String prefix = "fP_";
					for (int p = 0; p < tc.size() - 1; p++) {
						String para = e.substring(tc.get(p) + 1, tc.get(p + 1)).trim();
						if (StringUtil.isNotBlank(para)) {
							functionAndParam.put(prefix + order, para);
						}
						order++;
					}
					functionAndParams.add(functionAndParam);
				}
			}
		}
		return functionAndParams;
	}

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 字符所在位置i在不在引号范围内，在 true，不在 false
	 * @Date 上午 9:54 2021/11/15 0015
	 * @Param [i, quotesPositions]
	 **/
	private boolean judgePosition(int i, List<QuotesPosition> quotesPositions) {
		if (CollectionUtil.isNotEmpty(quotesPositions)) {
			for (int j = 0; j < quotesPositions.size(); j++) {
				if (quotesPositions.get(j).getStart() < i && quotesPositions.get(j).getEnd() > i) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return java.util.List<org.springblade.plugin.data.util.QuotesPosition>
	 * @Author MaQY
	 * @Description 获取所有起作用的引号的位置
	 * @Date 上午 9:28 2021/11/15 0015
	 * @Param [e]
	 **/
	private List<QuotesPosition> getActualQuotesPosition(String e) {
		boolean start = false;
		char startChar = '-';
		ArrayList<QuotesPosition> quotesPositions = new ArrayList<>();
		for (int i = 0; i < e.length(); i++) {
			if ((e.charAt(i) == '\'' || e.charAt(i) == '"') && !start) {
				start = true;
				startChar = e.charAt(i);
				QuotesPosition position = new QuotesPosition();
				position.setStart(i);
				quotesPositions.add(position);
			} else if (e.charAt(i) == startChar && start) {
				start = false;
				QuotesPosition quotesPosition = quotesPositions.get(quotesPositions.size() - 1);
				quotesPosition.setEnd(i);
				quotesPositions.remove(quotesPositions.size() - 1);
				quotesPositions.add(quotesPosition);
			}
		}
		return quotesPositions;
	}

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 判断是不是空格
	 * @Date 下午 1:21 2021/11/12 0012
	 * @Param [s]
	 **/
	private boolean judgeWhiteSpace(char s) {
		return s == ' ' || s == '\t';
	}

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 判断是不是大小写字符或者数字
	 * @Date 上午 11:44 2021/11/12 0012
	 * @Param [s]
	 **/
	private boolean judgeChar(char s) {
		if (s >= 'a' && s <= 'z' || s >= 'A' && s <= 'Z' || s >= '0' && s <= '9' || s == '_')
			return true;
		return false;
	}

	/**
	 * @return boolean
	 * @Author MaQY
	 * @Description 是否是操作连接符
	 * @Date 上午 11:06 2021/11/12 0012
	 * @Param [c]
	 **/
	private boolean connector(char c) {
		switch (c) {
			case '&':
			case ',':
			case '|':
			case '=':
			case '>':
			case '<':
			case '.': // 小数点
				return true;
		}
		return false;
	}

}
