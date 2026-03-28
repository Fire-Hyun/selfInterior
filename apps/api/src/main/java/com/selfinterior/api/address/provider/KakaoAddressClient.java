package com.selfinterior.api.address.provider;

import com.selfinterior.api.address.AddressCandidate;
import java.util.List;

public interface KakaoAddressClient {
  List<AddressCandidate> search(String query);
}
