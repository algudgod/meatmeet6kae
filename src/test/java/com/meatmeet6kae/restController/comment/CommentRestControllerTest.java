package com.meatmeet6kae.restController.comment;

import com.meatmeet6kae.service.comment.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class CommentRestControllerTest {
    @MockBean
    private CommentService commentService;
    @Autowired
    private MockMvc mockMvc;

    //@Test
    void getListComments() throws Exception {
        int boardNo = 10; // 테스트할 boardNo
        // MockMvc를 사용해 /getListComments/{boardNo} 호출 시뮬레이션
        mockMvc.perform(get("/comments/getListComments/{boardNo}", boardNo)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // HTTP 상태 200 OK 확인
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // JSON 응답 확인
                .andExpect(jsonPath("$").isArray()); // 응답이 배열인지 확인
    }
}
