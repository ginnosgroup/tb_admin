package org.zhinanzhen.b.service;

import org.zhinanzhen.tb.service.ServiceException;

public interface PdfGenerateService {
    int generate(int id)throws ServiceException;
}
