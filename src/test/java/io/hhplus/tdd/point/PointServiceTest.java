package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {
    @Mock
    private PointHistoryTable pointHistoryTable;
    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointServiceImpl pointService;


    @Test
    @DisplayName("유효하지 않은 사용자 잔액조회시 IllegalArgumentException 예외 발생 ")
    public void findByUserId_InvalidUser_ThrowsIllegalArgumentException(){
        long id = -1;
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.findByUserId(id)
        );

        // 예외 메시지 검증
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 사용자 입니다.");
    }

    @Test
    @DisplayName("사용자 잔액 조회시 정상 잔액 조회")
    public void findByUserId_success(){
        long id = 1;
        long amount = 1000;
        long time = System.currentTimeMillis();

        UserPoint userPoint = new UserPoint(id, amount, time);

        when(userPointTable.selectById(id)).thenReturn(userPoint);

        UserPoint result = pointService.findByUserId(id);

        assertThat(result.point()).isEqualTo(amount);
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("유효하지 않은 사용자 이력 조회시 IllegalArgumentException 예외 발생 ")
    public void getUserHistoryByUserId_InvalidUser_ThrowsIllegalArgumentException(){
        long id = -1;
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.getUserHistoryByUserId(id)
        );
        // 예외 메시지 검증
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 사용자 입니다.");
    }

    @Test
    @DisplayName("사용자 이력 조회시 정상 조회")
    public void getUserHistoryByUserId_success(){
        long id = 1;
        long userId = 1;
        long amount = 1000;
        long time = System.currentTimeMillis();

        List<PointHistory> history = new ArrayList<>();
        PointHistory pointHistory = new PointHistory(id, userId, amount, TransactionType.CHARGE, time);
        history.add(pointHistory);

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(history);

        List<PointHistory> result = pointService.getUserHistoryByUserId(id);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).id()).isEqualTo(id);
        assertThat(result.get(0).amount()).isEqualTo(amount);
        assertThat(result.get(0).type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    @DisplayName("유효하지 않은 사용자 잔액 충전시 IllegalArgumentException 예외 발생 ")
    public void charge_InvalidUser_ThrowsIllegalArgumentException(){
        long id = -1;
        long amount = 1000;
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.charge(id, amount)
        );
        // 예외 메시지 검증
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 사용자 입니다.");
    }
    @Test
    @DisplayName("유효하지 않은 잔액 충전시 IllegalArgumentException 예외 발생 ")
    public void charge_InvalidAmount_ThrowsIllegalArgumentException(){
        long id = 1;
        long amount = -1;
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.charge(id, amount)
        );
        // 예외 메시지 검증
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 금액입니다.");
    }

    @Test
    @DisplayName("잔액 충전시 최대 금액을 초과시 IllegalArgumentException 예외 발생 ")
    public void charge_maxAmount_ThrowsIllegalArgumentException(){
        long id = 1;
        long amount = 100000L;
        long MAX_POINT = 100000L;
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        UserPoint currentUser = new UserPoint(id, amount, System.currentTimeMillis());
        when(userPointTable.selectById(id)).thenReturn(currentUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.charge(id, amount)
        );
        // 예외 메시지 검증
        assertThat(exception.getMessage()).isEqualTo("잔고가 초과 되었습니다 포인트는 "+ MAX_POINT +"을 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("1000원 을가진 유저가 1000원 충전시 정상 적으로 총 금액은 2000원이 된다.")
    public void charge_success(){
        long id = 1;
        long amount = 1000;
        long chargePoint = 2000;

        long time = System.currentTimeMillis();
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        UserPoint currentUser = new UserPoint(id, amount, time);
        UserPoint updateUserPoint = new UserPoint(id, chargePoint, time);
        PointHistory expectedHistory = new PointHistory(1, id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        when(userPointTable.insertOrUpdate(id, chargePoint)).thenReturn(updateUserPoint);
        when(userPointTable.selectById(id)).thenReturn(currentUser);
        when(pointHistoryTable.insert(eq(id), eq(amount), eq(TransactionType.CHARGE), anyLong())).thenReturn(expectedHistory);
        // when: 실제 테스트 대상 메서드 실행
        UserPoint successUser = pointService.charge(id, amount);

        assertThat(successUser).isNotNull();
        assertThat(successUser.id()).isEqualTo(id);
        assertThat(successUser.point()).isEqualTo(currentUser.point() + amount);

        verify(pointHistoryTable).insert(eq(id), eq(amount), eq(TransactionType.CHARGE), anyLong());

        // 예외 메시지 검증

    }


    @Test
    @DisplayName("유효하지 않은 사용자 잔액 충전시 IllegalArgumentException 예외 발생 ")
    public void use_InvalidUser_ThrowsIllegalArgumentException(){
        long id = -1;
        long amount = 1000;
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.use(id, amount)
        );
        // 예외 메시지 검증
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 사용자 입니다.");
    }
    @Test
    @DisplayName("유효하지 않은 잔액 충전시 IllegalArgumentException 예외 발생 ")
    public void use_InvalidAmount_ThrowsIllegalArgumentException(){
        long id = 1;
        long amount = -1;
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.use(id, amount)
        );
        // 예외 메시지 검증
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 금액입니다.");
    }

    @Test
    @DisplayName("잔액 충전시 최대 금액을 초과시 IllegalArgumentException 예외 발생 ")
    public void use_minAmount_ThrowsIllegalArgumentException(){
        long id = 1;
        long amount = 1000L;
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        UserPoint currentUser = new UserPoint(id, 0, System.currentTimeMillis());

        when(userPointTable.selectById(id)).thenReturn(currentUser);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pointService.use(id, amount)
        );
        // 예외 메시지 검증
        assertThat(exception.getMessage()).isEqualTo("잔고에 금액이 부족합니다.");
    }

    @Test
    @DisplayName("1000원 을가진 유저가 500원 사용시 정상 적으로 총 금액은 500원이 된다.")
    public void use_success(){
        long id = 1;
        long amount = 1000;
        long usePoint = 500;

        long time = System.currentTimeMillis();
        // when & then: 잘못된 ID로 메서드를 호출 시 예외 발생 검증
        UserPoint currentUser = new UserPoint(id, amount, time);
        UserPoint updateUserPoint = new UserPoint(id, usePoint, time);
        PointHistory expectedHistory = new PointHistory(1, id, usePoint, TransactionType.USE, System.currentTimeMillis());

        when(userPointTable.insertOrUpdate(id, usePoint)).thenReturn(updateUserPoint);
        when(userPointTable.selectById(id)).thenReturn(currentUser);
        when(pointHistoryTable.insert(eq(id), eq(usePoint), eq(TransactionType.USE), anyLong())).thenReturn(expectedHistory);
        // when: 실제 테스트 대상 메서드 실행
        UserPoint successUser = pointService.use(id, usePoint);

        assertThat(successUser).isNotNull();
        assertThat(successUser.id()).isEqualTo(id);
        assertThat(successUser.point()).isEqualTo(currentUser.point() - usePoint);

        verify(pointHistoryTable).insert(eq(id), eq(usePoint), eq(TransactionType.USE), anyLong());

        // 예외 메시지 검증

    }

}
