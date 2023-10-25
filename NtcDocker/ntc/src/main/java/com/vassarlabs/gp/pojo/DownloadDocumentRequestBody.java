package com.vassarlabs.gp.pojo;

import com.vassarlabs.gp.constants.ErrorMessages;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class DownloadDocumentRequestBody {

//    @NotBlank(message = ErrorMessages.TYPE_NULL_ERROR)
    private String type;

    @NotNull(message = ErrorMessages.FILE_NAMES_NULL_ERROR)
    private List<String> fileNames;
}
