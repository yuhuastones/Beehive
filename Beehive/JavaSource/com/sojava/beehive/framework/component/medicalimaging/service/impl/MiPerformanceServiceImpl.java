package com.sojava.beehive.framework.component.medicalimaging.service.impl;

import com.sojava.beehive.framework.component.medicalimaging.bean.VMiExecutedStaffPerformance;
import com.sojava.beehive.framework.component.medicalimaging.bean.WorkStatistic;
import com.sojava.beehive.framework.component.medicalimaging.dao.MiPerformanceDao;
import com.sojava.beehive.framework.component.medicalimaging.service.MiPerformanceService;
import com.sojava.beehive.framework.math.Arith;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

@Service
public class MiPerformanceServiceImpl implements MiPerformanceService {

	@Resource private MiPerformanceDao miPerformanceDao;

	@Override
	@SuppressWarnings("unchecked")
	public void merit(Double budget, Double overtimeCost, Double nurseRate, Double medicalRate, Double manageRate, int year, int month, Date begin, Date end, String dept) throws Exception {
		WorkStatistic workStatistic;
		List<WorkStatistic> list = (List<WorkStatistic>) miPerformanceDao.query(WorkStatistic.class, new Criterion[]{Restrictions.eq("year", (Integer) year), Restrictions.eq("month", (Integer) month), Restrictions.eq("dept", dept)}, null, null, false);
		if (list.size() > 0) workStatistic = list.get(0);
		else workStatistic = new WorkStatistic();
		workStatistic.setBudget(budget);
		workStatistic.setOvertimeCost(overtimeCost);
		workStatistic.setNurseRate(nurseRate);
		workStatistic.setNurseCost(Arith.mul(
										Arith.div(workStatistic.getNurseRate(), 100),
										(workStatistic.getBudget()-workStatistic.getOvertimeCost())
									));
		workStatistic.setPerformanceTotal(workStatistic.getBudget() - workStatistic.getOvertimeCost() - workStatistic.getNurseCost());
		workStatistic.setMedicalRate(medicalRate);
		workStatistic.setMedicalTotal(Arith.mul(
										Arith.div(workStatistic.getMedicalRate(), 100),
										workStatistic.getPerformanceTotal()
									));
		workStatistic.setManageRate(manageRate);
		workStatistic.setManageTotal(Arith.mul(
										Arith.div(workStatistic.getManageRate(), 100),
										workStatistic.getPerformanceTotal()
									));
		workStatistic.setYear(year);
		workStatistic.setMonth(month);
		workStatistic.setBeginDate(begin);
		workStatistic.setEndDate(end);
		workStatistic.setDept(dept);
		miPerformanceDao.calRbrvsPrice(workStatistic);
	}

	@Override
	public HSSFWorkbook writeExcelOfStaffPerformance(File masterFile, WorkStatistic workStatistic, VMiExecutedStaffPerformance[] list) throws Exception {
		HSSFWorkbook book = generateExcelOfStaffPerformance(workStatistic, list);
		FileInputStream in = null;
		try {
			in = new FileInputStream(masterFile);
			HSSFWorkbook masterBook = new HSSFWorkbook(in);
			HSSFSheet sheet = book.getSheetAt(0);
			HSSFSheet masterSheet = masterBook.getSheetAt(0);
			for (int r = 4; r <= sheet.getLastRowNum(); r ++) {
				HSSFRow row = sheet.getRow(r);
				HSSFCell cell = row.getCell(0);
				String text = cell.getStringCellValue();
				if (text.equalsIgnoreCase("")
						|| text.replaceAll("\\Q　\\E", "").replaceAll("\\Q \\E", "").equalsIgnoreCase("合计")
						|| text.replaceAll("\\Q　\\E", "").replaceAll("\\Q \\E", "").equalsIgnoreCase("总计")
						|| isMerged(cell, sheet)
					) continue;
				HSSFCell nurseCell = row.getCell(3);
				HSSFCell overtimeCell = row.getCell(4);
				Object[] ret = getMasterValue(masterSheet, text, 3, 4);
				if (ret[0] instanceof Double) {
					nurseCell.setCellValue(Double.parseDouble(ret[0].toString()));
				} else {
					nurseCell.setCellValue(ret[0].toString());
				}
				if (ret[1] instanceof Double) {
					overtimeCell.setCellValue(Double.parseDouble(ret[1].toString()));
				} else {
					overtimeCell.setCellValue(ret[1].toString());
				}
			}
			autoSizeColumn(sheet);
		}
		finally {
			in.close();
		}

		return book;
	}

