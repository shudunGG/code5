package org.springblade.integrated.platform.constant;

/**
 * 考核评价-领导评价 常量
 *
 * @author JG🧸
 * @since 2022/10/8 13:52
 */
public interface LeaderAppriseConstant {

	//TODO【72个市直部门单位】打分角色
	/**
	 * 市委市政府主要领导 占20%
	 */
	public static final String SZ_APPRISE_SWSZFZYLD = "leaderApprise市委市政府主要领导";

	/**
	 * 人大政协市委专职副书记领导 占15%  ---->市直综合部门 改35%
	 */
	public static final String SZ_APPRISE_RDZXSWZZFSJLD = "leaderApprise人大政协市委专职副书记领导";

	/**
	 * 72常委 占20%
	 */
	public static final String SZ_APPRISE_72CW = "leaderApprise72常委";

	/**
	 * 72副市长 占10%
	 */
	public static final String SZ_APPRISE_72FSZ = "leaderApprise72副市长";

	/**
	 * 县区主要领导 占20%
	 */
	public static final String SZ_APPRISE_XQZYLD = "leaderApprise县区主要领导";

	/**
	 * 72人大政协法院检察院其他领导 占15%
	 */
	public static final String SZ_APPRISE_72RDZXFYJCYQTLD = "leaderApprise72人大政协法院检察院其他领导";


	//TODO【县区】打分角色
	/**
	 * 市委市政府主要领导 占20%
	 */
	public static final String XQ_APPRISE_SWSZFZYLD = "leaderApprise市委市政府主要领导";

	/**
	 * 人大政协市委专职副书记领导 占15%  ------->县区改35%
	 */
	public static final String XQ_APPRISE_RDZXSWZZFSJLD = "leaderApprise人大政协市委专职副书记领导";

	/**
	 * 县区常委 占20%
	 */
	public static final String XQ_APPRISE_XQCW = "leaderApprise县区常委";

	/**
	 * 县区常委 占10%
	 */
	public static final String XQ_APPRISE_XQFSZ = "leaderApprise县区副市长";

	/**
	 * 县区人大政协法院检察院其他领导 占15%
	 */
	public static final String XQ_APPRISE_XQRDZXFYJCYQTLD = "leaderApprise县区人大政协法院检察院其他领导";

	/**
	 * 县区市直部门领导 占20%
	 */
	public static final String XQ_APPRISE_XQSZBMLD = "leaderApprise县区市直部门领导";

	//TODO【20个其他市直单位】打分角色
	/**
	 * 市委市政府主要领导 占20%
	 */
	public static final String QTSZ_APPRISE_SWSZFZYLD = "leaderApprise市委市政府主要领导";

	/**
	 * 人大政协市委专职副书记领导 占15%  ---------->市直事业单位 改为45%
	 */
	public static final String QTSZ_APPRISE_RDZXSWZZFSJLD = "leaderApprise人大政协市委专职副书记领导";

	/**
	 * 20常委 占30%
	 */
	public static final String QTSZ_APPRISE_20CW = "leaderApprise20常委";

	/**
	 * 20副市长 占15%
	 */
	public static final String QTSZ_APPRISE_20FSZ = "leaderApprise20副市长";

	/**
	 * 20人大政协法院检察院其他领导 占20%
	 */
	public static final String QTSZ_APPRISE_20RDZXFYJCYQTLD = "leaderApprise20人大政协法院检察院其他领导";

}
