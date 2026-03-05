package com.arphoenix.toolschallange.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arphoenix.toolschallange.domain.records.PagamentoResponseRecord;
import com.arphoenix.toolschallange.service.PagamentoService;

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

}