	public Object[] getMasterValue(HSSFSheet sheet, String key, int... columnIndexs) {
		List<Object> result = new ArrayList<Object>();
		for (int r = 0; r <= sheet.getLastRowNum(); r ++) {
			HSSFRow row = sheet.getRow(r);
			HSSFCell firstCell = row.getCell(0);
			String text = firstCell.getStringCellValue();
			if (text.equalsIgnoreCase("")
					|| text.replaceAll("\\Q　\\E", "").replaceAll("\\Q \\E", "").equalsIgnoreCase("合计")
					|| text.replaceAll("\\Q　\\E", "").replaceAll("\\Q \\E", "").equalsIgnoreCase("总计")
					|| isMerged(firstCell, sheet)
					|| !key.equalsIgnoreCase(text)
				) continue;

			HSSFCell cell = null;
			for (int columnIndex : columnIndexs) {
				cell = row.getCell(columnIndex);
				Object ret = null;
				switch(cell.getCellType()) {
					case HSSFCell.CELL_TYPE_BLANK:
						ret = 0d;
						break;
					case HSSFCell.CELL_TYPE_FORMULA:
						ret = cell.getCellFormula();
						break;
					case HSSFCell.CELL_TYPE_NUMERIC:
						ret = cell.getNumericCellValue();
						break;
					default:
						ret = cell.getStringCellValue();
						break;
				}
				result.add(ret);
			}
		}

		return result.toArray(new Object[0]);
	}

	public boolean isMerged(HSSFCell cell, HSSFSheet sheet) {
		boolean ret = false;
		for (int i = 0; i < sheet.getNumMergedRegions(); i ++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			ret = range.isInRange(cell.getRowIndex(), cell.getColumnIndex());
		}

		return ret;
	}

	public void autoSizeColumn(HSSFSheet sheet) {
		HSSFRow row = sheet.getRow(0);
		for (int i = 0; i <= row.getLastCellNum(); i ++) {
			sheet.autoSizeColumn(i);
		}
	}

