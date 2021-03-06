package com.sojava.beehive.framework.component.inpatienthomepage.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the v_icd_transform database table.
 * 
 */
@Entity
@Table(name="v_icd_transform", schema="data_transform")
@NamedQuery(name="VIcdTransform.findAll", query="SELECT v FROM VIcdTransform v")
public class VIcdTransform implements Serializable {
	private static final long serialVersionUID = -2331151813646778349L;

	private String catalog;

	@EmbeddedId
	private VIcdTransformPK id;

	@Column(name="icd2_code")
	private String icd2Code;

	@Column(name="icd2_diagno")
	private String icd2Diagno;

	private String kind;

	private String status;

	private String type;

	public VIcdTransform() {
	}

	public String getCatalog() {
		return this.catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public VIcdTransformPK getId() {
		return this.id;
	}

	public void setId(VIcdTransformPK id) {
		this.id = id;
	}

	public String getIcd2Code() {
		return this.icd2Code;
	}

	public void setIcd2Code(String icd2Code) {
		this.icd2Code = icd2Code;
	}

	public String getIcd2Diagno() {
		return this.icd2Diagno;
	}

	public void setIcd2Diagno(String icd2Diagno) {
		this.icd2Diagno = icd2Diagno;
	}

	public String getKind() {
		return this.kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

}