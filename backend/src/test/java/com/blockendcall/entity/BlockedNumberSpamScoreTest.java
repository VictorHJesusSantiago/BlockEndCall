package com.blockendcall.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BlockedNumber — lógica de pontuação de spam")
class BlockedNumberSpamScoreTest {

    @Test
    @DisplayName("getSpamScore retorna 0 quando não há reports")
    void spamScore_noReports_returnsZero() {
        BlockedNumber number = BlockedNumber.builder()
                .reportCount(0)
                .falsePositiveCount(0)
                .build();

        assertThat(number.getSpamScore()).isZero();
    }

    @Test
    @DisplayName("getSpamScore é 10 pontos por report, sem penalidade")
    void spamScore_fiveReports_noPenalty() {
        BlockedNumber number = BlockedNumber.builder()
                .reportCount(5)
                .falsePositiveCount(0)
                .build();

        assertThat(number.getSpamScore()).isEqualTo(50);
    }

    @Test
    @DisplayName("getSpamScore é limitado a 100")
    void spamScore_manyReports_cappedAt100() {
        BlockedNumber number = BlockedNumber.builder()
                .reportCount(15)
                .falsePositiveCount(0)
                .build();

        assertThat(number.getSpamScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("getSpamScore subtrai 15 pontos por falso positivo")
    void spamScore_withFalsePositives_appliesPenalty() {
        BlockedNumber number = BlockedNumber.builder()
                .reportCount(5)   // base = 50
                .falsePositiveCount(2) // penalty = 30
                .build();

        assertThat(number.getSpamScore()).isEqualTo(20);
    }

    @Test
    @DisplayName("getSpamScore nunca vai abaixo de zero")
    void spamScore_heavyFalsePositives_doesNotGoBelowZero() {
        BlockedNumber number = BlockedNumber.builder()
                .reportCount(1)   // base = 10
                .falsePositiveCount(3) // penalty = 45
                .build();

        assertThat(number.getSpamScore()).isZero();
    }

    @Test
    @DisplayName("incrementReportCount incrementa em 1")
    void incrementReportCount_incrementsByOne() {
        BlockedNumber number = BlockedNumber.builder().reportCount(3).build();

        number.incrementReportCount();

        assertThat(number.getReportCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("incrementFalsePositive incrementa contagem de falsos positivos em 1")
    void incrementFalsePositive_incrementsByOne() {
        BlockedNumber number = BlockedNumber.builder().falsePositiveCount(1).build();

        number.incrementFalsePositive();

        assertThat(number.getFalsePositiveCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("builder define valores padrão corretos")
    void builderDefaults_areCorrect() {
        BlockedNumber number = BlockedNumber.builder()
                .phoneNumber("+5511999990000")
                .build();

        assertThat(number.getReportCount()).isEqualTo(1);
        assertThat(number.getFalsePositiveCount()).isZero();
        assertThat(number.isConfirmed()).isFalse();
        assertThat(number.isWhitelisted()).isFalse();
        assertThat(number.getConfirmationCount()).isZero();
    }
}
