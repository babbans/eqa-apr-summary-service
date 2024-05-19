package com.eqa.eqaadvancereportservice.service;

import com.eqa.eqaadvancereportservice.constants.AnnualProgramReportTaskConstant;
import com.eqa.eqaadvancereportservice.dto.AnnualProgramReportTaskDetailDTO;
import com.eqa.eqaadvancereportservice.dto.ResponseObject;
import com.eqa.eqaadvancereportservice.entity.AnnualProgramReportTaskDetail;
import com.eqa.eqaadvancereportservice.exception.CustomException;
import com.eqa.eqaadvancereportservice.exception.UnauthorizedException;
import com.eqa.eqaadvancereportservice.repository.AnnualProgramReportTaskDetailRepository;
import com.eqa.eqaadvancereportservice.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnnualProgramReportTaskDetailService {

    @Autowired
    private AnnualProgramReportTaskDetailRepository taskDetailRepository;

    @Autowired
    private ModelMapper modelMapper;

    public ResponseEntity<ResponseObject> findAll() throws CustomException {
        try {
            List<AnnualProgramReportTaskDetail> taskDetails = taskDetailRepository.findAll();
            log.info("AnnualProgramReportTaskDetail fetched successfully from DB");
            return CommonUtils.buildResponseEntity(Arrays.asList(AnnualProgramReportTaskConstant.APR_TASK_LIST_SUCCESS.getBusinessMsg()),
                    AnnualProgramReportTaskConstant.APR_TASK_LIST_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), groupTask(taskDetails),
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_LIST_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_LIST_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while fetching AnnualProgramReportTaskDetail list {}", ex.getMessage());
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_LIST_FAILED);
        }
    }

    public ResponseEntity<ResponseObject> save(AnnualProgramReportTaskDetailDTO dto) {
        try {
            List<AnnualProgramReportTaskDetail> taskDetails = new ArrayList<>();
            for (AnnualProgramReportTaskDetailDTO.TaskDTO task : dto.getTasks()) {
                AnnualProgramReportTaskDetail taskDetail = new AnnualProgramReportTaskDetail();
                taskDetail.setProgramId(dto.getProgramId());
                taskDetail.setDepartmentId(dto.getDepartmentId());
                taskDetail.setCollegeId(dto.getCollegeId());
                taskDetail.setAcademicYear(dto.getAcademicYear());
                taskDetail.setResponsible(task.getResponsible());
                taskDetail.setSection(task.getSectionId());
                taskDetail.setActive(task.isActive());
                taskDetail.setCreationDatetime(LocalDateTime.now());
                taskDetails.add(taskDetail);
            }
            List<AnnualProgramReportTaskDetail> savedTaskDetails = taskDetailRepository.saveAll(taskDetails);
            log.info("AnnualProgramReportTaskDetail saved successfully");

            return CommonUtils.buildResponseEntity(Arrays.asList(AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getBusinessMsg()),
                    AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), groupTask(savedTaskDetails),
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while saving AnnualProgramReportTaskDetail {}", ex.getMessage());
            if(ex instanceof UnauthorizedException){
                throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_UNAUTHORIZED_ACCESS);
            }
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_CREATION_FAILED);
        }
    }
    public ResponseEntity<ResponseObject> updateTaskDetail(AnnualProgramReportTaskDetail setting, long id) throws CustomException {
        AnnualProgramReportTaskDetail existingTaskDetail = getExistingTaskDetail(id);
        try {
            modelMapper.map(setting, existingTaskDetail);
            AnnualProgramReportTaskDetail updatedTaskDetail = taskDetailRepository.save(existingTaskDetail);
            log.info("Report TaskDetail updated successfully");
            return CommonUtils.buildResponseEntity(Arrays.asList(AnnualProgramReportTaskConstant.APR_TASK_UPDATE_SUCCESS.getBusinessMsg()),
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
            return CommonUtils.buildResponseEntity(Arrays.asList(AnnualProgramReportTaskConstant.APR_TASK_GET_SUCCESS.getBusinessMsg()),
                    AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus().getReasonPhrase(),
                    String.valueOf(Math.round(Math.random() * 100)), existingReportTaskDetail,
                    String.valueOf(AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus().value()), null,
                    new HttpHeaders(), AnnualProgramReportTaskConstant.APR_TASK_CREATE_SUCCESS.getHttpStatus());
        } catch (Exception ex) {
            log.error("Error while fetching AnnualProgramReportTaskDetail {}", ex.getMessage());
            throw new CustomException(AnnualProgramReportTaskConstant.APR_TASK_NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseObject> deleteTaskDetail(List<Long> ids) throws CustomException {
        try {
            taskDetailRepository.deleteWithIds(ids);
            log.info("AnnualProgramReportTaskDetail deleted with ids {}", ids);
            return CommonUtils.buildResponseEntity(Arrays.asList(AnnualProgramReportTaskConstant.APR_TASK_DELETE_SUCCESS.getBusinessMsg()),
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

            List<AnnualProgramReportTaskDetailDTO.TaskDTO> tasks = groupedTasks.stream().map(task -> {
                AnnualProgramReportTaskDetailDTO.TaskDTO taskDTO = new AnnualProgramReportTaskDetailDTO.TaskDTO();
                taskDTO.setResponsible(task.getResponsible());
                taskDTO.setSectionId(task.getSection());
                taskDTO.setActive(task.isActive());
                return taskDTO;
            }).collect(Collectors.toList());

            dto.setTasks(tasks);
            dtos.add(dto);
        }
        return dtos;
    }
}