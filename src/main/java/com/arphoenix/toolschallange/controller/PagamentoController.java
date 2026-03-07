package com.arphoenix.toolschallange.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arphoenix.toolschallange.domain.records.PagamentoRequestRecord;
import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.service.PagamentoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @GetMapping
    public ResponseEntity<List<PagamentoResponseRecord>> recuperarTodos() {
        List<PagamentoResponseRecord> response = pagamentoService.recuperarTodos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseRecord> recuperarPorId(@PathVariable String id) {
        PagamentoResponseRecord response = pagamentoService.recuperarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estorno/{id}")
    public ResponseEntity<PagamentoResponseRecord> estornar(@PathVariable String id) {
        PagamentoResponseRecord response = pagamentoService.estornar(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PagamentoResponseRecord> processarPagamento(
            @Valid @RequestBody PagamentoRequestRecord request) {
        PagamentoResponseRecord response = pagamentoService.processarPagamento(request);
        return ResponseEntity.ok(response);
    }

}
