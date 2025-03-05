package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    @DisplayName("특정유저포인트조회시정상적으로url호출시정상응답을반환한다")
    public void 특정유저포인트조회시정상적으로url호출시정상응답을반환한다() throws Exception {
        long id = 1L;
        UserPoint mockUserPoint = new UserPoint(id, 100, System.currentTimeMillis());

        // Mock 설정
        when(pointService.findByUserId(id)).thenReturn(mockUserPoint);

        mockMvc.perform(get("/point/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(100));
    }

    @Test
    @DisplayName("특정유저포인트충전시정상적으로url호출시정상응답을반환한다")
    public void 특정유저포인트충전시정상적으로url호출시정상응답을반환한다() throws Exception {
        long id = 1L;
        long amount = 130L;
        UserPoint mockUserPoint = new UserPoint(id, amount, System.currentTimeMillis());

        String requestBody = String.valueOf(amount);
        // Mock 설정
        when(pointService.charge(id, amount)).thenReturn(mockUserPoint);

        mockMvc.perform(patch("/point/{id}/charge",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))// 요청 본문)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(130));
    }


}
