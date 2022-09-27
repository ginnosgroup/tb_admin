package org.zhinanzhen.b.service;

import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface VisaOfficialService {
    int addVisa(VisaOfficialDTO visaOfficialDto) throws ServiceException;
}
