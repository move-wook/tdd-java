package io.hhplus.tdd.point;

public interface PointService {
    UserPoint findByUserId(long id);

    UserPoint charge(long id, long amount);
}
