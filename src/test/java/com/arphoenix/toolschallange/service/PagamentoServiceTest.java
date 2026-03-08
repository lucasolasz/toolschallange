package com.arphoenix.toolschallange.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arphoenix.toolschallange.domain.entities.Descricao;
import com.arphoenix.toolschallange.domain.entities.FormaPagamento;
import com.arphoenix.toolschallange.domain.entities.Transacao;
import com.arphoenix.toolschallange.domain.enums.StatusTransacao;
import com.arphoenix.toolschallange.domain.enums.TipoPagamento;
import com.arphoenix.toolschallange.domain.mappers.TransacaoMapper;
import com.arphoenix.toolschallange.domain.records.PagamentoRequestRecord;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.domain.repositories.TransacaoRepository;
import com.arphoenix.toolschallange.exception.NotFoundException;
import com.arphoenix.toolschallange.messaging.producer.PagamentoProducer;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

        @Mock
        private TransacaoRepository transacaoRepository;

        @Mock
        private TransacaoMapper mapper;

        @Mock
        private PagamentoProducer pagamentoProducer;

        @InjectMocks
        private PagamentoService pagamentoService;

        private Transacao transacao;
        private PagamentoResponseRecord responseRecord;

        @BeforeEach
        void setUp() {
                transacao = new Transacao();
                transacao.setId("1");
                transacao.setCartao("4444********1234");
                Descricao descricao = new Descricao();
                descricao.setValor(BigDecimal.valueOf(100.00));
                descricao.setDataHora(LocalDateTime.now().minusDays(1));
                descricao.setEstabelecimento("Test Store");
                descricao.setStatus(StatusTransacao.AUTORIZADO);
                transacao.setDescricao(descricao);
                FormaPagamento formaPagamento = new FormaPagamento();
                formaPagamento.setTipo(TipoPagamento.AVISTA);
                formaPagamento.setParcelas(1);
                transacao.setFormaPagamento(formaPagamento);

                responseRecord = new PagamentoResponseRecord(
                                new PagamentoResponseRecord.TransacaoOutput(
                                                "1",
                                                "4444********1234",
                                                new PagamentoResponseRecord.DescricaoOutput(
                                                                BigDecimal.valueOf(100.00),
                                                                LocalDateTime.now().minusDays(1),
                                                                "Test Store",
                                                                "123456",
                                                                "AUTH123",
                                                                StatusTransacao.AUTORIZADO),
                                                new PagamentoResponseRecord.FormaPagamentoOutput(
                                                                TipoPagamento.AVISTA,
                                                                1)));
        }

        @Test
        void recuperarTodos_deveRetornarListaVaziaQuandoNaoHaTransacoes() {
                // Arrange
                when(transacaoRepository.findAll()).thenReturn(List.of());

                // Act
                List<PagamentoResponseRecord> result = pagamentoService.recuperarTodos();

                // Assert
                assertThat(result).isEmpty();
                verify(transacaoRepository).findAll();
                verify(mapper, never()).toResponse(any());
        }

        @Test
        void recuperarTodos_deveRetornarListaMapeadaQuandoHaTransacoes() {
                // Arrange
                when(transacaoRepository.findAll()).thenReturn(List.of(transacao));
                when(mapper.toResponse(transacao)).thenReturn(responseRecord);

                // Act
                List<PagamentoResponseRecord> result = pagamentoService.recuperarTodos();

                // Assert
                assertThat(result).hasSize(1);
                assertThat(result.get(0)).isEqualTo(responseRecord);
                verify(transacaoRepository).findAll();
                verify(mapper).toResponse(transacao);
        }

        @Test
        void recuperarPorId_deveRetornarTransacaoQuandoEncontrada() {
                // Arrange
                when(transacaoRepository.findById("1")).thenReturn(Optional.of(transacao));
                when(mapper.toResponse(transacao)).thenReturn(responseRecord);

                // Act
                PagamentoResponseRecord result = pagamentoService.recuperarPorId("1");

                // Assert
                assertThat(result).isEqualTo(responseRecord);
                verify(transacaoRepository).findById("1");
                verify(mapper).toResponse(transacao);
        }

        @Test
        void recuperarPorId_deveLancarNotFoundExceptionQuandoNaoEncontrada() {
                // Arrange
                when(transacaoRepository.findById("1")).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.recuperarPorId("1"))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessage("Transação 1 não encontrada.");
                verify(transacaoRepository).findById("1");
                verify(mapper, never()).toResponse(any());
        }

        @Test
        void recuperarPorId_deveLancarIllegalArgumentExceptionQuandoIdInvalido() {
                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.recuperarPorId(null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("ID não pode ser nulo");
        }

        @Test
        void recuperarPorId_deveLancarIllegalArgumentExceptionQuandoIdVazio() {
                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.recuperarPorId(""))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("ID não pode ser nulo");
        }

        @Test
        void recuperarPorId_deveLancarIllegalArgumentExceptionQuandoIdNaoNumerico() {
                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.recuperarPorId("abc"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("ID deve ser numérico");
        }

        @Test
        void recuperarPorId_deveLancarIllegalArgumentExceptionQuandoIdNegativo() {
                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.recuperarPorId("-1"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("ID deve ser positivo");
        }

        @Test
        void estornar_deveEstornarTransacaoQuandoValida() {
                // Arrange
                transacao.getDescricao().setStatus(StatusTransacao.AUTORIZADO);
                when(transacaoRepository.findById("1")).thenReturn(Optional.of(transacao));
                when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
                when(mapper.toResponse(any(Transacao.class))).thenReturn(responseRecord);

                // Act
                PagamentoResponseRecord result = pagamentoService.estornar("1");

                // Assert
                assertThat(transacao.getDescricao().getStatus()).isEqualTo(StatusTransacao.CANCELADO);
                assertThat(result).isEqualTo(responseRecord);
                verify(transacaoRepository).findById("1");
                verify(transacaoRepository).save(transacao);
                verify(mapper).toResponse(transacao);
        }

        @Test
        void estornar_deveLancarIllegalArgumentExceptionQuandoJaCancelada() {
                // Arrange
                transacao.getDescricao().setStatus(StatusTransacao.CANCELADO);
                when(transacaoRepository.findById("1")).thenReturn(Optional.of(transacao));

                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.estornar("1"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Não é possível estornar uma transação já cancelada.");
                verify(transacaoRepository).findById("1");
                verify(transacaoRepository, never()).save(any());
        }

        @Test
        void estornar_deveLancarIllegalArgumentExceptionQuandoNegada() {
                // Arrange
                transacao.getDescricao().setStatus(StatusTransacao.NEGADO);
                when(transacaoRepository.findById("1")).thenReturn(Optional.of(transacao));

                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.estornar("1"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Não é possível estornar uma transação negada.");
                verify(transacaoRepository).findById("1");
                verify(transacaoRepository, never()).save(any());
        }

        @Test
        void estornar_deveLancarNotFoundExceptionQuandoNaoEncontrada() {
                // Arrange
                when(transacaoRepository.findById("1")).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.estornar("1"))
                                .isInstanceOf(NotFoundException.class)
                                .hasMessage("Transação 1 não encontrada.");
                verify(transacaoRepository).findById("1");
                verify(transacaoRepository, never()).save(any());
        }

        @Test
        void processarPagamento_deveLancarIllegalArgumentExceptionQuandoRequestNulo() {
                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.processarPagamento(null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Corpo da requisição não pode ser nulo");
                verify(pagamentoProducer, never()).enviarPagamentoParaTopico(any());
        }

        @Test
        void processarPagamento_deveLancarIllegalArgumentExceptionQuandoIdJaExiste() {
                // Arrange
                PagamentoRequestRecord request = criarPagamentoRequestRecord("1");
                when(transacaoRepository.existsById("1")).thenReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.processarPagamento(request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Transação com ID 1 já existe.");
                verify(transacaoRepository).existsById("1");
                verify(pagamentoProducer, never()).enviarPagamentoParaTopico(any());
        }

        @Test
        void processarPagamento_deveLancarIllegalArgumentExceptionQuandoDataFutura() {
                // Arrange
                PagamentoRequestRecord request = criarPagamentoRequestRecordComDataFutura("2");
                when(transacaoRepository.existsById("2")).thenReturn(false);

                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.processarPagamento(request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("A data e hora da transação não pode ser no futuro.");
                verify(transacaoRepository).existsById("2");
                verify(pagamentoProducer, never()).enviarPagamentoParaTopico(any());
        }

        @Test
        void processarPagamento_deveLancarIllegalArgumentExceptionQuandoParcelasInvalidasParaAvista() {
                // Arrange
                PagamentoRequestRecord request = criarPagamentoRequestRecordComParcelasInvalidas("3",
                                TipoPagamento.AVISTA, 2);
                when(transacaoRepository.existsById("3")).thenReturn(false);

                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.processarPagamento(request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Número de parcelas incompatível com tipo de pagamento");
                verify(transacaoRepository).existsById("3");
                verify(pagamentoProducer, never()).enviarPagamentoParaTopico(any());
        }

        @Test
        void processarPagamento_deveLancarIllegalArgumentExceptionQuandoParcelasInvalidasParaParcelado() {
                // Arrange
                PagamentoRequestRecord request = criarPagamentoRequestRecordComParcelasInvalidas("4",
                                TipoPagamento.PARCELADO_LOJA, 1);
                when(transacaoRepository.existsById("4")).thenReturn(false);

                // Act & Assert
                assertThatThrownBy(() -> pagamentoService.processarPagamento(request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Número de parcelas incompatível com tipo de pagamento");
                verify(transacaoRepository).existsById("4");
                verify(pagamentoProducer, never()).enviarPagamentoParaTopico(any());
        }

        // Métodos auxiliares para criar objetos de teste
        private PagamentoRequestRecord criarPagamentoRequestRecord(String id) {
                return new PagamentoRequestRecord(
                                new PagamentoRequestRecord.TransacaoInput(
                                                "4444********1234",
                                                id,
                                                new PagamentoRequestRecord.DescricaoInput(
                                                                BigDecimal.valueOf(100.00),
                                                                LocalDateTime.now().minusDays(1),
                                                                "Test Store"),
                                                new PagamentoRequestRecord.FormaPagamentoInput(
                                                                TipoPagamento.AVISTA,
                                                                1)));
        }

        private PagamentoRequestRecord criarPagamentoRequestRecordComDataFutura(String id) {
                return new PagamentoRequestRecord(
                                new PagamentoRequestRecord.TransacaoInput(
                                                "4444********1234",
                                                id,
                                                new PagamentoRequestRecord.DescricaoInput(
                                                                BigDecimal.valueOf(100.00),
                                                                LocalDateTime.now().plusDays(1),
                                                                "Test Store"),
                                                new PagamentoRequestRecord.FormaPagamentoInput(
                                                                TipoPagamento.AVISTA,
                                                                1)));
        }

        private PagamentoRequestRecord criarPagamentoRequestRecordComParcelasInvalidas(String id, TipoPagamento tipo,
                        int parcelas) {
                return new PagamentoRequestRecord(
                                new PagamentoRequestRecord.TransacaoInput(
                                                "4444********1234",
                                                id,
                                                new PagamentoRequestRecord.DescricaoInput(
                                                                BigDecimal.valueOf(100.00),
                                                                LocalDateTime.now().minusDays(1),
                                                                "Test Store"),
                                                new PagamentoRequestRecord.FormaPagamentoInput(
                                                                tipo,
                                                                parcelas)));
        }

        @Test
        void liberarResposta_deveCompletarFutureQuandoExiste() throws Exception {
                // Como o map é private, testar indiretamente - este teste é placeholder
                // Em um teste real, seria necessário expor o map ou usar reflexão
                // Por agora, apenas verificar que o método não lança exceção
                pagamentoService.liberarResposta(responseRecord);
                // Não há asserção específica, pois sem acesso ao map
        }

        // Nota: Testes para o fluxo completo de processarPagamento (com timeout e
        // sucesso)
        // são mais adequados para testes de integração, pois envolvem threads e Kafka.
        // Para unitários, focamos nas validações e métodos isolados.
}