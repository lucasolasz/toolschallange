package com.arphoenix.toolschallange.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arphoenix.toolschallange.domain.entities.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, String> {

}
