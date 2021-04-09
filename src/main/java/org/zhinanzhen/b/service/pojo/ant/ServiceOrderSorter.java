package org.zhinanzhen.b.service.pojo.ant;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;

public class ServiceOrderSorter extends Sorter {
	
	@Getter
	@Setter
	@JSONField(name = "adviser,name")
	String adviserName;

}
