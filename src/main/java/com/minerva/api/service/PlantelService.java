package com.minerva.api.service;

import java.util.List;

import com.minerva.api.dto.PlantelDTO;

public interface PlantelService {


    PlantelDTO savePlantel(PlantelDTO planteldto);

    PlantelDTO updatePlantel(Long plantelId, PlantelDTO planteldto);

    void deletePlantel(Long plantelId);

    void softDeletePlantel(Long plantelId);

    List<PlantelDTO> findAll();

    PlantelDTO getPlantelById(Long plantelId);
    
}
