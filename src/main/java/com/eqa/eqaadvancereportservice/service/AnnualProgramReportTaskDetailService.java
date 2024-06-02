package com.eqa.eqaadvancereportservice.service;

import com.eqa.eqaadvancereportservice.constants.AnnualProgramReportDataConstant;
import com.eqa.eqaadvancereportservice.constants.AnnualProgramReportTaskConstant;
import com.eqa.eqaadvancereportservice.dto.AnnualProgramReportTaskDetailDTO;
import com.eqa.eqaadvancereportservice.dto.ResponseObject;
import com.eqa.eqaadvancereportservice.entity.AnnualProgramReportTaskDetail;
import com.eqa.eqaadvancereportservice.entity.ReportMaster;
import com.eqa.eqaadvancereportservice.exception.CustomException;
import com.eqa.eqaadvancereportservice.exception.UnauthorizedException;
import com.eqa.eqaadvancereportservice.repository.AnnualProgramReportTaskDetailRepository;
import com.eqa.eqaadvancereportservice.repository.ReportMasterRepository;
import com.eqa.eqaadvancereportservice.util.CommonUtils;
import com.eqa.eqaadvancereportservice.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnnualProgramReportTaskDetailService {

    @Autowired
    private AnnualProgramReportTaskDetailRepository taskDetailRepository;

    @Autowired
    ReportMasterRepository reportMasterRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MessageUtil messageUtil;

    public ResponseEntity<ResponseObject> findAll() throws CustomException {
        try {
            List<AnnualProgramReportTaskDetail> taskDetails = taskDetailRepository.findAll();
            log.info("AnnualProgramReportTaskDetail fetched successfully from DB");
            List<AnnualProgramReportTaskDetailDTO> groupedTasks = groupTask(taskDetails);
            String localizedMessage = messageUtil.getLocalizedMessage(
                    AnnualProgramReportTaskConstant.APR_TASK_LIST_SUCCESS.getBusinessCode()
            );
            return CommonUtils.buildResponseEntity(Arrays.asList(localizedMessage),
                    AnnualProgramReportTaskConstant.APR_TASK_LIST_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), groupedTasks,
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_LIST_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_LIST_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while fetching AnnualProgramReportTaskDetail list {}", ex.getMessage());
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_LIST_FAILED);
        }
    }

    public ResponseEntity<ResponseObject> save(AnnualProgramReportTaskDetailDTO dto) {
        ReportMaster savedReportMaster = null;
        Optional<ReportMaster> reportMasterOptional = reportMasterRepository.findByCollegeIdAndDepartmentIdAndProgramIdAndAcademicYear(
                dto.getCollegeId(),
                dto.getDepartmentId(),
                dto.getProgramId(),
                dto.getAcademicYear()
        );
        if(!reportMasterOptional.isPresent()){
            ReportMaster reportMaster = new ReportMaster();
            reportMaster.setProgramId(dto.getProgramId());
            reportMaster.setDepartmentId(dto.getDepartmentId());
            reportMaster.setCollegeId(dto.getCollegeId());
            reportMaster.setAcademicYear(dto.getAcademicYear());

            savedReportMaster = reportMasterRepository.save(reportMaster);
            log.info("AnnualProgramReportMaster saved successfully {}", savedReportMaster);
        } else {
            savedReportMaster = reportMasterOptional.get();
        }
        try {

            List<AnnualProgramReportTaskDetail> taskDetails = new ArrayList<>();
            for (AnnualProgramReportTaskDetailDTO.TaskDTO task : dto.getTasks()) {
                AnnualProgramReportTaskDetail taskDetail = new AnnualProgramReportTaskDetail();
                taskDetail.setProgramId(dto.getProgramId());
                taskDetail.setDepartmentId(dto.getDepartmentId());
                taskDetail.setCollegeId(dto.getCollegeId());
                taskDetail.setAcademicYear(dto.getAcademicYear());
                taskDetail.setId(task.getId());
                taskDetail.setResponsible(task.getResponsible());
                taskDetail.setSection(task.getSectionId());
                taskDetail.setActive(task.isActive());
                if (task.getId() != null) {
                    taskDetail.setCreatedBy(task.getCreatedBy());
                    taskDetail.setCreationDatetime(task.getCreationDatetime());
                }
                taskDetail.setReportMaster(savedReportMaster); // Set the report master
                taskDetails.add(taskDetail);
            }

            List<AnnualProgramReportTaskDetail> savedTaskDetails = taskDetailRepository.saveAll(taskDetails);
            log.info("AnnualProgramReportTaskDetail saved/updated successfully");

            List<AnnualProgramReportTaskDetailDTO> groupedTasks = groupTask(savedTaskDetails);
            String localizedMessage = messageUtil.getLocalizedMessage(
                    AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getBusinessCode()
            );
            return CommonUtils.buildResponseEntity(Arrays.asList(localizedMessage),
                    AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), groupedTasks,
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while saving/updating AnnualProgramReportTaskDetail {}", ex.getMessage());
            if (ex instanceof UnauthorizedException) {
                throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_UNAUTHORIZED_ACCESS);
            }
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_CREATION_FAILED);
        }
    }
    public ResponseEntity<ResponseObject> updateTaskDetail(AnnualProgramReportTaskDetail taskDetail, long id) throws CustomException {
        AnnualProgramReportTaskDetail existingTaskDetail = getExistingTaskDetail(id);
        try {
            modelMapper.map(taskDetail, existingTaskDetail);
            AnnualProgramReportTaskDetail updatedTaskDetail = taskDetailRepository.save(existingTaskDetail);
            log.info("Report TaskDetail updated successfully");
            String localizedMessage = messageUtil.getLocalizedMessage(
                    AnnualProgramReportTaskConstant.APR_TASK_UPDATE_SUCCESS.getBusinessCode()
            );
            return CommonUtils.buildResponseEntity(Arrays.asList(localizedMessage),
                    AnnualProgramReportTaskConstant.APR_TASK_UPDATE_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), updatedTaskDetail,
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_UPDATE_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_UPDATE_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while updating TaskDetail {}", ex.getMessage());
            if(ex instanceof UnauthorizedException){
                throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_UNAUTHORIZED_ACCESS);
            }
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_UPDATE_FAILED);
        }
    }

    public ResponseEntity<ResponseObject> findById(Long id) throws CustomException {
        AnnualProgramReportTaskDetail existingReportTaskDetail = getExistingTaskDetail(id);
        try {
            String localizedMessage = messageUtil.getLocalizedMessage(
                AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getBusinessCode()
            );
            return CommonUtils.buildResponseEntity(Arrays.asList(localizedMessage),
                    AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), existingReportTaskDetail,
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while fetching AnnualProgramReportTaskDetail {}", ex.getMessage());
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_GET_FAILED);
        }
    }

    public ResponseEntity<ResponseObject> deleteTaskDetail(List<Long> ids) throws CustomException {
        try {
            taskDetailRepository.deleteWithIds(ids);
            log.info("AnnualProgramReportTaskDetail deleted with ids {}", ids);
            String localizedMessage = messageUtil.getLocalizedMessage(
                    AnnualProgramReportTaskConstant.APR_TASK_DELETE_SUCCESS.getBusinessCode()
            );
            return CommonUtils.buildResponseEntity(Arrays.asList(localizedMessage),
                    AnnualProgramReportTaskConstant.APR_TASK_DELETE_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), null,
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_DELETE_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_DELETE_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while deleting AnnualProgramReportTaskDetail {}", ex.getMessage());
            if(ex instanceof UnauthorizedException){
                throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_UNAUTHORIZED_ACCESS);
            }
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_DELETION_FAILED);
        }
    }
    public ResponseEntity<ResponseObject> findByReportId(String reportId) throws CustomException {
        try {
            List<AnnualProgramReportTaskDetail> taskDetails = taskDetailRepository.findByReportMaster_ReportId(reportId);
            if (taskDetails.isEmpty()) {
                throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_NOT_FOUND);
            }
            List<AnnualProgramReportTaskDetailDTO> groupedTasks = groupTask(taskDetails);String localizedMessage = messageUtil.getLocalizedMessage(
                    AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getBusinessCode()
            );
            return CommonUtils.buildResponseEntity(Arrays.asList(localizedMessage),
                    AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), groupedTasks,
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while fetching AnnualProgramReportTaskDetail by reportId {}", ex.getMessage());
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_GET_FAILED);
        }
    }

    private AnnualProgramReportTaskDetail getExistingTaskDetail(Long id) {
        return taskDetailRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("AnnualProgramReportTaskDetail not found with id {}", id);
                    return new CustomException(AnnualProgramReportTaskConstant.APR_TASK_NOT_FOUND);
                });
    }
    private List<AnnualProgramReportTaskDetailDTO> groupTask(List<AnnualProgramReportTaskDetail> taskDetails) {
        // Group by programId, departmentId, collegeId, and academicYear
        Map<String, List<AnnualProgramReportTaskDetail>> grouped = taskDetails.stream()
                .collect(Collectors.groupingBy(task -> task.getProgramId() + "-" + task.getDepartmentId() + "-" + task.getCollegeId() + "-" + task.getAcademicYear()));

        // Convert to DTO
        List<AnnualProgramReportTaskDetailDTO> dtos = new ArrayList<>();
        for (Map.Entry<String, List<AnnualProgramReportTaskDetail>> entry : grouped.entrySet()) {
            List<AnnualProgramReportTaskDetail> groupedTasks = entry.getValue();
            AnnualProgramReportTaskDetail firstTask = groupedTasks.get(0);

            AnnualProgramReportTaskDetailDTO dto = new AnnualProgramReportTaskDetailDTO();
            dto.setProgramId(firstTask.getProgramId());
            dto.setDepartmentId(firstTask.getDepartmentId());
            dto.setCollegeId(firstTask.getCollegeId());
            dto.setAcademicYear(firstTask.getAcademicYear());
            dto.setReportId(firstTask.getReportId().toString());

            List<AnnualProgramReportTaskDetailDTO.TaskDTO> tasks = groupedTasks.stream().map(task -> {
                AnnualProgramReportTaskDetailDTO.TaskDTO taskDTO = new AnnualProgramReportTaskDetailDTO.TaskDTO();
                taskDTO.setId(task.getId());
                taskDTO.setResponsible(task.getResponsible());
                taskDTO.setSectionId(task.getSection());
                taskDTO.setActive(task.isActive());
                taskDTO.setCreatedBy(task.getCreatedBy());
                taskDTO.setCreationDatetime(task.getCreationDatetime());
                taskDTO.setUpdatedBy(task.getUpdatedBy());
                taskDTO.setUpdateDatetime(task.getUpdateDatetime());
                return taskDTO;
            }).collect(Collectors.toList());

            dto.setTasks(tasks);
            dtos.add(dto);
        }
        return dtos;
    }
    public AnnualProgramReportTaskDetailDTO getTasksByReportId(String reportId) throws CustomException {
        List<AnnualProgramReportTaskDetail> taskDetails = taskDetailRepository.findByReportMaster_ReportId(reportId);
        if (taskDetails.isEmpty()) {
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_NOT_FOUND);
        }
        return groupTask(taskDetails).get(0);
    }
}
