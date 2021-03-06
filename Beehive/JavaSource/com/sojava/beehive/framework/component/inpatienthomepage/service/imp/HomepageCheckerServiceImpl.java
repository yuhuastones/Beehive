package com.sojava.beehive.framework.component.inpatienthomepage.service.imp;

import com.sojava.beehive.framework.component.inpatienthomepage.bean.DicAdministrativeDivision;
import com.sojava.beehive.framework.component.inpatienthomepage.bean.Dictionary;
import com.sojava.beehive.framework.component.inpatienthomepage.bean.IcdTransform;
import com.sojava.beehive.framework.component.inpatienthomepage.bean.InpatientHomepageAnaly;
import com.sojava.beehive.framework.component.inpatienthomepage.bean.InpatientHomepageAnalyCheck;
import com.sojava.beehive.framework.component.inpatienthomepage.bean.RecordRangeType;
import com.sojava.beehive.framework.component.inpatienthomepage.bean.VIcdTransform;
import com.sojava.beehive.framework.component.inpatienthomepage.dao.HomepageDao;
import com.sojava.beehive.framework.component.inpatienthomepage.service.HomepageCheckerService;
import com.sojava.beehive.framework.exception.ErrorException;
import com.sojava.beehive.framework.util.FormatUtil;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.Column;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;


@Service
public class HomepageCheckerServiceImpl implements HomepageCheckerService {

	@Resource private HomepageDao homepageDao;
	private List<InpatientHomepageAnaly> homepageList;
	private int homepageIndex = 1;

	private final String CHECK_TYPE_WARN = "WARN";
	private final String CHECK_TYPE_ERROR = "ERROR";
	private final String CHECK_TYPE_NONEMPTY = "NONEMPTY";
	private final String CHECK_TYPE_CONDITIONAL_NONEMPTY = "CONDITIONAL_NONEMPTY";
	private final String CHECK_TYPE_VALIDITY = "VALIDITY";
	private final String CHECK_TYPE_LOGICAL = "LOGICAL";
	private final float NIGHT_TIME = 16.4f;
	private final String DEFAULT_CITY = "黑龙江省鸡西市";

	private static boolean isInited = false;

	private static Dictionary[] rc001 = null; //性别代码
	private static Dictionary[] rc002 = null; //婚姻状况
	private static Dictionary[] rc003 = null; //职业代码
	private static Dictionary[] rc011 = null; //病案质量
	private static Dictionary[] rc013 = null; //麻醉方式
	private static Dictionary[] rc014_1 = null; //切口愈合等级
	private static Dictionary[] rc014_2 = null; //切口愈合类别
	private static Dictionary[] rc016 = null; //判断代码
	private static Dictionary[] rc019 = null; //离院方式
	private static Dictionary[] rc023 = null; //科室代码
	private static Dictionary[] rc026 = null; //入院途径代码
	private static Dictionary[] rc027 = null; //入院病情
	private static Dictionary[] rc028 = null; //药物过敏
	private static Dictionary[] rc029 = null; //手术级别
	private static Dictionary[] rc030 = null; //血型编码
	private static Dictionary[] rc031 = null; //Rh血型
	private static Dictionary[] rc032 = null; //医疗付费方式
	private static Dictionary[] rc033 = null; //联系人关系
	private static Dictionary[] rc035 = null; //民族代码
	private static Dictionary[] rc036 = null; //籍贯代码
	private static Dictionary[] rc037 = null; //有无字典
	private static Dictionary[] rc038 = null; //国籍字典
	private static Dictionary[] rc039 = null; //治疗类别
	private static Dictionary[] rc040 = null; //实施临床路径
	/*
	 *	行政区划
	 */
	private static DicAdministrativeDivision[] administrativeDivisionProv = null; //省
	private static DicAdministrativeDivision[] administrativeDivisionCity = null; //地级市
	//HIS对照
	private static VIcdTransform[] hisDiagnosisChinese = null;
	private static VIcdTransform[] hisDiagnosisWestern = null;
	private static VIcdTransform[] hisDiagnosisOperation = null;
	private static VIcdTransform[] hisDiagnosisPathology = null;
	//ICD2.0诊断
	private static IcdTransform[] icd2DiagnosisChinese = null;
	private static IcdTransform[] icd2DiagnosisWestern = null;
	private static IcdTransform[] icd2DiagnosisOperation = null;
	private static IcdTransform[] icd2DiagnosisPathology = null;

	private void initialize() {
		if (isInited) return;

		homepageList = new ArrayList<InpatientHomepageAnaly>();
		String _status = "";
		try {
			_status = "RC001";
			rc001 = homepageDao.getDictionary(RecordRangeType.rc001).toArray(new Dictionary[0]);
			_status = "RC002";
			rc002 = homepageDao.getDictionary(RecordRangeType.rc002).toArray(new Dictionary[0]);
			_status = "RC003";
			rc003 = homepageDao.getDictionary(RecordRangeType.rc003).toArray(new Dictionary[0]);
			_status = "RC011";
			rc011 = homepageDao.getDictionary(RecordRangeType.rc011).toArray(new Dictionary[0]);
			_status = "RC013";
			rc013 = homepageDao.getDictionary(RecordRangeType.rc013).toArray(new Dictionary[0]);
			_status = "RC014_1";
			rc014_1 = homepageDao.getDictionary(RecordRangeType.rc014_1).toArray(new Dictionary[0]);
			_status = "RC014_2";
			rc014_2 = homepageDao.getDictionary(RecordRangeType.rc014_2).toArray(new Dictionary[0]);
			_status = "RC016";
			rc016 = homepageDao.getDictionary(RecordRangeType.rc016).toArray(new Dictionary[0]);
			_status = "RC019";
			rc019 = homepageDao.getDictionary(RecordRangeType.rc019).toArray(new Dictionary[0]);
			_status = "RC023";
			rc023 = homepageDao.getDictionary(RecordRangeType.rc023).toArray(new Dictionary[0]);
			_status = "RC026";
			rc026 = homepageDao.getDictionary(RecordRangeType.rc026).toArray(new Dictionary[0]);
			_status = "RC027";
			rc027 = homepageDao.getDictionary(RecordRangeType.rc027).toArray(new Dictionary[0]);
			_status = "RC028";
			rc028 = homepageDao.getDictionary(RecordRangeType.rc028).toArray(new Dictionary[0]);
			_status = "RC029";
			rc029 = homepageDao.getDictionary(RecordRangeType.rc029).toArray(new Dictionary[0]);
			_status = "RC030";
			rc030 = homepageDao.getDictionary(RecordRangeType.rc030).toArray(new Dictionary[0]);
			_status = "RC031";
			rc031 = homepageDao.getDictionary(RecordRangeType.rc031).toArray(new Dictionary[0]);
			_status = "RC032";
			rc032 = homepageDao.getDictionary(RecordRangeType.rc032).toArray(new Dictionary[0]);
			_status = "RC033";
			rc033 = homepageDao.getDictionary(RecordRangeType.rc033).toArray(new Dictionary[0]);
			_status = "RC035";
			rc035 = homepageDao.getDictionary(RecordRangeType.rc035).toArray(new Dictionary[0]);
			_status = "RC036";
			rc036 = homepageDao.getDictionary(RecordRangeType.rc036).toArray(new Dictionary[0]);
			_status = "RC037";
			rc037 = homepageDao.getDictionary(RecordRangeType.rc037).toArray(new Dictionary[0]);
			_status = "RC038";
			rc038 = homepageDao.getDictionary(RecordRangeType.rc038).toArray(new Dictionary[0]);
			_status = "RC039";
			rc039 = homepageDao.getDictionary(RecordRangeType.rc039).toArray(new Dictionary[0]);
			_status = "RC040";
			rc040 = homepageDao.getDictionary(RecordRangeType.rc040).toArray(new Dictionary[0]);

			_status = "行政区划";
			administrativeDivisionProv = homepageDao.getAdministrativeDivision(RecordRangeType.ADMINISTRATIVE_DIVISION_PROV).toArray(new DicAdministrativeDivision[0]);
			administrativeDivisionCity = homepageDao.getAdministrativeDivision(RecordRangeType.ADMINISTRATIVE_DIVISION_CITY).toArray(new DicAdministrativeDivision[0]);

			_status = "HIS诊断对照 - 中医诊断";
			hisDiagnosisChinese = homepageDao.getHISDiagnosis(RecordRangeType.DIAGNOSIS_CHINESE).toArray(new VIcdTransform[0]);
			_status = "HIS诊断对照 - 西医诊断";
			hisDiagnosisWestern = homepageDao.getHISDiagnosis(RecordRangeType.DIAGNOSIS_WESTERN).toArray(new VIcdTransform[0]);
			_status = "HIS诊断对照 - 手术诊断";
			hisDiagnosisOperation = homepageDao.getHISDiagnosis(RecordRangeType.DIAGNOSIS_OPERATION).toArray(new VIcdTransform[0]);
			_status = "HIS诊断对照 - 病理诊断";

			hisDiagnosisPathology = homepageDao.getHISDiagnosis(RecordRangeType.DIAGNOSIS_PATHOLOGY).toArray(new VIcdTransform[0]);
			_status = "ICD2.0 - 中医诊断";
			icd2DiagnosisChinese = homepageDao.getICD20Diagnosis(RecordRangeType.DIAGNOSIS_CHINESE).toArray(new IcdTransform[0]);
			_status = "ICD2.0 - 西医诊断";
			icd2DiagnosisWestern = homepageDao.getICD20Diagnosis(RecordRangeType.DIAGNOSIS_WESTERN).toArray(new IcdTransform[0]);
			_status = "ICD2.0 - 手术诊断";
			icd2DiagnosisOperation = homepageDao.getICD20Diagnosis(RecordRangeType.DIAGNOSIS_OPERATION).toArray(new IcdTransform[0]);
			_status = "ICD2.0 - 病理诊断";
			icd2DiagnosisPathology = homepageDao.getICD20Diagnosis(RecordRangeType.DIAGNOSIS_PATHOLOGY).toArray(new IcdTransform[0]);

			isInited = true;
		}
		catch (Exception ex) {
			new ErrorException(getClass(), "初始化字典数据[" + _status + "]时发生错误 - " + ex.getClass().getName() + " : " + ex.getMessage());
		}
	}
	@Override
	public void loadCSV(File[] csvFile) throws Exception {
		initialize();
		for(File file : csvFile) {
			String fileName = file.getName();
			String[] fileProperty = fileName.split("\\Q.\\E")[0].split("\\Q_\\E");
			if (fileProperty.length != 3) throw new ErrorException(this.getClass(), "病案首页质控模块在解析CSV文件名时发生错误：文件名格式错误[格式为“分类_注释_版本号或批次号”，例：TCMMS_201901-08_1]");
			String kind = fileProperty[0];
			String type = fileProperty[1];
			int version = Integer.parseInt(fileProperty[2]);
			CSVParser parser = null;
			String status = "";
			try {
				status = "解析CSV文件";
				parser = CSVParser.parse(new FileReader(file), CSVFormat.DEFAULT.withFirstRecordAsHeader());

				status = "处理数据";
				parser.forEach(rec -> importHomepage(rec, kind, type, version));
				status = "导入数据";
				homepageDao.importHomepagesAndChecks(homepageList.toArray(new InpatientHomepageAnaly[0]));

				status = "修正数据";
				List<InpatientHomepageAnaly> patchList = homepagePatch(homepageList.toArray(new InpatientHomepageAnaly[0]));
				status = "导入修正数据";
				homepageDao.importHomepagesAndChecks(patchList.toArray(new InpatientHomepageAnaly[0]));
			}
			catch(Exception ex) {
				new ErrorException(getClass(), "病案首页质控模块" + status + "时发生错误：" + ex.getMessage());
			}
			finally {
				parser.close();
			}
		}
	}

	@Override
	public void importHomepage(CSVRecord record, String homepageKind, String homepageType, int version) {
		initialize();
		InpatientHomepageAnaly homepage = new InpatientHomepageAnaly(this.homepageIndex ++, homepageKind, homepageType, version);
		homepage.setInpatientHomepageAnalyChecks(new ArrayList<InpatientHomepageAnalyCheck>());
		List<InpatientHomepageAnalyCheck> checkRecord = homepage.getInpatientHomepageAnalyChecks();
		String name = null, value = null;
		int checkIndex = checkRecord.size() + 1;
		int itemIndex = 0;
		try {
			Map<String, Integer> headNames = record.getParser().getHeaderMap();
			for (Method method : homepage.getClass().getMethods()) {
				String methodName = method.getName();
				if (!methodName.startsWith("set")) continue;
				name = method.getAnnotations().length == 0 ? methodName.substring(3).toLowerCase() : ((Column) method.getAnnotations()[0]).name();
				if (name.equals("id") || name.equals("kind") || name.equals("type") || name.equals("checked") || name.equals("version") || name.equals("flag") || name.startsWith("inpatienthomepageanalycheck")) continue;
				Class<?> type = method.getParameterTypes()[0];
				name = headNames.containsKey(name) ? name : name.toUpperCase();
				if (name.equalsIgnoreCase("ZZJGDM")) itemIndex = 0;
				else itemIndex = headNames.get(name);
				value = record.get(itemIndex);

				try {
					Object val = transValueType(name, value, type);
					method.invoke(homepage, val);
				}
				catch(Exception ex) {
					checkRecord.add(new InpatientHomepageAnalyCheck(checkIndex ++, homepage.getId(), name, null, value, ex.getMessage(), CHECK_TYPE_WARN));
				}
			}
			/***********************************/
			/******* 信息校验（包含逻辑校验）******/
			/**********************************/
			dataVerify(homepage);
		}
		catch(Exception ex) {
			new ErrorException(getClass(), "在处理第" + record.getRecordNumber() + "条第" + itemIndex + "项(" + name + ")数据时发生错误：" + ex.getMessage());
			checkRecord.add(new InpatientHomepageAnalyCheck(
					checkIndex ++,
					homepage.getId(),
					name,
					null,
					value,
					"处理数据时发生错误 [" + ex.getMessage() + "]",
					CHECK_TYPE_ERROR
				)
			);
		}
		homepage.setChecked(checkRecord.size());
		homepage.setFlag(null);
		homepageList.add(homepage);
	}

	@Override
	public Object transValueType(String name, String value, Class<?> type) throws Exception {
		initialize();
		Object result = value;

		/***************************/
		/******* 数据类型转换 ********/ 
		/**************************/
		if (type.equals(Double.class)) {
			Double val = null;
			try {
				val = value.isEmpty() ? null : Double.parseDouble(value);
			}
			catch(NumberFormatException e) {
				throw new Exception("不是合法的数值" + e.getMessage());
			}
			result = val;
		} else if (type.equals(Integer.class)) {
			Integer val = null;
			try {
				val = value.isEmpty() ? null : Integer.parseInt(value);
			}
			catch(NumberFormatException e) {
				throw new Exception("不是合法的整数" + e.getMessage());
			}
			result = val;
		} else if (type.equals(Short.class)) {
			Short val = null;
			try {
				val = value.isEmpty() ? null : Short.parseShort(value);
			}
			catch(NumberFormatException e) {
				throw new Exception("不是合法的整数" + e.getMessage());
			}
			result = val;
		} else if (type.equals(Date.class)) {
			Date val = null;
			try {
				val = value.isEmpty() ? null : FormatUtil.parseDate(value);
			}
			catch(ParseException e) {
				throw new Exception("不是合法的日期" + e.getMessage());
			}
			result = val;
		} else if (type.equals(Timestamp.class)) {
			Timestamp val = null;
			try {
				val = value.isEmpty() ? null : new Timestamp(FormatUtil.parseDateTime(value).getTime());
			}
			catch(ParseException e) {
				throw new Exception("不是合法的时间" + e.getMessage());
			}
			result = val;
		}

		return result;
	}

