package com.sojava.beehive.framework.component.inpatienthomepage.dao.imp;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sojava.beehive.framework.component.inpatienthomepage.bean.Dictionary;
import com.sojava.beehive.framework.component.inpatienthomepage.dao.HomepageDao;
import com.sojava.beehive.framework.exception.CommonException;
import com.sojava.beehive.framework.exception.ErrorException;
import com.sojava.beehive.framework.exception.WarnException;
import com.sojava.beehive.hibernate.dao.impl.BeehiveDaoImpl;

@Repository
@Scope("prototype")
@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = {CommonException.class, ErrorException.class, WarnException.class, Exception.class, Throwable.class})
public class HomepageDaoImpl extends BeehiveDaoImpl implements HomepageDao {
	private static final long serialVersionUID = 5318913848127245067L;

	@SuppressWarnings("unchecked")
	@Override
	public List<String> typeList(String kind) throws Exception {
		List<String> list = null;

		try {
			String _kind = kind == null ? "" : " and kind='" + kind + "'";
			SQLQuery stmt = getSession().createSQLQuery("select distinct type from data_transform.inpatient_homepage_analy where 1=1" + _kind + " order by type");
			list = stmt.list();

			return list;
		}
		catch(Exception ex) {
			throw new ErrorException(getClass(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Dictionary> getDic(String kind) throws Exception {
		List<Dictionary> list = null;
		try {
			list = (List<Dictionary>) query(Dictionary.class, new Criterion[]{Restrictions.eq("kind", kind)}, new Order[]{Order.asc("id")}, null, false);

			return list;
		}
		catch(Exception ex) {
			throw new ErrorException(getClass(), ex);
		}
	}

	@Override
	public boolean emptyCheckinfo(int pid) throws Exception {

		Query stmt = getSession().createQuery("delete from InpatientHomepageAnalyCheck where inpatientHomepageAnaly.id=:pid");
		stmt.setInteger("pid", pid);
		return stmt.executeUpdate() > 0;
	}

}
