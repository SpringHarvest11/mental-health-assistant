package com.nie.mentalserver.controller;

import com.nie.mental.common.result.Result;
import com.nie.mentalpojo.dto.ConsultationSessionStartDTO;
import com.nie.mentalpojo.vo.ConsultationSessionStartVO;
import com.nie.mentalserver.service.PsychologicalChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/psychological-chat")
@Tag(name = "会话管理")
public class PsychologicalChatController {

    @Autowired
    private PsychologicalChatService psychologicalChatService;

    /**
     *开始会话
     * @param consultationSessionStartDTO
     * @return
     */
    @Operation(summary = "创建会话")
    @PostMapping("/session/start")
    public Result<ConsultationSessionStartVO> sessionStart(@Valid @RequestBody ConsultationSessionStartDTO consultationSessionStartDTO){
        ConsultationSessionStartVO consultationSessionStartVO = psychologicalChatService.sessionStart(consultationSessionStartDTO);
        return Result.success(consultationSessionStartVO);
    }
}
