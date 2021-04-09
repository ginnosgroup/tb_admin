package org.zhinanzhen.b.service.pojo.ant;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;

public class ServiceOrderSorter extends Sorter {
	
	@Getter
	@JSONField(name = "adviser,name")
	String adviserName;

}
