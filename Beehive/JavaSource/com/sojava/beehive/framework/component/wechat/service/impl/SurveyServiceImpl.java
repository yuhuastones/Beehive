package com.sojava.beehive.framework.component.wechat.service.impl;

import com.sojava.beehive.framework.component.wechat.bean.Survey;
import com.sojava.beehive.framework.component.wechat.bean.SurveyMain;
import com.sojava.beehive.framework.component.wechat.bean.SurveyOption;
import com.sojava.beehive.framework.component.wechat.bean.SurveyQuestion;
import com.sojava.beehive.framework.component.wechat.dao.SurveyDao;
import com.sojava.beehive.framework.component.wechat.service.SurveyService;
import com.sojava.beehive.framework.define.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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

		List<Survey> list = surveyDao.listSurvey(filters.toArray(new Criterion[0]), new Order[] {Order.desc("beginTime")}, page);
		for(Survey survey : list) {
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
		Map<String, Object> lastQuest = null;

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
			if (quest.getId() == 0) questions.add(0, question);
			else if (quest.getId() == 10000) lastQuest = question;
			else questions.add(question);
		}
		questions.add(lastQuest);
		rest.put("questions", questions);

		return rest;
	}

	public SurveyDao getSurveyDao() {
		return surveyDao;
	}

	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}

}
