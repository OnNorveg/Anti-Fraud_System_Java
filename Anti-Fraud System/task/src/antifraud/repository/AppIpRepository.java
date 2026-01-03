package antifraud.repository;

import antifraud.entity.AppIp;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppIpRepository extends CrudRepository<AppIp, Integer> {
    Optional<AppIp> findAppIpByIp(String ip);
}
