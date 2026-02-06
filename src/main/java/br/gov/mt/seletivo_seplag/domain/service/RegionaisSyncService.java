package br.gov.mt.seletivo_seplag.domain.service;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.RegionalEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.RegionalRepository;
import br.gov.mt.seletivo_seplag.infra.regionais.RegionaisClientFeign;
import br.gov.mt.seletivo_seplag.infra.regionais.RegionalExternalDto;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegionaisSyncService {

  private final RegionaisClientFeign client;
  private final RegionalRepository repo;


  public record SyncResult(
      int inserido,
      int inativado,
      int versionado,
      int contagemExterna
  ) {}

  @Transactional
  public SyncResult sync() {
    List<RegionalExternalDto> external = Optional.ofNullable(client.fetchAll()).orElseGet(List::of);

    // externalId -> dto (descarta nulos e ids nulos)
    Map<Integer, RegionalExternalDto> extById = new HashMap<>();
    for (RegionalExternalDto r : external) {
      if (r != null && r.id() != null) {
        extById.put(r.id(), r);
      }
    }

    // ativos atuais
    List<RegionalEntity> ativo = repo.findByAtivoTrue();

    // externalId -> entity ativa
    Map<Integer, RegionalEntity> activeByExtId = new HashMap<>();
    for (RegionalEntity a : ativo) {
      if (a.getExternalId() != null) {
        activeByExtId.put(a.getExternalId(), a);
      }
    }

    int insercao = 0;
    int inativacao = 0;
    int versionamento = 0;

    List<RegionalEntity> toSave = new ArrayList<>();
    Set<Long> idsInativados = new HashSet<>();

    // 1) novo no externo -> inserir
    // 3) alterado -> inativa antigo e cria novo
    for (RegionalExternalDto ext : extById.values()) {
      RegionalEntity atual = activeByExtId.get(ext.id());

      if (atual == null) {
        RegionalEntity regionalNulo = new RegionalEntity();
        regionalNulo.setExternalId(ext.id());
        regionalNulo.setNome(ext.nome());
        regionalNulo.setAtivo(true);
        toSave.add(regionalNulo);
        insercao++;
        continue;
      }

      String nomeAtual = safe(atual.getNome());
      String extName = safe(ext.nome());

      if (!nomeAtual.equals(extName)) {
        // inativa o atual
        if (!idsInativados.contains(atual.getId())) {
          atual.setAtivo(false);
          toSave.add(atual);
          idsInativados.add(atual.getId());
          inativacao++;
        }

        // cria novo ativo
        RegionalEntity n = new RegionalEntity();
        n.setExternalId(ext.id());
        n.setNome(ext.nome());
        n.setAtivo(true);
        toSave.add(n);
        versionamento++;
      }
    }

    // 2) ausente no externo -> inativar
    for (RegionalEntity cur : ativo) {
      Integer extId = cur.getExternalId();
      if (extId == null) continue;

      boolean existsInExternal = extById.containsKey(extId);
      if (!existsInExternal && !idsInativados.contains(cur.getId())) {
        cur.setAtivo(false);
        toSave.add(cur);
        idsInativados.add(cur.getId());
        inativacao++;
      }
    }

    if (!toSave.isEmpty()) {
      repo.saveAll(toSave);
    }

    return new SyncResult(insercao, inativacao, versionamento, extById.size());
  }

  private String safe(String s) {
    return s == null ? "" : s.trim();
  }
}
