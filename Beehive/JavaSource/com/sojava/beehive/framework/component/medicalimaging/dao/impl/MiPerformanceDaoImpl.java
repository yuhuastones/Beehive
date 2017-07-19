package com.sojava.beehive.framework.component.medicalimaging.dao.impl;

import com.sojava.beehive.framework.component.medicalimaging.bean.CalculatePerformance;
import com.sojava.beehive.framework.component.medicalimaging.bean.DicRbrvs;
import com.sojava.beehive.framework.component.medicalimaging.bean.RbrvsPrice;
import com.sojava.beehive.framework.component.medicalimaging.bean.StaffBonus;
import com.sojava.beehive.framework.component.medicalimaging.bean.StaffBonusPK;
import com.sojava.beehive.framework.component.medicalimaging.bean.WorkStatistic;
import com.sojava.beehive.framework.component.medicalimaging.dao.MiPerformanceDao;
import com.sojava.beehive.framework.exception.CommonException;
import com.sojava.beehive.framework.exception.ErrorException;
import com.sojava.beehive.framework.exception.WarnException;
import com.sojava.beehive.framework.math.Arith;
import com.sojava.beehive.hibernate.dao.impl.BeehiveDaoImpl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Scope("prototype")
@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = {CommonException.class, ErrorException.class, WarnException.class, Exception.class, Throwable.class})
public class MiPerformanceDaoImpl extends BeehiveDaoImpl implements MiPerformanceDao {
	private static final long serialVersionUID = 64685891293059799L;

	public void calRbrvsPrice(WorkStatistic workStatistic) throws Exception {
		Session session = null;
		Transaction t = null;
		try {
			session = getSessionFactory().openSession();
			t = session.beginTransaction();

			Mode calMode;
			/*
			 * Sharing Mode
			 */
			calMode = new SharingMode(getSession(), session, workStatistic);
			/*
			 * Single Mode
			 */
//			calMode = new SingleMode(getSession(), session, workStatistic);

			//Calculate the Main data of Work statistic.
			calMode.calMainInfo();
			//Calculate items of Workload for Price of RBRVS.
			calMode.calRbrvsPrice();

			t.commit();

			/*
			 * 核算开始
			 */
			t = session.beginTransaction();

			calMode.calPerformance();

			t.commit();
		}
		catch(Exception ex) {
			t.rollback();
			throw ex;
		}
		finally {
			session.flush();
			session.clear();
		}
	}
}

interface Mode {
	void calMainInfo() throws Exception;
	void calRbrvsPrice() throws Exception;
	void calPerformance() throws Exception;
}

class SharingMode implements Mode {
	private Session querySession;
	private Session updateSession;
	private WorkStatistic workStatistic;

	public SharingMode(Session querySession, Session updateSession, WorkStatistic workStatistic) {
		this.querySession = querySession;
		this.updateSession = updateSession;
		this.workStatistic = workStatistic;
	}

