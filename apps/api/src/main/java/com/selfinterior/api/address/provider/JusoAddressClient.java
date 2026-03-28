package com.selfinterior.api.address.provider;

import com.selfinterior.api.address.AddressCandidate;
import java.util.List;

public interface JusoAddressClient {
  List<AddressCandidate> search(String query);

  List<String> detailOptions(
      String roadCode, String buildingMainNo, String buildingSubNo, String queryType);
}
