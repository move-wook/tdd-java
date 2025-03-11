package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {
    UserPoint findByUserId(long id);

    UserPoint charge(long id, long amount);

    List<PointHistory> getUserHistoryByUserId(long id);

    UserPoint use(long id, long amount);
}
