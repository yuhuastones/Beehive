package com.sojava.beehive.framework.component.wechat.bean;

import java.io.Serializable;
import javax.persistence.*;

import java.util.Date;


/**
 * The persistent class for the survey_result database table.
 * 
 */
@Entity
@Table(name="survey_result", schema="wechat")
@NamedQuery(name="SurveyResult.findAll", query="SELECT s FROM SurveyResult s")
public class SurveyResult implements Serializable {
	private static final long serialVersionUID = -7077482932560236514L;

	@Id
	@SequenceGenerator(name="SURVEY_RESULT_ID_GENERATOR", sequenceName="WECHAT.SURVEY_RESULT_ID")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SURVEY_RESULT_ID_GENERATOR")
	private Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="begin_time")
	private Date beginTime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="end_time")
	private Date endTime;

	private Integer mid;

	private String openid;

	@Column(name="phone_number")
	private String phoneNumber;

	private String result;

	@Column(name="wechat_id")
	private String wechatId;

	public SurveyResult() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getBeginTime() {
		return this.beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getMid() {
		return this.mid;
	}

	public void setMid(Integer mid) {
		this.mid = mid;
	}

	public String getOpenid() {
		return this.openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getWechatId() {
		return this.wechatId;
	}

	public void setWechatId(String wechatId) {
		this.wechatId = wechatId;
	}

}