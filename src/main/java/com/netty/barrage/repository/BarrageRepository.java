package com.netty.barrage.repository;

import com.netty.barrage.domain.Barrage;
import org.springframework.data.repository.CrudRepository;

public interface BarrageRepository extends CrudRepository<Barrage, Integer> {

}