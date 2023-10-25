package com.vassarlabs.gp.service.api;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface TtobmaService {
    String downloadCapacityExcel(String excelName) throws IOException;

    String downloadDowntimeReportExcel() throws IOException;
}
