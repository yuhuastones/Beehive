package com.sojava.beehive.hibernate.dao.impl;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sojava.beehive.framework.component.user.bean.UserInfo;
import com.sojava.beehive.framework.define.Page;
import com.sojava.beehive.framework.exception.CommonException;
import com.sojava.beehive.framework.exception.ErrorException;
import com.sojava.beehive.framework.exception.WarnException;
import com.sojava.beehive.framework.util.hibernate.CriterionUtil;
import com.sojava.beehive.hibernate.dao.AnyihisDao;

@Repository
@Scope("prototype")//singleton
@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, rollbackFor = {CommonException.class, ErrorException.class, WarnException.class, Exception.class, Throwable.class})
public class AnyihisDaoImpl implements Serializable, AnyihisDao {
	private static final long serialVersionUID = 6946344400946027587L;

	@Resource(name = "anyihisSessionFactory") private SessionFactory sessionFactory;
	private UserInfo userInfo;

	@Override
	public List<?> query(Class<?> entity, Criterion[] filters, Order[] orders, Page page, boolean usableData) throws Exception {
		CriterionUtil criterionUtil = new CriterionUtil(getSession());
		criterionUtil.addCriterion(filters);
		criterionUtil.addOrder(orders);
		return criterionUtil.createCriteria(entity, page, usableData).list();
	}

	public Object get(Class<?> entity, Serializable id) throws Exception {
		return getSession().get(entity, id);
	}

	@Override
	public void save(Object[] entities) throws Exception {
		for (Object entity: entities) save(entity);
	}

	@Override
	public void save(Object entity) throws Exception {
		getSession().saveOrUpdate(entity);
	}

	public Session getSession() {
		return this.sessionFactory.getCurrentSession();
	}

	public Session openSession() {
		return this.sessionFactory.openSession();
	}

	@Override
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	@Override
	public UserInfo getUserInfo() {
		return this.userInfo;
	}
}
