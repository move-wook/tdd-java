package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointServiceImpl implements PointService {
    //table 안쓰고 내부 메모리 구조로 TDD용 테스트
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private final long MAX_POINT = 100000L;

    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    @Override
    public UserPoint findByUserId(long id) {
        if(id < 1){ throw new IllegalArgumentException("유효하지 않은 사용자 입니다.");}
        return userPointTable.selectById(id);
    }

    @Override
    public UserPoint charge(long id, long amount) {
        if(id < 1){ throw new IllegalArgumentException("유효하지 않은 사용자 입니다.");}
        if(amount < 1){ throw new IllegalArgumentException("유효하지 않은 금액입니다.");}

        UserPoint currentUser = userPointTable.selectById(id);

        //현재 잔액이 멕시멈 잔액을 넘기면 예외
        long totalAmount = currentUser.point() + amount;

        if(totalAmount > MAX_POINT){
            throw new IllegalArgumentException("잔고가 초과 되었습니다 포인트는 " + MAX_POINT + "을 초과할 수 없습니다.");
        }

        UserPoint updateUser = userPointTable.insertOrUpdate(id, totalAmount);

        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return updateUser;
    }

    @Override
    public List<PointHistory> getUserHistoryByUserId(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }
}
