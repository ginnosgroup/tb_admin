package org.zhinanzhen.b.service.pojo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExchangeRateDTO {

	double rate;

	Date updateDate;

}
