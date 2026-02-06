package br.gov.mt.seletivo_seplag.api.v1.controller;

import br.gov.mt.seletivo_seplag.api.v1.dto.RegionalDtos;
import br.gov.mt.seletivo_seplag.api.v1.dto.RegionalDtos.Response;
import br.gov.mt.seletivo_seplag.domain.service.RegionaisSyncService;
import br.gov.mt.seletivo_seplag.domain.service.RegionaisSyncService.SyncResult;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.RegionalRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
public class RegionaisController {

  private final RegionaisSyncService syncService;
  private final RegionalRepository repo;

  @PostMapping("/sync")
  public SyncResult sync() {
    return syncService.sync();
  }

  @GetMapping
  public List<Response> list(@RequestParam(defaultValue = "true") boolean ativo) {
    var list = ativo ? repo.findByAtivoTrue() : repo.findAll();
    return list.stream()
        .map(
            r -> new RegionalDtos.Response(r.getId(), r.getExternalId(), r.getNome(), r.getAtivo()))
        .toList();
  }
}
