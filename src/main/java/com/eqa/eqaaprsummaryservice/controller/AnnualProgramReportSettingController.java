package com.eqa.eqaaprsummaryservice.controller;

import com.eqa.eqaaprsummaryservice.constants.CommonConstants;
import com.eqa.eqaaprsummaryservice.dto.ResponseObject;
import com.eqa.eqaaprsummaryservice.entity.AnnualProgramReportSetting;
import com.eqa.eqaaprsummaryservice.exception.CustomException;
import com.eqa.eqaaprsummaryservice.service.AnnualProgramReportSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(CommonConstants.API_BASE_PATH + CommonConstants.REPORT_SETTING)
@Validated
@Slf4j
public class AnnualProgramReportSettingController {

    @Autowired
    private AnnualProgramReportSettingService settingService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllSettings() {
        log.info("getAllSettings() : Start");
        return settingService.findAll();
    }

    @PostMapping
    public ResponseEntity<ResponseObject> createSetting(@RequestHeader("username") String username, @Validated @RequestBody List<AnnualProgramReportSetting> settings) {
        log.info("createSetting() : Start");
        log.info("Setting data {}", settings);
        return settingService.save(settings);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateSetting(@RequestHeader("username") String username,@Validated @RequestBody AnnualProgramReportSetting setting,
                                                       @PathVariable("id") long id) throws CustomException {
        log.info("updateSetting() : Start");
        log.info("Setting id {} and Data {}", id, setting);
        return settingService.updateSetting(setting, id, username);
    }

    @DeleteMapping
    public ResponseEntity<ResponseObject> deleteSetting(@RequestParam List<Long> ids)
            throws CustomException {
        log.info("deleteSetting() : Start, ids are {}", ids);
        return settingService.deleteSetting(ids);
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseObject> getSettingById(@PathVariable Long id) throws CustomException{
        log.info("getSettingById() : Start, id is {}", id);
        return settingService.findById(id);
    }
}