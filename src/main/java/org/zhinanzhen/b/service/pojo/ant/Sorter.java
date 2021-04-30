package org.zhinanzhen.b.service.pojo.ant;

import com.alibaba.fastjson.annotation.JSONField;
import com.ikasoa.core.utils.StringUtil;

import lombok.Data;

@Data
public class Sorter {

	String id;

	@JSONField(name = "user,name")
	String userName;

	@JSONField(name = "adviser,name")
	String adviserName;

	public String getOrderBy(String key, String value) {
		if (StringUtil.isNotEmpty(key)) {
			if ("ascend".equalsIgnoreCase(value))
				return StringUtil.merge(key, " ASC");
			if ("descend".equalsIgnoreCase(value))
				return StringUtil.merge(key, " DESC");
			else
				return "";
		}
		return "";
	}

}
