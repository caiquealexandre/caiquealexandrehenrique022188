package br.gov.mt.seletivo_seplag.infra.regionais;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "regionaisClient", url = "${integration.regionais.base-url}")
public interface RegionaisClientFeign {
  @GetMapping("${integration.regionais.path}")
  List<RegionalExternalDto> fetchAll();
}
