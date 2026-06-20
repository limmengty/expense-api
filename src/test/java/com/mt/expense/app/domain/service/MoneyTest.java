package com.mt.expense.app.domain.vo;

import static org.assertj.core.api.Assertions.*;

import com.mt.expense.app.domain.exception.InvalidMoneyException;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for Money value object. */
class MoneyTest {

    @Test
    @DisplayName("should create money with valid amount and currency")
    void shouldCreateMoneyWithValidAmountAndCurrency() {
        Money money = Money.usd(new BigDecimal("100.50"));

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(money.currency()).isEqualTo(Currency.getInstance("USD"));
    }

    @Test
    @DisplayName("should throw InvalidMoneyException for negative amount")
    void shouldThrowForNegativeAmount() {
        assertThatThrownBy(() -> Money.usd(new BigDecimal("-10.00")))
                .isInstanceOf(InvalidMoneyException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    @DisplayName("should throw NullPointerException for null amount")
    void shouldThrowForNullAmount() {
        assertThatThrownBy(() -> new Money(null, Currency.getInstance("USD")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should correctly add two monies with same currency")
    void shouldCorrectlyAddTwoMonies() {
        Money m1 = Money.usd(new BigDecimal("50.00"));
        Money m2 = Money.usd(new BigDecimal("30.50"));

        Money result = m1.add(m2);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("80.50"));
    }

    @Test
    @DisplayName("should correctly subtract two monies")
    void shouldCorrectlySubtractTwoMonies() {
        Money m1 = Money.usd(new BigDecimal("100.00"));
        Money m2 = Money.usd(new BigDecimal("30.00"));

        Money result = m1.subtract(m2);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("should detect zero amount")
    void shouldDetectZeroAmount() {
        Money zero = Money.usd(BigDecimal.ZERO);
        Money nonZero = Money.usd(new BigDecimal("10.00"));

        assertThat(zero.isZero()).isTrue();
        assertThat(nonZero.isZero()).isFalse();
    }

    @Test
    @DisplayName("should correctly compare greater than")
    void shouldCorrectlyCompareGreaterThan() {
        Money m1 = Money.usd(new BigDecimal("100.00"));
        Money m2 = Money.usd(new BigDecimal("50.00"));

        assertThat(m1.amount().compareTo(m2.amount()) > 0).isTrue();
        assertThat(m2.amount().compareTo(m1.amount()) > 0).isFalse();
    }

    @Test
    @DisplayName("should enforce same currency for operations")
    void shouldEnforceSameCurrencyForOperations() {
        Money usd = Money.usd(new BigDecimal("100.00"));
        Money eur = Money.of(new BigDecimal("100.00"), "EUR");

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different currencies");
    }

    @Test
    @DisplayName("should have correct equals and hashCode")
    void shouldHaveCorrectEqualsAndHashCode() {
        Money m1 = Money.usd(new BigDecimal("100.00"));
        Money m2 = Money.usd(new BigDecimal("100.00"));
        Money m3 = Money.usd(new BigDecimal("50.00"));

        assertThat(m1).isEqualTo(m2);
        assertThat(m1).isNotEqualTo(m3);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }
}