	@Override
	public HSSFWorkbook generateExcelOfStaffPerformance(WorkStatistic workStatistic, VMiExecutedStaffPerformance[] list) throws Exception {
		int rowIndex = 0;
		int dataRowIndex = 0;
		List<Integer> amountRowIndex = new ArrayList<Integer>();
		HSSFWorkbook book = new HSSFWorkbook();
		String title = workStatistic.getDept() + workStatistic.getYear() + "年" + workStatistic.getMonth() + "月";
		String typeFormat = "_(* #,##0.00_);_(* (#,##0.00);_(* \"-\"??_);_(@_)";
		HSSFSheet sheet = book.createSheet(title);
		/*
		 * Header of Row
		 */
		HSSFRow headerRow = sheet.createRow(rowIndex ++);
		headerRow.setHeightInPoints((short) 25);
		HSSFCell cell = HSSFCellUtil.createCell(headerRow, 0, title + "绩效核算表");

		HSSFFont headerFont = book.createFont();
		headerFont.setFontName("黑体");
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headerFont.setFontHeightInPoints((short) 14);

		HSSFCellStyle headerStyle = book.createCellStyle();
		headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		headerStyle.setFont(headerFont);
		cell.setCellStyle(headerStyle);

		sheet.addMergedRegion(CellRangeAddress.valueOf("A1:J1"));

		HSSFFont titleFont = book.createFont();
		titleFont.setFontName("宋体");
		titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleFont.setFontHeightInPoints((short) 12);

		HSSFCellStyle titleStyle = book.createCellStyle();
		titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		titleStyle.setFont(titleFont);
		titleStyle.setBorderTop((short) 1);
		titleStyle.setBorderRight((short) 1);
		titleStyle.setBorderBottom((short) 1);
		titleStyle.setBorderLeft((short) 1);

		/*
		 * Title of Row
		 */
		HSSFRow titleRow = sheet.createRow(rowIndex ++);
		titleRow.setHeightInPoints((short) 15);
		HSSFCellUtil.createCell(titleRow, 0, "姓名", titleStyle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("A2:A3"));
		HSSFCellUtil.createCell(titleRow, 1, "医疗绩效", titleStyle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("B2:B3"));
		HSSFCellUtil.createCell(titleRow, 2, "经营管理绩效", titleStyle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("C2:C3"));
		HSSFCellUtil.createCell(titleRow, 3, "护理绩效", titleStyle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("D2:D3"));
		HSSFCellUtil.createCell(titleRow, 4, "加班费", titleStyle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("E2:E3"));
		HSSFCellUtil.createCell(titleRow, 5, "奖金合计", titleStyle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("F2:F3"));
		HSSFCellUtil.createCell(titleRow, 6, "工作量(点值：" + workStatistic.getPointValue() + ")", titleStyle);
		HSSFCellUtil.createCell(titleRow, 7, "", titleStyle);
		HSSFCellUtil.createCell(titleRow, 8, "", titleStyle);
		HSSFCellUtil.createCell(titleRow, 9, "", titleStyle);
		sheet.addMergedRegion(CellRangeAddress.valueOf("G2:J2"));

		HSSFRow subTitleRow = sheet.createRow(rowIndex ++);
		subTitleRow.setHeightInPoints((short) 15);
		HSSFCellUtil.createCell(subTitleRow, 0, "", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 1, "", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 2, "", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 3, "", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 4, "", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 5, "", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 6, "投照点数", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 7, "诊断点数", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 8, "辅助点数", titleStyle);
		HSSFCellUtil.createCell(subTitleRow, 9, "点数合计", titleStyle);

		/*
		 * Content
		 */
		HSSFFont groupFont = book.createFont();
		groupFont.setFontName("宋体");
		groupFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		groupFont.setFontHeightInPoints((short) 12);

		HSSFCellStyle groupStyle = book.createCellStyle();
		groupStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		groupStyle.setFont(groupFont);
		groupStyle.setBorderTop((short) 1);
		groupStyle.setBorderRight((short) 1);
		groupStyle.setBorderBottom((short) 1);
		groupStyle.setBorderLeft((short) 1);

		HSSFFont amountFont = book.createFont();
		amountFont.setFontName("宋体");
		amountFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		amountFont.setFontHeightInPoints((short) 12);

		HSSFCellStyle amountStyle = book.createCellStyle();
		amountStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		amountStyle.setFont(amountFont);
		amountStyle.setBorderTop((short) 1);
		amountStyle.setBorderRight((short) 1);
		amountStyle.setBorderBottom((short) 1);
		amountStyle.setBorderLeft((short) 1);
		amountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(typeFormat));

		HSSFFont contentFont = book.createFont();
		contentFont.setFontName("宋体");
		contentFont.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
		contentFont.setFontHeightInPoints((short) 12);