	@Override
	public void dataVerify(InpatientHomepageAnaly homepage) throws Exception {
		initialize();
		List<InpatientHomepageAnalyCheck> checkRecord = homepage.getInpatientHomepageAnalyChecks();
		int checkIndex = checkRecord.size() + 1;

		boolean hasSs = false;
		boolean hasMz = false;
		String status = "";

		/***************************************************************************************/
		/******* 病案首页数据逻辑检查，依据《三级公立中医医院病案首页数据采集接口标准与质量要求》。 ********/ 
		/***************************************************************************************/
		try {
			status = "1. ZZJGDM 组织机构代码 字符 22 必填 指医疗机构执业许可证上面的机构代码。";
			if (homepage.getZzjgdm().isEmpty() || isEmpty(homepage.getZzjgdm())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZZJGDM",
						"组织机构代码",
						homepage.getZzjgdm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (homepage.getZzjgdm().length() != 18) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZZJGDM",
						"组织机构代码",
						homepage.getZzjgdm(),
						"位数不正确(" + homepage.getZzjgdm().length() + ")，应为18位",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "2. JGMC 医疗机构名称 字符 80 必填";
			if (homepage.getJgmc().isEmpty() || isEmpty(homepage.getJgmc())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JGMC",
						"组织机构名称",
						homepage.getZzjgdm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "3. USERNAME 对应的系统登录用户名 字符 10 必填 医院名称或者编码";
			if (homepage.getUsername().isEmpty() || isEmpty(homepage.getUsername())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"USERNAME",
						"系统登录用户名",
						homepage.getUsername(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "4. YLFKFS 医疗付款方式 字符 3 必填 《值域范围参考RC032-医疗付费方式代码》";
			if (homepage.getYlfkfs().isEmpty() || isEmpty(homepage.getYlfkfs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YLFKFS",
						"医疗付款方式",
						homepage.getYlfkfs(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc032, RecordRangeType.code, homepage.getYlfkfs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YLFKFS",
						"医疗付款方式",
						homepage.getYlfkfs(),
						"代码不正确，《值域范围参考RC032-医疗付费方式代码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "5. JKKH 健康卡号 字符 50 在已统一发放“中华人民共和国居民健康卡”的地区填写健康卡号码，尚未发放“健康卡”的地区填写“-”";
			if (homepage.getJkkh().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JKKH",
						"医疗付款方式",
						homepage.getJkkh(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "6. ZYCS 住院次数 数字 4 必填 大于0的整数";
			if (homepage.getZycs().isEmpty() || isEmpty(homepage.getZycs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYCS",
						"住院次数",
						homepage.getZycs(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isInteger(homepage.getZycs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYCS",
						"住院次数",
						homepage.getZycs(),
						"必须是大于0的整数",
						CHECK_TYPE_VALIDITY
					));
			} else if (getInteger(homepage.getZycs()) <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYCS",
						"住院次数",
						homepage.getZycs(),
						"必须大于0",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "7. BAH 病案号 字符 50 必填";
			if (homepage.getBah().isEmpty() || isEmpty(homepage.getBah())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"BAH",
						"病案号",
						homepage.getBah(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "8. XM 姓名 字符 40 必填";
			if (homepage.getXm().isEmpty() || isEmpty(homepage.getXm())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XM",
						"姓名",
						homepage.getXm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "9. XB 性别 数字 1 必填 《值域范围参考RC001-性别代码》";
			if (homepage.getXb().isEmpty() || isEmpty(homepage.getXb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XB",
						"性别",
						homepage.getXb(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc001, RecordRangeType.code, homepage.getXb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XB",
						"性别",
						homepage.getXb(),
						"代码不正确，《值域范围参考RC001-性别代码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "10. CSRQ 出生日期 日期 10 必填 yyyy-mm-dd";
			if (homepage.getCsrq().isEmpty() || isEmpty(homepage.getCsrq())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CSRQ",
						"出生日期",
						homepage.getCsrq(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isDate(homepage.getCsrq())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CSRQ",
						"出生日期",
						homepage.getCsrq(),
						"格式不正确，yyyy-MM-dd",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "11. NL 年龄（岁） 数字 3 必填 患者入院年龄，指患者入院时按照日历计算的历法年龄，应以实足年龄的相应整数填写。大于或等于0的整数";
			Integer _age = getInteger(homepage.getNl());
			if (homepage.getNl().isEmpty() || isEmpty(homepage.getNl())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"NL",
						"年龄（岁）",
						homepage.getNl(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isInteger(homepage.getNl())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"NL",
						"年龄（岁）",
						homepage.getNl(),
						"不是数字",
						CHECK_TYPE_VALIDITY
					));
			} else if (_age < 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"NL",
						"年龄（岁）",
						homepage.getNl(),
						"必须是大于或等于0的整数",
						CHECK_TYPE_VALIDITY
					));
			} else if (isDatetime(homepage.getRysj()) && isDate(homepage.getCsrq())) {
				int age = getAge(homepage.getRysj(), homepage.getCsrq(), homepage.getSfzh(), 0);
				if (age != _age) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"NL",
							"年龄（岁）",
							homepage.getNl(),
							"与实际年龄（" + age + "）不符",
							CHECK_TYPE_LOGICAL
						));
				}
			}
			status = "12. GJ 国籍 字符 40 必填 《值域范围参考RC038-国籍字典》";
			if (homepage.getGj().isEmpty() || isEmpty(homepage.getGj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GJ",
						"国籍",
						homepage.getGj(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc038, RecordRangeType.code, homepage.getGj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GJ",
						"国籍",
						homepage.getGj(),
						"代码不正确，《值域范围参考RC038-国籍字典》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "13. BZYZS_NL 年龄不足1周岁的年龄（天） 数字 3 条件必填 新生儿病例";
			if (_age == 0) {
				if (homepage.getBzyzsNl().isEmpty() || isEmpty(homepage.getBzyzsNl())) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"BZYZS_NL",
							"年龄不足1周岁的年龄（天）",
							homepage.getBzyzsNl(),
							"不能为空",
							CHECK_TYPE_CONDITIONAL_NONEMPTY
						));
				} else if(!isFloat(homepage.getBzyzsNl())) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"BZYZS_NL",
							"年龄不足1周岁的年龄（天）",
							homepage.getBzyzsNl(),
							"必须为数字",
							CHECK_TYPE_VALIDITY
						));
				}
			}
			status = "14. XSETZ 新生儿出生体重(克) 数字 6 条件必填 测量新生儿体重要求精确到10克；应在活产后一小时内称取重量。；1、产妇和新生儿病案填写，从出生到28天为新生儿期，双胎及以上不同胎儿体重则继续填写下面的新生儿出生体重。；2、新生儿体重范围：100克-9999克，产妇的主要诊断或其他诊断编码中含有Z37.0,Z37.2, Z37.3, Z37.5, Z37.6编码时，不能为空新生儿出生体重";
	//		TODO 新生儿体重
	//		if () {
	//			
	//		}
			status = "15. XSETZ2 新生儿出生体重(克)2 数字 6 新生儿体重范围：100克-9999克；"
					+ "16. XSETZ3 新生儿出生体重(克)3 数字 6 新生儿体重范围：100克-9999克；"
					+ "17. XSETZ4 新生儿出生体重(克)4 数字 6 新生儿体重范围：100克-9999克；"
					+ "18. XSETZ5 新生儿出生体重(克)5 数字 6 新生儿体重范围：100克-9999克";
			if (_age == 0) {
				for (int i = 2; i <= 5; i ++) {
					String value = homepage.getClass().getMethod("getXsetz" + i, new Class[0]).invoke(homepage, new Object[0]).toString();
					if (!value.isEmpty() && !isFloat(value)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"XSETZ" + i,
								"新生儿出生体重(克)" + i,
								value,
								"必须为数字",
								CHECK_TYPE_VALIDITY
							));
					}
				}
			}
			status = "19. XSERYTZ 新生儿入院体重（克） 数字 6 条件必填 100克-9999克，精确到10克；新生儿入院当日的体重；小于等于28天的新生儿必填，填写了新生儿入出院体重的，不能为空年龄不足1周岁的年龄（天），且必须小于等于28天。";
			// TODO 新生儿入院体重
	//		if () {
	//			
	//		}
			status = "20. CSD 出生地 字符 200 必填 例如：陕西省商洛市商南县金丝峡镇梁家湾村58号，必填到省或自治区。";
			if (homepage.getCsd().isEmpty() || isEmpty(homepage.getCsd())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CSD",
						"出生地",
						homepage.getCsd(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc036, RecordRangeType.name, homepage.getCsd())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CSD",
						"不能为空",
						homepage.getCsd(),
						"必须以省或自治区开始",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "21. GG 籍贯 字符 50 必填 《值域范围参考RC036-籍贯》";
			if (homepage.getGg().isEmpty() || isEmpty(homepage.getGg())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GG",
						"籍贯",
						homepage.getGg(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc036, RecordRangeType.code, homepage.getGg())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GG",
						"籍贯",
						homepage.getGg(),
						"代码不正确，《值域范围参考RC036-籍贯》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "22. MZ 民族 字符 2 必填 《值域范围参考RC035-民族代码》";
			if (homepage.getMz().isEmpty() || isEmpty(homepage.getMz())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"MZ",
						"民族",
						homepage.getMz(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc035, RecordRangeType.code, homepage.getMz())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"MZ",
						"民族",
						homepage.getMz(),
						"代码不正确，《值域范围参考RC035-民族代码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "23. SFZH 身份证号 字符 18 必填 住院患者入院时要如实填写15位或18位身份证号码";
			if (homepage.getSfzh().isEmpty() || isEmpty(homepage.getSfzh())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SFZH",
						"身份证号",
						homepage.getSfzh(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (isInteger(homepage.getSfzh()) && homepage.getSfzh().length() != 15 && homepage.getSfzh().length() != 18) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SFZH",
						"身份证号",
						homepage.getSfzh(),
						"代码不正确，必须是15位或18位",
						CHECK_TYPE_VALIDITY
					));
			} else if (homepage.getSfzh().length() == 18 && isDate(homepage.getCsrq())) {
				status = "逻辑校验 - 身份证号出生日期与登记出生日期校验";
				DateFormat df = new SimpleDateFormat("yyyyMMdd");
				Date birthDay = df.parse(homepage.getSfzh().substring(6, 14));
				if (birthDay.getTime() != FormatUtil.parseDate(homepage.getCsrq()).getTime()) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SFZH",
						"身份证号",
						homepage.getSfzh(),
						"身份证出生日期与出生日期(CSRQ)不符(" + FormatUtil.DATE_FORMAT.format(birthDay) + "≠" + homepage.getCsrq() + ")",
						CHECK_TYPE_LOGICAL
					));
				}
			}
			status = "24. ZY 职业 字符 2 必填 《值域范围参考RC003-职业代码》";
			if (homepage.getZy().isEmpty() || isEmpty(homepage.getZy())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZY",
						"职业",
						homepage.getZy(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc003, RecordRangeType.code, homepage.getZy())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZY",
						"职业",
						homepage.getZy(),
						"代码不正确，《值域范围参考RC003-职业代码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "25. HY 婚姻 字符 1 必填 《值域范围参考RC002-婚姻代码》";
			if (homepage.getHy().isEmpty() || isEmpty(homepage.getHy())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"HY",
						"婚姻",
						homepage.getHy(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc002, RecordRangeType.code, homepage.getHy())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"HY",
						"婚姻",
						homepage.getHy(),
						"代码不正确，《值域范围参考RC002-婚姻代码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "26. XZZ 现住址 字符 200 必填 例如：陕西省商洛市商南县金丝峡镇梁家湾村58号，必填到省或自治区。";
			if (homepage.getXzz().isEmpty() || isEmpty(homepage.getXzz())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XZZ",
						"现住址",
						homepage.getXzz(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isEmpty(homepage.getXzz()) && !compareDic(rc036, RecordRangeType.name, homepage.getXzz())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XZZ",
						"现住址",
						homepage.getXzz(),
						"必须以省或自治区开始",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "27. DH 现住址电话 字符 40 必填";
			if (homepage.getDh().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"DH",
						"现住址电话",
						homepage.getDh(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "28. YB1 现住址邮政编码 字符 6 必填 6位数字";
			if (homepage.getYb1().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YB1",
						"现住址邮政编码",
						homepage.getYb1(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "29. HKDZ 户口地址 字符 200 必填 例如：陕西省商洛市商南县金丝峡镇梁家湾村58号，必填到省或自治区。";
			if (homepage.getHkdz().isEmpty() || isEmpty(homepage.getHkdz())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"HKDZ",
						"户口地址",
						homepage.getHkdz(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isEmpty(homepage.getHkdz()) && !compareDic(rc036, RecordRangeType.name, homepage.getHkdz())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"HKDZ",
						"户口地址",
						homepage.getHkdz(),
						"必须以省或自治区开始",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "30. YB2 户口地址邮政编码 字符 6 必填 6位数字";
			if (homepage.getYb2().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YB2",
						"户口地址邮政编码",
						homepage.getYb2(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "31. GZDWJDZ 工作单位及地址 字符 200 必填 例如：陕西省商洛市商南县金丝峡镇梁家湾村58号，必填到省或自治区。";
			if (homepage.getGzdwjdz().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GZDWJDZ",
						"工作单位及地址",
						homepage.getGzdwjdz(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if ((homepage.getZy().equals("11")//国家公务员
					|| homepage.getZy().equals("13")//专业技术人员
					|| homepage.getZy().equals("17")//职员
					|| homepage.getZy().equals("21")//企业管理人员
					|| homepage.getZy().equals("24")//工人
					|| homepage.getZy().equals("37"))//现役军人
					&& !compareDic(rc036, RecordRangeType.name, homepage.getGzdwjdz())) {
				status = "逻辑校验 - 根据职业验证工作单位";
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GZDWJDZ",
						"工作单位及地址",
						homepage.getGzdwjdz(),
						"职业(ZY)为“11(国家公务员)、13(专业技术人员)、17(职员)、21(企业管理人员)、24(工人)、37(现役军人)”时，必须登记工作单位及地址(GZDWJDZ)，且以省或自治区开始",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "32. DWDH 工作单位电话 字符 20 必填";
			if (homepage.getDwdh().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"DWDH",
						"工作单位电话",
						homepage.getDwdh(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "33. YB3 工作单位邮政编码 字符 6 必填 6位数字";
			if (homepage.getYb3().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YB3",
						"工作单位邮政编码",
						homepage.getYb3(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "34. LXRXM 联系人姓名 字符 40 必填";
			if (homepage.getLxrxm().isEmpty() || isEmpty(homepage.getLxrxm())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"LXRXM",
						"联系人姓名",
						homepage.getLxrxm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "35. GX 联系人关系 字符 1 必填 《值域范围参考RC033-联系人关系代码》";
			if (homepage.getGx().isEmpty() || isEmpty(homepage.getGx())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GX",
						"联系人关系",
						homepage.getGx(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc033, RecordRangeType.code, homepage.getGx())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GX",
						"联系人关系",
						homepage.getGx(),
						"代码不正确，《值域范围参考RC033-联系人关系代码》",
						CHECK_TYPE_VALIDITY
					));
			} else if (homepage.getGx().equals("1")//配偶
						&& !homepage.getHy().equals("2")/*已婚*/) {
				status = "逻辑校验 - 联系人关系为“配偶”时，婚姻状况应该是“已婚”";
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GX",
						"联系人关系",
						homepage.getGx(),
						"为“配偶”时，婚姻状况(HY)应该是“已婚”",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "36. DZ 联系人地址 字符 200 必填 例如：陕西省商洛市商南县金丝峡镇梁家湾村58号，必填到省或自治区。";
			if (homepage.getDz().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"DZ",
						"联系人地址",
						homepage.getDz(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isEmpty(homepage.getGzdwjdz()) && !compareDic(rc036, RecordRangeType.name, homepage.getDz())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"DZ",
						"联系人地址",
						homepage.getDz(),
						"必须以省或自治区开始",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "37. DH1 联系人电话 字符 40 必填";
			if (homepage.getDh1().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"DH1",
						"联系人电话",
						homepage.getDh1(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "38. RYTJ 入院途径 字符 1 必填 《值域范围参考RC026-入院途径代码》";
			if (homepage.getRytj().isEmpty() || isEmpty(homepage.getRytj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYTJ",
						"入院途径",
						homepage.getRytj(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc026, RecordRangeType.code, homepage.getRytj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYTJ",
						"入院途径",
						homepage.getRytj(),
						"代码不正确，《值域范围参考RC026-入院途径代码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "39. ZLLB 治疗类别 字符 3 《值域范围参考RC039-治疗类别字典》";
			if (!homepage.getZllb().isEmpty() && !isEmpty(homepage.getZllb()) && !compareDic(rc039, RecordRangeType.code, homepage.getZllb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZLLB",
						"治疗类别",
						homepage.getZllb(),
						"代码不正确，《值域范围参考RC039-治疗类别字典》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "40. RYSJ 入院时间 日期 必填 格式 yyyy-MM-dd HH:mm:ss；入院时间不能晚于出院时间";
			if (homepage.getRysj().isEmpty() || isEmpty(homepage.getRysj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYSJ",
						"入院时间",
						homepage.getRysj(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
						));
			} else if (!isDatetime(homepage.getRysj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYSJ",
						"入院时间",
						homepage.getRysj(),
						"格式不正确，yyyy-MM-dd HH:mm:ss",
						CHECK_TYPE_VALIDITY
						));
			}
			status = "41. RYSJ_S 时 必填 24小时制";
			if (homepage.getRysjS().isEmpty() || isEmpty(homepage.getRysjS())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYSJ_S",
						"入院时间（时）",
						homepage.getRysjS(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
						));
			} else if (!isInteger(homepage.getRysjS())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYSJ_S",
						"入院时间（时）",
						homepage.getRysjS(),
						"格式不正确，小时数（24小时制）",
						CHECK_TYPE_VALIDITY
						));
			} else {
				if (isDatetime(homepage.getRysj())) {
					Calendar _rysj = Calendar.getInstance();
					_rysj.setTime(FormatUtil.parseDateTime(homepage.getRysj()));
					int _hour = _rysj.get(Calendar.HOUR_OF_DAY);
					if (_hour != getInteger(homepage.getRysjS())) {
								checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"RYSJ_S",
								"入院时间（时）",
								homepage.getRysjS(),
								"与入院时间(RYSJ)不匹配",
								CHECK_TYPE_LOGICAL
								));
					}
				}
			}
			status = "42. RYKB 入院科别 字符 6 必填 《值域范围参考RC023-科室代码》";
			if (homepage.getRykb().isEmpty() || isEmpty(homepage.getRykb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYKB",
						"入院科别",
						homepage.getRykb(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc023, RecordRangeType.code, homepage.getRykb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYKB",
						"入院科别",
						homepage.getRykb(),
						"代码不正确，《值域范围参考RC023-科室代码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "43. RYBF 入院病房 字符 30 必填";
			if (homepage.getRybf().isEmpty() || isEmpty(homepage.getRybf())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RYBF",
						"入院病房",
						homepage.getRybf(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "44. ZKKB 转科科别 集合 可以多选 《值域范围参考RC023-科室代码》";
			if (!homepage.getZkkb().isEmpty() || !isEmpty(homepage.getZkkb()) && homepage.getZkkb().length() > 1) {
				String[] _zkkbs = homepage.getZkkb().split("\\Q,\\E");
				for (String _zkkb : _zkkbs) {
					if (!compareDic(rc023, RecordRangeType.code, _zkkb.trim())) {
								checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"ZKKB",
								"转科科别",
								homepage.getZkkb(),
								"代码不正确，《值域范围参考RC023-科室代码》",
								CHECK_TYPE_NONEMPTY
							));
					}
				}
			}
			status = "45. CYSJ 出院时间 日期 必填 格式 yyyy-MM-dd HH:mm:ss；入院时间不能晚于出院时间";
			if (homepage.getCysj().isEmpty() || isEmpty(homepage.getCysj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CYSJ",
						"出院时间",
						homepage.getCysj(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
						));
			} else if (!isDatetime(homepage.getCysj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CYSJ",
						"出院时间",
						homepage.getCysj(),
						"格式不正确，yyyy-MM-dd HH:mm:ss",
						CHECK_TYPE_VALIDITY
						));
			}
			// 出入院时间逻辑校验
			if (isDatetime(homepage.getRysj())
				&& isDatetime(homepage.getCysj())
				&& FormatUtil.parseDateTime(homepage.getRysj()).getTime() > FormatUtil.parseDateTime(homepage.getCysj()).getTime()) {
	
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CYSJ",
						"出院时间",
						homepage.getCysj(),
						"小于入院时间(RYSJ)",
						CHECK_TYPE_LOGICAL
						));
			}
			status = "46. CYSJ_S 时 必填";
			if (homepage.getCysjS().isEmpty() || isEmpty(homepage.getCysjS())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CYSJ_S",
						"出院时间（时）",
						homepage.getCysjS(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
						));
			} else if (!isInteger(homepage.getCysjS())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CYSJ_S",
						"出院时间（时）",
						homepage.getCysjS(),
						"格式不正确，小时数（24小时制）",
						CHECK_TYPE_VALIDITY
						));
			} else {
				if (isDatetime(homepage.getCysj())) {
					Calendar _cysj = Calendar.getInstance();
					_cysj.setTime(FormatUtil.parseDateTime(homepage.getCysj()));
					int _hour = _cysj.get(Calendar.HOUR_OF_DAY);
					if (_hour != getInteger(homepage.getCysjS())) {
								checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"CYSJ_S",
								"出院时间（时）",
								homepage.getCysjS(),
								"与出院时间(CYSJ)不匹配",
								CHECK_TYPE_LOGICAL
								));
					}
				}
			}
			status = "47. CYKB 出院科别 字符 6 必填 《值域范围参考RC023-科室代码》；国家重点专科科室必须有数据。";
			if (homepage.getCykb().isEmpty() || isEmpty(homepage.getCykb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CYKB",
						"出院科别",
						homepage.getCykb(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc023, RecordRangeType.code, homepage.getCykb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CYKB",
						"出院科别",
						homepage.getCykb(),
						"代码不正确，《值域范围参考RC023-科室代码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "48. CYBF 出院病房 字符 30 必填";
			if (homepage.getCybf().isEmpty() || isEmpty(homepage.getCybf())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"CYBF",
						"出院病房",
						homepage.getCybf(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "49. SJZY 实际住院(天) 数字 6 必填 大于0整数。入院时间与出院时间只计算一天，例如：2018年6月12日入院，2018年6月15日出院，计住院天数为3天";
			if (homepage.getSjzy().isEmpty() || isEmpty(homepage.getSjzy())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SJZY",
						"实际住院(天)",
						homepage.getSjzy(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isInteger(homepage.getSjzy()) || getInteger(homepage.getSjzy()) <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SJZY",
						"实际住院(天)",
						homepage.getSjzy(),
						"格式或天数不正确，必需大于0的整数",
						CHECK_TYPE_VALIDITY
					));
			} else if (isDatetime(homepage.getCysj()) && isDatetime(homepage.getRysj())) {
				long _cysj = FormatUtil.parseDateTime(homepage.getCysj()).getTime();
				long _rysj = FormatUtil.parseDateTime(homepage.getRysj()).getTime();
				int _sjzy = Integer.parseInt(((_cysj - _rysj)/86400000 + 1)+"");
				_sjzy = _sjzy == 0 ? 1 : _sjzy;
				if (_sjzy != getInteger(homepage.getSjzy())) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"SJZY",
							"实际住院(天)",
							homepage.getSjzy(),
							"与实际出、入院时间计算不符(" + _sjzy + ")",
							CHECK_TYPE_LOGICAL
						));
				}
			}
			status = "50. MZD_ZYZD 门(急)诊诊断(中医诊断) 字符 100 必填 采用中医病症分类代码国标版95（TCM95）与编码对应的病诊断名称；"
						+ "51. JBDM 门(急)诊诊断疾病代码(中医诊断) 字符 20 必填 采用中医病症分类代码国标版95（TCM95）";
			if (homepage.getJbdm().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JBDM",
						"门(急)诊诊断疾病代码(中医诊断)",
						homepage.getJbdm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isEmpty(homepage.getJbdm()) && !diagnosisVerify(homepage.getJbdm(), homepage.getMzdZyzd(), RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_CHINESE)) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JBDM",
						"门(急)诊诊断疾病代码(中医诊断)",
						homepage.getJbdm(),
						"与《中医病症分类代码国标版95（TCM95）》不匹配",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "52. MZZD_XYZD 门（急）诊诊断名称(西医诊断) 字符 100 必填 采用疾病分类代码国家临床版2.0(ICD-10)与编码对应的诊断名称；"
						+ "53. JBBM 门（急）诊诊断编码(西医诊断) 字符 20 必填 采用疾病分类代码国家临床版2.0编码（ICD-10）";
			if (homepage.getJbbm().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JBBM",
						"门（急）诊诊断编码(西医诊断)",
						homepage.getJbbm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isEmpty(homepage.getJbbm()) && !diagnosisVerify(homepage.getJbbm(), homepage.getMzzdXyzd(), RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_WESTERN)) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JBBM",
						"门（急）诊诊断编码(西医诊断)",
						homepage.getJbbm(),
						"与《疾病分类代码国家临床版2.0编码（ICD-10）》不匹配",
						CHECK_TYPE_VALIDITY
					));
			}
			//门（急）诊断逻辑校验
			if (!homepage.getRytj().isEmpty()
				&& (homepage.getRytj().equals("1")//门诊
					|| homepage.getRytj().equals("2"))//急诊
				&& (homepage.getJbbm().isEmpty() || isEmpty(homepage.getJbbm()))
				&& (homepage.getJbdm().isEmpty() || isEmpty(homepage.getJbdm()))) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JBBM",
						"门（急）诊诊断编码(西医诊断)",
						homepage.getJbbm(),
						"入院途径为门（急）诊(RYTJ)时，门（急）诊中医(JBDM)或西医诊断(JBBM)至少一项不能为空",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "54. SSLCLJ 实施临床路径 字符 1 必填 《值域范围参考RC040-实施临床路径字典》";
			if (homepage.getSslclj().isEmpty() || isEmpty(homepage.getSslclj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SSLCLJ",
						"实施临床路径",
						homepage.getSslclj(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc040, RecordRangeType.code, homepage.getSslclj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SSLCLJ",
						"实施临床路径",
						homepage.getSslclj(),
						"代码不正确，《值域范围参考RC040-实施临床路径字典》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "55. ZYYJ 使用医疗机构中药制剂 字符 1 必填 《值域范围参考RC016》；参照卫统4-2病案首页值域；当值为“是”时，医疗机构中药制剂费>0";
			if (homepage.getZyyj().isEmpty() || isEmpty(homepage.getZyyj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYYJ",
						"使用医疗机构中药制剂",
						homepage.getZyyj(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc016, RecordRangeType.code, homepage.getZyyj())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYYJ",
						"使用医疗机构中药制剂",
						homepage.getZyyj(),
						"代码不正确，《值域范围参考RC016》",
						CHECK_TYPE_VALIDITY
					));
			}
			// 诊断逻辑校验
			if (!homepage.getZyyj().isEmpty() && compareDic(rc016, RecordRangeType.code, homepage.getZyyj())) {
				if (homepage.getZyyj().equals("1") && homepage.getZyzjf() <= 0) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYYJ",
						"使用医疗机构中药制剂",
						homepage.getZyyj(),
						"当值为“是”时，医疗机构中药制剂费(ZYZJF)>0",
						CHECK_TYPE_LOGICAL
					));
				} else if (homepage.getZyzjf() > 0 && !homepage.getZyyj().equals("1")) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYYJ",
						"使用医疗机构中药制剂",
						homepage.getZyyj(),
						"医疗机构中药制剂费(ZYZJF)>0，值应为“是”",
						CHECK_TYPE_LOGICAL
					));
				}
			}
			status = "56. ZYZLSB 使用中医诊疗设备 字符 1 必填 《值域范围参考RC016》；参照卫统4-2病案首页值域";
			if (homepage.getZyzlsb().isEmpty() || isEmpty(homepage.getZyzlsb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZLSB",
						"使用中医诊疗设备",
						homepage.getZyzlsb(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc016, RecordRangeType.code, homepage.getZyzlsb())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZLSB",
						"使用中医诊疗设备",
						homepage.getZyzlsb(),
						"代码不正确，《值域范围参考RC016》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "57. ZYZLJS 使用中医诊疗技术 字符 1 必填 《值域范围参考RC016》；参照卫统4-2病案首页值域；中医类（中医和民族医医疗服务）费>0";
			if (homepage.getZyzljs().isEmpty() || isEmpty(homepage.getZyzljs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZLJS",
						"使用中医诊疗技术",
						homepage.getZyzljs(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc016, RecordRangeType.code, homepage.getZyzljs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZLJS",
						"使用中医诊疗技术",
						homepage.getZyzljs(),
						"代码不正确，《值域范围参考RC016》",
						CHECK_TYPE_VALIDITY
					));
			} else if (homepage.getZyzljs().equals("1") && (homepage.getZylZyzd()+homepage.getZyzl()+homepage.getZyqt()) <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZLJS",
						"使用中医诊疗技术",
						homepage.getZyzljs(),
						"使用中医诊疗技术，中医类（中医和民族医医疗服务）费应大于0",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "58. BZSH 辩证施护 字符 1 必填 《值域范围参考RC016》；参照卫统4-2病案首页值域";
			if (homepage.getBzsh().isEmpty() || isEmpty(homepage.getBzsh())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"BZSH",
						"辩证施护",
						homepage.getBzsh(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc016, RecordRangeType.code, homepage.getBzsh())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"BZSH",
						"辩证施护",
						homepage.getBzsh(),
						"代码不正确，《值域范围参考RC016》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "59. ZB 主病出院中医诊断 字符 100 必填 采用中医病症分类代码国标版95（TCM95）与编码对应的病诊断名称；"
						+ "60. ZB_JBBM 主病疾病编码 字符 20 必填 采用中医病症分类代码国标版95（TCM95）";
			if (homepage.getZbJbbm().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZB_JBBM",
						"主病疾病编码",
						homepage.getZbJbbm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!isEmpty(homepage.getZbJbbm()) && !diagnosisVerify(homepage.getZbJbbm(), homepage.getMzdZyzd(), RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_CHINESE)) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZB_JBBM",
						"主病疾病编码",
						homepage.getZbJbbm(),
						"与《中医病症分类代码国标版95（TCM95）》不匹配",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "61. ZB_RYBQ 主病入院病情 字符 1 必填 《值域范围参考RC027-入院病情》";
			if (homepage.getZbRybq().isEmpty() || isEmpty(homepage.getZbRybq())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZB_RYBQ",
						"主病入院病情",
						homepage.getZbRybq(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc027, RecordRangeType.code, homepage.getZbRybq())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZB_RYBQ",
						"主病入院病情",
						homepage.getZbRybq(),
						"代码不正确，《值域范围参考RC027-入院病情》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "62. ZYZD 出院主要诊断名称(西医) 字符 100 必填 采用疾病分类代码国家临床版2.0(ICD-10)与编码对应的诊断名称；"
						+ "63. ZYZD_JBBM 出院主要诊断编码(西医) 字符 20 必填 采用疾病分类代码国家临床版2.0编码（ICD-10）";
			if (homepage.getZyzdJbbm().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZD_JBBM",
						"出院主要诊断编码(西医)",
						homepage.getZyzdJbbm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (isEmpty(homepage.getZyzdJbbm()) && !diagnosisVerify(homepage.getZyzdJbbm(), homepage.getMzdZyzd(), RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_CHINESE)) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZD_JBBM",
						"出院主要诊断编码(西医)",
						homepage.getZyzdJbbm(),
						"与《疾病分类代码国家临床版2.0编码（ICD-10）》不匹配",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "64. XY_RYBQ 出院主要诊断入院病情(西医) 字符 1 必填 《值域范围参考RC027-入院病情》";
			if (homepage.getXyRybq().isEmpty() || isEmpty(homepage.getXyRybq())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XY_RYBQ",
						"出院主要诊断入院病情(西医)",
						homepage.getXyRybq(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc027, RecordRangeType.code, homepage.getXyRybq())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XY_RYBQ",
						"出院主要诊断入院病情(西医)",
						homepage.getXyRybq(),
						"代码不正确，《值域范围参考RC027-入院病情》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "65. ZZ1-ZZ7 主证出院中医诊断 字符 100 必填 最多采集7条；采用中医病症分类代码国标版95（TCM95）与编码对应的病诊断名称；"
					+ "66. ZZ_JBBM1-ZZ_JBBM7 主证疾病编码 字符 20 必填 最多采集7条；采用中医病症分类代码国标版95（TCM95）；"
					+ "67. ZZ_RYBQ1-ZZ_RYBQ7 主证住入院病情 字符 1 必填 最多采集7条；《值域范围参考RC027-入院病情》";
			for (int i = 1; i <= 7; i ++) {
				String zzValue = homepage.getClass().getMethod("getZz" + i, new Class[0])
								 .invoke(homepage, new Object[0]).toString();
				String jbbmValue = homepage.getClass().getMethod("getZzJbbm" + i, new Class[0])
								   .invoke(homepage, new Object[0]).toString();
				String rybqValue = homepage.getClass().getMethod("getZzRybq" + i, new Class[0])
								   .invoke(homepage, new Object[0]).toString();
				if (!jbbmValue.isEmpty() && !isEmpty(jbbmValue)) {
					if (!diagnosisVerify(jbbmValue, zzValue, RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_CHINESE)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"ZZ_JBBM" + i,
								"主证疾病编码" + i,
								jbbmValue,
								"与《中医病症分类代码国标版95（TCM95）》不匹配",
								CHECK_TYPE_VALIDITY
							));
					}
					if (rybqValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"ZZ_RYBQ" + i,
								"主证住入院病情" + i,
								rybqValue,
								"不能为空" + i,
								CHECK_TYPE_NONEMPTY
							));
					} else if (!compareDic(rc027, RecordRangeType.code, rybqValue)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"ZZ_RYBQ" + i,
								"主证住入院病情" + i,
								rybqValue,
								"代码不正确，《值域范围参考RC027-入院病情》",
								CHECK_TYPE_VALIDITY
							));
					}
				}
			}
			status = "68. QTZD1-QTZD40 出院其他诊断名称(西医) 字符 100 最多收集40条；采用疾病分类代码国家临床版2.0(ICD-10)与编码对应的诊断名称；"
					+ "69. ZYZD_JBBM1-ZYZD_JBBM40 出院其他诊断编码(西医) 字符 20 最多收集40条；采用疾病分类代码国家临床版2.0编码（ICD-10）；"
					+ "70. RYBQ1-RYBQ40 出院其他诊断入院病情(西医) 字符 1 《值域范围参考RC027-入院病情》";
			for (int i = 1; i <= 40; i ++) {
				String qtzdValue = homepage.getClass().getMethod("getQtzd" + i, new Class[0])
								 .invoke(homepage, new Object[0]).toString();
				String jbbmValue = homepage.getClass().getMethod("getZyzdJbbm" + i, new Class[0])
								   .invoke(homepage, new Object[0]).toString();
				String rybqValue = homepage.getClass().getMethod("getRybq" + i, new Class[0])
								   .invoke(homepage, new Object[0]).toString();
				if (!jbbmValue.isEmpty() || !isEmpty(jbbmValue)) {
					if (!diagnosisVerify(jbbmValue, qtzdValue, RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_WESTERN)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"ZYZD_JBBM" + i,
								"出院其他诊断编码(西医)" + i,
								jbbmValue,
								"与《疾病分类代码国家临床版2.0(ICD-10)》不匹配",
								CHECK_TYPE_VALIDITY
							));
					}
					if (rybqValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"RYBQ" + i,
								"出院其他诊断入院病情(西医)" + i,
								rybqValue,
								"不能为空" + i,
								CHECK_TYPE_NONEMPTY
							));
					} else if (!compareDic(rc027, RecordRangeType.code, rybqValue)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"RYBQ" + i,
								"出院其他诊断入院病情(西医)" + i,
								rybqValue,
								"代码不正确，《值域范围参考RC027-入院病情》",
								CHECK_TYPE_VALIDITY
							));
					}
				}
			}
			status = "71. WBYY 损伤、中毒外部原因名称 字符 100 条件必填 采用疾病分类代码国家临床版2.0(ICD-10)编码对应的外部原因名称；主要诊断ICD编码首字母为S或T时必填；"
					+ "72. JBBM1 损伤、中毒外部原因编码 字符 20 条件必填 采用疾病分类代码国家临床版2.0的编码(ICD-10)，主要诊断ICD编码首字母为S或T时必填";
			if (!homepage.getJbbm1().isEmpty() && !isEmpty(homepage.getJbbm1()) && !diagnosisVerify(homepage.getJbbm1(), homepage.getWbyy(), RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_WESTERN)) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JBBM1",
						"损伤、中毒外部原因编码",
						homepage.getJbbm1(),
						"与《疾病分类代码国家临床版2.0(ICD-10)》不匹配",
						CHECK_TYPE_VALIDITY
					));
			} else if (!isEmpty(homepage.getZyzdJbbm())
				&& (homepage.getZyzdJbbm().charAt(0) == 'S' || homepage.getZyzdJbbm().charAt(0) == 'T')
				&& (homepage.getJbbm1().isEmpty() || isEmpty(homepage.getJbbm1()))) {
				//逻辑校验
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JBBM1",
						"损伤、中毒外部原因编码",
						homepage.getJbbm1(),
						"主要诊断(ZYZD_JBBM)ICD编码首字母为S或T时必填",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "73. BLZD 病理诊断名称 字符 100 条件必填 采用疾病分类代码国家临床版2.0版肿瘤形态学编码(M码)与编码对应的病理名称；主要诊断ICD编码首字母为C或D00-D48时必填；"
					+ "74. JBBM2 病理诊断编码 字符 20 条件必填 采用疾病分类代码国家临床版2.0版肿瘤形态学编码(M码)；主要诊断ICD编码首字母为C或D00-D48时必填";
			if (!homepage.getJbbm2().isEmpty() && !isEmpty(homepage.getJbbm2()) && !diagnosisVerify(homepage.getJbbm2(), homepage.getBlzd(), RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_PATHOLOGY)) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JBBM2",
						"病理诊断编码",
						homepage.getJbbm2(),
						"与《疾病分类代码国家临床版2.0版肿瘤形态学编码(M码)》不匹配",
						CHECK_TYPE_VALIDITY
					));
			} else if (!homepage.getZyzdJbbm().isEmpty() && homepage.getZyzdJbbm().length() >= 3) {
				//逻辑校验
				int diagnoCode = getInteger(homepage.getZyzdJbbm().substring(1, 3));
				if ((homepage.getZyzdJbbm().charAt(0) == 'C' || (homepage.getZyzdJbbm().charAt(0) == 'D' && diagnoCode >= 0 && diagnoCode <= 48))
					&& (homepage.getJbbm2().isEmpty() || isEmpty(homepage.getJbbm2()))) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"JBBM2",
							"病理诊断编码",
							homepage.getJbbm2(),
							"主要诊断(ZYZD_JBBM)ICD编码首字母为C或D00-D48时必填",
							CHECK_TYPE_LOGICAL
						));
				}
			}
			status = "75. BLH 病理号 字符 50 条件必填 有病理诊断编码时必填";
			//逻辑校验
			if (!homepage.getJbbm2().isEmpty() && (homepage.getBlh().isEmpty() || isEmpty(homepage.getBlh()))) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"BLH",
						"病理号",
						homepage.getBlh(),
						"有病理诊断编码(JBBM2)时必填",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "76. YWGM 有无药物过敏 字符 1 必填 《值域范围参考RC028-药物过敏》";
			if (homepage.getYwgm().isEmpty() || isEmpty(homepage.getYwgm())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YWGM",
						"有无药物过敏",
						homepage.getYwgm(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc028, RecordRangeType.code, homepage.getYwgm())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YWGM",
						"有无药物过敏",
						homepage.getYwgm(),
						"代码不正确，《值域范围参考RC028-药物过敏》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "77. GMYW 过敏药物名称 字符 200 条件必填 “有无药物过敏”为“有”时必填；多种药物用英文逗号进行分隔";
			//逻辑校验
			if (homepage.getYwgm().equals("2") && (homepage.getGmyw().isEmpty() || isEmpty(homepage.getGmyw()))) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"GMYW",
						"过敏药物名称",
						homepage.getGmyw(),
						"“有无药物过敏(YWGM)”为“有”时必填；多种药物用英文逗号进行分隔",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "78. SJ 死亡患者尸检 字符 1 条件必填 《值域范围参考RC016-是否》；当离院方式为5死亡时，尸检为“1”或“2”。";
			//逻辑校验
			if (homepage.getLyfs().equals("5")
					&& (homepage.getSj().isEmpty() || !compareDic(rc016, RecordRangeType.code, homepage.getSj()))) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SJ",
						"死亡患者尸检",
						homepage.getSj(),
						"当离院方式(LYFS)为5死亡时，尸检为“1”或“2”",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "79. XX ABO血型 字符 1 必填 《值域范围参考RC030-血型编码》";
			if (homepage.getXx().isEmpty() || isEmpty(homepage.getXx())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XX",
						"ABO血型",
						homepage.getXx(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc030, RecordRangeType.code, homepage.getXx())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XX",
						"ABO血型",
						homepage.getXx(),
						"代码不正确，《值域范围参考RC030-血型编码》",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "80. RH Rh血型 字符 1 必填 《值域范围参考RC031-Rh血型》；当血费>0时，RH血型值为字典值。";
			if (homepage.getRh().isEmpty() || isEmpty(homepage.getRh())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RH",
						"Rh血型",
						homepage.getRh(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc031, RecordRangeType.code, homepage.getRh())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RH",
						"Rh血型",
						homepage.getRh(),
						"代码不正确，《值域范围参考RC031-Rh血型》",
						CHECK_TYPE_VALIDITY
					));
			}
			//逻辑校验
			if (homepage.getXf() > 0 && !homepage.getRh().equals("1") && !homepage.getRh().equals("2")) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"RH",
						"Rh血型",
						homepage.getRh(),
						"当血费(XF)>0时，RH血型(RH)值为“1”或“2”",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "81. KZR 科主任 字符 40 必填";
			if (homepage.getKzr().isEmpty() || isEmpty(homepage.getKzr())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"KZR",
						"科主任",
						homepage.getKzr(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "82. ZRYS 主任（副主任）医师 字符 40 必填";
			if (homepage.getZrys().isEmpty() || isEmpty(homepage.getZrys())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZRYS",
						"主任（副主任）医师",
						homepage.getZrys(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "83. ZZYS 主治医师 字符 40 必填";
			if (homepage.getZzys().isEmpty() || isEmpty(homepage.getZzys())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZZYS",
						"主治医师",
						homepage.getZzys(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "84. ZYYS 住院医师 字符 40 必填";
			if (homepage.getZyys().isEmpty() || isEmpty(homepage.getZyys())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYYS",
						"住院医师",
						homepage.getZyys(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "85. ZRHS 责任护士 字符 40 必填";
			if (homepage.getZrhs().isEmpty() || isEmpty(homepage.getZrhs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZRHS",
						"责任护士",
						homepage.getZrhs(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			/********* 不验证 **********
			 * 86. JXYS 进修医师 字符 40
			 * 87. SXYS 实习医师 字符 40
			 **************************/
			status = "88. BMY 编码员 字符 40 必填";
			if (homepage.getBmy().isEmpty()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"BMY",
						"编码员",
						homepage.getBmy(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			}
			status = "89. BAZL 病案质量 字符 1 《值域范围参考RC011-病案质量》";
			if (!homepage.getBazl().isEmpty() && !isEmpty(homepage.getBazl()) && !compareDic(rc011, RecordRangeType.code, homepage.getBazl())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"BAZL",
						"病案质量",
						homepage.getBazl(),
						"代码不正确，《值域范围参考RC011-病案质量》",
						CHECK_TYPE_VALIDITY
					));
			}
			/********* 不验证 **********
			 * 90. ZKYS	质控医师	字符	40
			 * 91. ZKHS	质控护士	字符	40
			 **************************/
			status = "92. ZKRQ 质控日期 日期 格式 yyyy-MM-dd";
			if (!homepage.getZkrq().isEmpty() && !isEmpty(homepage.getZkrq()) && !isDate(homepage.getZkrq())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZKRQ",
						"质控日期",
						homepage.getZkrq(),
						"格式不正确，yyyy-MM-dd",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "93-114 手术操作……";
			/* 93. SSJCZBM1 主要手术操作编码 字符 20 必填 手术操作名称第一行为“主要手术操作”，采用手术操作分类代码国家临床版2.0编码（ICD-9-CM3）
			 * 94. SSJCZMC1 主要手术操作名称 字符 100 必填 手术操作名称第一行为“主要手术操作”，采用手术操作分类代码国家临床版2.0（ICD-9-CM3）编码对应的名称
			 * 95. SSJCZRQ1 主要手术操作日期 日期 必填 格式 yyyy-MM-dd HH:mm:ss
			 * 96. SHJB1 主要手术操作级别 字符 1 条件必填 手术及操作编码属性为手术或介入治疗代码时必填。《值域范围参考RC029》
			 * 97. SZ1 主要手术操作术者 字符 40 条件必填 手术及操作编码属性为手术或介入治疗代码时必填
			 * 98. YZ1 主要手术操作Ⅰ助 字符 40 条件必填 手术及操作编码属性为手术或介入治疗代码时必填
			 * 99. EZ1 主要手术操作Ⅱ助 字符 40 条件必填 手术及操作编码属性为手术或介入治疗代码时必填
			 * 100. QKDJ1 主要手术操作切口愈合等级 字符 2 条件必填 手术编码属性为手术时必填，《值域范围参考RC014-切开愈合等级》
			 * 101. QKYLB1 主要手术操作切口愈合类别 字符 2 条件必填 手术编码属性为手术时必填，《值域范围参考RC014-切开愈合等级》
			 * 102. MZFS1 主要手术操作麻醉方式 字符 6 条件必填 手术编码属性为手术时必填，《值域范围参考RC013-麻醉方式》
			 * 103. MZYS1 主要手术操作麻醉医师 字符 40 条件必填 手术及操作编码属性为手术时必填
			 *
			 * 104. SSJCZBM2-SSJCZBM41 其他手术操作编码 字符 20 最多收集40条；采用手术操作分类代码国家临床版2.0编码（ICD-9-CM3）
			 * 105. SSJCZMC2-SSJCZMC41 其他手术操作名称 字符 100 最多收集40条；采用手术操作分类代码国家临床版2.0（ICD-9-CM3）编码对应的名称
			 * 106. SSJCZRQ2-SSJCZRQ41 其他手术操作日期 日期  最多收集40条，格式 yyyy-MM-dd HH:mm:ss
			 * 107. SHJB2-SHJB41 其他手术操作级别 字符 1 最多收集40条，手术及操作编码属性为手术或介入治疗代码时必填。《值域范围参考RC029-手术级别》
			 * 108. SZ2-SZ41 其他手术操作术者 字符 40 最多收集40条，手术及操作编码属性为手术或介入治疗代码时必填
			 * 109. YZ2-YZ41 其他手术操作Ⅰ助 字符 40 最多收集40条，手术及操作编码属性为手术或介入治疗代码时必填
			 * 110. EZ2-EZ41 其他手术操作Ⅱ助 字符 40 最多收集40条，手术及操作编码属性为手术或介入治疗代码时必填
			 * 111. QKDJ2-QKDJ41 其他手术操作切口愈合等级 字符 2 最多收集40条，手术编码属性为手术时必填，《值域范围参考RC014-切开愈合等级》
			 * 112. QKYLB2-QKYLB41 切口愈合类别 字符 2 最多收集40条，手术编码属性为手术时必填，《值域范围参考RC014-切开愈合等级》
			 * 113. MZFS2-MZFS41 其他手术操作麻醉方式 字符 6 最多收集40条，手术编码属性为手术时必填，《值域范围参考RC013-麻醉方式》
			 * 114. MZYS2-MZYS41 其他手术操作麻醉医师 字符 40 最多收集40条，手术及操作编码属性为手术时必填
			 */
			for (int i = 1; i <= 41; i ++) {
				String preText = i == 1 ? "主要" : "其他";
				String ssjczbmValue = homepage.getClass().getMethod("getSsjczbm" + i, new Class<?>[0]).invoke(homepage).toString();
				String ssjczmcValue = homepage.getClass().getMethod("getSsjczmc" + i, new Class<?>[0]).invoke(homepage).toString();
				String ssjczrqValue = homepage.getClass().getMethod("getSsjczrq" + i, new Class<?>[0]).invoke(homepage).toString();
				String shjbValue = homepage.getClass().getMethod("getShjb" + i, new Class<?>[0]).invoke(homepage).toString();
				String szValue = homepage.getClass().getMethod("getSz" + i, new Class<?>[0]).invoke(homepage).toString();
				String yzValue = homepage.getClass().getMethod("getYz" + i, new Class<?>[0]).invoke(homepage).toString();
				String qkdjValue = homepage.getClass().getMethod("getQkdj" + i, new Class<?>[0]).invoke(homepage).toString();
				String qkylbValue = homepage.getClass().getMethod("getQkylb" + i, new Class<?>[0]).invoke(homepage).toString();
				String mzfsValue = homepage.getClass().getMethod("getMzfs" + i, new Class<?>[0]).invoke(homepage).toString();
				String mzysValue = homepage.getClass().getMethod("getMzys" + i, new Class<?>[0]).invoke(homepage).toString();
				/*
				 * 为费用逻辑判断用
				 */
				hasSs = !hasSs ? !ssjczbmValue.isEmpty() && !isEmpty(ssjczbmValue) : hasSs;
				hasMz = !hasMz ? !mzfsValue.isEmpty() && !isEmpty(mzfsValue) : hasMz;
	
				if (!ssjczbmValue.isEmpty() && !isEmpty(ssjczbmValue)) {
					status = "手术诊断";
					if (!diagnosisVerify(ssjczbmValue, ssjczmcValue, RecordRangeType.ICD2, RecordRangeType.DIAGNOSIS_OPERATION)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"SSJCZBM" + i,
								preText + "手术操作编码" + i,
								ssjczbmValue,
								"与《手术操作分类代码国家临床版2.0编码（ICD-9-CM3）》不匹配",
								CHECK_TYPE_VALIDITY
							));
					}
					status = "手术时间";
					if (ssjczrqValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"SSJCZRQ" + i,
								preText + "手术操作日期" + i,
								ssjczrqValue,
								"当有手术(SSJCZBM" + i + ")时，不能为空",
								CHECK_TYPE_LOGICAL
							));
					} else if (!isDatetime(ssjczrqValue)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"SSJCZRQ" + i,
								preText + "手术操作日期" + i,
								ssjczrqValue,
								"格式不正确，yyyy-MM-dd HH:mm:ss",
								CHECK_TYPE_VALIDITY
							));
					}
					status = "手术级别";
					if (shjbValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"SHJB" + i,
								preText + "手术操作级别" + i,
								shjbValue,
								"当有手术时(SSJCZBM" + i + ")，不能为空",
								CHECK_TYPE_LOGICAL
							));
					} else if (!compareDic(rc029, RecordRangeType.code, shjbValue)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"SHJB" + i,
								preText + "手术操作级别" + i,
								shjbValue,
								"代码不正确，《值域范围参考RC029》",
								CHECK_TYPE_VALIDITY
							));
					}
					status = "术者";
					if (szValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"SZ" + i,
								preText + "手术操作术者" + i,
								szValue,
								"当有手术时(SSJCZBM" + i + ")，不能为空",
								CHECK_TYPE_LOGICAL
							));
					}
					status = "Ⅰ助";
					if (yzValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"YZ" + i,
								preText + "手术操作Ⅰ助" + i,
								yzValue,
								"当有手术时(SSJCZBM" + i + ")，不能为空",
								CHECK_TYPE_LOGICAL
							));
					}
					status = "切口愈合等级";
					if (qkdjValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"QKDJ" + i,
								preText + "切口愈合等级" + i,
								qkdjValue,
								"当有手术时(SSJCZBM" + i + ")，不能为空",
								CHECK_TYPE_LOGICAL
							));
					} else if (!compareDic(rc014_1, RecordRangeType.code, qkdjValue)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"QKDJ" + i,
								preText + "切口愈合等级" + i,
								qkdjValue,
								"代码不正确，《值域范围参考RC014-切开愈合等级》",
								CHECK_TYPE_VALIDITY
							));
					}
					status = "切口愈合类别";
					if (qkylbValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"QKYLB" + i,
								preText + "切口愈合类别" + i,
								qkylbValue,
								"当有手术时(SSJCZBM" + i + ")，不能为空",
								CHECK_TYPE_LOGICAL
							));
					} else if (!compareDic(rc014_2, RecordRangeType.code, qkylbValue)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"QKYLB" + i,
								preText + "切口愈合类别" + i,
								qkylbValue,
								"代码不正确，《值域范围参考RC014-切开愈合等级》",
								CHECK_TYPE_VALIDITY
							));
					}
					status = "麻醉方式";
					if (mzfsValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"MZFS" + i,
								preText + "麻醉方式" + i,
								mzfsValue,
								"当有手术时(SSJCZBM" + i + ")，不能为空",
								CHECK_TYPE_LOGICAL
							));
					} else if (!compareDic(rc013, RecordRangeType.code, mzfsValue)) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"MZFS" + i,
								preText + "麻醉方式" + i,
								mzfsValue,
								"代码不正确，《值域范围参考RC013-麻醉方式》",
								CHECK_TYPE_VALIDITY
							));
					}
					status = "麻醉医师";
					if (mzysValue.isEmpty()) {
						checkRecord.add(new InpatientHomepageAnalyCheck(
								checkIndex ++,
								homepage.getId(),
								"MZYS" + i,
								preText + "麻醉医师" + i,
								mzysValue,
								"当有手术时(SSJCZBM" + i + ")，不能为空",
								CHECK_TYPE_LOGICAL
							));
					}
				}
			}
			status = "115. LYFS 离院方式 字符 1 必填 《值域范围参考RC019》；指患者本次住院出院的方式，填写相应的阿拉伯数字；"
					+ "116. YZZY_JGMC 医嘱转院，拟接收医疗机构名称 字符 100 条件必填 离院方式为医嘱转院患者必填；"
					+ "117. WSY_JGMC 医嘱转社区卫生服务机构/乡镇卫生院，拟接收医疗机构名称 字符 100 条件必填 离院方式为医嘱转社区患者必填";
			if (homepage.getLyfs().isEmpty() || isEmpty(homepage.getLyfs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"LYFS",
						"离院方式",
						homepage.getLyfs(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc019, RecordRangeType.code, homepage.getLyfs())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"LYFS",
						"离院方式",
						homepage.getLyfs(),
						"代码不正确，《值域范围参考RC019》",
						CHECK_TYPE_VALIDITY
					));
			} else if (homepage.getLyfs().equals("2") && (homepage.getYzzyJgmc().isEmpty() || isEmpty(homepage.getYzzyJgmc()))) {
				status = "逻辑校验 - 医嘱转院";
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YZZY_JGMC",
						"拟接收医疗机构名称",
						homepage.getYzzyJgmc(),
						"离院方式(LYFS)为“2(医嘱转院)”时，不能为空",
						CHECK_TYPE_LOGICAL
					));
			} else if (homepage.getLyfs().equals("3") && (homepage.getWsyJgmc().isEmpty() || isEmpty(homepage.getWsyJgmc()))) {
				status = "逻辑校验 - 医嘱转社区卫生服务机构/乡镇卫生院";
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"WSY_JGMC",
						"拟接收医疗机构名称",
						homepage.getWsyJgmc(),
						"离院方式(LYFS)为“3(医嘱转社区卫生服务机构/乡镇卫生院)”时，不能为空",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "118. ZZYJH 是否有出院31天内再住院计划 数字 1 必填 《值域范围参考RC016》，指患者本次住院出院后31天内是否有诊疗需要的再住院安排。如果有再住院计划，则需要填写目的，如：进行二次手术；"
					+ "119. MD 目的 字符 100 条件必填 是否有出院31日内再住院计划填“有”时必填";
			if (homepage.getZzyjh().isEmpty() || isEmpty(homepage.getZzyjh())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZZYJH",
						"是否有出院31天内再住院计划",
						homepage.getZzyjh(),
						"不能为空",
						CHECK_TYPE_NONEMPTY
					));
			} else if (!compareDic(rc037, RecordRangeType.code, homepage.getZzyjh())) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZZYJH",
						"是否有出院31天内再住院计划",
						homepage.getZzyjh(),
						"代码不正确，《值域范围参考RC016》",
						CHECK_TYPE_VALIDITY
					));
			} else if (homepage.getZzyjh().equals("2") && (homepage.getMd().isEmpty() || isEmpty(homepage.getMd()))) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"MD",
						"目的",
						homepage.getMd(),
						"是否有出院31日内再住院计划(ZZYJH)填“有”时必填",
						CHECK_TYPE_CONDITIONAL_NONEMPTY
					));
			}
			status = "120. RYQ_T 颅脑损伤患者昏迷入院前时间（天) 数字 5 必填 大于等于0整数。；"
					+ "121. RYQ_XS 小时 数字 2 必填 大于等于0，小于24整数。；"
					+ "122. RYQ_FZ 分钟 数字 2 必填 大于等于0，小于60整数。";
			/*
			 * TODO
			 * 限定为颅脑损伤的患者
			 * 暂停验证，待确定诊断范围
			 */
			if (homepage.getZyzdJbbm().equals("颅脑损伤")) {
				if (homepage.getRyqT().isEmpty()) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYQ_T",
							"颅脑损伤患者昏迷入院前时间（天)",
							homepage.getRyqT(),
							"不能为空",
							CHECK_TYPE_NONEMPTY
						));
				} else if (!isInteger(homepage.getRyqT())) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYQ_T",
							"颅脑损伤患者昏迷入院前时间（天)",
							homepage.getRyqT(),
							"数值不正确，大于等于0整数",
							CHECK_TYPE_VALIDITY
						));
				}
				if (homepage.getRyqXs().isEmpty()) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYQ_XS",
							"颅脑损伤患者昏迷入院前时间（小时)",
							homepage.getRyqXs(),
							"不能为空",
							CHECK_TYPE_NONEMPTY
						));
				} else if (!isInteger(homepage.getRyqXs()) || getInteger(homepage.getRyqXs()) < 0) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYQ_XS",
							"颅脑损伤患者昏迷入院前时间（小时)",
							homepage.getRyqXs(),
							"数值不正确，大于等于0，小于24整数",
							CHECK_TYPE_VALIDITY
						));
				}
				if (homepage.getRyqFz().isEmpty()) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYQ_FZ",
							"颅脑损伤患者昏迷入院前时间（分钟)",
							homepage.getRyqFz(),
							"不能为空",
							CHECK_TYPE_NONEMPTY
						));
				} else if (!isInteger(homepage.getRyqFz()) || getInteger(homepage.getRyqFz()) < 0) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYQ_FZ",
							"颅脑损伤患者昏迷入院前时间（分钟)",
							homepage.getRyqFz(),
							"数值不正确，大于等于0，小于60整数",
							CHECK_TYPE_VALIDITY
						));
				}
			}
			status = "123. RYH_T 颅脑损伤患者昏迷时间天 数字 5 必填 大于等于0整数。；"
					+ "124. RYH_XS 小时 数字 2 必填 大于等于0，小于24整数。；"
					+ "125. RYH_FZ 分钟 数字 2 必填 大于等于0，小于60整数。";
			/*
			 * TODO
			 * 限定为颅脑损伤的患者
			 * 暂停验证，待确定诊断范围
			 */
			if (homepage.getZyzdJbbm().equals("颅脑损伤")) {
				if (homepage.getRyhT().isEmpty()) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYH_T",
							"颅脑损伤患者昏迷时间（天)",
							homepage.getRyhT(),
							"不能为空",
							CHECK_TYPE_NONEMPTY
						));
				} else if (!isInteger(homepage.getRyhT())) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYH_T",
							"颅脑损伤患者昏迷时间（天)",
							homepage.getRyhT(),
							"数值不正确，大于等于0整数",
							CHECK_TYPE_VALIDITY
						));
				}
				if (homepage.getRyhXs().isEmpty()) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYH_XS",
							"颅脑损伤患者昏迷时间（小时)",
							homepage.getRyhXs(),
							"不能为空",
							CHECK_TYPE_NONEMPTY
						));
				} else if (!isInteger(homepage.getRyhXs()) || getInteger(homepage.getRyhXs()) < 0) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYH_XS",
							"颅脑损伤患者昏迷时间（小时)",
							homepage.getRyhXs(),
							"数值不正确，大于等于0，小于24整数",
							CHECK_TYPE_VALIDITY
						));
				}
				if (homepage.getRyhFz().isEmpty()) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYH_FZ",
							"颅脑损伤患者昏迷时间（分钟)",
							homepage.getRyhFz(),
							"不能为空",
							CHECK_TYPE_NONEMPTY
						));
				} else if (!isInteger(homepage.getRyhFz()) || getInteger(homepage.getRyhFz()) < 0) {
					checkRecord.add(new InpatientHomepageAnalyCheck(
							checkIndex ++,
							homepage.getId(),
							"RYH_FZ",
							"颅脑损伤患者昏迷时间（分钟)",
							homepage.getRyhFz(),
							"数值不正确，大于等于0，小于60整数",
							CHECK_TYPE_VALIDITY
						));
				}
			}
			status = "126. ZFY 总费用 数字 (10,2) 必填 住院总费用必填且大于0；总费用大于或等于分项费用之和；";
			double _zfy = homepage.getYlfwf()//综合医疗服务类(1)一般医疗服务费
					+ homepage.getZlczf()//(2)一般治疗操作费
					+ homepage.getHlf()//(3)护理费
					+ homepage.getQtfy()//(4)其他费用
					+ homepage.getBlzdf()//诊断类(5)病理诊断费
					+ homepage.getZdf()//(6)实验室诊断费
					+ homepage.getYxxzdf()//(7)影像学诊断费
					+ homepage.getLczdxmf()//(8)临床诊断项目费
					+ homepage.getFsszlxmf()//治疗类(9)非手术治疗项目费(临床物理治疗费)
					+ homepage.getSszlf()//(10)手术治疗费(麻醉费+手术费)
					+ homepage.getKff()//康复类(11)康复费
					+ homepage.getZylZyzd()//中医类(中医和名族医医疗服务)（12）中医诊断
					+ homepage.getZyzl()//(13)中医治疗(中医外治+中医骨伤+针刺与灸法+中医推拿治疗+中医肛肠治疗+中医特殊治疗)
					+ homepage.getZyqt()//(14)中医其他(中医特殊调配加工+辨证施膳)
					+ homepage.getXyf()//西药类(15)西药费(抗菌药物费)
					+ homepage.getZcyf()//中药类(16)中成药费(医疗机构中药制剂费)
					+ homepage.getZcyf1()//(17)中草药费
					+ homepage.getXf()//血液和血液制品类(18)血费
					+ homepage.getBdblzpf()//(19)白蛋白类制品费
					+ homepage.getQdblzpf()//(20)球蛋白类制品费
					+ homepage.getNxyzlzpf()//(21)凝血因子类制品费
					+ homepage.getXbyzlzpf()//(22)细胞因子类制品费
					+ homepage.getJcyyclf()//耗材类(23)检查用一次性医用材料费
					+ homepage.getYyclf()//(24)治疗用一次性医用材料费
					+ homepage.getSsycxclf()//(25)手术用一次性医用材料费
					+ homepage.getQtf()//其他类(26)其他费
					;

			if (FormatUtil.formatDecimal(_zfy, 2) != homepage.getZfy()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZFY",
						"总费用",
						homepage.getZfy().toString(),
						"总费用(ZFY)与费用合计(" + _zfy + ")不符",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "127. ZFJE 自付金额 数字 (10,2) 必填 小于等于总费用";
			if (homepage.getZfje() > homepage.getZfy()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZFJE",
						"自付金额",
						homepage.getZfje().toString(),
						"应小于等于总费用(ZFY)",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "128. YLFWF 综合医疗服务类(1)一般医疗服务费 数字 (10,2) 必填 住院天数>1时，(1)一般医疗服务费>0。";
			//逻辑校验
			if (getInteger(homepage.getSjzy()) > 1 && homepage.getYlfwf() <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YLFWF",
						"(1)一般医疗服务费",
						homepage.getYlfwf().toString(),
						"住院天数(SJZY)>1时，(1)一般医疗服务费(YLFWF)>0",
						CHECK_TYPE_LOGICAL
					));
			}
			//验证(1)一般医疗服务费是否小于中医辨证论治费+中医辨证论治会诊费
			if (homepage.getYlfwf() < homepage.getBzlzf()+homepage.getZyblzhzf()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"YLFWF",
						"(1)一般医疗服务费",
						homepage.getYlfwf().toString(),
						"应大于等于中医辨证论治费(BZLZF)+中医辨证论治会诊费(ZYBLZHZF)",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "129. BZLZF 中医辨证论治费 数字 (10,2) 条件必填 发生辨证论治费时，必填。小于总费用。";
			if (homepage.getBzlzf() >= homepage.getZfy()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"BZLZF",
						"中医辨证论治费",
						homepage.getBzlzf().toString(),
						"应小于总费用(ZFY)",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "130. ZYBLZHZF 中医辨证论治会诊费 数字 (10,2) 条件必填 发生辨证论治会诊费时，必填。小于总费用。";
			if (homepage.getZyblzhzf() >= homepage.getZfy()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYBLZHZF",
						"中医辨证论治会诊费",
						homepage.getZyblzhzf().toString(),
						"应小于总费用(ZFY)",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "131. ZLCZF (2)一般治疗操作费 数字 (10,2)";
			status = "132. HLF (3)护理费 数字 (10,2) 住院天数>1时，(3)护理费>0。";
			if (getInteger(homepage.getSjzy()) > 1 && homepage.getHlf() <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"HLF",
						"(3)护理费",
						homepage.getHlf().toString(),
						"住院天数(SJZY)>1时，(3)护理费(HLF)>0",
						CHECK_TYPE_LOGICAL
					));
			}
			/* 
			 * 133. QTFY (4)其他费用 数字 (10,2)
			 * 134. BLZDF 诊断类(5)病理诊断费 数字 (10,2)
			 * 135. ZDF (6)实验室诊断费 数字 (10,2)
			 * 136. YXXZDF (7)影像学诊断费 数字 (10,2)
			 * 137. LCZDXMF (8)临床诊断项目费 数字 (10,2)
			 */
			status = "138. FSSZLXMF 治疗类(9)非手术治疗项目费 数字 (10,2)；"
					+ "139. ZLF 临床物理治疗费 数字 (10,2)";
			if (homepage.getFsszlxmf() < homepage.getZlf()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"FSSZLXMF",
						"(9)非手术治疗项目费",
						homepage.getFsszlxmf().toString(),
						"应大于等于临床物理治疗费(ZLF)",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "140. SSZLF (10)手术治疗费 数字 (10,2) 手术治疗费>=麻醉费+手术费。";
			if (homepage.getSszlf() < homepage.getMzf()+homepage.getSsf()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SSZLF",
						"(10)手术治疗费",
						homepage.getSszlf().toString(),
						"应大于等于麻醉费(MZF)+手术费(SSF)",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "141. MZF 麻醉费 数字 (10,2) 麻醉方式有值时，麻醉费>0。";
			//逻辑校验
			if (hasMz && homepage.getMzf() <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"MZF",
						"麻醉费",
						homepage.getMzf().toString(),
						"麻醉方式(MZFS1-41)有值时，麻醉费(MZF)>0",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "142. SSF 手术费 数字 (10,2) 发生手术时，手术费用>0。";
			//逻辑校验
			if (hasSs && homepage.getSsf() <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"SSF",
						"手术费",
						homepage.getSsf().toString(),
						"发生手术时，手术费用(SSF)>0",
						CHECK_TYPE_LOGICAL
					));
			}
			/* 
			 * 143. KFF 康复类(11)康复费 数字 (10,2)
			 */
			status = "144. ZYL_ZYZD 中医类(中医和名族医医疗服务)（12）中医诊断 数字 (10,2) 条件必填 发生中医诊断费时，必填；小于总费用；治疗类别为“1或2”且住院天数>1时，（12）中医诊断+(13)中医治疗+(14)中医其他>0；";
			//逻辑校验
			if ((homepage.getZllb().equals("1") || homepage.getZllb().equals("2"))
				&& getInteger(homepage.getSjzy()) > 1
				&& homepage.getZylZyzd()+homepage.getZyzl()+homepage.getZyqt() <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYL_ZYZD",
						"（12）中医诊断",
						homepage.getZylZyzd().toString(),
						"治疗类别为“1或2”且住院天数>1时，（12）中医诊断(ZYL_ZYZD)+(13)中医治疗(ZYZL)+(14)中医其他(ZYQT)>0",
						CHECK_TYPE_LOGICAL
					));
			}
			status = "145. ZYZL (13)中医治疗 数字 (10,2) 条件必填 发生中医治疗费时，必填；小于总费用；中医治疗费>=中医外治+中医骨伤+针刺与灸法+中医推拿治疗+中医肛肠治疗+中医特殊治疗；治疗类别为“1或2”且住院天数>1时，(13)中医治疗+(16)中成药费+(17)中草药费>0。";
			//中医治疗费>=中医外治+中医骨伤+针刺与灸法+中医推拿治疗+中医肛肠治疗+中医特殊治疗；
			Double zyzl = homepage.getZywz()+homepage.getZygs()+homepage.getZcyjf()+homepage.getZytnzl()+homepage.getZygczl()+homepage.getZytszl();
			if (homepage.getZyzl() < zyzl) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZL",
						"(13)中医治疗",
						homepage.getZyzl().toString(),
						"应大于等于中医外治(ZYWZ)+中医骨伤(ZYGS)+针刺与灸法(ZCYJF)+中医推拿治疗(ZYTNZL)+中医肛肠治疗(ZYGCZL)+中医特殊治疗(ZYTSZL)",
						CHECK_TYPE_VALIDITY
					));
			}
			//治疗类别为"1或2"且住院天数>1时，(13)中医治疗+(16)中成药费+(17)中草药费>0
			//逻辑校验
			if ((homepage.getZllb().equals("1") || homepage.getZllb().equals("2"))
				&& getInteger(homepage.getSjzy()) > 1
				&& homepage.getZyzl()+homepage.getZcyf()+homepage.getZcyf1() <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZL",
						"(13)中医治疗",
						homepage.getZyzl().toString(),
						"治疗类别为“1或2”且住院天数>1时，(13)中医治疗(ZYZL)+(16)中成药费(ZCYF)+(17)中草药费(ZCYF1)>0",
						CHECK_TYPE_LOGICAL
					));
			}
			/* 
			 * 146. ZYWZ 中医外治 数字 (10,2) 条件必填 发生中医外治费时，必填。小于总费用。
			 * 147. ZYGS 中医骨伤 数字 (10,2) 条件必填 发生中医骨伤费时，必填。小于总费用。
			 * 148. ZCYJF 针刺与灸法 数字 (10,2) 条件必填 发生针刺与灸法费时，必填。小于总费用。
			 * 149. ZYTNZL 中医推拿治疗 数字 (10,2) 条件必填 发生中医推拿治疗费时，必填。小于总费用。
			 * 150. ZYGCZL 中医肛肠治疗 数字 (10,2) 条件必填 发生中医肛肠治疗费时，必填。小于总费用。
			 * 151. ZYTSZL 中医特殊治疗 数字 (10,2) 条件必填 发生中医特殊治疗费时，必填。小于总费用。
			 * 152. ZYQT (14)中医其他 数字 (10,2) 条件必填 发生中医其他费时，必填。小于总费用。
			 * 153. ZYTSTPJG 中医特殊调配加工 数字 (10,2) 条件必填 发生中医特殊调配加工费时，必填。小于总费用。
			 * 154. BZSS 辨证施膳 数字 (10,2) 条件必填 发生辨证施膳费时，必填。小于总费用。
			 */
			status = "155. XYF 西药类(15)西药费 数字 (10,2)";
			if (homepage.getXyf() < homepage.getKjywf()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"XYF",
						"(15)西药费",
						homepage.getXyf().toString(),
						"应大于等于抗菌药物费(KJYWF)",
						CHECK_TYPE_VALIDITY
					));
			}
			/* 156. KJYWF	抗菌药物费	数字	(10,2)
			 */
			status = "157. ZCYF 中药类(16)中成药费 数字 (10,2) 条件必填 发生中成药费时，必填；中成药费>=医疗机构中药制剂费。";
			if (homepage.getZcyf() < homepage.getZyzjf()) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZCYF",
						"(16)中成药费",
						homepage.getZcyf().toString(),
						"应大于等于医疗机构中药制剂费(ZYZJF)",
						CHECK_TYPE_VALIDITY
					));
			}
			status = "158. ZYZJF 医疗机构中药制剂费 数字 (10,2) 条件必填 使用医疗机构中药制剂值为“是”时，必填，且值大于0，小于总费用。";
			//逻辑校验
			if (homepage.getZyyj().equals("1") && homepage.getZyzjf() <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"ZYZJF",
						"医疗机构中药制剂费",
						homepage.getZyzjf().toString(),
						"使用医疗机构中药制剂(ZYYJ)值为“是”时，必填，且值大于0，小于总费用",
						CHECK_TYPE_LOGICAL
					));
			}
			/* 
			 * 159. ZCYF1 (17)中草药费 数字 (10,2) 条件必填 发生中草药费费时，必填。小于总费用。
			 * 160. XF 血液和血液制品类(18)血费 数字 (10,2)
			 * 161. BDBLZPF (19)白蛋白类制品费 数字 (10,2)
			 * 162. QDBLZPF (20)球蛋白类制品费 数字 (10,2)
			 * 163. NXYZLZPF (21)凝血因子类制品费 数字 (10,2)
			 * 164. XBYZLZPF (22)细胞因子类制品费 数字 (10,2)
			 */
			status = "165. JCYYCLF 耗材类(23)检查用一次性医用材料费 数字 (10,2) 住院天数>1时，(23)检查用一次性医用材料费+(24)治疗用一次性医用材料费+(25)手术用一次性医用材料费>0";
			//逻辑校验
			if (getInteger(homepage.getSjzy()) > 1 && homepage.getJcyyclf()+homepage.getYyclf()+homepage.getSsycxclf() <= 0) {
				checkRecord.add(new InpatientHomepageAnalyCheck(
						checkIndex ++,
						homepage.getId(),
						"JCYYCLF",
						"(23)检查用一次性医用材料费",
						homepage.getJcyyclf().toString(),
						"住院天数>1时，(23)检查用一次性医用材料费(JCYYCLF)+(24)治疗用一次性医用材料费(YYCLF)+(25)手术用一次性医用材料费(SSYCXCLF)>0",
						CHECK_TYPE_LOGICAL
					));
			}
			/* 
			 * 166. YYCLF (24)治疗用一次性医用材料费 数字 (10,2)
			 * 167. SSYCXCLF (25)手术用一次性医用材料费 数字 (10,2)
			 * 168. QTF 其他类(26)其他费 数字 (10,2) 住院天数>1时，（26）其他类<总费用
			 */
	
			homepage.setChecked(checkRecord.size());
		}
		catch(Exception ex) {
			throw new Exception("数据校验[" + status + "]时发生错误：" + ex.getMessage());
		}
	}

	@Override
	public void saveHomepage(InpatientHomepageAnaly[] homepageList) throws Exception {
		homepageDao.importHomepagesAndChecks(homepageList);
	}

	@Override
	public List<InpatientHomepageAnaly> homepagePatch(InpatientHomepageAnaly[] homepageList) throws Exception {
		initialize();
		String status = "";
		try {
			List<InpatientHomepageAnaly> patchedList = new ArrayList<InpatientHomepageAnaly>();
			for (InpatientHomepageAnaly homepage : homepageList) {
				InpatientHomepageAnalyCheck[] checkList = homepage.getInpatientHomepageAnalyChecks().toArray(new InpatientHomepageAnalyCheck[0]);
				InpatientHomepageAnaly homepagePatched = homepage.clone();
				InpatientHomepageAnalyCheck checkRecord = null;
				homepagePatched.getId().setVersion(homepagePatched.getId().getVersion() + .1f);
				homepagePatched.setFlag("PATCHED");
				homepagePatched.setInpatientHomepageAnalyChecks(null);
				boolean isPatched = false;
				status = "医疗付款方式";
				if ((checkRecord = checkedIsError(checkList, "YLFKFS", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setYlfkfs("9");//其他
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "性别";
				if ((checkRecord = checkedIsError(checkList, "XB", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setXb("9");//未说明的性别
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "XB", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC001-性别代码》")) != null
						&& compareDic(rc001, RecordRangeType.name, homepagePatched.getXb())) {
					isPatched = true;
					homepagePatched.setXb(getDic(rc001, RecordRangeType.name, homepagePatched.getXb(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "年龄";
				int age = getAge(homepagePatched.getRysj(), homepagePatched.getCsrq(), homepagePatched.getSfzh(), 0);
				if ((checkRecord = checkedIsError(checkList, "NL", CHECK_TYPE_NONEMPTY, null)) != null
				 || (checkRecord = checkedIsError(checkList, "NL", CHECK_TYPE_VALIDITY, "不是数字")) != null
				 || (checkRecord = checkedIsError(checkList, "NL", CHECK_TYPE_VALIDITY, "必须是大于或等于0的整数")) != null
				 || (checkRecord = checkedIsError(checkList, "NL", CHECK_TYPE_LOGICAL, "与实际年龄（" + age + "）不符")) != null) {
					isPatched = true;
					homepagePatched.setNl(age+"");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "国籍";
				if ((checkRecord = checkedIsError(checkList, "GJ", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setGj("ZZZ");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "GJ", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC038-国籍字典》")) != null
						&& compareDic(rc038, RecordRangeType.name, homepagePatched.getGj())) {
					isPatched = true;
					homepagePatched.setGj(getDic(rc038, RecordRangeType.name, homepagePatched.getGj(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "籍贯";
				if ((checkRecord = checkedIsError(checkList, "GG", CHECK_TYPE_NONEMPTY, null)) != null
				 && compareDic(rc036, RecordRangeType.name, homepagePatched.getHkdz())) {
					isPatched = true;
					homepagePatched.setGg(getDic(rc036, RecordRangeType.name, homepagePatched.getHkdz(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "GG", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC036-籍贯》")) != null
						&& compareDic(rc036, RecordRangeType.name, homepagePatched.getGg())) {
					isPatched = true;
					homepagePatched.setGg(getDic(rc036, RecordRangeType.name, homepagePatched.getGg(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "民族";
				if ((checkRecord = checkedIsError(checkList, "MZ", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setMz("1");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "MZ", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC035-民族代码》")) != null
						&& compareDic(rc035, RecordRangeType.name, homepagePatched.getMz())) {
					isPatched = true;
					homepagePatched.setMz(getDic(rc035, RecordRangeType.name, homepagePatched.getMz(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "职业";
				if ((checkRecord = checkedIsError(checkList, "ZY", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setZy("90");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "ZY", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC003-职业代码》")) != null
						&& compareDic(rc003, RecordRangeType.name, homepagePatched.getZy())) {
					isPatched = true;
					homepagePatched.setZy(getDic(rc003, RecordRangeType.name, homepagePatched.getZy(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "婚姻";
				if ((checkRecord = checkedIsError(checkList, "HY", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setHy("9");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "HY", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC002-婚姻代码》")) != null
						&& compareDic(rc002, RecordRangeType.name, homepagePatched.getHy())) {
					isPatched = true;
					homepagePatched.setHy(getDic(rc002, RecordRangeType.name, homepagePatched.getHy(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "现住址";
				if ((checkRecord = checkedIsError(checkList, "XZZ", CHECK_TYPE_NONEMPTY, null)) != null
				 || (checkRecord = checkedIsError(checkList, "XZZ", CHECK_TYPE_VALIDITY, "必须以省或自治区开始")) != null) {
					isPatched = true;
					homepagePatched.setXzz(patchAddress(homepagePatched.getXzz()));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "户口地址";
				if ((checkRecord = checkedIsError(checkList, "HKDZ", CHECK_TYPE_NONEMPTY, null)) != null
				 || (checkRecord = checkedIsError(checkList, "HKDZ", CHECK_TYPE_VALIDITY, "必须以省或自治区开始")) != null) {
					isPatched = true;
					homepagePatched.setHkdz(patchAddress(homepagePatched.getHkdz()));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "工作单位及地址";
				if ((checkRecord = checkedIsError(checkList, "GZDWJDZ", CHECK_TYPE_NONEMPTY, null)) != null
				 || (checkRecord = checkedIsError(checkList, "GZDWJDZ", CHECK_TYPE_LOGICAL, "职业(ZY)为“11(国家公务员)、13(专业技术人员)、17(职员)、21(企业管理人员)、24(工人)、37(现役军人)”时，必须登记工作单位及地址(GZDWJDZ)，且以省或自治区开始")) != null) {
					isPatched = true;
					homepagePatched.setGzdwjdz(patchAddress(homepagePatched.getGzdwjdz()));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "联系人地址";
				if ((checkRecord = checkedIsError(checkList, "DZ", CHECK_TYPE_NONEMPTY, null)) != null
				 || (checkRecord = checkedIsError(checkList, "DZ", CHECK_TYPE_VALIDITY, "必须以省或自治区开始")) != null) {
					isPatched = true;
					homepagePatched.setDz(patchAddress(homepagePatched.getDz()));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "联系人关系";
				if ((checkRecord = checkedIsError(checkList, "GX", CHECK_TYPE_NONEMPTY, null)) != null
				  && homepagePatched.getXm().equals(homepagePatched.getLxrxm())) {
					isPatched = true;
					homepagePatched.setGx("0");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "GX", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC033-联系人关系代码》")) != null
						&& compareDic(rc033, RecordRangeType.name, homepagePatched.getGx())) {
					isPatched = true;
					homepagePatched.setGx(getDic(rc033, RecordRangeType.name, homepagePatched.getGx(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "治疗类别";
				if ((checkRecord = checkedIsError(checkList, "ZLLB", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC039-治疗类别字典》")) != null
				 && compareDic(rc039, RecordRangeType.name, homepagePatched.getZllb())) {
					isPatched = true;
					homepagePatched.setZllb(getDic(rc039, RecordRangeType.name, homepagePatched.getZllb(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "入院时间";
				if ((checkRecord = checkedIsError(checkList, "RYSJ", CHECK_TYPE_VALIDITY, "格式不正确，yyyy-MM-dd HH:mm:ss")) != null
				 && !isEmpty(homepagePatched.getRysj()) && homepagePatched.getRysj().split("\\Q:\\E").length > 3) {
					isPatched = true;
					homepagePatched.setRysj(homepagePatched.getRysj().substring(0, 19));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "入院时间（时）";
				if (((checkRecord = checkedIsError(checkList, "RYSJ_S", CHECK_TYPE_NONEMPTY, null)) != null
				 || (checkRecord = checkedIsError(checkList, "RYSJ_S", CHECK_TYPE_VALIDITY, "格式不正确，小时数（24小时制）")) != null
				 || (checkRecord = checkedIsError(checkList, "RYSJ_S", CHECK_TYPE_LOGICAL, "与入院时间(RYSJ)不匹配")) != null)
				 && isDatetime(homepagePatched.getRysj())) {
					isPatched = true;
					Calendar _rysj = Calendar.getInstance();
					_rysj.setTime(FormatUtil.parseDateTime(homepagePatched.getRysj()));
					homepagePatched.setRysjS(_rysj.get(Calendar.HOUR_OF_DAY)+"");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "入院科别";
				if ((checkRecord = checkedIsError(checkList, "RYKB", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC023-科室代码》")) != null
				 && compareDic(rc023, RecordRangeType.name, homepagePatched.getRykb())) {
					isPatched = true;
					homepagePatched.setRykb(getDic(rc023, RecordRangeType.name, homepagePatched.getRykb(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				/*
				 * TODO 转科科别
				 */
				status = "出院时间";
				if ((checkRecord = checkedIsError(checkList, "CYSJ", CHECK_TYPE_VALIDITY, "格式不正确，yyyy-MM-dd HH:mm:ss")) != null
				 && !isEmpty(homepagePatched.getCysj()) && homepagePatched.getCysj().split("\\Q:\\E").length > 3) {
					isPatched = true;
					homepagePatched.setCysj(homepagePatched.getCysj().substring(0, 19));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "出院时间（时）";
				if (((checkRecord = checkedIsError(checkList, "CYSJ_S", CHECK_TYPE_NONEMPTY, null)) != null
				 || (checkRecord = checkedIsError(checkList, "CYSJ_S", CHECK_TYPE_VALIDITY, "格式不正确，小时数（24小时制）")) != null
				 || (checkRecord = checkedIsError(checkList, "CYSJ_S", CHECK_TYPE_LOGICAL, "与出院时间(CYSJ)不匹配")) != null)
				 && isDatetime(homepagePatched.getCysj())) {
					isPatched = true;
					Calendar _cysj = Calendar.getInstance();
					_cysj.setTime(FormatUtil.parseDateTime(homepagePatched.getCysj()));
					homepagePatched.setCysjS(_cysj.get(Calendar.HOUR_OF_DAY)+"");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "出院科别";
				if ((checkRecord = checkedIsError(checkList, "CYKB", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC023-科室代码》")) != null
				 && compareDic(rc023, RecordRangeType.name, homepagePatched.getCykb())) {
					isPatched = true;
					homepagePatched.setCykb(getDic(rc023, RecordRangeType.name, homepagePatched.getCykb(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "入院途径";
				if ((checkRecord = checkedIsError(checkList, "RYTJ", CHECK_TYPE_NONEMPTY, null)) != null
				 && (!isEmpty(homepagePatched.getJbdm()) || !isEmpty(homepagePatched.getJbbm()))) {
					isPatched = true;
					//科别为急诊或夜间(>16:40)，记急诊，否则门诊
					if (homepagePatched.getRykb().equals("50.16") || homepagePatched.getCykb().equals("50.16")//急诊科系
					|| isNight(homepagePatched.getRysj()))//夜间(>16:40)
						homepagePatched.setRytj("2");
					else
						homepagePatched.setRytj("1");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "RYTJ", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC026-入院途径代码》")) != null
						&& compareDic(rc026, RecordRangeType.name, homepagePatched.getRytj())) {
					isPatched = true;
					homepagePatched.setRytj(getDic(rc026, RecordRangeType.name, homepagePatched.getRytj(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "实际住院(天)";
				if (isDatetime(homepagePatched.getCysj()) && isDatetime(homepagePatched.getRysj())) {
					long _cysj = FormatUtil.parseDateTime(homepagePatched.getCysj()).getTime();
					long _rysj = FormatUtil.parseDateTime(homepagePatched.getRysj()).getTime();
					int _sjzy = Integer.parseInt(((_cysj - _rysj)/86400000 + 1)+"");
					_sjzy = _sjzy == 0 ? 1 : _sjzy;
					
					if ((checkRecord = checkedIsError(checkList, "SJZY", CHECK_TYPE_NONEMPTY, null)) != null
					 || (checkRecord = checkedIsError(checkList, "SJZY", CHECK_TYPE_VALIDITY, "格式或天数不正确，必需大于0的整数")) != null
					 || (checkRecord = checkedIsError(checkList, "SJZY", CHECK_TYPE_LOGICAL, "与实际出、入院时间计算不符(" + _sjzy + ")")) != null) {
						homepagePatched.setSjzy(_sjzy+"");
						checkRecord.setFlag("PATCHED");
						homepagePatched.addCheck(checkRecord);
					}
				}
				status = "门(急)诊中医诊断";
				if ((checkRecord = checkedIsError(checkList, "JBDM", CHECK_TYPE_NONEMPTY, null)) != null) {
					homepagePatched.setJbdm("-");
					homepagePatched.setMzdZyzd("-");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "JBDM", CHECK_TYPE_VALIDITY, "与《中医病症分类代码国标版95（TCM95）》不匹配")) != null) {
					IcdTransform d = transDiagno(homepagePatched.getJbdm(), homepagePatched.getMzdZyzd(), RecordRangeType.DIAGNOSIS_CHINESE);
					homepagePatched.setJbdm(d.getCode());
					homepagePatched.setMzdZyzd(d.getDiagno());
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "门(急)诊西医诊断";
				if ((checkRecord = checkedIsError(checkList, "JBBM", CHECK_TYPE_NONEMPTY, null)) != null) {
					homepagePatched.setJbbm("-");
					homepagePatched.setMzzdXyzd("-");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "JBBM", CHECK_TYPE_VALIDITY, "与《疾病分类代码国家临床版2.0编码（ICD-10）》不匹配")) != null) {
					IcdTransform d = transDiagno(homepagePatched.getJbbm(), homepagePatched.getMzzdXyzd(), RecordRangeType.DIAGNOSIS_WESTERN);
					homepagePatched.setJbbm(d.getCode());
					homepagePatched.setMzzdXyzd(d.getDiagno());
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "实施临床路径";
				if ((checkRecord = checkedIsError(checkList, "SSLCLJ", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setSslclj("3");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "SSLCLJ", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC040-实施临床路径字典》")) != null
						&& compareDic(rc040, RecordRangeType.name, homepagePatched.getSslclj())) {
					isPatched = true;
					homepagePatched.setSslclj(getDic(rc040, RecordRangeType.name, homepagePatched.getSslclj(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "使用医疗机构中药制剂";
				if ((checkRecord = checkedIsError(checkList, "ZYYJ", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setZyyj(homepagePatched.getZyzjf() > 0 ? "1" : "2");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "ZYYJ", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC016》")) != null
						&& compareDic(rc016, RecordRangeType.name, homepagePatched.getZyyj())) {
					isPatched = true;
					homepagePatched.setZyyj(getDic(rc016, RecordRangeType.name, homepagePatched.getZyyj(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "使用中医诊疗设备";
				if ((checkRecord = checkedIsError(checkList, "ZYZLSB", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setZyzlsb("2");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "ZYZLSB", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC016》")) != null
						&& compareDic(rc016, RecordRangeType.name, homepagePatched.getZyzlsb())) {
					isPatched = true;
					homepagePatched.setZyzlsb(getDic(rc016, RecordRangeType.name, homepagePatched.getZyzlsb(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "使用中医诊疗技术";
				if ((checkRecord = checkedIsError(checkList, "ZYZLJS", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setZyzljs((homepagePatched.getZylZyzd()+homepagePatched.getZyzl()+homepagePatched.getZyqt()) > 0 ? "1" : "2");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "ZYZLJS", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC016》")) != null
						&& compareDic(rc016, RecordRangeType.name, homepagePatched.getZyzljs())) {
					isPatched = true;
					homepagePatched.setZyzljs(getDic(rc016, RecordRangeType.name, homepagePatched.getZyzljs(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "ZYZLJS", CHECK_TYPE_LOGICAL, "使用中医诊疗技术，中医类（中医和民族医医疗服务）费应大于0")) != null) {
					homepagePatched.setZyzljs("2");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "辩证施护";
				if ((checkRecord = checkedIsError(checkList, "BZSH", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setBzsh("2");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "BZSH", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC016》")) != null
						&& compareDic(rc016, RecordRangeType.name, homepagePatched.getBzsh())) {
					isPatched = true;
					homepagePatched.setBzsh(getDic(rc016, RecordRangeType.name, homepagePatched.getBzsh(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				/*
				 * ZB	主病出院中医诊断
				 * ZB_JBBM	主病疾病编码
				 * ZB_RYBQ	主病入院病情
				 */
				status = "主病出院中医诊断";
				if ((checkRecord = checkedIsError(checkList, "ZB_JBBM", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setZbJbbm("-");
					homepagePatched.setZb("-");
					homepagePatched.setZbRybq("-");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "ZB_JBBM", CHECK_TYPE_VALIDITY, "与《中医病症分类代码国标版95（TCM95）》不匹配")) != null) {
					IcdTransform d = transDiagno(homepagePatched.getZbJbbm(), homepagePatched.getZb(), RecordRangeType.DIAGNOSIS_CHINESE);
					homepagePatched.setZbJbbm(d.getCode());
					homepagePatched.setZb(d.getDiagno());
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				/*
				 * ZYZD	出院主要诊断名称(西医)
				 * ZYZD_JBBM	出院主要诊断编码(西医)
				 * XY_RYBQ	出院主要诊断入院病情(西医)
				 */
				status = "出院主要诊断(西医)";
				if ((checkRecord = checkedIsError(checkList, "ZYZD_JBBM", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setZyzdJbbm("-");
					homepagePatched.setZyzd("-");
					homepagePatched.setXyRybq("-");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "ZYZD_JBBM", CHECK_TYPE_VALIDITY, "与《疾病分类代码国家临床版2.0编码（ICD-10）》不匹配")) != null) {
					IcdTransform d = transDiagno(homepagePatched.getZyzdJbbm(), homepagePatched.getZyzd(), RecordRangeType.DIAGNOSIS_WESTERN);
					homepagePatched.setZyzdJbbm(d.getCode());
					homepagePatched.setZyzd(d.getDiagno());
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				/*
				 * ZZ1-ZZ7	主证出院中医诊断
				 * ZZ_JBBM1-ZZ_JBBM7	主证疾病编码
				 * ZZ_RYBQ1-ZZ_RYBQ7	主证住入院病情
				 */
				for (int i = 1; i <= 7; i ++) {
					status = "主证出院中医诊断" + i;
					String _code = "Zzjbbm" + i;
					String _diagno = "Zz" + i;
					String _rybq = "ZzRybq" + i;
					if ((checkRecord = checkedIsError(checkList, "ZZ_JBBM" + i, CHECK_TYPE_NONEMPTY, null)) != null) {
						homepagePatched.getClass().getMethod("set" + _code, new Class[] {String.class}).invoke(homepagePatched, new Object[] {"-"});
						homepagePatched.getClass().getMethod("set" + _diagno, new Class[] {String.class}).invoke(homepagePatched, new Object[] {"-"});
						homepagePatched.getClass().getMethod("set" + _rybq, new Class[] {String.class}).invoke(homepagePatched, new Object[] {"-"});
						checkRecord.setFlag("PATCHED");
						homepagePatched.addCheck(checkRecord);
					} else if ((checkRecord = checkedIsError(checkList, "ZZ_JBBM" + i, CHECK_TYPE_VALIDITY, "与《中医病症分类代码国标版95（TCM95）》不匹配")) != null) {
						IcdTransform d = transDiagno(homepagePatched.getClass().getMethod("get" + _code, new Class[0]).invoke(homepagePatched, new Object[0]).toString(),
													 homepagePatched.getClass().getMethod("get" + _diagno, new Class[0]).invoke(homepagePatched, new Object[0]).toString(),
													 RecordRangeType.DIAGNOSIS_CHINESE);
						homepagePatched.getClass().getMethod("set" + _code, new Class[] {String.class}).invoke(homepagePatched, new Object[] {d.getCode()});
						homepagePatched.getClass().getMethod("set" + _diagno, new Class[] {String.class}).invoke(homepagePatched, new Object[] {d.getDiagno()});
						checkRecord.setFlag("PATCHED");
						homepagePatched.addCheck(checkRecord);
					}
				}
				/*
				 * QTZD1-QTZD40	出院其他诊断名称(西医)
				 * ZYZD_JBBM1-ZYZD_JBBM40	出院其他诊断编码(西医)
				 * RYBQ1-RYBQ40	出院其他诊断入院病情(西医)
				 */
				for (int i = 1; i <= 40; i ++) {
					status = "出院其他诊断(西医)" + i;
					String _code = "ZyzdJbbm" + i;
					String _diagno = "Qtzd" + i;
					String _rybq = "Rybq" + i;
					if ((checkRecord = checkedIsError(checkList, "ZYZD_JBBM1" + i, CHECK_TYPE_NONEMPTY, null)) != null) {
						homepagePatched.getClass().getMethod("set" + _code, new Class[] {String.class}).invoke(homepagePatched, new Object[] {"-"});
						homepagePatched.getClass().getMethod("set" + _diagno, new Class[] {String.class}).invoke(homepagePatched, new Object[] {"-"});
						homepagePatched.getClass().getMethod("set" + _rybq, new Class[] {String.class}).invoke(homepagePatched, new Object[] {"-"});
						checkRecord.setFlag("PATCHED");
						homepagePatched.addCheck(checkRecord);
					} else if ((checkRecord = checkedIsError(checkList, "ZYZD_JBBM1" + i, CHECK_TYPE_VALIDITY, "与《疾病分类代码国家临床版2.0编码（ICD-10）》不匹配")) != null) {
						IcdTransform d = transDiagno(homepagePatched.getClass().getMethod("get" + _code, new Class[0]).invoke(homepagePatched, new Object[0]).toString(),
													 homepagePatched.getClass().getMethod("get" + _diagno, new Class[0]).invoke(homepagePatched, new Object[0]).toString(),
													 RecordRangeType.DIAGNOSIS_WESTERN);
						homepagePatched.getClass().getMethod("set" + _code, new Class[] {String.class}).invoke(homepagePatched, new Object[] {d.getCode()});
						homepagePatched.getClass().getMethod("set" + _diagno, new Class[] {String.class}).invoke(homepagePatched, new Object[] {d.getDiagno()});
						checkRecord.setFlag("PATCHED");
						homepagePatched.addCheck(checkRecord);
					}
				}
				status = "损伤、中毒外部原因";
				if ((checkRecord = checkedIsError(checkList, "JBBM1", CHECK_TYPE_VALIDITY, "与《疾病分类代码国家临床版2.0(ICD-10)》不匹配")) != null) {
					IcdTransform d = transDiagno(homepagePatched.getJbbm1(), homepagePatched.getWbyy(), RecordRangeType.DIAGNOSIS_WESTERN);
					homepagePatched.setJbbm1(d.getCode());
					homepagePatched.setWbyy(d.getDiagno());
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "病理诊断";
				if ((checkRecord = checkedIsError(checkList, "JBBM2", CHECK_TYPE_VALIDITY, "与《疾病分类代码国家临床版2.0版肿瘤形态学编码(M码)》不匹配")) != null) {
					IcdTransform d = transDiagno(homepagePatched.getJbbm2(), homepagePatched.getBlzd(), RecordRangeType.DIAGNOSIS_PATHOLOGY);
					homepagePatched.setJbbm2(d.getCode());
					homepagePatched.setBlzd(d.getDiagno());
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "有无药物过敏";
				if ((checkRecord = checkedIsError(checkList, "YWGM", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setYwgm("1");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "YWGM", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC028-药物过敏》")) != null
						&& compareDic(rc028, RecordRangeType.name, homepagePatched.getYwgm())) {
					isPatched = true;
					homepagePatched.setYwgm(getDic(rc028, RecordRangeType.name, homepagePatched.getYwgm(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "ABO血型";
				if ((checkRecord = checkedIsError(checkList, "XX", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					if (homepagePatched.getXf() > 0) homepagePatched.setXx("5");
					else homepagePatched.setXx("6");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "XX", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC030-血型编码》")) != null
						&& compareDic(rc030, RecordRangeType.name, homepagePatched.getXx())) {
					isPatched = true;
					homepagePatched.setXx(getDic(rc030, RecordRangeType.name, homepagePatched.getXx(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "Rh血型";
				if ((checkRecord = checkedIsError(checkList, "RH", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					if (homepagePatched.getXf() > 0) homepagePatched.setRh("3");
					else homepagePatched.setRh("4");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "RH", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC031-Rh血型》")) != null
						&& compareDic(rc031, RecordRangeType.name, homepagePatched.getRh())) {
					isPatched = true;
					homepagePatched.setRh(getDic(rc031, RecordRangeType.name, homepagePatched.getRh(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "病案质量";
				if ((checkRecord = checkedIsError(checkList, "BAZL", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC011-病案质量》")) != null
				 && compareDic(rc011, RecordRangeType.name, homepagePatched.getBazl())) {
					isPatched = true;
					homepagePatched.setBazl(getDic(rc011, RecordRangeType.name, homepagePatched.getBazl(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				status = "质控日期";
				if ((checkRecord = checkedIsError(checkList, "ZKRQ", CHECK_TYPE_VALIDITY, "格式不正确，yyyy-MM-dd")) != null
				 && homepagePatched.getZkrq().split("\\Q:\\E").length > 3) {
					isPatched = true;
					homepagePatched.setZkrq(homepagePatched.getZkrq().substring(0, 19));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}
				/*
				 * SSJCZBM1	主要手术操作编码
				 * SSJCZMC1	主要手术操作名称
				 * SSJCZRQ1	主要手术操作日期
				 * SHJB1	主要手术操作级别
				 * SZ1	主要手术操作术者
				 * YZ1	主要手术操作Ⅰ助
				 * EZ1	主要手术操作Ⅱ助
				 * QKDJ1	主要手术操作切口愈合等级
				 * QKYLB1	主要手术操作切口愈合类别
				 * MZFS1	主要手术操作麻醉方式
				 * MZYS1	主要手术操作麻醉医师
				 * SSJCZBM2-SSJCZBM41	其他手术操作编码
				 * SSJCZMC2-SSJCZMC41	其他手术操作名称
				 * SSJCZRQ2-SSJCZRQ41	其他手术操作日期
				 * SHJB2-SHJB41	其他手术操作级别
				 * SZ2-SZ41	其他手术操作术者
				 * YZ2-YZ41	其他手术操作Ⅰ助
				 * EZ2-EZ41	其他手术操作Ⅱ助
				 * QKDJ2-QKDJ41	其他手术操作切口愈合等级
				 * QKYLB2-QKYLB41	切口愈合类别
				 * MZFS2-MZFS41	其他手术操作麻醉方式
				 * MZYS2-MZYS41	其他手术操作麻醉医师
				 */
				for (int i = 1; i <= 41; i ++) {
					status = "主要（其他）手术操作" + i;
					String _code = "Ssjczbm" + i;
					String _diagno = "Ssjczmc" + i;
					if ((checkRecord = checkedIsError(checkList, "SSJCZBM" + i, CHECK_TYPE_VALIDITY, "与《手术操作分类代码国家临床版2.0编码（ICD-9-CM3）》不匹配")) != null) {
						IcdTransform d = transDiagno(homepagePatched.getClass().getMethod("get" + _code, new Class[0]).invoke(homepagePatched, new Object[0]).toString(),
													 homepagePatched.getClass().getMethod("get" + _diagno, new Class[0]).invoke(homepagePatched, new Object[0]).toString(),
													 RecordRangeType.DIAGNOSIS_OPERATION
													);
						homepagePatched.getClass().getMethod("set" + _code, new Class[] {String.class}).invoke(homepagePatched, new Object[] {d.getCode()});
						homepagePatched.getClass().getMethod("set" + _diagno, new Class[] {String.class}).invoke(homepagePatched, new Object[] {d.getDiagno()});
						checkRecord.setFlag("PATCHED");
						homepagePatched.addCheck(checkRecord);
					}
				}
				status = "是否有出院31天内再住院计划";
				if ((checkRecord = checkedIsError(checkList, "ZZYJH", CHECK_TYPE_NONEMPTY, null)) != null) {
					isPatched = true;
					homepagePatched.setZzyjh("2");
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				} else if ((checkRecord = checkedIsError(checkList, "ZZYJH", CHECK_TYPE_VALIDITY, "代码不正确，《值域范围参考RC016》")) != null
						&& compareDic(rc016, RecordRangeType.name, homepagePatched.getZzyjh())) {
					isPatched = true;
					homepagePatched.setZzyjh(getDic(rc016, RecordRangeType.name, homepagePatched.getZzyjh(), RecordRangeType.code));
					checkRecord.setFlag("PATCHED");
					homepagePatched.addCheck(checkRecord);
				}

				if ((homepage.getFlag() == null || !homepage.getFlag().equals("orig")) && isPatched) {
					homepage.setFlag("orig");
					patchedList.add(homepagePatched);
					patchedList.add(homepage);
				}
			}
			return patchedList;
		}
		catch(Exception ex) {
			throw new Exception("数据修正[" + status + "]时发生错误：" + ex.getMessage());
		}
	}

	public boolean compareDic(Dictionary[] dic, RecordRangeType type, String keyword) {
		boolean result = false;
		initialize();
		if (keyword != null && !keyword.isEmpty()) {
			for (Dictionary d : dic) {
				if (type.equals(RecordRangeType.name) && dic.equals(rc036)) {
					if (keyword.length() < 2) return false;
					result = d.getLabel().substring(0, 2).equals(keyword.substring(0, 2));
				} else {
					if (type == RecordRangeType.code) {
						result = d.getCode().equals(keyword);
					} else if (type == RecordRangeType.name) {
						result = d.getLabel().equals(keyword);
					}
				}
				if (result) return result;
			}
		}
		return result;
	}

	public String getDic(Dictionary[] dic, RecordRangeType type, String keyword, RecordRangeType returnType) {
		String result = "";
		initialize();
		if (keyword != null && !keyword.isEmpty()) {
			for (Dictionary d : dic) {
				if (type.equals(RecordRangeType.name) && dic.equals(rc036)) {
					if (keyword.length() > 2 && d.getLabel().substring(0, 2).equals(keyword.substring(0, 2))) result = returnType.equals(RecordRangeType.code) ? d.getCode() : d.getLabel();
				} else if ((type == RecordRangeType.code && d.getCode().equals(keyword)) || (type == RecordRangeType.name && d.getLabel().equals(keyword))) {
					result = returnType.equals(RecordRangeType.code) ? d.getCode() : d.getLabel();
				}
				if (!result.equals("")) return result;
			}
		}
		return result;
	}

	public boolean compareDate(String gdate, String ldate, Integer age) throws Exception {
		initialize();
		Date _gdate = null, _ldate = null;
		Calendar _gcal = null, _lcal = null;
		try {
			_gdate = FormatUtil.parseDate(gdate);
			_gcal = Calendar.getInstance();
			_gcal.setTime(_gdate);
			if (ldate != null) {
				_ldate = FormatUtil.parseDate(ldate);
				_lcal = Calendar.getInstance();
				_lcal.setTime(_ldate);
			}

			if (age != null && ldate != null) 
				if (_gcal.get(Calendar.YEAR) - _lcal.get(Calendar.YEAR) != age) throw new Exception("年龄与出生日期不符");

			return true;
		}
		catch (ParseException ex) {
			throw new Exception("格式错误(yyyy-MM-dd)");
		}
		catch (Exception ex) {
			throw new Exception("发生错误：" + ex.getMessage());
		}
	}

	public boolean diagnosisVerify(String code, String diagnosis, RecordRangeType kind, RecordRangeType type) throws Exception {
		initialize();
		boolean result = false;

		if (kind.equals(RecordRangeType.ICD2)) {
			IcdTransform[] diagnoses = null;
			if (kind.equals(RecordRangeType.ICD2) && type.equals(RecordRangeType.DIAGNOSIS_CHINESE)) diagnoses = icd2DiagnosisChinese;
			else if (kind.equals(RecordRangeType.ICD2) && type.equals(RecordRangeType.DIAGNOSIS_WESTERN)) diagnoses = icd2DiagnosisWestern;
			else if (kind.equals(RecordRangeType.ICD2) && type.equals(RecordRangeType.DIAGNOSIS_OPERATION)) diagnoses = icd2DiagnosisOperation;
			else if (kind.equals(RecordRangeType.ICD2) && type.equals(RecordRangeType.DIAGNOSIS_PATHOLOGY)) diagnoses = icd2DiagnosisPathology;
	
			for (IcdTransform diagno : diagnoses) {
				result = diagno.getCode().equals(code) && (diagnosis == null ? true : diagno.getDiagno().equals(diagnosis));
				if (result) break;
			}
		} else if (kind.equals(RecordRangeType.HIS)) {
			VIcdTransform[] diagnoses = null;
			if (kind.equals(RecordRangeType.HIS) && type.equals(RecordRangeType.DIAGNOSIS_CHINESE)) diagnoses = hisDiagnosisChinese;
			else if (kind.equals(RecordRangeType.HIS) && type.equals(RecordRangeType.DIAGNOSIS_WESTERN)) diagnoses = hisDiagnosisWestern;
			else if (kind.equals(RecordRangeType.HIS) && type.equals(RecordRangeType.DIAGNOSIS_OPERATION)) diagnoses = hisDiagnosisOperation;
			else if (kind.equals(RecordRangeType.HIS) && type.equals(RecordRangeType.DIAGNOSIS_PATHOLOGY)) diagnoses = hisDiagnosisPathology;
	
			for (VIcdTransform diagno : diagnoses) {
				result = diagno.getId().getCode().equals(code) && (diagnosis == null ? true : diagno.getId().getDiagno().equals(diagnosis));
				if (result) break;
			}
		}

		return result;
	}

	public boolean isDate(String date) {
		try {
			FormatUtil.parseDate(date);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

	public boolean isDatetime(String datetime) {
		try {
			FormatUtil.parseDateTime(datetime);
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

	public boolean isNumber(String num, boolean isInt) {
		try {
			if (isInt) Long.parseLong(num);
			else Double.parseDouble(num);

			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

	public boolean isInteger(String num) {
		return isNumber(num, true);
	}

	public boolean isFloat(String num) {
		return isNumber(num, false);
	}

	public int getInteger(String num) {
		try {
			return Integer.parseInt(num);
		}
		catch(Exception ex) {
			return 0;
		}
	}

	public boolean isEmpty(String value) {
		return value.equals("-") || value.equals("─");
	}

	public Integer getAge(String rysj, String csrq, String sfzh, Integer defaultValue) {
		Integer ret = defaultValue;
		try {
			csrq = ((csrq == null || !isDate(csrq)) && sfzh != null && sfzh.length() == 18) ? sfzh.substring(5, 13) : csrq;
			if (rysj == null || csrq == null || !isDatetime(rysj) || !isDate(csrq)) return ret;
			Calendar _rysj = Calendar.getInstance();
			Calendar _csrq = Calendar.getInstance();
			_rysj.setTime(FormatUtil.parseDateTime(rysj));
			_csrq.setTime(FormatUtil.parseDate(csrq));
			ret = _rysj.get(Calendar.YEAR) - _csrq.get(Calendar.YEAR);
		}
		catch(Exception e) {}

		return ret;
	}

	public InpatientHomepageAnalyCheck checkedIsError(InpatientHomepageAnalyCheck[] list, String name, String type, String message) throws CloneNotSupportedException {
		for (InpatientHomepageAnalyCheck check : list) {
			if (check.getName().equalsIgnoreCase(name) && check.getType().equals(type) && (message == null || check.getMessage().equals(message))) {
				return check.clone();
			}
		}
		return null;
	}

	public IcdTransform transDiagno(String code, String diagnosis, RecordRangeType type) throws Exception {
		IcdTransform rest = new IcdTransform();
		rest.setOrigCode(code);
		rest.setOriDiagno(diagnosis);
		rest.setCode(code);
		rest.setDiagno(diagnosis);

		if (diagnosisVerify(code, diagnosis, RecordRangeType.HIS, type)) {
			VIcdTransform[] diagnos = null;
			if (type.equals(RecordRangeType.DIAGNOSIS_CHINESE)) {
				diagnos = hisDiagnosisChinese;
			} else if (type.equals(RecordRangeType.DIAGNOSIS_WESTERN)) {
				diagnos = hisDiagnosisWestern;
			} else if (type.equals(RecordRangeType.DIAGNOSIS_OPERATION)) {
				diagnos = hisDiagnosisOperation;
			} else if (type.equals(RecordRangeType.DIAGNOSIS_PATHOLOGY)) {
				diagnos = hisDiagnosisPathology;
			}

			for (VIcdTransform diagno : diagnos) {
				if (diagno.getId().getCode().equals(code) && diagno.getId().getDiagno().equals(diagnosis)) {
					rest.setCode(diagno.getIcd2Code());
					rest.setDiagno(diagno.getIcd2Diagno());
				}
			}

			return rest;
		} else if (diagnosisVerify(code, diagnosis, RecordRangeType.ICD2, type)) {
			IcdTransform[] diagnos = null;
			if (type.equals(RecordRangeType.DIAGNOSIS_CHINESE)) {
				diagnos = icd2DiagnosisChinese;
			} else if (type.equals(RecordRangeType.DIAGNOSIS_WESTERN)) {
				diagnos = icd2DiagnosisWestern;
			} else if (type.equals(RecordRangeType.DIAGNOSIS_OPERATION)) {
				diagnos = icd2DiagnosisOperation;
			} else if (type.equals(RecordRangeType.DIAGNOSIS_PATHOLOGY)) {
				diagnos = icd2DiagnosisPathology;
			}

			List<IcdTransform> list = new ArrayList<IcdTransform>();
			for (IcdTransform diagno : diagnos) {
				if (diagno.getCode().equals(code)) {
					list.add(diagno);
				}
			}
			if (list.size() == 1) {
				rest.setCode(list.get(0).getCode());
				rest.setDiagno(list.get(0).getCode());
			}
		}

		return rest;
	}

	public boolean isNight(String datetime) throws Exception {
		if (datetime != null && isDatetime(datetime)) {
			Calendar c = Calendar.getInstance();
			c.setTime(FormatUtil.parseDateTime(datetime));
			float time = Float.parseFloat(c.get(Calendar.HOUR_OF_DAY) + "." + c.get(Calendar.MINUTE));

			return time > NIGHT_TIME;
		}

		return false;
	}

	public String patchAddress(String address) {
		String addr = address.replaceFirst("-", "").replaceFirst("─", "");
		String ret = this.DEFAULT_CITY + addr;
		for (DicAdministrativeDivision city : administrativeDivisionCity) {
			if (addr.length() >= city.getName().length() - 1 && city.getName().equals(addr.substring(0, city.getName().length() - 1))) {
				return city.getProv() + addr;
			} else if (addr.length() >= city.getShortName().length() - 1 && city.getShortName().equals(addr.substring(0, city.getShortName().length() - 1))) {
				return city.getProv() + city.getName() + addr.substring(city.getShortName().length() - 1);
			}
		}
		for (DicAdministrativeDivision prov : administrativeDivisionProv) {
			if (addr.length() >= prov.getShortName().length() - 1 && prov.getShortName().equals(addr.substring(0, prov.getShortName().length() - 1))) {
				return prov.getName() + addr.substring(prov.getShortName().length() - 1);
			}
		}

		return ret;
	}

	public HomepageDao getHomepageDao() {
		return homepageDao;
	}

	public void setHomepageDao(HomepageDao homepageDao) {
		this.homepageDao = homepageDao;
	}

	public List<InpatientHomepageAnaly> getHomepageList() {
		return homepageList;
	}

	public void setHomepageList(List<InpatientHomepageAnaly> homepageList) {
		this.homepageList = homepageList;
	}

}
