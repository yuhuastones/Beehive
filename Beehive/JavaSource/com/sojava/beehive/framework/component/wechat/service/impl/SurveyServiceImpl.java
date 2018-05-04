package com.sojava.beehive.framework.component.wechat.service.impl;

import com.sojava.beehive.framework.component.wechat.bean.SurveyMain;
import com.sojava.beehive.framework.component.wechat.bean.SurveyOption;
import com.sojava.beehive.framework.component.wechat.bean.SurveyQuestion;
import com.sojava.beehive.framework.component.wechat.bean.SurveyResult;
import com.sojava.beehive.framework.component.wechat.dao.SurveyDao;
import com.sojava.beehive.framework.component.wechat.service.SurveyService;
import com.sojava.beehive.framework.define.Page;
import com.sojava.beehive.framework.util.AES;
import com.sojava.beehive.framework.util.FormatUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class SurveyServiceImpl implements SurveyService {

	@Resource private SurveyDao surveyDao;

	@Override
	public JSONArray listSurvey() throws Exception {
		JSONArray result = new JSONArray();
		Page page = new Page(0, 1000);
		List<Criterion> filters = new ArrayList<Criterion>();
		filters.add(Restrictions.eq("status", "启用"));
		Date now = new Date();
		filters.add(Restrictions.or(
				Restrictions.and(Restrictions.le("beginTime", now), Restrictions.ge("endTime", now)),
				Restrictions.and(Restrictions.le("beginTime", now), Restrictions.isNull("endTime")),
				Restrictions.and(Restrictions.ge("endTime", now), Restrictions.isNull("beginTime")))
			);

		List<SurveyMain> list = surveyDao.listSurvey(filters.toArray(new Criterion[0]), new Order[] {Order.desc("beginTime")}, page);
		for(SurveyMain survey : list) {
			JSONObject item = new JSONObject();
			item.accumulate("id", survey.getId());
			item.accumulate("title", survey.getTitle());
			item.accumulate("subTitle", survey.getSubTitle());
			item.accumulate("kind", survey.getKind());

			result.add(item);
		}

		return result;
	}

	@Override
	public Map<String, Object> getSurvey(int id) throws Exception {
		Map<String, Object> rest = new HashMap<String, Object>();
		List<Map<String, Object>> questions = new ArrayList<Map<String, Object>>();

		SurveyMain survey = surveyDao.getSurvey(id);

		rest.put("id", survey.getId());
		rest.put("title", survey.getTitle());
		rest.put("subTitle", survey.getSubTitle());
		rest.put("questionCount", survey.getQuestionCount());

		for (SurveyQuestion quest: survey.getSurveyQuestions()) {
			Map<String, Object> question = new HashMap<String, Object>();
			question.put("id", quest.getId());
			question.put("title", quest.getTitle());
			question.put("subTitle", quest.getSubTitle());
			question.put("inputShowing", quest.getInputShowing() == 1);
			question.put("placeholder", quest.getPlaceholder());
			question.put("multiSelect", quest.getMultiSelect() == 1);
			List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
			for (SurveyOption opt: quest.getSurveyOptions()) {
				Map<String, Object> option = new HashMap<String, Object>();
				option.put("label", opt.getLabel());
				option.put("inputShowing", opt.getInputShowing() == 1);
				option.put("isRight", opt.getIsRight() == 1);

				options.add(option);
			}
			question.put("options", options);
			questions.add(question);
		}
		rest.put("questions", questions);

		return rest;
	}

	@Override
	public void save(String data) throws Exception {
		JSONObject answer = JSONObject.fromObject(data);

		SurveyResult rest = new SurveyResult();
		rest.setMid(answer.getInt("id"));
		rest.setOpenid(answer.getString("openid"));
		rest.setBeginTime(FormatUtil.parseDateTime(answer.getString("beginTime")));
		rest.setEndTime(new Date());
		rest.setPhoneNumber(decryptData(answer.getString("encryptedData"), answer.getString("iv"), answer.getString("sessionKey")));
		rest.setResult(data);

		surveyDao.save(rest);
	}

	public String decryptData(String encryptedData, String iv, String sessionKey) throws Exception {
		String rest = null;

		byte[] resultByte = AES.decrypt(Base64.decodeBase64(encryptedData), Base64.decodeBase64(sessionKey), Base64.decodeBase64(iv));
		if (null != resultByte && resultByte.length > 0) {
			String userInfo = new String(resultByte, "UTF-8");

			JSONObject userJson = JSONObject.fromObject(userInfo);
			rest = userJson.getString("phoneNumber");
		}

		return rest;
	}

	public SurveyDao getSurveyDao() {
		return surveyDao;
	}

	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}

}
