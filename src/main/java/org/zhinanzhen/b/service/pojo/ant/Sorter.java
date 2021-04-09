package org.zhinanzhen.b.service.pojo.ant;

import com.ikasoa.core.utils.StringUtil;

import lombok.Data;

@Data
public class Sorter {

	String id;

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
