package com.sojava.beehive.framework.component.user.bean;

// Generated 2014-4-12 10:12:55 by Hibernate Tools 3.4.0.CR1

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * UserInfo generated by hbm2java
 */
@Entity
@Table(name = "user_info", schema = "dic")
public class UserInfo implements Serializable {
	private static final long serialVersionUID = -5020975536490067946L;

	@Id
	@Column(name = "seq", unique = true, nullable = false)
	private int seq;
	@Column(name = "user_id", length = 32)
	private String userId;
	@Column(name = "user_name", length = 32)
	private String userName;
	@Column(name = "name", length = 32)
	private String name;
	@Column(name = "password", length = 128)
	private String password;
	@Column(name = "dept_name", length = 64)
	private String deptName;
	@Column(name = "dept_name_py", length = 64)
	private String deptNamePy;
	@Column(name = "title", length = 64)
	private String title;
	@Column(name = "job", length = 64)
	private String job;

	public UserInfo() {}

	public UserInfo(int seq) {
		this.seq = seq;
	}

	public UserInfo(int seq, String userId, String userName, String name,
			String password, String deptName, String deptNamePy, String title, String job) {
		this.seq = seq;
		this.userId = userId;
		this.userName = userName;
		this.name = name;
		this.password = password;
		this.deptName = deptName;
		this.setDeptNamePy(deptNamePy);
		this.title = title;
		this.job = job;
	}

	public int getSeq() {
		return this.seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDeptName() {
		return this.deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getDeptNamePy() {
		return deptNamePy;
	}

	public void setDeptNamePy(String deptNamePy) {
		this.deptNamePy = deptNamePy;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getJob() {
		return this.job;
	}

	public void setJob(String job) {
		this.job = job;
	}

}
