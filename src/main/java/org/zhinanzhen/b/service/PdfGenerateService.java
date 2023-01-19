package org.zhinanzhen.b.service;

import org.zhinanzhen.tb.service.ServiceException;

public interface PdfGenerateService {
    String generate(int id)throws ServiceException;
}
