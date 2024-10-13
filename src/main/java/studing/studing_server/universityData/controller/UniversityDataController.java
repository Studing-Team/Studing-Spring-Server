package studing.studing_server.universityData.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studing.studing_server.common.dto.SuccessMessage;
import studing.studing_server.common.dto.SuccessStatusResponse;
import studing.studing_server.universityData.dto.UniversityNameRequest;
import studing.studing_server.universityData.service.UniversityDataService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/universityData")
@RequiredArgsConstructor
public class UniversityDataController {


    private final UniversityDataService universityDataService;

    @GetMapping("/university")
    public ResponseEntity<SuccessStatusResponse<List<String>>> getAllUniversityNames() {
        List<String> universityNames = universityDataService.getAllUniversityNames();

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.UNIVERSITY_GET_SUCCESS, universityNames));
    }


    @PostMapping("/department")
    public ResponseEntity<SuccessStatusResponse<List<String>>> getDepartmentNamesByUniversity(@RequestBody UniversityNameRequest universityNameRequest) {
        List<String> departmentNames = universityDataService.getDepartmentNamesByUniversity(universityNameRequest.universityName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessStatusResponse.of(SuccessMessage.DEPARTMENT_GET_SUCCESS, departmentNames));
    }


}