	public void calMainInfo() throws Exception {
		Query stmt;

		/*
		 * Calculateing the Point total.
		 */
		workStatistic.setPointTotal(0d);

		//Technician Group
		stmt = this.querySession.createQuery(
				"select "
				+ "sum(technicianValue) as amount"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "(executeTechnicianStaffId is not null or executeTechnicianAssociateStaffId is not null)"
				+ " and reportDate between :begin and :end"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		workStatistic.setPointTotal(workStatistic.getPointTotal() + (double) stmt.uniqueResult());

		//Diagnostician Group
		stmt = this.querySession.createQuery(
				"select "
				+ "sum(diagnosticianValue) as amount"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "reportDate between :begin and :end"
				+ " and status=:status"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setString("status", "已审核");
		workStatistic.setPointTotal(workStatistic.getPointTotal() + (double) stmt.uniqueResult());

		/*
		 * Calculateing the Point value.
		 */
		workStatistic.setPointValue(Arith.div(workStatistic.getMedicalTotal(), workStatistic.getPointTotal(), 20));

		this.updateSession.saveOrUpdate(workStatistic);
	}

	@SuppressWarnings("unchecked")
	public void calRbrvsPrice() throws Exception {
		Query stmt;

		/*
		 * Empty the RbrvsPrice of WorkStatistic.
		 */
		stmt = this.updateSession.createQuery("delete from RbrvsPrice where workStatistic=:workStatistic");
		stmt.setEntity("workStatistic", workStatistic);
		stmt.executeUpdate();

		/*
		 * Sum Workload for Technician,Diagnostician,Verifier.
		 */
		//投照组
		stmt = this.querySession.createQuery(
				"select "
				+ "rbrvsId,"
				+ "technicianValue,"
				+ "sum(technicianValue) as amount,"
				+ "count(technicianValue) as quantity"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "(executeTechnicianStaffId is not null or executeTechnicianAssociateStaffId is not null)"
				+ " and reportDate between :begin and :end"
				+ " group by rbrvsId, technicianValue"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		for(Object[] recs: (List<Object[]>) stmt.list()) {
			RbrvsPrice rbrvsPrice = new RbrvsPrice(
					workStatistic,
					new DicRbrvs(Integer.parseInt(recs[0].toString())),
					Double.parseDouble(recs[1].toString()),
					Double.parseDouble(recs[2].toString()),
					0,
					0,
					Integer.parseInt(recs[3].toString()),
					"投照",
					"工作量"
				);
			//计算单价
			rbrvsPrice.setPrice(Arith.mul(workStatistic.getPointValue(), rbrvsPrice.getPoint()));
			//计算总价
			rbrvsPrice.setAmount(Arith.mul(rbrvsPrice.getPrice(), rbrvsPrice.getQuantity()));

			this.updateSession.save(rbrvsPrice);
		}
		//诊断组
		stmt = this.querySession.createQuery(
				"select "
				+ "rbrvsId,"
				+ "diagnosticianValue,"
				+ "sum(diagnosticianValue) as amount,"
				+ "count(diagnosticianValue) as quantity"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "reportDate between :begin and :end"
				+ " and status=:status"
				+ " group by rbrvsId, diagnosticianValue"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setString("status", "已审核");
		for(Object[] recs: (List<Object[]>) stmt.list()) {
			RbrvsPrice rbrvsPrice = new RbrvsPrice(
					workStatistic,
					new DicRbrvs(Integer.parseInt(recs[0].toString())),
					Double.parseDouble(recs[1].toString()),
					Double.parseDouble(recs[2].toString()),
					0,
					0,
					Integer.parseInt(recs[3].toString()),
					"诊断",
					"工作量"
				);
			//计算单价
			rbrvsPrice.setPrice(Arith.mul(workStatistic.getPointValue(), rbrvsPrice.getPoint()));
			//计算总价
			rbrvsPrice.setAmount(Arith.mul(rbrvsPrice.getPrice(), rbrvsPrice.getQuantity()));

			this.updateSession.save(rbrvsPrice);
		}
	}

	@SuppressWarnings("unchecked")
	public void calPerformance() throws Exception {
		Query stmt;

		//清除历史数据
		stmt = this.updateSession.createQuery("delete from CalculatePerformance where workStatistic=:workStatistic");
		stmt.setEntity("workStatistic", workStatistic);
		stmt.executeUpdate();

		//核算个人分数
		stmt = this.querySession.createQuery(
				"select "
				+ "rbrvsPriceId,"
				+ "rbrvsId,"
				+ "price,"
				+ "point,"
				+ "count(rbrvsId) as quantity,"
				+ "coalesce(worker1Id, 0) as worker1Id,"
				+ "coalesce(worker1Name, '') as worker1Name,"
				+ "coalesce(worker1Coef, 0) as worker1Coef,"
				+ "coalesce(worker2Id, 0) as worker2Id,"
				+ "coalesce(worker2Name, '') as worker2Name,"
				+ "coalesce(worker2Coef, 0) as worker2Coef,"
				+ "type,"
				+ "kind,"
				+ "dept,"
				+ "executeDiagnosticianIsStudent"
				+ " from "
				+ "VMiExectuedPerformance"
				+ " where "
				+ " reportDate between :begin and :end"
				+ " and workStatisticsId=:workStatisticsId"
				+ " group by rbrvsPriceId,rbrvsId,price,point,worker1Id,worker1Name,worker1Coef,worker2Id,worker2Name,worker2Coef,type,kind,dept,execute_diagnostician_is_student"
			);
		stmt.setInteger("workStatisticsId", workStatistic.getId());
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		for(Object[] recs: (List<Object[]>) stmt.list()) {
			CalculatePerformance calculatePerformance = new CalculatePerformance();
			calculatePerformance.setWorkStatistic(workStatistic);
			calculatePerformance.setRbrvsPrice(new RbrvsPrice(Integer.parseInt(recs[0].toString())));
			calculatePerformance.setDicRbrvs(new DicRbrvs(Integer.parseInt(recs[1].toString())));
			calculatePerformance.setPrice(Double.parseDouble(recs[2].toString()));
			calculatePerformance.setPoint(Double.parseDouble(recs[3].toString()));
			calculatePerformance.setQuantity(Integer.parseInt(recs[4].toString()));
			calculatePerformance.setWorker1StaffId(Integer.parseInt(recs[5].toString()));
			calculatePerformance.setWorker1Coef(Double.parseDouble(recs[7].toString()));
			calculatePerformance.setWorker2StaffId(Integer.parseInt(recs[8].toString()));
			calculatePerformance.setWorker2Coef(Double.parseDouble(recs[10].toString()));
			calculatePerformance.setType(recs[11].toString());
			calculatePerformance.setKind(recs[12].toString());
			calculatePerformance.setDept(recs[13].toString());
			boolean isStudent = Integer.parseInt(recs[14].toString()) == 1;
			boolean isDiagno = calculatePerformance.getType().equalsIgnoreCase("诊断");
			if (isDiagno && !isStudent) {
				calculatePerformance.setWorker2Coef(0d);
			}
			calculatePerformance.setWorkerCoefTotal(calculatePerformance.getWorker1Coef()+calculatePerformance.getWorker2Coef());

			calculatePerformance.setPointTotal(Arith.mul(calculatePerformance.getPoint(), calculatePerformance.getQuantity()));
			double work1Percent = Arith.div(calculatePerformance.getWorker1Coef(), calculatePerformance.getWorkerCoefTotal());
			calculatePerformance.setWorker1Point(Arith.mul(work1Percent, calculatePerformance.getPoint()));
			calculatePerformance.setWorker1PointTotal(Arith.mul(calculatePerformance.getWorker1Point(), calculatePerformance.getQuantity()));
			calculatePerformance.setWorker2Point(calculatePerformance.getPoint()-calculatePerformance.getWorker1Point());
			calculatePerformance.setWorker2PointTotal(Arith.mul(calculatePerformance.getWorker2Point(), calculatePerformance.getQuantity()));
			calculatePerformance.setAmount(Arith.mul(calculatePerformance.getPrice(), calculatePerformance.getQuantity()));
			calculatePerformance.setWorkerPrice(Arith.div(calculatePerformance.getAmount(), calculatePerformance.getWorkerCoefTotal()));
			calculatePerformance.setWorker1Total(Arith.mul(work1Percent, calculatePerformance.getWorkerPrice()));
			calculatePerformance.setWorker2Total(calculatePerformance.getAmount()-calculatePerformance.getWorker1Total());

			calculatePerformance.setWorker1Quantity(Arith.mul(work1Percent, calculatePerformance.getQuantity()));
			calculatePerformance.setWorker2Quantity(calculatePerformance.getQuantity()-calculatePerformance.getWorker1Quantity());

			if (work1Percent == 1) {
				calculatePerformance.setWorker1StatisQuantity(1d*calculatePerformance.getQuantity());
				calculatePerformance.setWorker2StatisQuantity(0d);
			} else {
				calculatePerformance.setWorker1StatisQuantity(0.5d*calculatePerformance.getQuantity());
				calculatePerformance.setWorker2StatisQuantity(0.5d*calculatePerformance.getQuantity());
			}

			this.updateSession.save(calculatePerformance);
		}
	}
}

class SingleMode implements Mode {
	private Session querySession;
	private Session updateSession;
	private WorkStatistic workStatistic;

	public SingleMode(Session querySession, Session updateSession, WorkStatistic workStatistic) {
		this.querySession = querySession;
		this.updateSession = updateSession;
		this.workStatistic = workStatistic;
	}

	public void calMainInfo() throws Exception {
		Query stmt;

		/*
		 * Sum the Point total.
		 * include Technician,Associate,Diagnostician,Verifier
		 */
		workStatistic.setPointTotal(0d);
		workStatistic.setTechPointTotal(0d);
		workStatistic.setDiagnoPointTotal(0d);
		//Technician
		stmt = this.querySession.createQuery(
				"select "
				+ "sum(technicianValue*executeTechnicianCoef) as amount"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "executeTechnicianStaffId is not null"
				+ " and executeTechnicianCoef > 0"
				+ " and reportDate between :begin and :end"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		workStatistic.setTechPointTotal(workStatistic.getTechPointTotal() + (double) stmt.uniqueResult());
		//Technician Associate
		stmt = this.querySession.createQuery(
				"select "
				+ "sum(technicianValue*executeTechnicianAssociateCoef) as amount"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "executeTechnicianAssociateStaffId is not null"
				+ " and executeTechnicianAssociateCoef > 0"
				+ " and reportDate between :begin and :end"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		workStatistic.setTechPointTotal(workStatistic.getTechPointTotal() + (double) stmt.uniqueResult());
		//Diagnostician
		stmt = this.querySession.createQuery(
				"select "
				+ "sum(diagnosticianValue*executeDiagnosticianCoef) as amount"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "executeDiagnosticianStaffId is not null"
				+ " and executeDiagnosticianCoef > 0"
				+ " and reportDate between :begin and :end"
				+ " and status=:status"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setString("status", "已审核");
		workStatistic.setDiagnoPointTotal(workStatistic.getDiagnoPointTotal() + (double) stmt.uniqueResult());
		//Verifier
		stmt = this.querySession.createQuery(
				"select "
				+ "sum(diagnosticianValue*executeVerifierCoef) as amount"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "executeVerifierStaffId is not null"
				+ " and executeVerifierCoef > 0"
				+ " and executeDiagnosticianIsStudent = 1"
				+ " and reportDate between :begin and :end"
				+ " and status=:status"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setString("status", "已审核");
		workStatistic.setDiagnoPointTotal(workStatistic.getDiagnoPointTotal() + (double) stmt.uniqueResult());

		/*
		 * Calculateing the Point value.
		 */
		workStatistic.setTechPointValue(workStatistic.getTechTotal()/workStatistic.getTechPointTotal());
		workStatistic.setDiagnoPointValue(workStatistic.getDiagnoTotal()/workStatistic.getDiagnoPointTotal());

		this.updateSession.saveOrUpdate(workStatistic);
	}

	@SuppressWarnings("unchecked")
	public void calRbrvsPrice() throws Exception {
		Query stmt;

		/*
		 * Empty the RbrvsPrice of WorkStatistic.
		 */
		stmt = this.updateSession.createQuery("delete from RbrvsPrice where workStatistic=:workStatistic");
		stmt.setEntity("workStatistic", workStatistic);
		stmt.executeUpdate();

		/*
		 * Sum Workload for Technician,Associate,Diagnostician,Verifier.
		 */
		//Technician
		stmt = this.querySession.createQuery(
				"select "
				+ "rbrvsId,"
				+ "executeTechnicianStaffId as staffId,"
				+ "technicianValue*executeTechnicianCoef as technicianValue,"
				+ "sum(technicianValue*executeTechnicianCoef) as amount,"
				+ "count(id) as quantity"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ " executeTechnicianStaffId is not null"
				+ " and executeTechnicianCoef > 0"
				+ " and reportDate between :begin and :end"
				+ " group by rbrvsId, technicianValue, executeTechnicianStaffId, executeTechnicianCoef"
				+ " order by rbrvsId"
			);
//		stmt = this.querySession.createSQLQuery(
//				"select "
//				+ "rbrvs_id,"
//				+ "technician_value,"
//				+ "sum(amount) as amount,"
//				+ "sum(quantity) as quantity"
//				+ " from ("
//				+ "	select "
//				+ "	rbrvs_id,"
//				+ "	technician_value,"
//				+ "	sum(technician_value)*execute_technician_coef as amount,"
//				+ "	count(id) as quantity"
//				+ "	from "
//				+ "	medicalimaging.v_mi_executed"
//				+ "	where "
//				+ "	execute_technician_staff_id is not null"
//				+ "	and execute_technician_coef > 0"
//				+ "	and report_date between :begin and :end"
//				+ "	group by rbrvs_id,technician_value,execute_technician_coef,execute_technician_staff_id"
//				+ ") a"
//				+ " group by rbrvs_id,technician_value"
//				+ " order by rbrvs_id"
//			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		for(Object[] recs: (List<Object[]>) stmt.list()) {
			RbrvsPrice rbrvsPrice = new RbrvsPrice(
					workStatistic,
					new DicRbrvs(Integer.parseInt(recs[0].toString())),
					Integer.parseInt(recs[1].toString()),
					Double.parseDouble(recs[2].toString()),
					Double.parseDouble(recs[3].toString()),
					0,
					0,
					Integer.parseInt(recs[4].toString()),
					"操作",
					"工作量"
				);
			rbrvsPrice.setPrice(workStatistic.getTechPointValue()*rbrvsPrice.getPoint());
			rbrvsPrice.setAmount(rbrvsPrice.getPrice()*rbrvsPrice.getQuantity());

			this.updateSession.save(rbrvsPrice);
		}
		//Associate
		stmt = this.querySession.createQuery(
				"select "
				+ "rbrvsId,"
				+ "executeTechnicianAssociateStaffId as staffId,"
				+ "technicianValue*executeTechnicianAssociateCoef as assistValue,"
				+ "sum(technicianValue*executeTechnicianAssociateCoef) as amount,"
				+ "count(id) as quantity"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "executeTechnicianAssociateStaffId is not null"
				+ " and executeTechnicianAssociateCoef > 0"
				+ " and reportDate between :begin and :end"
				+ " group by rbrvsId, technicianValue, executeTechnicianAssociateStaffId, executeTechnicianAssociateCoef"
				+ " order by rbrvsId"
			);
//		stmt = this.querySession.createSQLQuery(
//				"select "
//				+ "rbrvs_id,"
//				+ "assist_value,"
//				+ "sum(amount) as amount,"
//				+ "sum(quantity) as quantity"
//				+ " from ("
//				+ "	select "
//				+ "	rbrvs_id,"
//				+ "	technician_value as assist_value,"
//				+ "	sum(technician_value)*execute_technician_associate_coef as amount,"
//				+ "	count(id) as quantity"
//				+ "	from "
//				+ "	medicalimaging.v_mi_executed"
//				+ "	where "
//				+ "	execute_technician_associate_staff_id is not null"
//				+ "	and execute_technician_associate_coef > 0"
//				+ "	and report_date between :begin and :end"
//				+ "	group by rbrvs_id,technician_value,execute_technician_associate_coef,execute_technician_associate_staff_id"
//				+ ") a"
//				+ " group by rbrvs_id,assist_value"
//				+ " order by rbrvs_id"
//			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		for(Object[] recs: (List<Object[]>) stmt.list()) {
			RbrvsPrice rbrvsPrice = new RbrvsPrice(
					workStatistic,
					new DicRbrvs(Integer.parseInt(recs[0].toString())),
					Integer.parseInt(recs[1].toString()),
					Double.parseDouble(recs[2].toString()),
					Double.parseDouble(recs[3].toString()),
					0,
					0,
					Integer.parseInt(recs[4].toString()),
					"辅助",
					"工作量"
				);
			rbrvsPrice.setPrice(workStatistic.getTechPointValue()*rbrvsPrice.getPoint());
			rbrvsPrice.setAmount(rbrvsPrice.getPrice()*rbrvsPrice.getQuantity());

			this.updateSession.save(rbrvsPrice);
		}
		//Diagnostician
		stmt = this.querySession.createQuery(
				"select "
				+ "rbrvsId,"
				+ "executeDiagnosticianStaffId,"
				+ "diagnosticianValue*executeDiagnosticianCoef as diagnosticianValue,"
				+ "sum(diagnosticianValue*executeDiagnosticianCoef) as amount,"
				+ "count(id) as quantity"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "executeDiagnosticianStaffId is not null"
				+ " and executeDiagnosticianCoef > 0"
				+ " and reportDate between :begin and :end"
				+ " and status=:status"
				+ " group by rbrvsId, diagnosticianValue, executeDiagnosticianStaffId, executeDiagnosticianCoef"
				+ " order by rbrvsId"
			);
//		stmt = this.querySession.createSQLQuery(
//				"select "
//				+ "rbrvs_id,"
//				+ "diagnostician_value,"
//				+ "sum(amount) as amount,"
//				+ "sum(quantity) as quantity"
//				+ " from ("
//				+ "	select "
//				+ "	rbrvs_id,"
//				+ "	diagnostician_value,"
//				+ "	sum(diagnostician_value)*execute_diagnostician_coef as amount,"
//				+ "	count(id) as quantity"
//				+ "	from "
//				+ "	medicalimaging.v_mi_executed"
//				+ "	where "
//				+ "	execute_diagnostician_staff_id is not null"
//				+ "	and execute_diagnostician_coef > 0"
//				+ "	and report_date between :begin and :end"
//				+ " and status=:status"
//				+ "	group by rbrvs_id,diagnostician_value,execute_diagnostician_coef,execute_diagnostician_staff_id"
//				+ ") a"
//				+ " group by rbrvs_id,diagnostician_value"
//				+ " order by rbrvs_id"
//			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setString("status", "已审核");
		for(Object[] recs: (List<Object[]>) stmt.list()) {
			RbrvsPrice rbrvsPrice = new RbrvsPrice(
					workStatistic,
					new DicRbrvs(Integer.parseInt(recs[0].toString())),
					Integer.parseInt(recs[1].toString()),
					Double.parseDouble(recs[2].toString()),
					Double.parseDouble(recs[3].toString()),
					0,
					0,
					Integer.parseInt(recs[4].toString()),
					"阅片",
					"工作量"
				);
			rbrvsPrice.setPrice(workStatistic.getDiagnoPointValue()*rbrvsPrice.getPoint());
			rbrvsPrice.setAmount(rbrvsPrice.getPrice()*rbrvsPrice.getQuantity());

			this.updateSession.save(rbrvsPrice);
		}
		//Verifier
		stmt = this.querySession.createQuery(
				"select "
				+ "rbrvsId,"
				+ "executeVerifierStaffId as staffId,"
				+ "diagnosticianValue*executeVerifierCoef as verifierValue,"
				+ "sum(diagnosticianValue*executeVerifierCoef) as amount,"
				+ "count(id) as quantity"
				+ " from "
				+ "VMiExecuted"
				+ " where "
				+ "executeVerifierStaffId is not null"
				+ " and executeVerifierCoef > 0"
				+ " and executeDiagnosticianIsStudent = 1"
				+ " and reportDate between :begin and :end"
				+ " and status=:status"
				+ " group by rbrvsId, diagnosticianValue, executeVerifierStaffId, executeVerifierCoef"
				+ " order by rbrvsId"
			);
//		stmt = this.querySession.createSQLQuery(
//				"select "
//				+ "rbrvs_id,"
//				+ "verifier_value,"
//				+ "sum(amount) as amount,"
//				+ "sum(quantity) as quantity"
//				+ " from ("
//				+ "	select "
//				+ "	rbrvs_id,"
//				+ "	diagnostician_value as verifier_value,"
//				+ "	sum(diagnostician_value)*execute_verifier_coef as amount,"
//				+ "	count(id) as quantity"
//				+ "	from "
//				+ "	medicalimaging.v_mi_executed"
//				+ "	where "
//				+ "	execute_verifier_staff_id is not null"
//				+ "	and execute_verifier_coef > 0"
//				+ " and execute_diagnostician_is_student = 1"
//				+ "	and report_date between :begin and :end"
//				+ " and status=:status"
//				+ "	group by rbrvs_id,diagnostician_value,execute_verifier_coef,execute_verifier_staff_id"
//				+ ") a"
//				+ " group by rbrvs_id,verifier_value"
//				+ " order by rbrvs_id"
//			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setString("status", "已审核");
		for(Object[] recs: (List<Object[]>) stmt.list()) {
			RbrvsPrice rbrvsPrice = new RbrvsPrice(
					workStatistic,
					new DicRbrvs(Integer.parseInt(recs[0].toString())),
					Integer.parseInt(recs[1].toString()),
					Double.parseDouble(recs[2].toString()),
					Double.parseDouble(recs[3].toString()),
					0,
					0,
					Integer.parseInt(recs[4].toString()),
					"审片",
					"工作量"
				);
			rbrvsPrice.setPrice(workStatistic.getDiagnoPointValue()*rbrvsPrice.getPoint());
			rbrvsPrice.setAmount(rbrvsPrice.getPrice()*rbrvsPrice.getQuantity());

			this.updateSession.save(rbrvsPrice);
		}
	}

	@SuppressWarnings("unchecked")
	public void calPerformance() throws Exception {
		Query stmt;

		double techCoef = 0, assistCoef = 0, diagnoCoef = 0, verifierCoef = 0;
//		stmt = this.querySession.createQuery(
//				"select "
//				+ "sum(staffCoef) as amount,"
//				+ "type"
//				+ " from "
//				+ "VMiExectuedPerformanceSingleMode"
//				+ " where "
//				+ "reportDate between :begin and :end"
//				+ " and workStatisticsId=:workStatisticsId"
//				+ " group by type, kind, dept"
//			);
		stmt = this.querySession.createSQLQuery(
				"select sum(staff_coef) as amount, type"
				+ " from ("
				+ "	select "
				+ "	staff_id,"
				+ "	staff_coef,"
				+ "	type"
				+ "	from "
				+ "	medicalimaging.v_mi_exectued_performance_single_mode"
				+ "	where "
				+ "	work_statistics_id=:workStatisticsId"
				+ "	 and report_date between :begin and :end"
				+ "	 and kind='工作量'"
				+ "	 and dept='影像科'"
				+ "	group by staff_id, staff_coef, type"
				+ ") a"
				+ " group by type"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setInteger("workStatisticsId", workStatistic.getId());
		for(Object[] rec: (List<Object[]>) stmt.list()) {
			double _coef = Double.parseDouble(rec[0].toString());
			String _type = rec[1].toString();

			if (_type.equalsIgnoreCase("操作")) techCoef = _coef;
			else if (_type.equalsIgnoreCase("辅助")) assistCoef = _coef;
			else if (_type.equalsIgnoreCase("阅片")) diagnoCoef = _coef;
			else if (_type.equalsIgnoreCase("审片")) verifierCoef = _coef;
		}

		double techTotal = 0d, assistTotal = 0d, diagnoTotal = 0d, verifierTotal = 0d;
		stmt = this.querySession.createSQLQuery(
				"select "
				+ "sum(amount) as amount,"
				+ "sum(quantity) as quantity,"
				+ "type"
				+ " from ("
				+ "	select "
				+ "	price*count(id) as amount,"
				+ "	count(id) as quantity,"
				+ "	type"
				+ "	from "
				+ "	medicalimaging.v_mi_exectued_performance_single_mode"
				+ "	where "
				+ "	report_date between :begin and :end"
				+ "	and work_statistics_id=:workStatisticsId"
				+ "	and kind=:kind"
				+ "	and dept=:dept"
				+ "	group by staff_id, rbrvs_id, price, type"
				+ ") a"
				+ " group by type"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setInteger("workStatisticsId", workStatistic.getId());
		stmt.setString("kind", "工作量");
		stmt.setString("dept", "影像科");
		for(Object[] rec: (List<Object[]>) stmt.list()) {
			double _amount = Double.parseDouble(rec[0].toString());
			String _type = (String) rec[2];

			if (_type.equalsIgnoreCase("操作")) techTotal = _amount;
			else if (_type.equalsIgnoreCase("辅助")) assistTotal = _amount;
			else if (_type.equalsIgnoreCase("阅片")) diagnoTotal = _amount;
			else if (_type.equalsIgnoreCase("审片")) verifierTotal = _amount;
		}

		double  techPrice = techTotal/techCoef,
				assistPrice = assistTotal/assistCoef,
				diagnoPrice = diagnoTotal/diagnoCoef,
				verifierPrice = verifierTotal/verifierCoef;

		stmt = this.updateSession.createQuery(
				"delete from StaffBonus"
				+ " where "
				+ "id.workStatisticsId=:workStatisticsId"
				+ " and id.kind=:kind"
				+ " and id.dept=:dept");
		stmt.setInteger("workStatisticsId", workStatistic.getId());
		stmt.setString("kind", "奖金核算");
		stmt.setString("dept", "影像科");
		stmt.executeUpdate();

		stmt = this.querySession.createSQLQuery(
				"select "
				+ "staff_id,"
				+ "staff_coef,"
				+ "sum(amount) as amount,"
				+ "sum(quantity) as quantity,"
				+ "type"
				+ " from ("
				+ "	select "
				+ " staff_id,"
				+ "	staff_coef,"
				+ "	price*count(id) as amount,"
				+ "	count(id) as quantity,"
				+ "	type"
				+ "	from "
				+ "	medicalimaging.v_mi_exectued_performance_single_mode"
				+ "	where "
				+ "	report_date between :begin and :end"
				+ "	and work_statistics_id=:workStatisticsId"
				+ "	and kind=:kind"
				+ "	and dept=:dept"
				+ "	group by staff_id, staff_coef,rbrvs_id, price, type"
				+ ") a"
				+ " group by staff_id, staff_coef, type"
			);
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setInteger("workStatisticsId", workStatistic.getId());
		stmt.setString("kind", "工作量");
		stmt.setString("dept", "影像科");

		for(Object[] rec: (List<Object[]>) stmt.list()) {
			int _staffId = Integer.parseInt(rec[0].toString());
			double _staffCoef = Double.parseDouble(rec[1].toString());
			int _quantity = Integer.parseInt(rec[3].toString());
			String _type = (String) rec[4];

			StaffBonus bonus = new StaffBonus();
			bonus.setId(new StaffBonusPK(workStatistic.getId(), _staffId, _type, "奖金核算", "影像科"));
			if (_type.equalsIgnoreCase("操作")) {
				bonus.setPrice(techPrice);
			} else if (_type.equalsIgnoreCase("辅助")) {
				bonus.setPrice(assistPrice);
			} else if (_type.equalsIgnoreCase("阅片")) {
				bonus.setPrice(diagnoPrice);
			} else if (_type.equalsIgnoreCase("审片")) {
				bonus.setPrice(verifierPrice);
			}
			bonus.setQuantity(_quantity);
			bonus.setStaffCoef(_staffCoef);
			bonus.setAmount(bonus.getPrice()*bonus.getStaffCoef());

			this.updateSession.save(bonus);
		}
/*

		stmt = this.querySession.createQuery(
				"select "
				+ "	b.price,"
				+ "count() as amount"
				+ "	a.reportDate,"
				+ "	a.reportTime,"
				+ "	a.executeTechnicianStaffId as staffId,"
				+ "	a.executeTechnician as staffName,"
				+ "	a.executeTechnicianCoef as staffCoef,"
				+ "	b.type,"
				+ "	b.kind,"
				+ "	a.kind as dept"
				+ "	from VMiExecuted a,"
				+ "	RbrvsPrice b"
				+ "	where "
				+ "	a.executeTechnicianStaffId is not null"
				+ "	and coalesce(a.executeTechnicianCoef, 0) > 0"
				+ "	and a.rbrvsId=b.rbrvsId"
				+ "	and b.type=:type"
				+ "	and b.kind=:kind"
				+ " and a.kind=:dept"
				+ " and a.reportDate between :begin and :end"
				+ " and b.workStatisticsId=:workStatisticsId"
			);
		stmt.setInteger("workStatisticsId", workStatistic.getId());
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		stmt.setString("type", "操作");
		stmt.setString("kind", "工作量");
		stmt.setString("dept", "影像科");
		for (Object[] recs: (List<Object[]>) stmt.list()) {
			RbrvsPrice rbrvsPrice = new RbrvsPrice(
					workStatistic,
					new DicRbrvs((int) recs[0]),
					(double) recs[1],
					(double) recs[2],
					0,
					0,
					Integer.parseInt(Long.toString((long) recs[3])),
					"审片",
					"工作量"
				);
			rbrvsPrice.setPrice(workStatistic.getDiagnoPointValue()*rbrvsPrice.getPoint());
			rbrvsPrice.setAmount(rbrvsPrice.getPrice()*rbrvsPrice.getQuantity());
		}
*/
/*
		double techCoef, assocCoef, diagnoCoef, verifierCoef;
		//Tech Coef
		stmt = querySession.createQuery(
				"select sum(coef) amount from ("
				+ "select "
				+ "executeTechnicianStaffId as staffId,"
				+ "executeTechnicianCoef as coef"
				+ "	from "
				+ "VMiExecuted"
				+ "	where "
				+ "	reportTime between :begin and :end"
				+ "	and executeTechnicianStaffId is not null"
				+ " group by executeTechnicianStaffId, executeTechnicianCoef"
				+ ") a");
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		techCoef = Double.parseDouble(Long.toString((long) stmt.uniqueResult()));
		//Associate Coef
		stmt = querySession.createQuery(
				"select sum(coef) amount from ("
				+ "select "
				+ "executeTechnicianAssociateStaffId as staffId,"
				+ "executeTechnicianAssociateCoef as coef"
				+ "	from "
				+ "VMiExecuted"
				+ "	where "
				+ "	reportTime between :begin and :end"
				+ "	and executeTechnicianAssociateStaffId is not null"
				+ " group by executeTechnicianAssociateStaffId, executeTechnicianAssociateCoef"
				+ ") a");
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		assocCoef = Double.parseDouble(Long.toString((long) stmt.uniqueResult()));
		//Diagno Coef
		stmt = querySession.createQuery(
				"select sum(coef) amount from ("
				+ "select "
				+ "executeDiagnosticianStaffId as staffId,"
				+ "executeDiagnosticianCoef as coef"
				+ "	from "
				+ "VMiExecuted"
				+ "	where "
				+ "	reportTime between :begin and :end"
				+ "	and executeDiagnosticianStaffId is not null"
				+ " group by executeDiagnosticianStaffId, executeDiagnosticianCoef"
				+ ") a");
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		diagnoCoef = Double.parseDouble(Long.toString((long) stmt.uniqueResult()));
		//Verifier Coef
		stmt = querySession.createQuery(
				"select sum(coef) amount from ("
				+ "select "
				+ "executeVerifierStaffId as staffId,"
				+ "executeVerifierCoef as coef"
				+ "	from "
				+ "VMiExecuted"
				+ "	where "
				+ "	reportTime between :begin and :end"
				+ "	and executeVerifierStaffId is not null"
				+ " group by executeVerifierStaffId, executeVerifierCoef"
				+ ") a");
		stmt.setDate("begin", workStatistic.getBeginDate());
		stmt.setDate("end", workStatistic.getEndDate());
		verifierCoef = Double.parseDouble(Long.toString((long) stmt.uniqueResult()));
*/
	}	
}