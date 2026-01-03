package antifraud.repository;

import antifraud.entity.AppCard;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppCardRepository extends CrudRepository<AppCard, Integer> {
    Optional<AppCard> findAppCardByCardNumber(String cardNumber);
}