		HSSFCellStyle contentStyle = book.createCellStyle();
		contentStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		contentStyle.setFont(contentFont);
		contentStyle.setBorderTop((short) 1);
		contentStyle.setBorderRight((short) 1);
		contentStyle.setBorderBottom((short) 1);
		contentStyle.setBorderLeft((short) 1);
		contentStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(typeFormat));

		String groupName = "";
		for (VMiExecutedStaffPerformance rec: list) {
			String _groupName = rec.getGroupName();
			String staffName = rec.getStaffName();

			if (staffName.equalsIgnoreCase("合计") || staffName.equalsIgnoreCase("总计")) {
				createAmountRow(sheet, rowIndex ++, dataRowIndex, amountRowIndex.toArray(new Integer[0]), rec, amountStyle);
				if (staffName.equals("合计")) amountRowIndex.add(rowIndex);
			} else {
				if (!groupName.equals(_groupName)) {
					createGroupRow(sheet, rowIndex ++, rec, groupStyle);
					groupName = _groupName;
					dataRowIndex = rowIndex+1;
				}
				createContentRow(sheet, rowIndex ++, rec, contentStyle);
			}
		}
		cell.setAsActiveCell();

		return book;
	}

	public HSSFCell createCell(HSSFRow row, int cellIndex, double value, HSSFCellStyle style) {
		HSSFCell cell = row.createCell(cellIndex);
		cell.setCellValue(value);
		cell.setCellStyle(style);
		cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

		return cell;
	}

	public void createGroupRow(HSSFSheet sheet, int rowIndex, VMiExecutedStaffPerformance rec, HSSFCellStyle style) {
		HSSFRow row = sheet.createRow(rowIndex);
		HSSFCellUtil.createCell(row, 0, rec.getGroupName(), style);
		for (int i = 1; i < 10; i ++) HSSFCellUtil.createCell(row, i, "", style);
		sheet.addMergedRegion(CellRangeAddress.valueOf("A" + (rowIndex+1) + ":J" + (rowIndex+1)));

		row.setHeightInPoints(15);
	}

	//合计、总计
	public void createAmountRow(HSSFSheet sheet, int rowIndex, int dataRowIndex, Integer[] amountRowIndex, VMiExecutedStaffPerformance rec, HSSFCellStyle style) {
		int cellIndex = 0;
		boolean isTotal = rec.getStaffName().equals("合计");
		String formulaStr = "";
		String label;

		if (isTotal) {
			label = "合　计";
		} else {
			label = "总　计";
			for (int a: amountRowIndex) formulaStr += "*" + a + "+";
			formulaStr = formulaStr.length() > 0 ? formulaStr.substring(0, formulaStr.length() - 1) : "";
		}
		HSSFRow row = sheet.createRow(rowIndex);
		HSSFCellUtil.createCell(row, cellIndex ++, label, style);
		HSSFCell cell = createCell(row, cellIndex ++, rec.getMedicalTotal(), style);
		cell.setCellFormula(isTotal ? "SUM(B" + dataRowIndex + ":B" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "B"));

		cell = createCell(row, cellIndex ++, 0, style);
		cell.setCellFormula(isTotal ? "SUM(C" + dataRowIndex + ":C" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "C"));
		cell = createCell(row, cellIndex ++, 0, style);
		cell.setCellFormula(isTotal ? "SUM(D" + dataRowIndex + ":D" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "D"));
		cell = createCell(row, cellIndex ++, 0, style);
		cell.setCellFormula(isTotal ? "SUM(E" + dataRowIndex + ":E" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "E"));

		cell = createCell(row, cellIndex ++, rec.getTotalAmount(), style);
		cell.setCellFormula(isTotal ? "SUM(F" + dataRowIndex + ":F" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "F"));
		cell = createCell(row, cellIndex ++, rec.getTechPointTotal(), style);
		cell.setCellFormula(isTotal ? "SUM(G" + dataRowIndex + ":G" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "G"));
		cell = createCell(row, cellIndex ++, rec.getDiagnoPointTotal(), style);
		cell.setCellFormula(isTotal ? "SUM(H" + dataRowIndex + ":H" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "H"));
		cell = createCell(row, cellIndex ++, rec.getAssistPointTotal(), style);
		cell.setCellFormula(isTotal ? "SUM(I" + dataRowIndex + ":I" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "I"));
		cell = createCell(row, cellIndex ++, rec.getPointTotal(), style);
		cell.setCellFormula(isTotal ? "SUM(J" + dataRowIndex + ":J" + rowIndex + ")" : formulaStr.replaceAll("\\Q*\\E", "J"));

		row.setHeightInPoints(15);
	}

	public void createContentRow(HSSFSheet sheet, int rowIndex, VMiExecutedStaffPerformance rec, HSSFCellStyle style) {
		int cellIndex = 0;
		HSSFRow row = sheet.createRow(rowIndex);
		HSSFCellUtil.createCell(row, cellIndex ++, rec.getStaffName(), style);
		createCell(row, cellIndex ++, rec.getMedicalTotal(), style);
		createCell(row, cellIndex ++, rec.getManageTotal(), style);
		createCell(row, cellIndex ++, 0, style);
		createCell(row, cellIndex ++, 0, style);
		HSSFCell cell = createCell(row, cellIndex ++, rec.getTotalAmount(), style);
		cell.setCellFormula("sum(B" + (rowIndex+1) + ":E" + (rowIndex+1) + ")");
		createCell(row, cellIndex ++, rec.getTechPointTotal(), style);
		createCell(row, cellIndex ++, rec.getDiagnoPointTotal(), style);
		createCell(row, cellIndex ++, rec.getAssistPointTotal(), style);
		cell = createCell(row, cellIndex ++, rec.getPointTotal(), style);
		cell.setCellFormula("sum(G" + (rowIndex+1) + ":I" + (rowIndex+1) + ")");

		row.setHeightInPoints(15);
	}

	public MiPerformanceDao getMiPerformanceDao() {
		return miPerformanceDao;
	}

	public void setMiPerformanceDao(MiPerformanceDao miPerformanceDao) {
		this.miPerformanceDao = miPerformanceDao;
	}
}
