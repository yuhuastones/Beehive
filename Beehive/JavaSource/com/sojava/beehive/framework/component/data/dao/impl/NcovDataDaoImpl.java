package com.sojava.beehive.framework.component.data.dao.impl;

import com.sojava.beehive.framework.component.data.bean.NcovGoods;
import com.sojava.beehive.framework.component.data.dao.NcovDataDao;
import com.sojava.beehive.framework.exception.CommonException;
import com.sojava.beehive.framework.exception.ErrorException;
import com.sojava.beehive.framework.exception.WarnException;
import com.sojava.beehive.framework.util.ValueUtil;
import com.sojava.beehive.hibernate.dao.impl.BeehiveDaoImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Scope("prototype")
@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = {CommonException.class, ErrorException.class, WarnException.class, Exception.class, Throwable.class})
public class NcovDataDaoImpl extends BeehiveDaoImpl implements NcovDataDao {
	private static final long serialVersionUID = -1857537182764109596L;

	@SuppressWarnings("unchecked")
	@Override
	public List<NcovGoods> goodsSumByDestType(Date datetime, String srcDept, String type) throws ErrorException {
		List<NcovGoods> rest = new ArrayList<NcovGoods>();

		Session session = getSession();
		String sql = "select "
					 + "a.dept_dest_type"
					 + "sum(ptkz) as \"普通口罩\""
					 + "sum(wkkz) as \"外科口罩\""
					 + "sum(n95) as \"N95口罩\""
					 + "sum(klfhkz) as \"颗粒防护口罩\""
					 + "sum(fhf) as \"防护服\""
					 + "sum(gly) as \"隔离衣\""
					 + "sum(rjst) as \"乳胶手套\""
					 + "sum(mz) as \"帽子\""
					 + "sum(hmj) as \"护目镜\""
					 + "sum(xt) as \"鞋套\""
					 + "sum(xdy84) as \"84消毒液\""
					 + "sum(jj75) as \"酒精75%\""
					 + "sum(sxdj) as \"手消毒剂\""
					 + "sum(twj) as \"体温计\""
					 + "sum(sf) as \"沙方\""
					 + "sum(jj75_45) as \"酒精75%(4.5L/桶)\""
					 + "sum(dqjcst_s) as \"丁腈检查手套S码\""
					 + "sum(dqjcst_l) as \"丁腈检查手套L码\""
					 + "sum(jx) as \"胶靴\""
					 + " from "
					 + " data.ncov_goods a"
					 + " left join data.dic_catalog b on b.kind='卫材库' and b.type='科系' and b.name=dept_dest_type"
					 + " where "
					 + " time=:time"
					 + " and dept_src=:deptSrc"
					 + " and type=:type"
					 + " group by a.dept_dest_type b.sort"
					 + " order by coalesce(b.sort 99::int2)";
		SQLQuery stmt = session.createSQLQuery(sql);
		stmt.setTimestamp("time", datetime);
		stmt.setString("deptSrc", srcDept);
		stmt.setString("type", type);
		List<?> list = stmt.list();
		for (Object[] items : (List<Object[]>) list) {
			NcovGoods goods = new NcovGoods();
			goods.setDeptDest(items[0].toString());
			goods.setPtkz(ValueUtil.get(items[1], 0d));
			goods.setWkkz(ValueUtil.get(items[2], 0d));
			goods.setN95(ValueUtil.get(items[3], 0d));
			goods.setKlfhkz(ValueUtil.get(items[4], 0d));
			goods.setFhf(ValueUtil.get(items[5], 0d));
			goods.setGly(ValueUtil.get(items[6], 0d));
			goods.setRjst(ValueUtil.get(items[7], 0d));
			goods.setMz(ValueUtil.get(items[8], 0d));
			goods.setHmj(ValueUtil.get(items[9], 0d));
			goods.setXt(ValueUtil.get(items[10], 0d));
			goods.setXdy84(ValueUtil.get(items[11], 0d));
			goods.setJj75(ValueUtil.get(items[12], 0d));
			goods.setSxdj(ValueUtil.get(items[13], 0d));
			goods.setTwj(ValueUtil.get(items[14], 0d));
			goods.setSf(ValueUtil.get(items[15], 0d));
			goods.setJj7545(ValueUtil.get(items[16], 0d));
			goods.setDqjcstS(ValueUtil.get(items[17], 0d));
			goods.setDqjcstL(ValueUtil.get(items[18], 0d));
			goods.setJx(ValueUtil.get(items[19], 0d));
			goods.setType(type);

			rest.add(goods);
		}

		return rest;
	}
}
